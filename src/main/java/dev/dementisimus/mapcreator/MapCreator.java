package dev.dementisimus.mapcreator;

import dev.dementisimus.capi.core.config.Config;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import dev.dementisimus.capi.core.core.CoreAPI;
import dev.dementisimus.capi.core.helpers.bukkit.BukkitHelper;
import dev.dementisimus.capi.core.setup.SetUpData;
import dev.dementisimus.capi.core.translations.CoreAPITranslations;
import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.api.MapCreatorAPI;
import dev.dementisimus.mapcreator.commands.COMMAND_mapcreator;
import dev.dementisimus.mapcreator.creator.AbstractCreator;
import dev.dementisimus.mapcreator.creator.CreatorConstants;
import dev.dementisimus.mapcreator.listeners.PlayerCommandPreprocessListener;
import dev.dementisimus.mapcreator.listeners.PlayerJoinListener;
import dev.dementisimus.mapcreator.listeners.SetUpDoneListener;
import dev.dementisimus.mapcreator.listeners.SetUpStateChangeListener;
import dev.dementisimus.mapcreator.listeners.SetUpStatePrintInstructionsListener;
import dev.dementisimus.mapcreator.setup.AdditionalSetUpState;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.ResourceBundle;

import static dev.dementisimus.capi.core.setup.DefaultSetUpState.LANGUAGE;
import static dev.dementisimus.mapcreator.setup.AdditionalSetUpState.DEFAULT_WORLD_FOR_USAGE;
import static dev.dementisimus.mapcreator.setup.AdditionalSetUpState.MAPPOOL;
import static dev.dementisimus.mapcreator.setup.AdditionalSetUpState.USE_API_MODE_ONLY;
import static java.lang.System.out;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreator @ MapCreator
 *
 * @author dementisimus
 * @since 10.07.2020:18:07
 */
public class MapCreator extends JavaPlugin {

    private static MapCreator mapCreator;
    private BukkitCoreAPI bukkitCoreAPI;
    private CoreAPI coreAPI;

    public static MapCreator getMapCreator() {
        return mapCreator;
    }

    @Override
    public void onEnable() {
        this.init();
    }

    private void init() {
        this.bukkitCoreAPI = new BukkitCoreAPI(this);
        mapCreator = this;
        this.coreAPI = this.bukkitCoreAPI.getCoreAPI();
        BukkitHelper.registerEvents(this, new Listener[]{new SetUpStatePrintInstructionsListener(), new SetUpStateChangeListener(), new SetUpDoneListener()});
        try {
            this.coreAPI.prepareInit(new String[]{LANGUAGE.name(), MAPPOOL.name(), DEFAULT_WORLD_FOR_USAGE.name(), USE_API_MODE_ONLY.name()}, new ResourceBundle[]{getBundle(this.coreAPI.getBaseName(), ENGLISH), getBundle(this.coreAPI.getBaseName(), GERMAN)}, capi -> capi.init(initialized -> {
                if(initialized) {
                    new Config(this.getCoreAPI().getConfigFile()).read(result -> {
                        if(result != null) {
                            AbstractCreator.setWorldPoolFolder(result.getString(MAPPOOL.name()));
                            SetUpData setUpData = this.getCoreAPI().getSetUpData();
                            setUpData.setData(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD, Boolean.parseBoolean(result.getString(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name())));
                            setUpData.setData(USE_API_MODE_ONLY, Boolean.parseBoolean(result.getString(USE_API_MODE_ONLY.name())));
                            if(setUpData.getBoolean(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
                                setUpData.setData(DEFAULT_WORLD_FOR_USAGE, result.getString(DEFAULT_WORLD_FOR_USAGE.name()));
                            }
                        }
                        this.setMapCreatorSettings();
                    });
                }
            }));
        }catch(Exception ex) {
            System.out.println(new BukkitTranslation(CoreAPITranslations.CONSOLE_ERROR_NO_PLUGIN_FOUND.id).get(true));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void registerCommandsAndEvents() {
        try {
            SetUpData setUpData = this.getCoreAPI().getSetUpData();
            if(!setUpData.getBoolean(USE_API_MODE_ONLY)) {
                Objects.requireNonNull(this.getCommand("mapcreator")).setExecutor(new COMMAND_mapcreator());
                Objects.requireNonNull(this.getCommand("mc")).setExecutor(new COMMAND_mapcreator());
            }
            if(setUpData.getBoolean(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
                BukkitHelper.registerEvents(this, new Listener[]{new PlayerJoinListener(), new PlayerCommandPreprocessListener()});
            }
        }catch(Exception ex) {
            out.println(new BukkitTranslation(CoreAPITranslations.CONSOLE_ERROR_NO_PLUGIN_FOUND.id).get(true));
        }
    }

    public void setMapCreatorSettings() {
        Bukkit.getScheduler().runTask(this, () -> {
            this.registerCommandsAndEvents();
            SetUpData setUpData = this.getCoreAPI().getSetUpData();
            if(setUpData.getBoolean(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
                MapCreatorAPI creator = new MapCreatorAPI();
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
                creator.setMapType(type);
                creator.setMapName(map);
                creator.loadSync(true, CreatorConstants.DEFAULT_WORLD);
            }
            if(!setUpData.getBoolean(USE_API_MODE_ONLY)) {
                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player != null) {
                            String mapName = player.getWorld().getName();
                            if(!mapName.equalsIgnoreCase(CreatorConstants.DEFAULT_WORLD)) {
                                player.sendActionBar(new BukkitTranslation(Translations.PLAYER_ACTIONBAR_CURRENTWORLD.id).get(player, "$world$", mapName));
                            }
                        }
                    }
                }, 30, 30);
            }else {
                out.println(new BukkitTranslation(Translations.CONSOLE_API_ENABLED.id).get(true));
            }
        });
    }

    public BukkitCoreAPI getBukkitCoreAPI() {
        return this.bukkitCoreAPI;
    }

    public CoreAPI getCoreAPI() {
        return this.coreAPI;
    }

}