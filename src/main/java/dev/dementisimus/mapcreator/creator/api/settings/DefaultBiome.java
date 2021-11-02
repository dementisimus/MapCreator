package dev.dementisimus.mapcreator.creator.api.settings;

import org.bukkit.Material;
import org.bukkit.block.Biome;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultBiome @ MapCreator
 *
 * @author dementisimus
 * @since 02.11.2021:11:32
 */
public interface DefaultBiome {

    DefaultWorldEnvironment getWorldEnvironment();

    Biome getBiome();

    String getTranslationProperty();

    Material getIcon();
}
