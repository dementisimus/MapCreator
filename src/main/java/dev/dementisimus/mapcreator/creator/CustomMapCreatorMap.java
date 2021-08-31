package dev.dementisimus.mapcreator.creator;

import com.google.common.base.Preconditions;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.pools.BukkitSynchronousExecutor;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;

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

    @Getter
    @Setter
    private File importableWorldFile;

    @Getter
    @Setter
    private String mapName;

    @Getter
    @Setter
    private String mapCategory;

    @Getter
    @Setter
    private SlimeWorld slimeWorld;

    @Getter
    @Setter
    private String loadedBy;

    @Getter
    @Setter
    private Date loadedSince;

    public CustomMapCreatorMap() {
        this.slimePlugin = MapCreatorPlugin.getMapCreatorPlugin().getSlimePlugin();
        this.slimeLoader = MapCreatorPlugin.getMapCreatorPlugin().getSlimeLoader();
    }

    public CustomMapCreatorMap(String mapName, String mapCategory) {
        this();

        this.mapName = mapName;
        this.mapCategory = mapCategory;
    }

    @Override
    public void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance();
        if(!this.isLocked()) {
            SlimeWorld slimeWorld = null;
            try {
                if(this.exists()) {
                    slimeWorld = this.getSlimePlugin().loadWorld(this.getSlimeLoader(), this.getWorldFileName(), readOnly, slimePropertyMap);
                }else {
                    slimeWorld = this.getSlimePlugin().createEmptyWorld(this.getSlimeLoader(), this.getWorldFileName(), readOnly, slimePropertyMap);
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

    @Override
    public void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        World world = Bukkit.getWorld(this.getWorldFileName());
        if(world != null) {
            if(this.exists()) {
                try {
                    if(save) this.getSlimeLoader().saveWorld(this.getWorldFileName(), ((CraftSlimeWorld) slimeWorld).serialize(), false);
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

    @Override
    public void delete(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.exists()) {
            try {
                this.slimeLoader.deleteWorld(this.getWorldFileName());
                performance.setSuccess();
            }catch(Exception exception) {
                performance.setSuccess(exception);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void leave(Callback<MapCreator.Performance> performanceCallback) {
        this.checkArguments();

        this.save(false, null, performanceCallback);
    }

    @Override
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

    @Override
    public boolean isLocked() {
        try {
            return this.slimeLoader.isWorldLocked(this.getWorldFileName());
        }catch(UnknownWorldException | IOException exception) {
            return false;
        }
    }

    @Override
    public boolean exists() {
        try {
            return this.slimeLoader.worldExists(this.getWorldFileName());
        }catch(IOException exception) {
            return false;
        }
    }

    @Override
    public String getWorldFileName() {
        return this.mapCategory.toUpperCase() + CATEGORY_MAP_SEPARATOR + this.mapName;
    }

    @Override
    public void checkArguments() {
        Preconditions.checkNotNull(this.mapName, "mapName == null");
        Preconditions.checkNotNull(this.mapCategory, "mapCategory == null");
        Preconditions.checkNotNull(this.slimePlugin, "slimePlugin == null");
        Preconditions.checkNotNull(this.slimeLoader, "slimeLoader == null");
    }

    @Override
    public String getCategoryIdentifier() {
        return this.getMapCategory() + CATEGORY_MAP_SEPARATOR;
    }

    @Override
    public String getFileName() {
        return this.getCategoryIdentifier() + this.getMapName();
    }

    @Override
    public String getPrettyFileName() {
        return "§c§l" + this.getMapCategory() + "§7/§7§l" + this.getMapName();
    }
}
