package dev.dementisimus.mapcreator;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.CoreAPI;
import dev.dementisimus.capi.core.config.Config;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.setup.DefaultSetUpState;
import dev.dementisimus.capi.core.setup.SetUpData;
import dev.dementisimus.mapcreator.creator.MapCreator;
import dev.dementisimus.mapcreator.creator.SlimeDataSource;
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
    private MapCreator mapCreator;

    public static MapCreatorPlugin getMapCreatorPlugin() {
        return mapCreatorPlugin;
    }

    @Override
    public void onEnable() {
        mapCreatorPlugin = this;
        this.bukkitCoreAPI = new BukkitCoreAPI(this, true);
        this.coreAPI = this.bukkitCoreAPI.getCoreAPI();

        this.coreAPI.prepareInit(new String[]{DefaultSetUpState.LANGUAGE.name(), MAPPOOL, DEFAULT_WORLD_FOR_USAGE, USE_API_MODE_ONLY}, () -> {
            this.retrieveSlimePlugin();
            this.mapCreator = new MapCreator(this.getSlimePlugin(), SlimeDataSource.MONGODB);
            this.slimeLoader = this.mapCreator.getSlimeLoader();

            this.coreAPI.registerAdditionalModuleToInject(MapCreatorPlugin.class, this);
            this.coreAPI.registerAdditionalModuleToInject(CoreAPI.class, this.coreAPI);
            this.coreAPI.registerAdditionalModuleToInject(BukkitCoreAPI.class, this.bukkitCoreAPI);
            this.coreAPI.registerAdditionalModuleToInject(SlimePlugin.class, this.getSlimePlugin());
            this.coreAPI.registerAdditionalModuleToInject(SlimeLoader.class, this.getSlimeLoader());
            this.coreAPI.registerAdditionalModuleToInject(MapCreator.class, this.getMapCreator());

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
                MapCreator mapCreator = new MapCreator(this.getSlimePlugin(), "mongodb");
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

    private void retrieveSlimePlugin() {
        SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if(plugin == null) {
            throw new IllegalArgumentException("SlimePlugin is NULL");
        }
        this.slimePlugin = plugin;
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

    public MapCreator getMapCreator() {
        return this.mapCreator;
    }

    public static class SetupStates {

        public static final String MAPPOOL = "MAPPOOL";
        public static final String SET_DEFAULT_WORLD_INSTEAD_OF_WORLD = "SET_DEFAULT_WORLD_INSTEAD_OF_WORLD";
        public static final String DEFAULT_WORLD_FOR_USAGE = "DEFAULT_WORLD_FOR_USAGE";
        public static final String USE_API_MODE_ONLY = "USE_API_MODE_ONLY";
    }
}
