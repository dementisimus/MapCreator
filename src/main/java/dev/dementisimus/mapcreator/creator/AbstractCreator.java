package dev.dementisimus.mapcreator.creator;

import dev.dementisimus.capi.core.annotations.ToDo;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.config.Config;
import dev.dementisimus.capi.core.pools.ThreadPool;
import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.utils.FileUtils;
import dev.dementisimus.capi.core.utils.LocationManager;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.mapcreator.setup.AdditionalSetUpState;
import dev.dementisimus.mapcreator.translation.Prefixes;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.dementisimus.mapcreator.MapCreator.getMapCreator;
import static org.bukkit.Bukkit.getScheduler;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class Creator @ MapCreator
 *
 * @author dementisimus
 * @since 10.07.2020:18:08
 */
public abstract class AbstractCreator {

    public static final String DEFAULT_MAP = "DEFAULTMAPS/defaultWorld";
    private static final String[] HIDDEN_FOLDERS = new String[]{"datapacks", "data", "DIM1", "DIM-1", "poi", "region", "stats", "playerdata", "advancements", "entities"};
    private static final HashMap<Player, PreviousLocationObject> PREVIOUS_LOCATIONS = new HashMap<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
    public static File worldPoolFolder;
    static File defaultWorld;
    String mapType;
    String mapName;
    Player player;

    public AbstractCreator() {

    }

    public AbstractCreator(@NotNull Player player) {
        this.player = player;
    }

    public AbstractCreator(@NotNull String mapType, @NotNull String mapName) {
        this.mapType = mapType;
        this.mapName = mapName;
    }

    public static void setWorldPoolFolder() {
        ThreadPool.execute(() -> new Config(MapCreator.getMapCreator().getCoreAPI().getConfigFile()).read(result -> {
            if(result != null) {
                String worldPoolFolderPath = result.getString(AdditionalSetUpState.MAPPOOL.name());
                if(!worldPoolFolderPath.endsWith("/")) {
                    worldPoolFolderPath += "/";
                }
                File worldPoolFolder = new File(worldPoolFolderPath);
                if(!worldPoolFolder.exists()) {
                    FileUtils.createDirectory(worldPoolFolder);
                }
                setInnerWorldPoolFolder(worldPoolFolder);
            }
        }));
    }

    public static void setWorldPoolFolder(final String worldPoolFolderPath) {
        String wFp = worldPoolFolderPath;
        if(!wFp.endsWith("/")) {
            wFp += "/";
        }
        File worldPoolFolder = new File(wFp);
        if(!worldPoolFolder.exists()) {
            FileUtils.createDirectory(worldPoolFolder);
        }
        setInnerWorldPoolFolder(worldPoolFolder);
    }

    private static void setInnerWorldPoolFolder(File worldPoolFolder) {
        AbstractCreator.worldPoolFolder = worldPoolFolder;
        File defaultWorldSave = new File(getWorldPoolFolderPath() + DEFAULT_MAP);
        if(!defaultWorldSave.exists()) {
            System.out.println(new BukkitTranslation(Translations.DEFAULT_WORLD_NOT_AVAILABLE).get(true));
            Bukkit.getPluginManager().disablePlugin(getMapCreator());
            defaultWorld = null;
            return;
        }
        setDefaultWorld();
    }

    private static void setDefaultWorld() {
        AbstractCreator.defaultWorld = new File(getWorldPoolFolderPath() + DEFAULT_MAP);
    }

    private static String getWorldPoolFolderPath() {
        return AbstractCreator.worldPoolFolder.getAbsolutePath() + "/";
    }

    public static PreviousLocationObject getPreviousLocation(Player player) {
        return PREVIOUS_LOCATIONS.get(player);
    }

    public static void setPreviousLocation(Player player) {
        if(getPreviousLocation(player) != null && getPreviousLocation(player).getLocation().get("world").toString().equalsIgnoreCase(player.getWorld().getName())) {
            return;
        }
        PREVIOUS_LOCATIONS.put(player, new PreviousLocationObject(player));
    }

    public static void resetPreviousLocation(Player player) {
        PREVIOUS_LOCATIONS.remove(player);
    }

