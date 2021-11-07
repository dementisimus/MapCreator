package dev.dementisimus.mapcreator.creator.api.settings.biomes.overworld;

import dev.dementisimus.mapcreator.creator.api.settings.biomes.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Biome;

import static dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment.OVERWORLD;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultOverworldBiome @ MapCreator
 *
 * @author dementisimus
 * @since 02.11.2021:11:33
 */
@AllArgsConstructor
public enum DefaultOverworldBiome implements DefaultBiome {

    OCEAN(OVERWORLD, Biome.OCEAN, "default.biome.ocean", Material.SEAGRASS),
    PLAINS(OVERWORLD, Biome.PLAINS, "default.biome.plains", Material.GRASS),
    DESERT(OVERWORLD, Biome.DESERT, "default.biome.desert", Material.SAND),
    MOUNTAINS(OVERWORLD, Biome.MOUNTAINS, "default.biome.mountains", Material.STONE),
    FOREST(OVERWORLD, Biome.FOREST, "default.biome.forest", Material.OAK_LEAVES),
    TAIGA(OVERWORLD, Biome.TAIGA, "default.biome.taiga", Material.SPRUCE_SAPLING),
    SWAMP(OVERWORLD, Biome.SWAMP, "default.biome.swamp", Material.LILY_PAD),
    RIVER(OVERWORLD, Biome.RIVER, "default.biome.river", Material.SALMON),
    FROZEN_OCEAN(OVERWORLD, Biome.FROZEN_OCEAN, "default.biome.frozen.ocean", Material.PACKED_ICE),
    FROZEN_RIVER(OVERWORLD, Biome.FROZEN_RIVER, "default.biome.frozen.river", Material.ICE),
    SNOWY_TUNDRA(OVERWORLD, Biome.SNOWY_TUNDRA, "default.biome.snowy.tundra", Material.SNOWBALL),
    SNOWY_MOUNTAINS(OVERWORLD, Biome.SNOWY_MOUNTAINS, "default.biome.snowy.mountains", Material.SNOW_BLOCK),
    MUSHROOM_FIELDS(OVERWORLD, Biome.MUSHROOM_FIELDS, "default.biome.mushroom.fields", Material.RED_MUSHROOM),
    MUSHROOM_FIELD_SHORE(OVERWORLD, Biome.MUSHROOM_FIELD_SHORE, "default.biome.mushroom.fields.shore", Material.BROWN_MUSHROOM),
    BEACH(OVERWORLD, Biome.BEACH, "default.biome.beach", Material.TURTLE_EGG),
    DESERT_HILLS(OVERWORLD, Biome.DESERT_HILLS, "default.biome.desert.hills", Material.SANDSTONE),
    WOODED_HILLS(OVERWORLD, Biome.WOODED_HILLS, "default.biome.wooded.hills", Material.STICK),
    TAIGA_HILLS(OVERWORLD, Biome.TAIGA_HILLS, "default.biome.taiga.hills", Material.SPRUCE_LEAVES),
    MOUNTAIN_EDGE(OVERWORLD, Biome.MOUNTAIN_EDGE, "default.biome.mountain.edge", Material.COBBLESTONE),
    JUNGLE(OVERWORLD, Biome.JUNGLE, "default.biome.jungle", Material.JUNGLE_SAPLING),
    JUNGLE_HILLS(OVERWORLD, Biome.JUNGLE_HILLS, "default.biome.jungle.hills", Material.JUNGLE_LEAVES),
    JUNGLE_EDGE(OVERWORLD, Biome.JUNGLE_EDGE, "default.biome.jungle.edge", Material.JUNGLE_BOAT),
    DEEP_OCEAN(OVERWORLD, Biome.DEEP_OCEAN, "default.biome.deep.ocean", Material.KELP),
    STONE_SHORE(OVERWORLD, Biome.STONE_SHORE, "default.biome.stone.shore", Material.STONE_BRICKS),
    SNOWY_BEACH(OVERWORLD, Biome.SNOWY_BEACH, "default.biome.snowy.beach", Material.POWDER_SNOW_BUCKET),
    BIRCH_FOREST(OVERWORLD, Biome.BIRCH_FOREST, "default.biome.birch.forest", Material.BIRCH_SAPLING),
    BIRCH_FOREST_HILLS(OVERWORLD, Biome.BIRCH_FOREST_HILLS, "default.biome.birch.forest.hills", Material.BIRCH_LEAVES),
    DARK_FOREST(OVERWORLD, Biome.DARK_FOREST, "default.biome.dark.forest", Material.DARK_OAK_SAPLING),
    SNOWY_TAIGA(OVERWORLD, Biome.SNOWY_TAIGA, "default.biome.snowy.taiga", Material.FERN),
    SNOWY_TAIGA_HILLS(OVERWORLD, Biome.SNOWY_TAIGA_HILLS, "default.biome.snowy.taiga.hills", Material.POPPY),
    GIANT_TREE_TAIGA(OVERWORLD, Biome.GIANT_TREE_TAIGA, "default.biome.giant.tree.taiga", Material.PODZOL),
    GIANT_TREE_TAIGA_HILLS(OVERWORLD, Biome.GIANT_TREE_TAIGA_HILLS, "default.biome.giant.tree.taiga.hills", Material.MOSSY_COBBLESTONE),
    WOODED_MOUNTAINS(OVERWORLD, Biome.WOODED_MOUNTAINS, "default.biome.wooded.mountains", Material.CALCITE),
    SAVANNA(OVERWORLD, Biome.SAVANNA, "default.biome.savanna", Material.ACACIA_SAPLING),
    SAVANNA_PLATEAU(OVERWORLD, Biome.SAVANNA_PLATEAU, "default.biome.savanna.plateau", Material.ACACIA_LEAVES),
    BADLANDS(OVERWORLD, Biome.BADLANDS, "default.biome.badlands", Material.RED_SAND),
    WOODED_BADLANDS_PLATEAU(OVERWORLD, Biome.WOODED_BADLANDS_PLATEAU, "default.biome.wooded.badlands.plateau", Material.COARSE_DIRT),
    BADLANDS_PLATEAU(OVERWORLD, Biome.BADLANDS_PLATEAU, "default.biome.badlands.plateau", Material.DEAD_BUSH),
    WARM_OCEAN(OVERWORLD, Biome.WARM_OCEAN, "default.biome.warm.ocean", Material.TROPICAL_FISH_BUCKET),
    LUKEWARM_OCEAN(OVERWORLD, Biome.LUKEWARM_OCEAN, "default.biome.lukewarm.ocean", Material.SQUID_SPAWN_EGG),
    COLD_OCEAN(OVERWORLD, Biome.COLD_OCEAN, "default.biome.cold.ocean", Material.GRAVEL),
    DEEP_WARM_OCEAN(OVERWORLD, Biome.DEEP_WARM_OCEAN, "default.biome.deep.warm.ocean", Material.TROPICAL_FISH),
    DEEP_LUKEWARM_OCEAN(OVERWORLD, Biome.DEEP_LUKEWARM_OCEAN, "default.biome.deep.lukewarm.ocean", Material.PRISMARINE_CRYSTALS),
    DEEP_COLD_OCEAN(OVERWORLD, Biome.DEEP_COLD_OCEAN, "default.biome.deep.cold.ocean", Material.FLINT),
    DEEP_FROZEN_OCEAN(OVERWORLD, Biome.DEEP_FROZEN_OCEAN, "default.biome.deep.frozen.ocean", Material.SALMON_BUCKET),
    THE_VOID(OVERWORLD, Biome.THE_VOID, "default.biome.the.void", Material.DEAD_TUBE_CORAL),
    SUNFLOWER_PLAINS(OVERWORLD, Biome.SUNFLOWER_PLAINS, "default.biome.sunflower.plains", Material.SUNFLOWER),
    DESERT_LAKES(OVERWORLD, Biome.DESERT_LAKES, "default.biome.desert.lakes", Material.CACTUS),
    GRAVELLY_MOUNTAINS(OVERWORLD, Biome.GRAVELLY_MOUNTAINS, "default.biome.gravelly.mountains", Material.SNOW),
    FLOWER_FOREST(OVERWORLD, Biome.FLOWER_FOREST, "default.biome.flower.forest", Material.PINK_TULIP),
    TAIGA_MOUNTAINS(OVERWORLD, Biome.TAIGA_MOUNTAINS, "default.biome.taiga.mountains", Material.LARGE_FERN),
    SWAMP_HILLS(OVERWORLD, Biome.SWAMP_HILLS, "default.biome.swamp.hills", Material.BLUE_ORCHID),
    ICE_SPIKES(OVERWORLD, Biome.ICE_SPIKES, "default.biome.ice.spikes", Material.PACKED_ICE),
    MODIFIED_JUNGLE(OVERWORLD, Biome.MODIFIED_JUNGLE, "default.biome.modified.jungle", Material.MELON_SLICE),
    MODIFIED_JUNGLE_EDGE(OVERWORLD, Biome.MODIFIED_JUNGLE_EDGE, "default.biome.modified.jungle.edge", Material.MELON),
    TALL_BIRCH_FOREST(OVERWORLD, Biome.TALL_BIRCH_FOREST, "default.biome.tall.birch.forest", Material.BEE_NEST),
    TALL_BIRCH_HILLS(OVERWORLD, Biome.TALL_BIRCH_HILLS, "default.biome.tall.birch.hills", Material.BEEHIVE),
    DARK_FOREST_HILLS(OVERWORLD, Biome.DARK_FOREST_HILLS, "default.biome.dark.forest.hills", Material.RED_MUSHROOM_BLOCK),
    SNOWY_TAIGA_MOUNTAINS(OVERWORLD, Biome.SNOWY_TAIGA_MOUNTAINS, "default.biome.snowy.taiga.mountains", Material.POPPY),
    GIANT_SPRUCE_TAIGA(OVERWORLD, Biome.GIANT_SPRUCE_TAIGA, "default.biome.giant.spruce.taiga", Material.BROWN_MUSHROOM),
    GIANT_SPRUCE_TAIGA_HILLS(OVERWORLD, Biome.GIANT_SPRUCE_TAIGA_HILLS, "default.biome.giant.spruce.taiga.hills", Material.MOSSY_COBBLESTONE),
    MODIFIED_GRAVELLY_MOUNTAINS(OVERWORLD, Biome.MODIFIED_GRAVELLY_MOUNTAINS, "default.biome.modified.gravelly.mountains", Material.GRAVEL),
    SHATTERED_SAVANNA(OVERWORLD, Biome.SHATTERED_SAVANNA, "default.biome.shattered.savanna", Material.COARSE_DIRT),
    SHATTERED_SAVANNA_PLATEAU(OVERWORLD, Biome.SHATTERED_SAVANNA_PLATEAU, "default.biome.shattered.savanna.plateau", Material.DIRT_PATH),
    ERODED_BADLANDS(OVERWORLD, Biome.ERODED_BADLANDS, "default.biome.eroded.badlands", Material.TERRACOTTA),
    MODIFIED_WOODED_BADLANDS_PLATEAU(OVERWORLD, Biome.MODIFIED_WOODED_BADLANDS_PLATEAU, "default.biome.modified.wooded.badlands.plateau", Material.GRASS_BLOCK),
    MODIFIED_BADLANDS_PLATEAU(OVERWORLD, Biome.MODIFIED_BADLANDS_PLATEAU, "default.biome.modified.badlands.plateau", Material.ORANGE_TERRACOTTA),
    BAMBOO_JUNGLE(OVERWORLD, Biome.BAMBOO_JUNGLE, "default.biome.bamboo.jungle", Material.BAMBOO),
    BAMBOO_JUNGLE_HILLS(OVERWORLD, Biome.BAMBOO_JUNGLE_HILLS, "default.biome.bamboo.jungle.hills", Material.PANDA_SPAWN_EGG),
    DRIPSTONE_CAVES(OVERWORLD, Biome.DRIPSTONE_CAVES, "default.biome.dripstone.caves", Material.DRIPSTONE_BLOCK),
    LUSH_CAVES(OVERWORLD, Biome.LUSH_CAVES, "default.biome.lush.caves", Material.GLOW_BERRIES);

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
