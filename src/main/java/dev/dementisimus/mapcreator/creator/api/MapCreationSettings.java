package dev.dementisimus.mapcreator.creator.api;

import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreationSettings @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:20:06
 */
public interface MapCreationSettings {

    WorldData toWorldData();

    SlimePropertyMap toSlimePropertyMap();

}
