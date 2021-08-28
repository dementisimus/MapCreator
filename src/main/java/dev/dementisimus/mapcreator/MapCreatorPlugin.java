package dev.dementisimus.mapcreator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.CoreAPI;
import dev.dementisimus.capi.core.config.Config;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.setup.DefaultSetUpState;
import dev.dementisimus.capi.core.setup.SetUpData;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.SlimeDataSource;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.SetupStates.DEFAULT_WORLD_FOR_USAGE;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.SetupStates.MAPPOOL;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.SetupStates.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.SetupStates.USE_API_MODE_ONLY;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreatorPlugin @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:19:27
 */
public class MapCreatorPlugin extends JavaPlugin {

    private static MapCreatorPlugin mapCreatorPlugin;

    private CoreAPI coreAPI;
    private BukkitCoreAPI bukkitCoreAPI;
    private SlimePlugin slimePlugin;
    private SlimeLoader slimeLoader;
    private CustomMapCreator customMapCreator;

    public static MapCreatorPlugin getMapCreatorPlugin() {
        return mapCreatorPlugin;
    }

    @Override
    public void onEnable() {
        mapCreatorPlugin = this;
        this.bukkitCoreAPI = new BukkitCoreAPI(this, true);
        this.coreAPI = this.bukkitCoreAPI.getCoreAPI();

        this.coreAPI.prepareInit(new String[]{DefaultSetUpState.LANGUAGE.name(), MAPPOOL, DEFAULT_WORLD_FOR_USAGE, USE_API_MODE_ONLY}, () -> {
            this.coreAPI.enableDatabaseUsage(new String[]{Storage.CATEGORIES}, new String[]{Storage.Rows.NAME});

            this.slimePlugin = this.retrieveSlimePlugin();

            this.customMapCreator = new CustomMapCreator(this, SlimeDataSource.MONGODB);
            this.slimeLoader = this.customMapCreator.getSlimeLoader();

            this.coreAPI.registerAdditionalModuleToInject(MapCreatorPlugin.class, this);
            this.coreAPI.registerAdditionalModuleToInject(BukkitCoreAPI.class, this.bukkitCoreAPI);
            this.coreAPI.registerAdditionalModuleToInject(SlimePlugin.class, this.getSlimePlugin());
            this.coreAPI.registerAdditionalModuleToInject(SlimeLoader.class, this.getSlimeLoader());
            this.coreAPI.registerAdditionalModuleToInject(CustomMapCreator.class, this.getCustomMapCreator());
            this.coreAPI.registerAdditionalModuleToInject(CustomMapCreatorInventory.class, this.getCustomMapCreator().getCustomMapCreatorInventory());
            this.coreAPI.registerAdditionalModuleToInject(DataManagement.class, this.bukkitCoreAPI.getCoreAPI().getDataManagement());

            this.coreAPI.init(initializedCoreAPI -> {
                new Config(this.getCoreAPI().getConfigFile()).read(result -> {
                    if(result != null) {
                        /*AbstractCreator.setWorldPoolFolder(result.getString(MAPPOOL.name()));
                        SetUpData setUpData = this.getCoreAPI().getSetUpData();
                        setUpData.setData(SET_DEFAULT_WORLD_INSTEAD_OF_WORLD, Boolean.parseBoolean(result.getString(SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name())));
                        setUpData.setData(USE_API_MODE_ONLY, Boolean.parseBoolean(result.getString(USE_API_MODE_ONLY.name())));
                        if(setUpData.getBoolean(SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
                            setUpData.setData(DEFAULT_WORLD_FOR_USAGE, result.getString(DEFAULT_WORLD_FOR_USAGE.name()));
                        }*/
                    }
                    this.registerOptionalCommands();
                    this.setMapCreatorSettings();
                });
            });
        });
    }

