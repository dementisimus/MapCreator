package dev.dementisimus.mapcreator.creator.api.settings.biomes.the_end;

import dev.dementisimus.mapcreator.creator.api.settings.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.DefaultWorldEnvironment;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultTheEndBiome @ MapCreator
 *
 * @author dementisimus
 * @since 02.11.2021:11:35
 */
@AllArgsConstructor
public enum DefaultTheEndBiome implements DefaultBiome {

    THE_END(DefaultWorldEnvironment.THE_END, Biome.THE_END, "default.biome.the.end", Material.END_STONE),
    SMALL_END_ISLANDS(DefaultWorldEnvironment.THE_END, Biome.SMALL_END_ISLANDS, "default.biome.small.end.islands", Material.END_STONE_BRICKS),
    END_MIDLANDS(DefaultWorldEnvironment.THE_END, Biome.END_MIDLANDS, "default.biome.end.midlands", Material.ENDER_PEARL),
    END_HIGHLANDS(DefaultWorldEnvironment.THE_END, Biome.END_HIGHLANDS, "default.biome.end.highlands", Material.CHORUS_FLOWER),
    END_BARRENS(DefaultWorldEnvironment.THE_END, Biome.END_BARRENS, "default.biome.end.barrens", Material.CHORUS_FRUIT);

    private final DefaultWorldEnvironment worldEnvironment;
    private final org.bukkit.block.Biome biome;
    private final String translationProperty;
    private final Material icon;

    @Override
    public DefaultWorldEnvironment getWorldEnvironment() {
        return this.worldEnvironment;
    }

    @Override
    public org.bukkit.block.Biome getBiome() {
        return this.biome;
    }

    @Override
    public String getTranslationProperty() {
        return this.translationProperty;
    }

    @Override
    public Material getIcon() {
        return this.icon;
    }
}
