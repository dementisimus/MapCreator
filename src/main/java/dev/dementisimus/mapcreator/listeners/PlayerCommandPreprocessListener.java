package dev.dementisimus.mapcreator.listeners;

import dev.dementisimus.capi.core.annotations.bukkit.OptionalBukkitListener;
import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class PlayerCommandPreprocessListener @ MapCreator
 *
 * @author dementisimus
 * @since 14.11.2020:18:07
 */
@OptionalBukkitListener
public class PlayerCommandPreprocessListener implements Listener {

    private final ArrayList<Player> teleportWarningSent = new ArrayList<>();

    @EventHandler
    public void on(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        if(command.startsWith("/tp ") || command.startsWith("/teleport ")) {
            if(!this.teleportWarningSent.contains(player)) {
                player.sendMessage(new BukkitTranslation(Translations.CREATOR_TELEPORT_WARNING).get(player));
                this.teleportWarningSent.add(player);
                event.setCancelled(true);
            }
        }
    }

}
