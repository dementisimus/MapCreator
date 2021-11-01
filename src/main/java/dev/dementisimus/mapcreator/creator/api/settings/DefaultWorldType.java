package dev.dementisimus.mapcreator.creator.api.settings;

import lombok.Getter;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultWorldType @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:20:58
 */
public enum DefaultWorldType {

    DEFAULT("default"),
    FLAT("flat"),
    LARGE_BIOMES("large_biomes"),
    AMPLIFIED("amplified"),
    CUSTOMIZED("customized"),
    DEBUG_ALL_BLOCK_STATES("debug_all_block_states");

    @Getter private final String id;

    DefaultWorldType(String id) {
        this.id = id;
    }
}