    private void registerOptionalCommands() {
        SetUpData setUpData = this.getCoreAPI().getSetUpData();
        if(!setUpData.getBoolean(USE_API_MODE_ONLY)) {
            this.getCoreAPI().registerOptionalCommands(true);
        }
        if(setUpData.getBoolean(SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
            this.getCoreAPI().registerOptionalListeners(true);
        }
    }

    private void setMapCreatorSettings() {
        Bukkit.getScheduler().runTask(this, () -> {
            SetUpData setUpData = this.getCoreAPI().getSetUpData();
            if(setUpData.getBoolean(SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
                CustomMapCreator customMapCreator = new CustomMapCreator(this, "mongodb");
                String mapEntry = setUpData.getString(DEFAULT_WORLD_FOR_USAGE);
                String[] entry = (mapEntry.contains("/") ? mapEntry.split("/") : null);
                String type;
                String map;
                if(entry == null) {
                    type = "DEFAULTMAPS";
                    map = mapEntry;
                }else {
                    type = entry[0];
                    map = entry[1];
                }
            }
            if(!setUpData.getBoolean(USE_API_MODE_ONLY)) {
                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player != null) {
                            String mapName = player.getWorld().getName();
                            /*if(!mapName.equalsIgnoreCase(CreatorConstants.DEFAULT_WORLD)) {
                                player.sendActionBar(new BukkitTranslation(Translations.PLAYER_ACTIONBAR_CURRENTWORLD).get(player, "$world$", mapName));
                            }*/
                        }
                    }
                }, 30, 30);
            }else {
                //out.println(new BukkitTranslation(Translations.CONSOLE_API_ENABLED).get(true));
            }
        });
    }

    private SlimePlugin retrieveSlimePlugin() {
        SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if(plugin == null) {
            throw new IllegalArgumentException("SlimePlugin is NULL");
        }
        return plugin;
    }

    public CoreAPI getCoreAPI() {
        return this.coreAPI;
    }

    public BukkitCoreAPI getBukkitCoreAPI() {
        return this.bukkitCoreAPI;
    }

    public SlimePlugin getSlimePlugin() {
        return this.slimePlugin;
    }

    public SlimeLoader getSlimeLoader() {
        return this.slimeLoader;
    }

    public CustomMapCreator getCustomMapCreator() {
        return this.customMapCreator;
    }

    public static class SetupStates {

        public static final String MAPPOOL = "MAPPOOL";
        public static final String SET_DEFAULT_WORLD_INSTEAD_OF_WORLD = "SET_DEFAULT_WORLD_INSTEAD_OF_WORLD";
        public static final String DEFAULT_WORLD_FOR_USAGE = "DEFAULT_WORLD_FOR_USAGE";
        public static final String USE_API_MODE_ONLY = "USE_API_MODE_ONLY";
    }

    public static class Storage {

        public static final String CATEGORIES = "categories";

        public static class Rows {

            public static final String NAME = "";
        }

        public static class Categories {

            public static final String NAME = "name";
            public static final String ICON = "icon";
        }
    }

    public static class ItemDataStorageKeys {

        public static final String CATEGORY = "CATEGORY";
    }

    public static class Translations {

        public static final String INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY = "inventory.section.categories.add";
        public static final String INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION = "inventory.section.categories.add.sign.instruction";
        public static final String INVENTORY_SECTION_CATEGORY_CREATE_MAP = "inventory.section.category.create.map";
        public static final String INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_1 = "inventory.section.category.create.map.map.icon.lore.instructions.1";
        public static final String INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_2 = "inventory.section.category.create.map.map.icon.lore.instructions.2";
        public static final String BACK = "back";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_RIGHT_CLICK = "inventory.section.maps.map.action.right.click";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_LEFT_CLICK = "inventory.section.maps.map.action.left.click";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_MAP_LOADED_BY = "inventory.section.category.maps.map.loaded.by";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_MAP_LOADED_SINCE = "inventory.section.category.maps.map.loaded.since";
    }
}
