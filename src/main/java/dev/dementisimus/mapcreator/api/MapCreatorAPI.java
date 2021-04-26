package dev.dementisimus.mapcreator.api;

import dev.dementisimus.mapcreator.creator.AbstractCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreatorAPI @ MapCreatorAPI
 *
 * @author dementisimus
 * @since 24.07.2020:17:40
 */
public class MapCreatorAPI extends AbstractCreator {

    public MapCreatorAPI() {
        super();
    }

    public MapCreatorAPI(@NotNull Player player) {
        super(player);
    }

    public MapCreatorAPI(@NotNull String mapType, @NotNull String mapName) {
        super(mapType, mapName);
    }

}
