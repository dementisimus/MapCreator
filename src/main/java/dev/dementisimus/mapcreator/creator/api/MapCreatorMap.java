package dev.dementisimus.mapcreator.creator.api;

import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;

import java.util.Date;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreatorMap @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:21:52
 */
public interface MapCreatorMap {

    /**
     * Creates a new {@link MapCreatorMap} object for further use in the map creation process
     *
     * @param mapName     The map name {@link MapCreatorMap} object should represent
     * @param mapCategory The map category {@link MapCreatorMap} object should represent
     */
    static MapCreatorMap of(String mapName, String mapCategory) {
        return new CustomMapCreatorMap(mapName, mapCategory);
    }

    void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback);

    void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback);

    void delete(Callback<MapCreator.Performance> performanceCallback);

    void leave(Callback<MapCreator.Performance> performanceCallback);

    void importWorld(Callback<MapCreator.Performance> performanceCallback);

    void clone(Callback<MapCreator.Performance> performanceCallback);

    boolean isLocked();

    boolean exists();

    String getFileName();

    String getPrettyName();

    SlimeWorld getSlimeWorld();

    void setSlimeWorld(SlimeWorld slimeWorld);

    String getLoadedBy();

    Date getLoadedSince();
}
