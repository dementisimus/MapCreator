package dev.dementisimus.mapcreator.creator.api.settings.difficulty;

import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultDifficulty @ MapCreator
 *
 * @author dementisimus
 * @since 02.11.2021:17:20
 */
@AllArgsConstructor
public enum DefaultDifficulty {

    PEACEFUL(Difficulty.PEACEFUL, "map.creation.settings.difficulty.peaceful", 11, Material.AXOLOTL_BUCKET),
    EASY(Difficulty.EASY, "map.creation.settings.difficulty.easy", 12, Material.APPLE),
    NORMAL(Difficulty.NORMAL, "map.creation.settings.difficulty.normal", 14, Material.GOLDEN_APPLE),
    HARD(Difficulty.HARD, "map.creation.settings.difficulty.hard", 15, Material.ENCHANTED_GOLDEN_APPLE);

    @Getter private final Difficulty difficulty;
    @Getter private final String translationProperty;
    @Getter private final int slot;
    @Getter private final Material icon;

    public String translate(Player player) {
        return new BukkitTranslation(this.getTranslationProperty()).get(player);
    }
}
