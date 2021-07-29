package dev.dementisimus.mapcreator.creator;

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
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreatorMap @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:21:48
 */
public record CustomMapCreatorMap(String mapName, String mapCategory, SlimePlugin slimePlugin, SlimeLoader slimeLoader) implements MapCreatorMap {

    @Override
    public void create(SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) throws IOException, WorldAlreadyExistsException, CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException {
        MapCreator.Performance performance = new MapCreator.Performance();
        if(!this.isLoaded()) {
            if(!this.exists()) {
                performance.setSlimeWorld(this.slimePlugin().createEmptyWorld(this.slimeLoader(), this.getWorldFileName(), false, slimePropertyMap));
                performance.setSuccess();
            }else {
                this.load(false, slimePropertyMap, performanceCallback);
                return;
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_ALREADY_LOADED);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) throws CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException, IOException, WorldAlreadyExistsException {
        MapCreator.Performance performance = new MapCreator.Performance();
        if(!this.isLoaded()) {
            if(this.exists()) {
                performance.setSlimeWorld(this.slimePlugin().loadWorld(this.slimeLoader(), this.getWorldFileName(), readOnly, slimePropertyMap));
                performance.setSuccess();
            }else {
                this.create(slimePropertyMap, performanceCallback);
                return;
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_ALREADY_LOADED);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback) throws IOException {
        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        World world = Bukkit.getWorld(this.getWorldFileName());
        if(world != null) {
            if(this.exists()) {
                if(save) this.slimeLoader().saveWorld(this.getWorldFileName(), ((CraftSlimeWorld) slimeWorld).serialize(), false);
                if(world.getPlayers().isEmpty()) {
                    performance.setSuccess();
                    BukkitSynchronousExecutor.execute(MapCreatorPlugin.getMapCreatorPlugin(), () -> {
                        Bukkit.unloadWorld(world, true);
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
        MapCreator.Performance performance = new MapCreator.Performance((SlimeWorld) null);
        if(this.isLoaded()) {
            if(this.exists()) {
                this.slimeLoader.deleteWorld(this.getWorldFileName());
                performance.setSuccess();
            }else {
                performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_DOES_NOT_EXIST);
            }
        }else {
            performance.setSuccess(MapCreator.Performance.FailureReason.WORLD_NOT_LOADED);
        }
        performanceCallback.done(performance);
    }

    @Override
    public void leave(Callback<MapCreator.Performance> performanceCallback) throws IOException {
        this.save(false, null, performanceCallback);
    }

    @Override
    public boolean isLoaded() {
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
        return this.mapCategory + "." + this.mapName;
    }
}
