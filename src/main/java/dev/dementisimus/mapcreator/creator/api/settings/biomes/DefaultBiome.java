package dev.dementisimus.mapcreator.creator.api.settings.biomes;

import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
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

    default String translate(Player player) {
        return new BukkitTranslation(this.getTranslationProperty()).get(player);
    }
}
