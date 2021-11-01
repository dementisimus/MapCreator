package dev.dementisimus.mapcreator.creator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.config.ConfigManager;
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
import java.util.ArrayList;
import java.util.Date;
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
    private final CustomMapCreatorInventory customMapCreatorInventory;
    private SlimeLoader slimeLoader;
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

        if(this.slimeLoader == null) {
            String[] dataSources = new String[]{SlimeDataSource.REDIS, SlimeDataSource.FILE};
            for(String dataSource : dataSources) {
                SlimeLoader slimeLoader = this.getSlimeLoader(dataSource);

                if(slimeLoader != null) {
                    this.slimeLoader = slimeLoader;
                    this.mapCreatorPlugin.setSlimeDataSource(dataSource);
                    break;
                }
            }
        }

        this.customMapCreatorInventory = new CustomMapCreatorInventory(this);
    }

    @Override
    public void perform(Action action, MapCreatorMap mapCreatorMap, Callback<Performance> performanceCallback) {
        CustomMapCreatorMap customMapCreatorMap = (CustomMapCreatorMap) mapCreatorMap;

        this.awaitPerformance(action, customMapCreatorMap, performance -> {
            BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> {
                SlimeWorld slimeWorld = performance.getSlimeWorld();

                if(!action.equals(Action.CLONE)) {
                    if(slimeWorld != null) {
                        this.slimePlugin.generateWorld(slimeWorld);

                        customMapCreatorMap.setSlimeWorld(slimeWorld);
                        customMapCreatorMap.setLoadedSince(new Date());

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

    @Override
    public List<String> listMapsByCategory(String category) {
        try {
            return this.slimeLoader.listWorlds().stream().filter(world -> world.startsWith(category + MapCreatorMap.CATEGORY_MAP_SEPARATOR)).collect(Collectors.toList());
        }catch(IOException exception) {
            return new ArrayList<>();
        }
    }

    public void awaitPerformance(Action action, CustomMapCreatorMap mapCreatorMap, Callback<Performance> performanceCallback) {
        this.manageWorldConfig(action, mapCreatorMap);
        BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> this.ensureNoPlayersLeftOnMap(action, mapCreatorMap, () -> ThreadPool.execute(() -> {
            switch(action) {
                case LOAD -> mapCreatorMap.load(mapCreatorMap.isReadOnly(), mapCreatorMap.getMapCreationSettings().toSlimePropertyMap(), performanceCallback);
                case SAVE -> mapCreatorMap.save(true, mapCreatorMap.getSlimeWorld(), performanceCallback);
                case LEAVE_WITHOUT_SAVING -> mapCreatorMap.leave(performanceCallback);
                case DELETE -> mapCreatorMap.delete(performanceCallback);
                case IMPORT -> mapCreatorMap.importWorld(performanceCallback);
                case CLONE -> mapCreatorMap.clone(performanceCallback);
                case RENAME -> mapCreatorMap.rename(performanceCallback);
            }
        })));
    }

    public SlimeLoader getSlimeLoader() {
        return this.slimeLoader;
    }

    public Map<String, MapCreatorMap> getMapCreatorMaps() {
        return MAP_CREATOR_MAPS;
    }

    public CustomMapCreatorInventory getCustomMapCreatorInventory() {
        return this.customMapCreatorInventory;
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
                     * ToDo: replace with previous location
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
            case SAVE, LEAVE_WITHOUT_SAVING, DELETE, IMPORT, RENAME -> worldsConfig.getWorlds().remove(mapCreatorMap.getFileName());
            default -> worldsConfig.getWorlds().put(mapCreatorMap.getFileName(), mapCreatorMap.getMapCreationSettings().toWorldData());
        }
        worldsConfig.save();
    }

    private SlimeLoader getSlimeLoader(String dataSource) {
        return this.slimePlugin.getLoader(dataSource);
    }

    public static class CustomAction {

        public static String getActionMessage(Player player, CustomMapCreatorMap map, String actionMessageTranslationProperty, String elapsed, boolean isPostAction) {
            String cloneFrom = map.getCloneFrom() == null ? "" : map.getCloneFrom().getPrettyName();

            String basicActionMessageProperty = isPostAction ? MapCreatorPlugin.Translations.BASIC_POST_ACTION_MESSAGE : MapCreatorPlugin.Translations.BASIC_PRE_ACTION_MESSAGE;

            String newName = "";
            if(map.getRenameTo() != null) {
                newName = map.getRenameTo().getPrettyName();
            }
            return new BukkitTranslation(basicActionMessageProperty).get(player, new String[]{"$prefix$", "$map$", "$action$", "$elapsed$", "$newName$"}, new String[]{
                    MapCreatorPlugin.Strings.PREFIX, map.getPrettyName(), new BukkitTranslation(actionMessageTranslationProperty).get(player, "$clone$", cloneFrom), elapsed, newName
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
