package dev.dementisimus.mapcreator.creator;

import com.grinderwolf.swm.api.SlimePlugin;
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
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    @Setter
    private CustomWorldImporter customWorldImporter;

    @Getter
    @Setter
    private CustomMapTemplates customMapTemplates;

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
                switch(action) {
                    case LOAD -> customMapCreatorMap.load(false, this.getSlimePropertyMap(), performance :: set);
                    case SAVE -> customMapCreatorMap.save(true, customMapCreatorMap.getSlimeWorld(), performance :: set);
                    case LEAVE -> customMapCreatorMap.leave(performance :: set);
                    case DELETE -> customMapCreatorMap.delete(performance :: set);
                    case IMPORT -> customMapCreatorMap.importWorld(performance :: set);
                }
                performance.get().setAction(action);
                BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> {
                    SlimeWorld slimeWorld = performance.get().getSlimeWorld();
                    if(slimeWorld != null) {
                        this.slimePlugin.generateWorld(slimeWorld);
                        this.addMapCreatorMap(customMapCreatorMap);
                    }else {
                        this.removeMapCreatorMap(customMapCreatorMap);
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
    public Map<String, CustomMapCreatorMap> getMapCreatorMaps() {
        return MAP_CREATOR_MAPS;
    }

    @Override
    public CustomMapCreatorInventory getCustomMapCreatorInventory() {
        return this.customMapCreatorInventory;
    }

    @Override
    public List<String> getSlimeLoaderWorlds() throws IOException {
        return this.getSlimeLoader().listWorlds();
    }

    @Override
    public @Nullable CustomMapCreatorMap getMapCreatorMap(String mapName) {
        return this.getMapCreatorMaps().get(mapName);
    }

    @Override
    public void addMapCreatorMap(CustomMapCreatorMap customMapCreatorMap) {
        this.getMapCreatorMaps().put(customMapCreatorMap.getFileName(), customMapCreatorMap);
    }

    @Override
    public void removeMapCreatorMap(CustomMapCreatorMap customMapCreatorMap) {
        this.getMapCreatorMaps().remove(customMapCreatorMap.getFileName());
    }

    @Override
    public void ensureNoPlayersLeftOnMap(Action action, CustomMapCreatorMap customMapCreatorMap, EmptyCallback emptyCallback) {
        World world = Bukkit.getWorld(customMapCreatorMap.getWorldFileName());
        if(world == null) {
            emptyCallback.done();
            return;
        }
        switch(action) {
            case SAVE, LEAVE, DELETE -> {
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
            case SAVE, LEAVE, DELETE, IMPORT -> worldsConfig.getWorlds().remove(customMapCreatorMap.getWorldFileName());
            default -> worldsConfig.getWorlds().put(customMapCreatorMap.getWorldFileName(), this.getWorldData());
        }
        worldsConfig.save();
    }
}
