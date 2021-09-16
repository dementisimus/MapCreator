package dev.dementisimus.mapcreator.listener;

import dev.dementisimus.capi.core.injection.annotations.bukkit.BukkitSetupListener;
import dev.dementisimus.capi.core.setup.events.SerializeSetupStateEvent;
import dev.dementisimus.capi.core.setup.states.SetupState;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.ExtraSetupStates.DEFAULT_WORLD;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SerializeSetupStateListener @ MapCreator
 *
 * @author dementisimus
 * @since 30.08.2021:13:29
 */
@BukkitSetupListener
public class SerializeSetupStateListener implements Listener {

    @EventHandler
    public void on(SerializeSetupStateEvent event) {
        SetupState setupState = event.getSetupState();

        if(setupState.equals(DEFAULT_WORLD)) {
            event.setData(setupState.getString().replace("/", CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR));
        }
    }
}
