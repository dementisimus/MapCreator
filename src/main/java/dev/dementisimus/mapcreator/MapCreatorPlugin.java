package dev.dementisimus.mapcreator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.CoreAPI;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.properties.DataSourceProperty;
import dev.dementisimus.capi.core.database.types.SQLTypes;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.logger.CoreAPILogger;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.capi.core.setup.states.type.SetupStateBoolean;
import dev.dementisimus.capi.core.setup.states.type.SetupStateString;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.SlimeDataSource;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

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
    @Getter private Database database;

    @Override
    public void onEnable() {
        mapCreatorPlugin = this;
        this.bukkitCoreAPI = new BukkitCoreAPI(this, true);
        this.coreAPI = this.bukkitCoreAPI.getCoreAPI();

        this.setupManager = this.coreAPI.getSetupManager();

        this.coreAPI.enableMainSetupStates();

        this.coreAPI.enableExtraSetupState(WORLD_IMPORTER_REQUIRED);
        this.coreAPI.enableExtraSetupState(WORLD_IMPORTER_FOLDER_LOCATION);
        this.coreAPI.enableExtraSetupState(API_MODE);
        this.coreAPI.enableExtraSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS);
        this.coreAPI.enableExtraSetupState(DEFAULT_WORLD);
        this.coreAPI.enableExtraSetupState(SIMPLE_TEMPLATE_MAP_WANTED);

        this.coreAPI.prepareInit(() -> {
            this.coreAPI.enableDatabase(DataSource.PROPERTY);
            this.database = this.coreAPI.getDatabase();

            this.coreAPI.registerAdditionalModuleToInject(MapCreatorPlugin.class, this);
            this.coreAPI.registerAdditionalModuleToInject(BukkitCoreAPI.class, this.bukkitCoreAPI);

            this.coreAPI.init(initializedCoreAPI -> {
                this.slimePlugin = this.retrieveSlimePlugin();

                this.customMapCreator = new CustomMapCreator(this, SlimeDataSource.MONGODB);
                this.slimeLoader = this.customMapCreator.getSlimeLoader();

                this.coreAPI.registerAdditionalModuleToInject(SlimePlugin.class, this.getSlimePlugin());
                this.coreAPI.registerAdditionalModuleToInject(SlimeLoader.class, this.getSlimeLoader());
                this.coreAPI.registerAdditionalModuleToInject(CustomMapCreator.class, this.getCustomMapCreator());
                this.coreAPI.registerAdditionalModuleToInject(CustomMapCreatorInventory.class, this.getCustomMapCreator().getCustomMapCreatorInventory());

                if(!this.setupManager.getSetupState(API_MODE).getBoolean()) {
                    this.getCoreAPI().setRegisterOptionalCommands(true);

                    CustomWorldImporter customWorldImporter = new CustomWorldImporter(this);
                    customWorldImporter.scanForImportableWorlds();

                    this.customMapCreator.setCustomWorldImporter(customWorldImporter);

                    CustomMapTemplates customMapTemplates = new CustomMapTemplates(this);
                    customMapTemplates.downloadSimpleTemplate();

                    this.customMapCreator.setCustomMapTemplates(customMapTemplates);

                    BossBar bossBar = BossBar.bossBar(Component.empty(), 1, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_12);
                    Bukkit.getScheduler().runTaskTimer(this, () -> {
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            if(player != null) {
                                String fullMapName = player.getWorld().getName();
                                if(fullMapName.contains(MapCreatorMap.CATEGORY_MAP_SEPARATOR)) {
                                    String[] fullMapNameSplitted = fullMapName.replace(MapCreatorMap.CATEGORY_MAP_SEPARATOR, "/").split("/");
                                    String category = fullMapNameSplitted[0];
                                    String name = fullMapNameSplitted[1];

                                    bossBar.name(Component.text("§c§l" + category + "§7/§f§l" + name));

                                    player.showBossBar(bossBar);
                                }else {
                                    player.hideBossBar(bossBar);
                                }
                            }
                        }
                    }, 30, 30);
                }else {
                    CoreAPILogger.info(new Translation(Translations.API_MODE_ENABLED).get(true));
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

        public static final SetupStateBoolean WORLD_IMPORTER_REQUIRED = new SetupStateBoolean("WORLD_IMPORTER_REQUIRED", "setup.extra.state.world.importer.required");
        public static final SetupStateString WORLD_IMPORTER_FOLDER_LOCATION = new SetupStateString("WORLD_IMPORTER_FOLDER_LOCATION", "setup.extra.state.world.importer.folder.location");
        public static final SetupStateBoolean API_MODE = new SetupStateBoolean("API_MODE", "setup.extra.state.api.mode");
        public static final SetupStateBoolean USE_DEFAULT_WORLD_FOR_PLAYERS = new SetupStateBoolean("USE_DEFAULT_WORLD_FOR_PLAYERS", "setup.extra.state.use.default.world.for.players");
        public static final SetupStateString DEFAULT_WORLD = new SetupStateString("DEFAULT_WORLD", "setup.extra.state.default.world");
        public static final SetupStateBoolean SIMPLE_TEMPLATE_MAP_WANTED = new SetupStateBoolean("SIMPLE_TEMPLATE_MAP_WANTED", "setup.extra.state.simple.template.map.wanted");

    }

    public static class Strings {

        public static final String PREFIX = "§c§lMap§f§lCreator §7§l» ";

    }

    public static class DataSource implements DataSourceProperty {

        public static final DataSource PROPERTY = new DataSource();

        public static final String NAME = "name";
        public static final String ICON = "icon";

        @Override
        public String name() {
            return "categories";
        }

        @Override
        public Map<String, String> fields() {
            return Map.ofEntries(Map.entry(NAME, SQLTypes.LONGTEXT), Map.entry(ICON, SQLTypes.LONGTEXT));
        }
    }

    public static class ItemDataStorageKeys {

        public static final String CATEGORY = "CATEGORY";
    }

    public static class Translations {

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
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_WORLD = "inventory.section.category.maps.import.world";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_NO_IMPORTABLE_WORLDS_FOUND = "inventory.section.category.maps.no.importable.worlds.found";
        public static final String INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND = "inventory.section.categories.noting.found";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND = "inventory.section.category.maps.noting.found";
        public static final String INVENTORY_SECTION_CATEGORY_MAPS_TEMPLATES_EMPTY = "inventory.section.category.maps.templates.empty";

        public static final String TEMPLATES_DOWNLOAD_FAILURE = "templates.download.failure";
        public static final String TEMPLATES_IMPORT_SUCCESS = "templates.import.success";

        public static final String MAPCREATOR_PERFORMANCE_FAILURE = "mapcreator.performance.failure";

        public static final String BASIC_PRE_ACTION_MESSAGE = "basic.pre.action.message";
        public static final String BASIC_POST_ACTION_MESSAGE = "basic.post.action.message";

    }
}
