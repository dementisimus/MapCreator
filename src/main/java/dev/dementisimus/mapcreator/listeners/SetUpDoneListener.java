package dev.dementisimus.mapcreator.listeners;

import dev.dementisimus.capi.core.events.bukkit.BukkitSetUpDoneEvent;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.mapcreator.creator.AbstractCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SetUpDoneListener @ MapCreator
 *
 * @author dementisimus
 * @since 15.07.2020:17:21
 */
public class SetUpDoneListener implements Listener {

    @EventHandler
    public void on(BukkitSetUpDoneEvent event) {
        if(event.isDone()) {
            AbstractCreator.setWorldPoolFolder();
            MapCreator.getMapCreator().registerCommandsAndEvents();
        }
    }

}
