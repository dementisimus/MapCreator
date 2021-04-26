package dev.dementisimus.mapcreator.listeners;

import dev.dementisimus.capi.core.utils.LocationManager;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.mapcreator.creator.CreatorConstants;
import dev.dementisimus.mapcreator.setup.AdditionalSetUpState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.Map;

import static dev.dementisimus.mapcreator.MapCreator.getMapCreator;
import static org.bukkit.Bukkit.getScheduler;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class PlayerJoinListener @ MapCreator
 *
 * @author dementisimus
 * @since 23.07.2020:22:41
 */
public class PlayerJoinListener implements Listener {

    private final Map<Player, Integer> taskId = new HashMap<>();

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(MapCreator.getMapCreator().getCoreAPI().getSetUpData().getBoolean(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
            teleport(player);
        }
    }

    @EventHandler
    public void on(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if(MapCreator.getMapCreator().getCoreAPI().getSetUpData().getBoolean(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD)) {
            int[] cou = {0};
            taskId.put(player, getScheduler().runTaskTimer(getMapCreator(), () -> {
                if(cou[0] == 1) {
                    cou[0] = 0;
                    getScheduler().cancelTask(taskId.get(player));
                }
                teleport(player);
                cou[0]++;

            }, 10, 1).getTaskId());
        }
    }

    private World getWorld() {
        return Bukkit.getWorld(CreatorConstants.DEFAULT_WORLD);
    }

    private void teleport(Player player) {
        LocationManager.getConfigLocation(getWorld().getWorldFolder(),
                                          getWorld().getName(),
                                          "SPAWN",
                                          location -> getScheduler().runTask(getMapCreator(), () -> {
                                              if(location != null) {
                                                  LocationManager.teleport(player, location);
                                              }
                                          }));
    }

}
