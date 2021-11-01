package dev.dementisimus.mapcreator.creator;

import com.google.common.base.Preconditions;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.pools.BukkitSynchronousExecutor;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.api.MapCreator;
import dev.dementisimus.mapcreator.creator.api.MapCreatorMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreatorMap @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:21:48
 */
public class CustomMapCreatorMap implements MapCreatorMap {

    @Getter private final SlimePlugin slimePlugin;
    @Getter private final SlimeLoader slimeLoader;
    @Getter private final CustomMapCreator customMapCreator;
    @Getter
    @Setter
    CustomMapCreatorMap recentlyViewed;
    @Getter private String mapCategory;
    private boolean readOnly;
    @Getter private SlimeWorld slimeWorld;
    @Getter
    @Setter
    private File importableWorldFile;

    @Getter
    @Setter
    private String mapName;

    @Getter
    @Setter
    private CustomMapCreatorMap renameTo;

    @Setter private String loadedBy;

    @Setter private Date loadedSince;

    @Getter
    @Setter
    private CustomMapCreatorMap cloneFrom;

    public CustomMapCreatorMap() {
        MapCreatorPlugin mapCreatorPlugin = MapCreatorPlugin.getMapCreatorPlugin();

        this.slimePlugin = mapCreatorPlugin.getSlimePlugin();
        this.slimeLoader = mapCreatorPlugin.getSlimeLoader();
        this.customMapCreator = mapCreatorPlugin.getCustomMapCreator();
    }

    public CustomMapCreatorMap(String mapName, String mapCategory) {
        this();

        this.mapName = mapName;
        this.mapCategory = mapCategory.toUpperCase();
    }

    public CustomMapCreatorMap(String fullMap) {
        this();

        if(fullMap.contains(CATEGORY_MAP_SEPARATOR)) {
            String[] mapParts = fullMap.replace(CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR, "/").split("/");
            this.mapCategory = mapParts[0];
            this.mapName = mapParts[1];
        }
    }

    public CustomMapCreatorMap(String mapName, String mapCategory, boolean readOnly) {
        this(mapName, mapCategory);

        this.readOnly = readOnly;
    }

    public CustomMapCreatorMap(String fullMap, boolean readOnly) {
        this(fullMap);

        this.readOnly = readOnly;
    }

    @Override
    public boolean isLocked() {
        try {
            return this.slimeLoader.isWorldLocked(this.getFileName());
        }catch(UnknownWorldException | IOException exception) {
            return false;
        }
    }

    @Override
    public boolean exists() {
        try {
            return this.slimeLoader.worldExists(this.getFileName());
        }catch(IOException exception) {
            return false;
        }
    }

    @Override
    public String getFileName() {
        return this.getFileName(this.getMapName());
    }

    @Override
    public String getFileName(String mapName) {
        return this.getCategoryIdentifier() + mapName;
    }

    @Override
    public String getPrettyName() {
        return this.getPrettyName(this.getMapName());
    }

    @Override
    public String getPrettyName(String mapName) {
        return "§c§l" + this.getMapCategory() + "§7/§f§l" + mapName;
    }

    @Override
    public @Nullable String getLoadedBy() {
        return this.loadedBy;
    }

    @Override
    public @Nullable Date getLoadedSince() {
        return this.loadedSince;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public void teleportTo(Player player) {
        World world = Bukkit.getWorld(this.getFileName());

        if(this.slimeWorld == null || this.slimeWorld.getPropertyMap() == null || world == null) return;

        //ToDO: replace with spawn location out of database

        SlimePropertyMap slimePropertyMap = this.slimeWorld.getPropertyMap();

        int x = slimePropertyMap.getValue(SlimeProperties.SPAWN_X);
        int y = slimePropertyMap.getValue(SlimeProperties.SPAWN_Y);
        int z = slimePropertyMap.getValue(SlimeProperties.SPAWN_Z);

        this.teleportTo(player, new Location(world, x, y, z));
    }

    @Override
    public void teleportTo(Player player, Location location) {
        player.teleport(location);
    }

    @Override
    public void teleportTo(Player player, String locationKey) {
        //ToDo: allow users to create custom location keys in the map management inventory
    }

    public void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance();
        if(!this.isLocked()) {
            SlimeWorld slimeWorld = null;
            try {
                if(this.exists()) {
                    slimeWorld = this.getSlimePlugin().loadWorld(this.getSlimeLoader(), this.getFileName(), readOnly, slimePropertyMap);
                }else {
                    slimeWorld = this.getSlimePlugin().createEmptyWorld(this.getSlimeLoader(), this.getFileName(), readOnly, slimePropertyMap);
                }
            }catch(Exception exception) {
                performance.setSuccess(exception);
            }
            if(slimeWorld != null) {
                performance.setSlimeWorld(slimeWorld);
                performance.setSuccess();
                this.setLoadedSince(new Date());
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_LOCKED);
        }
        performanceCallback.done(performance);
    }

