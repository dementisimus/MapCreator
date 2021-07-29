package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import com.grinderwolf.swm.api.SlimePlugin;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.core.BukkitCoreAPI;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class WorldLoadListener @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:22:49
 */
@BukkitListener(additionalModulesToInject = {BukkitCoreAPI.class, SlimePlugin.class})
public class WorldLoadListener implements Listener {

    @Inject BukkitCoreAPI bukkitCoreAPI;
    @Inject SlimePlugin slimePlugin;

    @EventHandler
    public void on(WorldLoadEvent event) {
        World world = event.getWorld();

    }

}
