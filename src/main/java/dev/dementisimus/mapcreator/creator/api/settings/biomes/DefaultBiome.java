package dev.dementisimus.mapcreator.creator.api.settings.biomes;

import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.nether.DefaultNetherBiome;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.overworld.DefaultOverworldBiome;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.the_end.DefaultTheEndBiome;
import dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    static DefaultBiome of(String value) {
        DefaultBiome defaultBiome = null;
        List<DefaultBiome> defaultBiomeList = new ArrayList<>();

        defaultBiomeList.addAll(Arrays.asList(DefaultOverworldBiome.values()));
        defaultBiomeList.addAll(Arrays.asList(DefaultNetherBiome.values()));
        defaultBiomeList.addAll(Arrays.asList(DefaultTheEndBiome.values()));

        for(DefaultBiome biome : defaultBiomeList) {
            if(new BukkitTranslation(biome.getTranslationProperty()).matches(value)) {
                defaultBiome = biome;
                break;
            }
        }

        return defaultBiome;
    }

    DefaultWorldEnvironment getWorldEnvironment();

    Biome getBiome();

    String getTranslationProperty();

    Material getIcon();

    default String translate(Player player) {
        return new BukkitTranslation(this.getTranslationProperty()).get(player);
    }
}
