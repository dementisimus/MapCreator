package dev.dementisimus.mapcreator.creator.interfaces;

import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.dementisimus.capi.core.callback.Callback;

import java.io.IOException;
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

    void create(SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) throws IOException, WorldAlreadyExistsException, CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException;

    void load(boolean readOnly, SlimePropertyMap slimePropertyMap, Callback<MapCreator.Performance> performanceCallback) throws CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException, IOException, WorldAlreadyExistsException;

    void save(boolean save, SlimeWorld slimeWorld, Callback<MapCreator.Performance> performanceCallback) throws IOException;

    void delete(Callback<MapCreator.Performance> performanceCallback) throws UnknownWorldException, IOException;

    void leave(Callback<MapCreator.Performance> performanceCallback) throws IOException;

    boolean isLoaded();

    boolean exists();

    String getWorldFileName();
}
