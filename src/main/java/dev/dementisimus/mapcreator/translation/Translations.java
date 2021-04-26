package dev.dementisimus.mapcreator.translation;

/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class Translations @ MapCreator
 *
 * @author dementisimus
 * @since 10.07.2020:18:16
 */
public enum Translations {

    PLAYER_ACTIONBAR_CURRENTWORLD("player.actionbar.currentworld"),
    CONSOLE_SETUP_MAPPOOL("console.setup.mappool"),
    DEFAULT_WORLD_NOT_AVAILABLE("default_world.not.available"),
    CREATOR_WORLD_ALREADY_LOADED("creator.world.already.loaded"),
    CREATOR_WORLD_ALREADY_LOADED_NAME_ONLY("creator.world.already.loaded.name.only"),
    CREATOR_WORLD_NOT_LOADED_ERROR("creator.world.not.loaded.error"),
    CREATOR_WORLD_NOT_LOADED_ERROR_EXCEPTION("creator.world.not.loaded.error.exception"),
    CREATOR_WORLD_NOT_LOADED("creator.world.not.loaded"),
    CREATOR_WORLD_LOADED("creator.world.loaded"),
    CREATOR_WORLD_SAVED("creator.world.saved"),
    CREATOR_WORLD_LEFT("creator.world.left"),
    CREATOR_WORLD_DELETED("creator.world.deleted"),
    COMMAND_MAPCREATOR_HELP("command.mapcreator.help"),
    COMMAND_MAPCREATOR_SET_SPAWN("command.mapcreator.set.spawn"),
    CONSOLE_SETUP_SET_DEFAULT_WORLD_INSTEAD_OF_WORLD("console.setup.set.default.world.instead.of.world"),
    CONSOLE_SETUP_USE_API_MODE_ONLY("console.setup.use.api_mode.only"),
    CONSOLE_SETUP_DEFAULT_WORLD_FOR_USAGE("console.setup.default.world.for.usage"),
    CONSOLE_API_ENABLED("console.api.enabled"),
    CREATOR_WORLD_NOT_LOADED_BUKKIT_ERROR("creator.world.not.loaded.bukkit.error"),
    CREATOR_TELEPORTED_TO_WORLD("creator.teleported.to.world"),
    CREATOR_TELEPORTED_TO_WORLD_BY_PLAYER("creator.teleported.to.world.by.player"),
    CREATOR_TELEPORT_WARNING("creator.teleport.warning"),
    CREATOR_WORLD_NOT_LOADED_MAP_ONLY("creator.world.not.loaded.map.only"),
    CREATOR_PLAYERS_TELEPORTED("creator.players.teleported"),
    CREATOR_PLAYER_TELEPORTED("creator.player.teleported"),
    CREATOR_NO_PLAYER_TELEPORTED("creator.no.player.teleported"),
    CREATOR_COMMAND_TREE_SUBFOLDER_DOES_NOT_EXIST("creator.command.tree.subfolder.does.not.exist");

    public final String id;

    Translations(String id) {
        this.id = id;
    }

}