    public void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        World world = Bukkit.getWorld(this.getFileName());
        if(world != null) {
            if(this.exists()) {
                try {
                    if(save) this.getSlimeLoader().saveWorld(this.getFileName(), ((CraftSlimeWorld) slimeWorld).serialize(), false);
                    if(world.getPlayers().isEmpty()) {
                        performance.setSuccess();
                        BukkitSynchronousExecutor.execute(MapCreatorPlugin.getMapCreatorPlugin(), () -> {
                            Bukkit.unloadWorld(world, save);
                        });
                    }else {
                        performance.setSuccess(MapCreator.Performance.FailureReason.PLAYERS_ON_MAP);
                    }
                }catch(Exception exception) {
                    performance.setSuccess(exception);
                }
            }else {
                performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_NOT_LOADED);
        }
        performanceCallback.done(performance);
    }

    public void delete(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.exists()) {
            try {
                this.slimeLoader.deleteWorld(this.getFileName());
                performance.setSuccess();
            }catch(Exception exception) {
                performance.setSuccess(exception);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
        }
        performanceCallback.done(performance);
    }

    public void leave(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        this.save(false, null, performanceCallback);
    }

    public void importWorld(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.getImportableWorldFile() != null) {
            if(!this.exists()) {
                try {
                    this.slimePlugin.importWorld(this.getImportableWorldFile(), this.getFileName(), this.slimeLoader);
                    performance.setSuccess();
                }catch(Exception exception) {
                    performance.setSuccess(exception);
                }
            }else {
                performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_ALREADY_EXISTS_IN_DATA_SOURCE);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.NO_IMPORTABLE_WORLD);
        }
        performanceCallback.done(performance);
    }

    public void clone(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.getCloneFrom() != null) {
            if(this.getCloneFrom().exists()) {
                this.customMapCreator.perform(MapCreator.Action.LOAD, this.getCloneFrom(), loadClonePerformance -> {
                    if(loadClonePerformance.isSuccess()) {
                        try {
                            loadClonePerformance.getSlimeWorld().clone(this.getFileName(), this.slimeLoader, false);
                            this.customMapCreator.perform(MapCreator.Action.LOAD, this, loadPerformance -> {
                                performance.setSlimeWorld(loadPerformance.getSlimeWorld());
                                this.customMapCreator.perform(MapCreator.Action.LEAVE_WITHOUT_SAVING, this.getCloneFrom(), leavePerformance -> {
                                    performance.setSuccess();
                                    performanceCallback.done(performance);
                                });
                            });
                        }catch(Exception exception) {
                            performance.setSuccess(exception);
                            performanceCallback.done(performance);
                        }
                    }else {
                        performance.setSuccess(MapCreator.Performance.FailureReason.NO_CLONEABLE_MAP);
                        performanceCallback.done(performance);
                    }
                });
            }else {
                performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
                performanceCallback.done(performance);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.NO_CLONEABLE_MAP);
            performanceCallback.done(performance);
        }
    }

    public void rename(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance();

        try {
            boolean success = this.slimeLoader.renameWorld(this.getFileName(), this.getRenameTo().getFileName());

            performance.setSuccess(success);
            performanceCallback.done(performance);
        }catch(UnknownWorldException | IOException exception) {
            performance.setSuccess(exception);
            performanceCallback.done(performance);
        }
    }

    public void checkArguments() {
        Preconditions.checkNotNull(this.mapName, "mapName == null");
        Preconditions.checkNotNull(this.mapCategory, "mapCategory == null");
        Preconditions.checkNotNull(this.slimePlugin, "slimePlugin == null");
        Preconditions.checkNotNull(this.slimeLoader, "slimeLoader == null");
    }

    public CustomMapCreatorMap setMapCategory(String mapCategory) {
        this.mapCategory = mapCategory.toUpperCase();
        return this;
    }

    public String getCategoryIdentifier() {
        return this.getMapCategory().toUpperCase() + CATEGORY_MAP_SEPARATOR;
    }

    public void setSlimeWorld(SlimeWorld slimeWorld) {
        this.slimeWorld = slimeWorld;
    }
}