    public static String printDirectoryTree(File folder) {
        if(!folder.isDirectory()) {
            return null;
        }
        List<World> worlds = Bukkit.getWorlds();
        List<String> loadedWorlds = new ArrayList<>();
        for(World world : worlds) {
            loadedWorlds.add(world.getName());
        }

        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(folder, indent, sb, loadedWorlds);
        return sb.toString();
    }

    @ToDo(task = "TODO create an inv instead of printing these lines! : Folders = Chests, Fils = Paper!")
    private static void printDirectoryTree(File folder, int indent, StringBuilder sb, List<String> loadedWorlds) {
        if(!folder.isDirectory()) {
            return;
        }
        sb.append(getIndentString(indent));
        sb.append("§6+--");
        sb.append("§e").append(folder.getName()).append("§c | §e").append(DATE_FORMAT.format(new Date(folder.lastModified()))).append(loadedWorlds.contains(folder.getName()) ? " §7| §a§l✔§7" : "");
        sb.append("\n");
        for(File file : Objects.requireNonNull(folder.listFiles((dir, name) -> dir.isDirectory() && !Arrays.asList(HIDDEN_FOLDERS).contains(name)))) {
            printDirectoryTree(file, indent + 1, sb, loadedWorlds);
        }
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            sb.append("§6|  ");
        }
        return sb.toString();
    }


    public static void preparePlayerForTeleportation(Player player, String world) {
        if(player != null) {
            getScheduler().runTask(getMapCreator(), () -> {
                if(!world.equalsIgnoreCase(CreatorConstants.DEFAULT_WORLD)) {
                    player.setGameMode(GameMode.CREATIVE);
                    new Config(getPreviousLocationsFile(defaultWorld)).read(player.getUniqueId().toString(), jStringObject -> {
                        if(jStringObject != null) {
                            PreviousLocationObject previousLocationObject = PreviousLocationObject.fromJsonString(player, jStringObject);
                            previousLocationObject.restorePreviousLocation(true, false);
                        }
                    });
                }
            });
        }
    }

    public static void teleportToExistingWorld(Player executor, World world, Player target, Player sendMessageTo, String translation, String mapType) {
        if(executor != null && target != null && sendMessageTo != null) {
            setPreviousLocationAndPreparePlayer(world, target);
            if(mapType == null) {
                mapType = "?";
            }
            String finalMapType = mapType;
            LocationManager.getConfigLocation(world.getWorldFolder(), world.getName(), "SPAWN", location -> {
                if(location != null) {
                    sendMessageTo.sendMessage(new BukkitTranslation(translation).get(target, new String[]{"$prefix$", "$map$", "$type$", "$byPlayer$"}, new String[]{Prefixes.MAPCREATOR, world.getName(), finalMapType, executor.getName()}));
                    preparePlayerForTeleportation(target, world.getName());
                    getScheduler().runTask(getMapCreator(), () -> {
                        try {
                            LocationManager.teleport(target, location);
                        }catch(IllegalArgumentException ignored) {

                        }
                    });
                }
            });
        }
    }

    private static void setPreviousLocationAndPreparePlayer(World bukkitWorld, Player player) {
        if(player != null) {
            setPreviousLocation(player);
            preparePlayerForTeleportation(player, bukkitWorld.getName());
        }
    }

    private static File getPreviousLocationsFile(File file) {
        return new File(file, "previousLocations.json");
    }

    private static void sendMessage(Player player, String translation, String mapType, String mapName) {
        if(player != null) {
            player.sendMessage(new BukkitTranslation(translation).get(player, new String[]{"$prefix$", "$type$", "$map$"}, new String[]{Prefixes.MAPCREATOR, mapType, mapName}));
        }
    }

    public void loadSync(boolean setDefaultWorldSettings, String defaultMapName) {
        this.loadInnerSyncHeader(defaultMapName);
        this.loadInnerSyncBody(setDefaultWorldSettings, defaultMapName, b -> this.loadInnerSyncFooter(defaultMapName));
    }

    public void load(boolean setDefaultWorldSettings) {
        ThreadPool.execute(() -> {
            if(this.loadInnerSyncHeader(this.mapName)) {
                this.loadInnerSyncBody(setDefaultWorldSettings, this.mapName, b -> this.loadInnerSyncFooter(this.mapName));
            }
        });
    }

    private boolean loadInnerSyncHeader(String defaultMapName) {
        this.mapType = this.mapType.toUpperCase();
        World world = Bukkit.getWorld(defaultMapName);
        if(world != null) {
            teleportToExistingWorld(this.player, world, this.player, this.player, Translations.CREATOR_WORLD_ALREADY_LOADED, this.mapType);
            return false;
        }
        File worldFolder = new File(defaultMapName);
        FileUtils.createDirectory(worldFolder);
        if(this.checkIfMapExists()) {
            defaultWorld = this.getOldSave();
        }
        this.removeSessionFiles(defaultWorld);
        FileUtils.copy(defaultWorld, worldFolder);
        return true;
    }

    private void loadWorldDyn(String defaultMapName, Callback<World> cb) {
        getScheduler().runTask(getMapCreator(), () -> cb.done(Bukkit.getServer().createWorld(new WorldCreator(defaultMapName))));
    }

    private void loadInnerSyncBody(boolean setDefaultWorldSettings, String defaultMapName, Callback<Boolean> cb) {
        this.loadWorldDyn(defaultMapName, bukkitWorld -> LocationManager.getConfigLocationAsMap(defaultWorld, defaultMapName, "SPAWN", map -> getScheduler().runTask(getMapCreator(), () -> {
            if(setDefaultWorldSettings) {
                bukkitWorld.setAutoSave(true);
                bukkitWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                bukkitWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
                bukkitWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                bukkitWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                bukkitWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                bukkitWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            }
            if(this.player != null) {
                if(bukkitWorld != null) {
                    setPreviousLocationAndPreparePlayer(bukkitWorld, this.player);
                    this.player.teleport(LocationManager.getLocationFromMap(bukkitWorld, map));
                }
                Location loc = LocationManager.getLocationFromMap(bukkitWorld, map);
                if(loc != null) {
                    LocationManager.teleport(this.player, loc);
                    cb.done(true);
                    return;
                }
                cb.done(false);
            }
        })));
    }

    private void loadInnerSyncFooter(String defaultMapName) {
        File mapTypeIdentifier = new File(defaultMapName, this.mapType + ".mapType");
        try {
            mapTypeIdentifier.createNewFile();
        }catch(IOException e) {
            e.printStackTrace();
        }
        sendMessage(this.player, Translations.CREATOR_WORLD_LOADED, this.mapType, this.mapName);
    }

    public void save() {
        this.generalPreparation(true, false);
    }

    public void leave() {
        this.generalPreparation(false, false);
    }

    public void delete() {
        this.generalPreparation(false, true);
    }

    public boolean checkIfMapExists() {
        setDefaultWorld();
        return this.checkInnerIfMapExists(this.getOldSaveParent()) && this.checkInnerIfMapExists(this.getOldSave());
    }

    private boolean checkInnerIfMapExists(File file) {
        if(!file.exists() || file.listFiles() == null || file.listFiles().length == 0) {
            if(this.mapName != null && Bukkit.getWorld(this.mapName) != null) {
                FileUtils.createDirectory(file);
                return false;
            }
            return false;
        }
        return true;
    }

    private void manageFiles(File worldFolder, boolean save, boolean delete) {
        File oldSave = this.getOldSave();
        if(save) {
            this.removeSessionFiles(worldFolder);
            if(oldSave.exists()) {
                FileUtils.delete(oldSave);
            }
            FileUtils.createDirectory(oldSave);
            FileUtils.copy(worldFolder, oldSave);
        }else {
            if(delete) {
                if(this.checkIfMapExists()) {
                    FileUtils.delete(oldSave);
                }
            }
        }
        FileUtils.delete(worldFolder);
    }

    private void generalPreparation(boolean save, boolean delete) {
        ThreadPool.execute(() -> {
            this.checkIfMapExists();
            this.prepareWorld(save, delete, o -> {
                Object[] mapPreparation = (Object[]) o;
                if(mapPreparation == null) {
                    sendMessage(this.player, Translations.CREATOR_WORLD_NOT_LOADED, this.mapType, this.mapName);
                    return;
                }
                World world = (World) mapPreparation[1];
                File worldFolder = (File) mapPreparation[2];
                getScheduler().runTaskTimer(MapCreator.getMapCreator(), scheduler -> {
                    Bukkit.unloadWorld(this.mapName, save);
                    this.manageFiles(worldFolder, save, delete);
                    if(this.player != null) {
                        if(world.getName().equalsIgnoreCase(this.player.getLocation().getWorld().getName())) {
                            this.player.getInventory().clear();
                        }
                    }
                    scheduler.cancel();
                }, 10, 10);
            });
        });
    }

    private void prepareWorld(boolean save, boolean delete, Callback<Object> cb) {
        World world = Bukkit.getWorld(this.mapName);
        if(world == null) {
            cb.done(null);
            return;
        }
        File worldFolder = world.getWorldFolder();
        this.evaluateTeleportLocation(world, save, delete, b -> cb.done(new Object[]{this.mapType, world, worldFolder}));
    }

    private void evaluateTeleportLocation(World world, boolean save, boolean delete, Callback<Boolean> cb) {
        LocationManager.getConfigLocation(world.getWorldFolder(), world.getName(), "SPAWN", location -> {
            getScheduler().runTask(getMapCreator(), () -> {
                List<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld().getName().equalsIgnoreCase(world.getName())).collect(Collectors.toList());
                while(true) {
                    if(players.isEmpty()) {
                        cb.done(true);
                        break;
                    }
                    Player currentPlayer = players.get(0);
                    if(currentPlayer != null) {
                        if(!currentPlayer.equals(this.player)) {
                            this.evalMsg(currentPlayer, save, delete);
                        }else {
                            this.evalMsg(this.player, save, delete);
                        }
                        PreviousLocationObject previousLocationObject = new PreviousLocationObject(currentPlayer);
                        String prevLoc = previousLocationObject.toJsonString();
                        File previousLocations = getPreviousLocationsFile(world.getWorldFolder());
                        new Config(previousLocations).addData(currentPlayer.getUniqueId().toString(), prevLoc).write();
                        if(getPreviousLocation(currentPlayer) != null) {
                            getPreviousLocation(currentPlayer).restorePreviousLocation(true, true);
                        }else {
                            World w = Bukkit.getWorlds().get(0);
                            if(Bukkit.getWorld(CreatorConstants.DEFAULT_WORLD) != null) {
                                w = Bukkit.getWorld(CreatorConstants.DEFAULT_WORLD);
                            }
                            World finalW = w;
                            LocationManager.getConfigLocation(w.getWorldFolder(), w.getName(), "SPAWN", loc -> currentPlayer.teleport(Objects.requireNonNullElseGet(loc, finalW :: getSpawnLocation)));
                        }
                    }
                    players.remove(currentPlayer);
                    if(players.isEmpty()) {
                        cb.done(true);
                        break;
                    }
                }
            });
        });
    }

    private void evalMsg(Player player, boolean save, boolean delete) {
        if(player != null) {
            if(save) {
                sendMessage(player, Translations.CREATOR_WORLD_SAVED, this.mapType, this.mapName);
            }else {
                if(!delete) {
                    sendMessage(player, Translations.CREATOR_WORLD_LEFT, this.mapType, this.mapName);
                }else {
                    sendMessage(player, Translations.CREATOR_WORLD_DELETED, this.mapType, this.mapName);
                }
            }
        }
    }

    private File getOldSaveParent() {
        return new File(getWorldPoolFolderPath() + this.mapType);
    }

    private File getOldSave() {
        return new File(this.getOldSaveParent() + "/" + this.mapName);
    }

    private void removeSessionFiles(File file) {
        String[] sessionFiles = new String[]{"uid.dat", "session.lock", "level.dat_old"};
        for(String sessionFile : sessionFiles) {
            if(new File(file, sessionFile).exists()) {
                new File(file, sessionFile).delete();
            }
        }
    }

    public String getMapName() {
        return this.mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapType() {
        return this.mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType.toUpperCase();
    }
}
