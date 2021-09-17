package dev.dementisimus.mapcreator.listener;

import dev.dementisimus.capi.core.injection.annotations.bukkit.BukkitSetupListener;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.capi.core.setup.events.SetNextExtraSetupStateEvent;
import dev.dementisimus.capi.core.setup.states.SetupState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.ExtraSetupStates.*;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SetNextExtraSetupStateListener @ MapCreator
 *
 * @author dementisimus
 * @since 29.08.2021:23:58
 */
@BukkitSetupListener
public class SetNextExtraSetupStateListener implements Listener {

    @EventHandler
    public void on(SetNextExtraSetupStateEvent event) {
        SetupManager setupManager = event.getSetupManager();
        SetupState currentSetupState = event.getCurrentSetupState();

        if(currentSetupState.equals(WORLD_IMPORTER_REQUIRED)) {
            if(setupManager.getSetupState(WORLD_IMPORTER_REQUIRED).getBoolean()) {
                event.setNextSetupState(WORLD_IMPORTER_FOLDER_LOCATION);
            }else {
                event.setNextSetupState(API_MODE);
            }
        }else if(currentSetupState.equals(USE_DEFAULT_WORLD_FOR_PLAYERS)) {
            if(setupManager.getSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS).getBoolean()) {
                event.setNextSetupState(DEFAULT_WORLD);
            }else {
                event.setNextSetupState(SIMPLE_TEMPLATE_MAP_WANTED);
            }
        }else if(currentSetupState.equals(API_MODE)) {
            if(setupManager.getSetupState(API_MODE).getBoolean()) {
                event.setCancelled(true);
            }else {
                event.setNextSetupState(USE_DEFAULT_WORLD_FOR_PLAYERS);
            }
        }
    }
}
