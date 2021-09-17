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
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.pools.BukkitSynchronousExecutor;
import dev.dementisimus.capi.core.pools.ThreadPool;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.api.MapCreator;
import dev.dementisimus.mapcreator.creator.api.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private static final Map<String, MapCreatorMap> MAP_CREATOR_MAPS = new HashMap<>();

    private final MapCreatorPlugin mapCreatorPlugin;
    @Getter private final SetupManager setupManager;
    private final SlimePlugin slimePlugin;
    private final SlimeLoader slimeLoader;
    private final CustomMapCreatorInventory customMapCreatorInventory;

    @Getter
    @Setter
    private CustomWorldImporter customWorldImporter;

    @Getter
    @Setter
    private CustomMapTemplates customMapTemplates;

    public CustomMapCreator() {
        this.mapCreatorPlugin = MapCreatorPlugin.getMapCreatorPlugin();
        this.setupManager = this.mapCreatorPlugin.getSetupManager();
        this.slimePlugin = this.mapCreatorPlugin.getSlimePlugin();
        this.slimeLoader = this.slimePlugin.getLoader(this.mapCreatorPlugin.getSlimeDataSource());
        this.customMapCreatorInventory = new CustomMapCreatorInventory(this);
    }

    @Override
    public void perform(Action action, MapCreatorMap mapCreatorMap, Callback<Performance> performanceCallback) {
        this.awaitPerformance(action, mapCreatorMap, performance -> {
            BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> {
                SlimeWorld slimeWorld = performance.getSlimeWorld();

                if(!action.equals(Action.CLONE)) {
                    if(slimeWorld != null) {
                        this.slimePlugin.generateWorld(slimeWorld);
                        this.addMapCreatorMap(mapCreatorMap);
                    }else {
                        this.removeMapCreatorMap(mapCreatorMap);
                    }
                }

                performance.setAction(action);
                performanceCallback.done(performance);
            });
        });
    }

    public void awaitPerformance(Action action, MapCreatorMap mapCreatorMap, Callback<Performance> performanceCallback) {
        this.manageWorldConfig(action, mapCreatorMap);
        this.ensureNoPlayersLeftOnMap(action, mapCreatorMap, () -> ThreadPool.execute(() -> {
            switch(action) {
                case LOAD -> mapCreatorMap.load(false, this.getSlimePropertyMap(), performanceCallback);
                case SAVE -> mapCreatorMap.save(true, mapCreatorMap.getSlimeWorld(), performanceCallback);
                case LEAVE_WITHOUT_SAVING -> mapCreatorMap.leave(performanceCallback);
                case DELETE -> mapCreatorMap.delete(performanceCallback);
                case IMPORT -> mapCreatorMap.importWorld(performanceCallback);
                case CLONE -> mapCreatorMap.clone(performanceCallback);
            }
        }));
    }

    public SlimeLoader getSlimeLoader() {
        return this.slimeLoader;
    }

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

    public SlimePropertyMap getSlimePropertyMap() {
        return this.getWorldData().toPropertyMap();
    }

    public Map<String, MapCreatorMap> getMapCreatorMaps() {
        return MAP_CREATOR_MAPS;
    }

    public CustomMapCreatorInventory getCustomMapCreatorInventory() {
        return this.customMapCreatorInventory;
    }

    public List<String> listWorldsByCategory(String categoryName) throws IOException {
        return this.slimeLoader.listWorlds().stream().filter(world -> world.startsWith(categoryName + CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR)).collect(Collectors.toList());
    }

    public @Nullable MapCreatorMap getMapCreatorMap(String mapName) {
        return this.getMapCreatorMaps().get(mapName);
    }

    public void addMapCreatorMap(MapCreatorMap mapCreatorMap) {
        this.getMapCreatorMaps().put(mapCreatorMap.getFileName(), mapCreatorMap);
    }

    public void removeMapCreatorMap(MapCreatorMap mapCreatorMap) {
        this.getMapCreatorMaps().remove(mapCreatorMap.getFileName());
    }

    public void ensureNoPlayersLeftOnMap(Action action, MapCreatorMap mapCreatorMap, EmptyCallback emptyCallback) {
        World world = Bukkit.getWorld(mapCreatorMap.getFileName());
        if(world == null) {
            emptyCallback.done();
            return;
        }
        switch(action) {
            case SAVE, LEAVE_WITHOUT_SAVING, DELETE -> {
                world.getPlayers().forEach(player -> {
                    /*
                     * replace with previous location
                     * */
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                });
                if(!world.getPlayers().isEmpty()) {
                    this.ensureNoPlayersLeftOnMap(action, mapCreatorMap, emptyCallback);
                }else {
                    emptyCallback.done();
                }
            }
            default -> emptyCallback.done();
        }
    }

    public void manageWorldConfig(MapCreator.Action action, MapCreatorMap mapCreatorMap) {
        WorldsConfig worldsConfig = ConfigManager.getWorldConfig();
        switch(action) {
            case SAVE, LEAVE_WITHOUT_SAVING, DELETE, IMPORT -> worldsConfig.getWorlds().remove(mapCreatorMap.getFileName());
            default -> worldsConfig.getWorlds().put(mapCreatorMap.getFileName(), this.getWorldData());
        }
        worldsConfig.save();
    }

    public static class CustomAction {

        public static String getActionMessage(Player player, CustomMapCreatorMap map, String actionMessageTranslationProperty, String elapsed, boolean isPostAction) {
            String cloneFrom = map.getCloneFrom() == null ? "" : map.getCloneFrom().getPrettyName();

            String basicActionMessageProperty = isPostAction ? MapCreatorPlugin.Translations.BASIC_POST_ACTION_MESSAGE : MapCreatorPlugin.Translations.BASIC_PRE_ACTION_MESSAGE;
            return new BukkitTranslation(basicActionMessageProperty).get(player, new String[]{"$prefix$", "$map$", "$action$", "$elapsed$"}, new String[]{
                    MapCreatorPlugin.Strings.PREFIX, map.getPrettyName(), new BukkitTranslation(actionMessageTranslationProperty).get(player, "$clone$", cloneFrom), elapsed
            });
        }

        public enum User {

            TELEPORT("mapcreator.action.player.item.teleport", "mapcreator.action.message.player.teleport", 12, Material.ENDER_EYE),
            BACK("back", "back", 18, Material.RED_DYE);

            @Getter String translationProperty;
            @Getter String actionMessageTranslationProperty;
            @Getter int actionItemSlot;
            @Getter Material actionItemMaterial;

            User(String translationProperty, String actionMessageTranslationProperty, int actionItemSlot, Material actionItemMaterial) {
                this.translationProperty = translationProperty;
                this.actionMessageTranslationProperty = actionMessageTranslationProperty;
                this.actionItemSlot = actionItemSlot;
                this.actionItemMaterial = actionItemMaterial;
            }

            public void sendActionMessage(Player player, CustomMapCreatorMap map) {
                if(!this.equals(TELEPORT)) {
                    player.sendMessage(getActionMessage(player, map, this.getActionMessageTranslationProperty(), "", true));
                    return;
                }
                player.sendMessage(new BukkitTranslation(this.getActionMessageTranslationProperty()).get(player, new String[]{"$prefix$", "$map$"}, new String[]{
                        MapCreatorPlugin.Strings.PREFIX, map.getPrettyName()
                }));
            }
        }
    }
}
