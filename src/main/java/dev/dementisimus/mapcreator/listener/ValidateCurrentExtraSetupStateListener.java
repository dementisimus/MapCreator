package dev.dementisimus.mapcreator.listener;

import dev.dementisimus.capi.core.injection.annotations.bukkit.BukkitSetupListener;
import dev.dementisimus.capi.core.setup.events.ValidateCurrentExtraSetupStateEvent;
import dev.dementisimus.capi.core.setup.states.SetupState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.ExtraSetupStates.DEFAULT_WORLD;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.ExtraSetupStates.WORLD_IMPORTER_FOLDER_LOCATION;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class ValidateCurrentExtraSetupStateListener @ MapCreator
 *
 * @author dementisimus
 * @since 29.08.2021:23:50
 */
@BukkitSetupListener
public class ValidateCurrentExtraSetupStateListener implements Listener {

    @EventHandler
    public void on(ValidateCurrentExtraSetupStateEvent event) {
        SetupState currentSetupState = event.getCurrentSetupState();
        String input = event.getInput();

        if(currentSetupState.equals(WORLD_IMPORTER_FOLDER_LOCATION)) {
            File worldImportFolder = new File(input);
            if(!worldImportFolder.exists() || !worldImportFolder.isDirectory()) {
                event.setValidInput(false);
            }
        }else if(currentSetupState.equals(DEFAULT_WORLD)) {
            if(!input.contains("/")) {
                event.setValidInput(false);
            }
        }
    }
}
