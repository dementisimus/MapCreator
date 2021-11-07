package dev.dementisimus.mapcreator.creator.api.settings.worldtype;

import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultWorldType @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:20:58
 */
@AllArgsConstructor
public enum DefaultWorldType {

    DEFAULT("default", "map.creation.settings.world.type.default", 10, Material.GRASS),
    FLAT("flat", "map.creation.settings.world.type.flat", 11, Material.GRASS_BLOCK),
    LARGE_BIOMES("large_biomes", "map.creation.settings.world.type.large.biomes", 12, Material.DARK_OAK_SAPLING),
    AMPLIFIED("amplified", "map.creation.settings.world.type.amplified", 14, Material.COBBLESTONE),
    CUSTOMIZED("customized", "map.creation.settings.world.type.customized", 15, Material.RAW_COPPER),
    DEBUG_ALL_BLOCK_STATES("debug_all_block_states", "map.creation.settings.world.type.debug.all.block.states", 16, Material.WITHER_ROSE);

    @Getter private final String id;
    @Getter private final String translationProperty;
    @Getter private final int slot;
    @Getter private final Material icon;

    public String translate(Player player) {
        return new BukkitTranslation(this.getTranslationProperty()).get(player);
    }
}
