package dev.dementisimus.mapcreator.creator.api.settings.biomes.nether;

import dev.dementisimus.mapcreator.creator.api.settings.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.DefaultWorldEnvironment;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Biome;

import static dev.dementisimus.mapcreator.creator.api.settings.DefaultWorldEnvironment.NETHER;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultNetherBiome @ MapCreator
 *
 * @author dementisimus
 * @since 02.11.2021:11:33
 */
@AllArgsConstructor
public enum DefaultNetherBiome implements DefaultBiome {

    NETHER_WASTES(NETHER, Biome.NETHER_WASTES, "default.biome.nether.wastes", Material.NETHER_QUARTZ_ORE),
    SOUL_SAND_VALLEY(NETHER, Biome.SOUL_SAND_VALLEY, "default.biome.soul.sand.valley", Material.SOUL_SOIL),
    CRIMSON_FOREST(NETHER, Biome.CRIMSON_FOREST, "default.biome.crimson.forest", Material.WEEPING_VINES),
    WARPED_FOREST(NETHER, Biome.WARPED_FOREST, "default.biome.warped.forest", Material.TWISTING_VINES),
    BASALT_DELTAS(NETHER, Biome.BASALT_DELTAS, "default.biome.basalt.deltas", Material.BASALT);

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
