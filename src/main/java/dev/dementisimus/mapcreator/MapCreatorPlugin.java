package dev.dementisimus.mapcreator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.callback.EmptyCallback;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.properties.DataSourceProperty;
import dev.dementisimus.capi.core.database.types.SQLTypes;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.logger.CoreAPILogger;
import dev.dementisimus.capi.core.setup.MainSetupStates;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.capi.core.setup.states.type.SetupStateBoolean;
import dev.dementisimus.capi.core.setup.states.type.SetupStateString;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.SlimeDataSource;
import dev.dementisimus.mapcreator.creator.aswm.ASWMDownloads;
import dev.dementisimus.mapcreator.creator.aswm.SlimeDataSoureConfig;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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

    @Getter private BukkitCoreAPI bukkitCoreAPI;
    @Getter private SlimePlugin slimePlugin;
    @Getter private SlimeLoader slimeLoader;
    @Getter private String slimeDataSource;
    @Getter private CustomMapCreator customMapCreator;
    @Getter private SetupManager setupManager;
    @Getter private Database database;

    @Override
    public void onEnable() {
        mapCreatorPlugin = this;

        this.bukkitCoreAPI = new BukkitCoreAPI(this);

        this.setupManager = this.bukkitCoreAPI.getSetupManager();

        this.bukkitCoreAPI.enableMainSetupStates();

        this.bukkitCoreAPI.enableExtraSetupState(WORLD_IMPORTER_REQUIRED);
        this.bukkitCoreAPI.enableExtraSetupState(WORLD_IMPORTER_FOLDER_LOCATION);
        this.bukkitCoreAPI.enableExtraSetupState(API_MODE);
        this.bukkitCoreAPI.enableExtraSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS);
        this.bukkitCoreAPI.enableExtraSetupState(DEFAULT_WORLD);
        this.bukkitCoreAPI.enableExtraSetupState(SIMPLE_TEMPLATE_MAP_WANTED);

        this.bukkitCoreAPI.enableDatabase(DataSource.PROPERTY);
        this.database = this.bukkitCoreAPI.getDatabase();

        this.bukkitCoreAPI.prepare(coreAPIInjector -> {

            coreAPIInjector.addInjectionModule(MapCreatorPlugin.class, this);

            this.bukkitCoreAPI.init(() -> {
                this.retrieveSlimePlugin(() -> {
                    this.customMapCreator = new CustomMapCreator();
                    this.slimeLoader = this.customMapCreator.getSlimeLoader();

                    coreAPIInjector.addInjectionModule(CustomMapCreator.class, this.getCustomMapCreator());
                    coreAPIInjector.addInjectionModule(CustomMapCreatorInventory.class, this.customMapCreator.getCustomMapCreatorInventory());
                    coreAPIInjector.addInjectionModule(SlimePlugin.class, this.getSlimePlugin());
                    coreAPIInjector.addInjectionModule(SlimeLoader.class, this.getSlimeLoader());

                    if(!this.setupManager.getSetupState(API_MODE).getBoolean()) {
                        this.bukkitCoreAPI.setRegisterOptionalListeners(true);
                        this.bukkitCoreAPI.setRegisterOptionalCommands(true);

                        CustomWorldImporter customWorldImporter = new CustomWorldImporter(this);
                        customWorldImporter.scanForImportableWorlds();

                        this.customMapCreator.setCustomWorldImporter(customWorldImporter);

                        CustomMapTemplates customMapTemplates = new CustomMapTemplates(this);
                        customMapTemplates.downloadSimpleTemplate();

                        this.customMapCreator.setCustomMapTemplates(customMapTemplates);

                        BossBar bossBar = Bukkit.createBossBar(" ", BarColor.YELLOW, BarStyle.SEGMENTED_12);
                        bossBar.setVisible(true);
                        Bukkit.getScheduler().runTaskTimer(this, () -> {
                            for(Player player : Bukkit.getOnlinePlayers()) {
                                if(player != null) {
                                    String fullMapName = player.getWorld().getName();
                                    if(fullMapName.contains(CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR)) {
                                        String[] fullMapNameSplitted = fullMapName.replace(CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR, "/").split("/");
                                        String category = fullMapNameSplitted[0];
                                        String name = fullMapNameSplitted[1];

                                        bossBar.setTitle("§c§l" + category + "§7/§f§l" + name);

                                        bossBar.addPlayer(player);
                                    }else {
                                        bossBar.removePlayer(player);
                                    }
                                }
                            }
                        }, 30, 30);
                    }else {
                        CoreAPILogger.info(new Translation(Translations.API_MODE_ENABLED).get(true));
                    }
                });
            });
        });
    }

    private void retrieveSlimePlugin(EmptyCallback emptyCallback) {
        SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        File pluginFile = new File("./plugins/" + ASWMDownloads.PLUGIN_FILE_NAME);

        this.slimeDataSource = SlimeDataSource.of(this.setupManager.getSetupState(MainSetupStates.STORAGE).getString());

        if(plugin == null) {
            this.bukkitCoreAPI.setSkipInjection(true);

            CoreAPILogger.info(new Translation(Translations.ASWM_INIT_PLUGIN_NOT_FOUND_DOWNLOADING_FILES).get("$prefix$", Strings.PREFIX, true));

            this.bukkitCoreAPI.getFileDownloader().downloadFile(ASWMDownloads.CLASS_MODIFIER_URL, "./" + ASWMDownloads.CLASS_MODIFIER_FILE_NAME, classModifierDownload -> {
                this.bukkitCoreAPI.getFileDownloader().downloadFile(ASWMDownloads.PLUGIN_URL, "./plugins/" + ASWMDownloads.PLUGIN_FILE_NAME, pluginDownload -> {
                    try {
                        SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().loadPlugin(pluginFile);

                        if(slimePlugin != null) {
                            this.slimePlugin = slimePlugin;

                            SlimeDataSoureConfig slimeDataSoureConfig = new SlimeDataSoureConfig(this.getDatabase(), this.getSetupManager());
                            slimeDataSoureConfig.modify();

                            CoreAPILogger.warning(new Translation(Translations.ASWM_INIT_DONE_RESTART_REQUIRED_AUTO_RESTART).get(new String[]{
                                    "$prefix$", "$classModifierVersion$"
                            }, new String[]{Strings.PREFIX, ASWMDownloads.VERSION}, true));

                            Bukkit.getScheduler().runTaskTimer(this, runnable -> {
                                Bukkit.shutdown();

                                runnable.cancel();
                            }, 200, 200);
                        }

                    }catch(InvalidPluginException | InvalidDescriptionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }else {
            this.slimePlugin = plugin;
            emptyCallback.done();
        }
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

        public static final String PREFIX = "§c§lMap§f§lCreator §7§l»";

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
        public static final String INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_INSTRUCTION = "inventory.section.categories.add.instruction";
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

        public static final String ASWM_INIT_PLUGIN_NOT_FOUND_DOWNLOADING_FILES = "aswm.init.plugin.not.found.downloading.files";
        public static final String ASWM_INIT_UNSUPPORTED_DATABASE = "aswm.init.unsupported.database";
        public static final String ASWM_INIT_CONFIG_DONE = "aswm.init.config.done";
        public static final String ASWM_INIT_DONE_RESTART_REQUIRED_AUTO_RESTART = "aswm.init.done.restart.required.auto.restart";

    }
}
