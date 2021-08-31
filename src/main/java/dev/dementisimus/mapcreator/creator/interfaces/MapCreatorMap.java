package dev.dementisimus.mapcreator.creator.interfaces;

import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.dementisimus.capi.core.callback.Callback;
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

    String CATEGORY_MAP_SEPARATOR = "...";

    void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback);

    void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback);

    void delete(Callback<MapCreator.Performance> performanceCallback);

    void leave(Callback<MapCreator.Performance> performanceCallback);

    void importWorld(Callback<MapCreator.Performance> performanceCallback);

    boolean isLocked();

    boolean exists();

    String getWorldFileName();

    void checkArguments();

    String getCategoryIdentifier();

    String getFileName();

    String getPrettyFileName();
}
