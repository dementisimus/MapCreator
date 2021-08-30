package dev.dementisimus.mapcreator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.CoreAPI;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.capi.core.setup.states.type.SetupStateBoolean;
import dev.dementisimus.capi.core.setup.states.type.SetupStateString;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.SlimeDataSource;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.ExtraSetupStates.*;
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

    @Getter private static MapCreatorPlugin mapCreatorPlugin;

    @Getter private CoreAPI coreAPI;
    @Getter private BukkitCoreAPI bukkitCoreAPI;
    @Getter private SlimePlugin slimePlugin;
    @Getter private SlimeLoader slimeLoader;
    @Getter private CustomMapCreator customMapCreator;
    @Getter private SetupManager setupManager;

    @Override
    public void onEnable() {
        mapCreatorPlugin = this;
        this.bukkitCoreAPI = new BukkitCoreAPI(this, true);
        this.coreAPI = this.bukkitCoreAPI.getCoreAPI();

        this.coreAPI.prepareInit(() -> {
            this.coreAPI.enableDatabase(new String[]{Storage.CATEGORIES}, new String[]{Storage.Rows.NAME});

            this.setupManager = this.coreAPI.getSetupManager();

            this.coreAPI.enableMainSetupStates();

            this.coreAPI.enableExtraSetupState(WORLD_IMPORT_NEEDED);
            this.coreAPI.enableExtraSetupState(WORLD_IMPORT_FOLDER_LOCATION);
            this.coreAPI.enableExtraSetupState(API_MODE);
            this.coreAPI.enableExtraSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS);
            this.coreAPI.enableExtraSetupState(DEFAULT_WORLD);

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

            if(!this.setupManager.getSetupState(WORLD_IMPORT_NEEDED).isPresentInConfig(this.getCoreAPI())) {
                if(this.getCoreAPI().getConfigFile().delete()) {
                    System.out.println(new Translation(Translations.SETUP_OLD_VERSION_FOUND_RESTART_SETUP).get(Locale.ENGLISH, true));
                }
            }

            this.coreAPI.init(initializedCoreAPI -> {
                if(!this.setupManager.getSetupState(API_MODE).getBoolean()) {
                    this.getCoreAPI().setRegisterOptionalCommands(true);

                    Bukkit.getScheduler().runTaskTimer(this, () -> {
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            if(player != null) {
                                String mapName = player.getWorld().getName();
                                if(mapName.contains(MapCreatorMap.CATEGORY_MAP_SEPARATOR)) {
                                    player.sendActionBar(Component.text(new BukkitTranslation(Translations.PLAYER_ACTIONBAR_CURRENT_WORLD).get(player, "$world$", mapName)));
                                }
                            }
                        }
                    }, 30, 30);
                }else {
                    System.out.println(new Translation(Translations.API_MODE_ENABLED).get(true));
                }
                if(this.setupManager.getSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS).getBoolean()) {
                    this.getCoreAPI().setRegisterOptionalListeners(true);
                }
            });
        });
    }

    /*
     *
     * ToDO (before release): instead of throwing an Exception, print a "warning" message, download
       the latest version of AdvancedSlimeWorldManager, load it as a plugin & set it up with the database credentials given!
     *
     * */
    private SlimePlugin retrieveSlimePlugin() {
        SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if(plugin == null) {
            throw new IllegalArgumentException("SlimePlugin is NULL");
        }
        return plugin;
    }

    public static class ExtraSetupStates {

        public static final SetupStateBoolean WORLD_IMPORT_NEEDED = new SetupStateBoolean("WORLD_IMPORT_NEEDED", "setup.extra.state.world.import.needed");
        public static final SetupStateString WORLD_IMPORT_FOLDER_LOCATION = new SetupStateString("WORLD_IMPORT_FOLDER_LOCATION", "setup.extra.state.world.import.folder.location");
        public static final SetupStateBoolean API_MODE = new SetupStateBoolean("API_MODE", "setup.extra.state.api.mode");
        public static final SetupStateBoolean USE_DEFAULT_WORLD_FOR_PLAYERS = new SetupStateBoolean("USE_DEFAULT_WORLD_FOR_PLAYERS", "setup.extra.state.use.default.world.for.players");
        public static final SetupStateString DEFAULT_WORLD = new SetupStateString("DEFAULT_WORLD", "setup.extra.state.default.world");

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

        public static final String SETUP_OLD_VERSION_FOUND_RESTART_SETUP = "setup.old.version.found.restart.setup";

        public static final String API_MODE_ENABLED = "api.mode.enabled";

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
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_MAP = "inventory.section.category.maps.import.map";

        public static final String PLAYER_ACTIONBAR_CURRENT_WORLD = "player.actionbar.current.world";

    }
}
