package dev.dementisimus.mapcreator.creator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.callback.EmptyCallback;
import dev.dementisimus.capi.core.pools.BukkitSynchronousExecutor;
import dev.dementisimus.capi.core.pools.ThreadPool;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreator @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:19:38
 */
public class CustomMapCreator implements MapCreator {

    private final MapCreatorPlugin mapCreatorPlugin;
    private final SlimePlugin slimePlugin;
    private final SlimeLoader slimeLoader;
    private final CustomMapCreatorInventory customMapCreatorInventory;

    public CustomMapCreator(MapCreatorPlugin mapCreatorPlugin, String slimeDataSource) {
        this.mapCreatorPlugin = mapCreatorPlugin;
        this.slimePlugin = this.mapCreatorPlugin.getSlimePlugin();
        this.slimeLoader = this.slimePlugin.getLoader(slimeDataSource);
        this.customMapCreatorInventory = new CustomMapCreatorInventory(this);
    }

    @Override
    public void perform(Action action, CustomMapCreatorMap customMapCreatorMap, Callback<Performance> performanceCallback) {
        this.manageWorldConfig(action, customMapCreatorMap);
        this.ensureNoPlayersLeftOnMap(action, customMapCreatorMap, () -> {
            ThreadPool.execute(() -> {
                AtomicReference<Performance> performance = new AtomicReference<>(new Performance((SlimeWorld) null));
                try {
                    switch(action) {
                        case CREATE -> customMapCreatorMap.create(this.getSlimePropertyMap(), performance :: set);
                        case LOAD -> customMapCreatorMap.load(false, this.getSlimePropertyMap(), performance :: set);
                        case SAVE -> customMapCreatorMap.save(true, this.getSlimeWorld(customMapCreatorMap), performance :: set);
                        case DELETE -> customMapCreatorMap.delete(performance :: set);
                        case LEAVE -> customMapCreatorMap.leave(performance :: set);
                    }
                }catch(IOException | WorldAlreadyExistsException | CorruptedWorldException | NewerFormatException | WorldInUseException | UnknownWorldException ex) {
                    if(ex instanceof IOException) {
                        performance.get().setSuccess(Performance.FailureReason.NOT_ABLE_TO_OBTAIN_FROM_DATA_SOURCE);
                    }else if(ex instanceof WorldAlreadyExistsException) {
                        performance.get().setSuccess(Performance.FailureReason.WORLD_ALREADY_EXISTS_IN_DATA_SOURCE);
                    }else if(ex instanceof CorruptedWorldException) {
                        performance.get().setSuccess(Performance.FailureReason.CORRUPTED_WORLD);
                    }else if(ex instanceof NewerFormatException) {
                        performance.get().setSuccess(Performance.FailureReason.WORLD_USES_NEWER_VERSION_OF_SRF);
                    }else if(ex instanceof WorldInUseException) {
                        performance.get().setSuccess(Performance.FailureReason.WORLD_IS_ALREADY_BEING_USED_BY_ANOTHER_SERVER);
                    }else if(ex instanceof UnknownWorldException) {
                        performance.get().setSuccess(Performance.FailureReason.WORLD_NOT_FOUND);
                    }
                    performanceCallback.done(performance.get());
                    return;
                }
                performance.get().setAction(action);
                BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> {
                    SlimeWorld slimeWorld = performance.get().getSlimeWorld();
                    if(slimeWorld != null) {
                        this.slimePlugin.generateWorld(slimeWorld);
                        this.addSlimeWorld(customMapCreatorMap.getWorldFileName(), slimeWorld);
                    }
                    performanceCallback.done(performance.get());
                });
            });
        });
    }

    @Override
    public SlimeLoader getSlimeLoader() {
        return this.slimeLoader;
    }

    @Override
    public WorldData getWorldData() {
        WorldData worldData = new WorldData();
        worldData.setDataSource(SlimeDataSource.MONGODB);
        worldData.setSpawn("0, 100, 0");
        worldData.setDifficulty("easy");
        worldData.setAllowAnimals(false);
        worldData.setAllowMonsters(false);
        worldData.setDragonBattle(false);
        worldData.setPvp(true);
        worldData.setEnvironment("normal");
        worldData.setWorldType("default_1_1");
        worldData.setDefaultBiome("minecraft:plains");
        return worldData;
    }

    @Override
    public SlimePropertyMap getSlimePropertyMap() {
        return this.getWorldData().toPropertyMap();
    }

    @Override
    public Map<String, SlimeWorld> getSlimeWorlds() {
        return this.slimeWorlds;
    }

    @Override
    public CustomMapCreatorInventory getCustomMapCreatorInventory() {
        return this.customMapCreatorInventory;
    }

    @Override
    public List<String> getWorlds() throws IOException {
        return this.getSlimeLoader().listWorlds();
    }

    @Override
    public @Nullable SlimeWorld getSlimeWorld(CustomMapCreatorMap customMapCreatorMap) {
        return this.getSlimeWorlds().getOrDefault(customMapCreatorMap.getWorldFileName(), null);
    }

    @Override
    public void addSlimeWorld(String mapName, SlimeWorld slimeWorld) {
        this.getSlimeWorlds().put(mapName, slimeWorld);
    }

    @Override
    public void removeSlimeWorld(String mapName) {
        this.getSlimeWorlds().remove(mapName);
    }

    @Override
    public void ensureNoPlayersLeftOnMap(Action action, CustomMapCreatorMap customMapCreatorMap, EmptyCallback emptyCallback) {
        World world = Bukkit.getWorld(customMapCreatorMap.getWorldFileName());
        if(world == null) {
            emptyCallback.done();
            return;
        }
        switch(action) {
            case SAVE, DELETE, LEAVE -> {
                world.getPlayers().forEach(player -> {
                    /*
                     * replace with previous location
                     * */
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                });
                if(!world.getPlayers().isEmpty()) {
                    this.ensureNoPlayersLeftOnMap(action, customMapCreatorMap, emptyCallback);
                }else {
                    emptyCallback.done();
                }
            }
            default -> emptyCallback.done();
        }
    }

    @Override
    public void manageWorldConfig(Action action, CustomMapCreatorMap customMapCreatorMap) {
        WorldsConfig worldsConfig = ConfigManager.getWorldConfig();
        switch(action) {
            case SAVE, DELETE, LEAVE -> worldsConfig.getWorlds().remove(customMapCreatorMap.getWorldFileName());
            default -> worldsConfig.getWorlds().put(customMapCreatorMap.getWorldFileName(), this.getWorldData());
        }
        worldsConfig.save();
    }
}
