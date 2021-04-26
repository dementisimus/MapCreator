package dev.dementisimus.mapcreator.commands;

import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.utils.LocationManager;
import dev.dementisimus.mapcreator.api.MapCreatorAPI;
import dev.dementisimus.mapcreator.creator.AbstractCreator;
import dev.dementisimus.mapcreator.translation.Prefixes;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class COMMAND_mapcreator @ MapCreator
 *
 * @author dementisimus
 * @since 12.07.2020:12:56
 */
public class COMMAND_mapcreator implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String l, @NotNull String[] args) {
        Player player = ((cs instanceof Player) ? (Player) cs : null);
        if(player != null) {
            MapCreatorAPI creator = new MapCreatorAPI(player);
            int argsLength = args.length;
            if(argsLength == 0 || argsLength == 1 || argsLength == 2) {
                if(argsLength == 1) {
                    if(args[0].equalsIgnoreCase("tree")) {
                        player.sendMessage(Objects.requireNonNull(AbstractCreator.printDirectoryTree(AbstractCreator.worldPoolFolder)));
                    }else if(args[0].equalsIgnoreCase("centerLocation")) {
                        player.teleport(LocationManager.round(player.getLocation()));
                    }else {
                        sendHelpMessage(player);
                    }
                }else if(argsLength == 2) {
                    if(args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("spawn")) {
                        LocationManager.setLocation("SPAWN", player.getLocation(), b -> sendLocationSetMessage(player));
                    }else if(args[0].equalsIgnoreCase("tree")) {
                        File subFolder = new File(AbstractCreator.worldPoolFolder, args[1].toUpperCase());
                        if(subFolder.exists()) {
                            player.sendMessage(Objects.requireNonNull(AbstractCreator.printDirectoryTree(subFolder)));
                        }else {
                            player.sendMessage(new BukkitTranslation(Translations.CREATOR_COMMAND_TREE_SUBFOLDER_DOES_NOT_EXIST.id).get(player,
                                                                                                                                        new String[]{
                                                                                                                                                "$prefix$",
                                                                                                                                                "$folder$"},
                                                                                                                                        new String[]{
                                                                                                                                                Prefixes.MAPCREATOR,
                                                                                                                                                args[1].toUpperCase()}));
                        }
                    }else if(args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                        String arg = args[1];
                        List<Player> players = Bukkit.getOnlinePlayers()
                                                     .stream()
                                                     .filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(arg))
                                                     .collect(Collectors.toList());
                        if(arg.equalsIgnoreCase("all")) {
                            int count = 0;
                            Player firstP = null;
                            StringBuilder playersTeleportedBuilder = new StringBuilder();
                            for(Player pl : Bukkit.getOnlinePlayers()) {
                                if(!pl.getName().equalsIgnoreCase(player.getName())) {
                                    teleport(player, pl, pl, Translations.CREATOR_TELEPORTED_TO_WORLD_BY_PLAYER);
                                    if(count == 0) {
                                        firstP = pl;
                                    }
                                    count++;
                                    playersTeleportedBuilder.append(pl.getName()).append(", ");
                                }
                            }
                            String playersTeleported = playersTeleportedBuilder.toString();
                            if(playersTeleported.endsWith(", ")) {
                                playersTeleported = playersTeleported.substring(0, playersTeleported.length() - 2);
                            }
                            if(count == 0) {
                                player.sendMessage(new BukkitTranslation(Translations.CREATOR_NO_PLAYER_TELEPORTED.id).get(player,
                                                                                                                           "$prefix$",
                                                                                                                           Prefixes.MAPCREATOR));
                            }else {
                                if(count == 1) {
                                    player.sendMessage(new BukkitTranslation(Translations.CREATOR_PLAYER_TELEPORTED.id).get(player,
                                                                                                                            new String[]{"$prefix$",
                                                                                                                                         "$player$"},
                                                                                                                            new String[]{
                                                                                                                                    Prefixes.MAPCREATOR,
                                                                                                                                    firstP.getName()}));
                                }else {
                                    player.sendMessage(new BukkitTranslation(Translations.CREATOR_PLAYERS_TELEPORTED.id).get(player,
                                                                                                                             new String[]{"$prefix$",
                                                                                                                                          "$playerCount$",
                                                                                                                                          "$players$"},
                                                                                                                             new String[]{
                                                                                                                                     Prefixes.MAPCREATOR,
                                                                                                                                     String.valueOf(
                                                                                                                                             count),
                                                                                                                                     playersTeleported}));
                                }
                            }
                        }else if(players != null && !players.isEmpty()) {
                            Player target = players.get(0);
                            if(!target.getName().equalsIgnoreCase(player.getName())) {
                                teleport(player, player, player, Translations.CREATOR_TELEPORTED_TO_WORLD);
                                target.sendMessage(new BukkitTranslation(Translations.CREATOR_PLAYER_TELEPORTED.id).get(player,
                                                                                                                        new String[]{"$prefix$",
                                                                                                                                     "$player$"},
                                                                                                                        new String[]{
                                                                                                                                Prefixes.MAPCREATOR,
                                                                                                                                target.getName()}));
                            }else {
                                sendHelpMessage(player);
                            }
                        }else if(!player.getWorld().getName().equalsIgnoreCase(arg)) {
                            if(Bukkit.getWorld(arg) != null) {
                                AbstractCreator.teleportToExistingWorld(player,
                                                                        Bukkit.getWorld(arg),
                                                                        player,
                                                                        player,
                                                                        Translations.CREATOR_TELEPORTED_TO_WORLD,
                                                                        null);
                            }else {
                                player.sendMessage(new BukkitTranslation(Translations.CREATOR_WORLD_NOT_LOADED_MAP_ONLY.id).get(player,
                                                                                                                                new String[]{
                                                                                                                                        "$prefix$",
                                                                                                                                        "$map$"},
                                                                                                                                new String[]{
                                                                                                                                        Prefixes.MAPCREATOR,
                                                                                                                                        arg}));
                            }
                        }else {
                            sendHelpMessage(player);
                        }
                    }else {
                        sendHelpMessage(player);
                    }
                }else {
                    sendHelpMessage(player);
                }
            }else {
                if(argsLength <= 4) {
                    String workType = args[0];
                    String mapType = args[1];
                    String mapName = args[2];
                    creator.setMapType(mapType);
                    creator.setMapName(mapName);
                    if(argsLength == 3) {
                        if(workType.equalsIgnoreCase("LOAD")) {
                            loadWorld(player, mapName, creator, false);
                        }else if(workType.equalsIgnoreCase("SAVE")) {
                            creator.save();
                        }else if(workType.equalsIgnoreCase("LEAVE")) {
                            creator.leave();
                        }else if(workType.equalsIgnoreCase("DELETE")) {
                            creator.delete();
                        }else {
                            sendHelpMessage(player);
                        }
                    }else {
                        boolean useDefaultWorldSettings;
                        try {
                            useDefaultWorldSettings = Boolean.parseBoolean(args[3]);
                        }catch(Exception ex) {
                            sendHelpMessage(player);
                            return false;
                        }
                        if(workType.equalsIgnoreCase("LOAD")) {
                            loadWorld(player, mapName, creator, useDefaultWorldSettings);
                        }else {
                            sendHelpMessage(player);
                        }
                    }
                }else {
                    sendHelpMessage(player);
                }
            }
        }
        return false;
    }

    private void teleport(Player p, Player target, Player sendMessageTo, Translations translations) {
        AbstractCreator.teleportToExistingWorld(p, p.getWorld(), target, sendMessageTo, translations, null);
    }

    private void loadWorld(Player p, String world, MapCreatorAPI creator, boolean setDefaultWorldSettings) {
        if(!p.getWorld().getName().equalsIgnoreCase(world)) {
            creator.load(setDefaultWorldSettings);
        }else {
            p.sendMessage(new BukkitTranslation(Translations.CREATOR_WORLD_ALREADY_LOADED_NAME_ONLY.id).get(p,
                                                                                                            new String[]{"$prefix$", "$map$"},
                                                                                                            new String[]{Prefixes.MAPCREATOR,
                                                                                                                         world}));
        }
    }

    private void sendHelpMessage(Player p) {
        p.sendMessage(new BukkitTranslation(Translations.COMMAND_MAPCREATOR_HELP.id).get(p,
                                                                                         new String[]{"$prefix$"},
                                                                                         new String[]{Prefixes.MAPCREATOR}));
    }

    private void sendLocationSetMessage(Player p) {
        p.sendMessage(new BukkitTranslation(Translations.COMMAND_MAPCREATOR_SET_SPAWN.id).get(p,
                                                                                              new String[]{"$prefix$", "$world$"},
                                                                                              new String[]{Prefixes.MAPCREATOR,
                                                                                                           p.getWorld().getName()}));
    }

}
