package dev.dementisimus.mapcreator.listeners;

import dev.dementisimus.capi.core.events.bukkit.BukkitPrintCustomInstructionsMessageEvent;
import dev.dementisimus.capi.core.setup.SetUpState;
import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.mapcreator.setup.AdditionalSetUpState;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SetUpStatePrintInstructionsListener @ MapCreator
 *
 * @author dementisimus
 * @since 13.07.2020:12:29
 */
public class SetUpStatePrintInstructionsListener implements Listener {

    @EventHandler
    public void on(BukkitPrintCustomInstructionsMessageEvent event) {
        String state = event.getCurrentState();
        SetUpState setUpState = MapCreator.getMapCreator().getCoreAPI().getSetUpState();
        if(state.equalsIgnoreCase(AdditionalSetUpState.MAPPOOL.name())) {
            System.out.println(new BukkitTranslation(Translations.CONSOLE_SETUP_MAPPOOL.id).get(true));
            setUpState.setCurrentSetUpState(AdditionalSetUpState.MAPPOOL.name(), false);
        }else if(state.equalsIgnoreCase(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name())) {
            System.out.println(new BukkitTranslation(Translations.CONSOLE_SETUP_SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.id).get(true));
            setUpState.setCurrentSetUpState(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name(), false);
        }else if(state.equalsIgnoreCase(AdditionalSetUpState.USE_API_MODE_ONLY.name())) {
            System.out.println(new BukkitTranslation(Translations.CONSOLE_SETUP_USE_API_MODE_ONLY.id).get(true));
            setUpState.setCurrentSetUpState(AdditionalSetUpState.USE_API_MODE_ONLY.name(), false);
        }else if(state.equalsIgnoreCase(AdditionalSetUpState.DEFAULT_WORLD_FOR_USAGE.name())) {
            System.out.println(new BukkitTranslation(Translations.CONSOLE_SETUP_DEFAULT_WORLD_FOR_USAGE.id).get(true));
        }
    }

}
