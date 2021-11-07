package dev.dementisimus.mapcreator.creator.api.settings.environment;

import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class DefaultWorldEnvironment @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:21:22
 */
@AllArgsConstructor
public enum DefaultWorldEnvironment {

    OVERWORLD("normal", "world.environment.overworld", Material.GRASS_BLOCK, 11),
    NETHER("nether", "world.environment.nether", Material.NETHERRACK, 13),
    THE_END("the_end", "world.environment.the.end", Material.END_STONE, 15);

    @Getter private final String id;
    @Getter private final String translationProperty;
    @Getter private final Material icon;
    @Getter private final int slot;

    public String translate(Player player) {
        return new BukkitTranslation(this.getTranslationProperty()).get(player);
    }
}
