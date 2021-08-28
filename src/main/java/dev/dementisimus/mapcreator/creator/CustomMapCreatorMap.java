package dev.dementisimus.mapcreator.creator;

import com.google.common.base.Preconditions;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
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
    public void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) throws CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException, IOException, WorldAlreadyExistsException {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance();
        if(!this.isLocked()) {
            SlimeWorld slimeWorld;
            if(this.exists()) {
                slimeWorld = this.getSlimePlugin().loadWorld(this.getSlimeLoader(), this.getWorldFileName(), readOnly, slimePropertyMap);
            }else {
                slimeWorld = this.getSlimePlugin().createEmptyWorld(this.getSlimeLoader(), this.getWorldFileName(), readOnly, slimePropertyMap);
            }
            performance.setSlimeWorld(slimeWorld);
            performance.setSuccess();
            this.setLoadedSince(new Date());
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_LOCKED);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback) throws IOException {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        World world = Bukkit.getWorld(this.getWorldFileName());
        if(world != null) {
            if(this.exists()) {
                if(save) this.getSlimeLoader().saveWorld(this.getWorldFileName(), ((CraftSlimeWorld) slimeWorld).serialize(), false);
                if(world.getPlayers().isEmpty()) {
                    performance.setSuccess();
                    BukkitSynchronousExecutor.execute(MapCreatorPlugin.getMapCreatorPlugin(), () -> {
                        Bukkit.unloadWorld(world, save);
                    });
                }else {
                    performance.setSuccess(MapCreator.Performance.FailureReason.PLAYERS_ON_MAP);
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
    public void delete(Callback<MapCreator.Performance> performanceCallback) throws UnknownWorldException, IOException {
        this.checkArguments();

        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.exists()) {
            this.slimeLoader.deleteWorld(this.getWorldFileName());
            performance.setSuccess();
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void leave(Callback<MapCreator.Performance> performanceCallback) throws IOException {
        this.checkArguments();

        this.save(false, null, performanceCallback);
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
    public String getFullMapName() {
        return this.getCategoryIdentifier() + this.getMapName();
    }

    @Override
    public String getNiceFullMapName() {
        return "§c§l" + this.getMapCategory() + "§7/§7§l" + this.getMapName();
    }
}
