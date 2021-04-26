package dev.dementisimus.mapcreator.listeners;

import dev.dementisimus.capi.core.events.bukkit.BukkitChangeSetUpStateEvent;
import dev.dementisimus.capi.core.helpers.SetUpHelper;
import dev.dementisimus.capi.core.setup.DefaultSetUpState;
import dev.dementisimus.capi.core.setup.SetUpData;
import dev.dementisimus.capi.core.setup.SetUpState;
import dev.dementisimus.capi.core.translations.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.mapcreator.setup.AdditionalSetUpState;
import dev.dementisimus.mapcreator.translation.Translations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SetUpStateChangeListener @ MapCreator
 *
 * @author dementisimus
 * @since 13.07.2020:13:03
 */
public class SetUpStateChangeListener implements Listener {

    @EventHandler
    public void on(BukkitChangeSetUpStateEvent event) {
        String currentState = event.getCurrentState();
        SetUpState setUpState = MapCreator.getMapCreator().getCoreAPI().getSetUpState();
        SetUpData setUpData = MapCreator.getMapCreator().getCoreAPI().getSetUpData();
        SetUpHelper setUpHelper = new SetUpHelper(currentState, event.getIssuedCommand(), setUpState, setUpData);
        if(currentState.equalsIgnoreCase(AdditionalSetUpState.MAPPOOL.name())) {
            if(new File(event.getIssuedCommand()).exists() && new File(event.getIssuedCommand(),
                                                                       (event.getIssuedCommand()
                                                                             .endsWith("/") ? "DEFAULTMAPS/defaultWorld" : "/DEFAULTMAPS/defaultWorld"))
                    .exists()) {
                setUpData.setData(AdditionalSetUpState.MAPPOOL.name(), event.getIssuedCommand());
                setUpState.setCurrentSetUpState(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name(), true);
            }else {
                System.out.println(new BukkitTranslation(Translations.DEFAULT_WORLD_NOT_AVAILABLE.id).get(true));
                System.out.println(new BukkitTranslation(Translations.CONSOLE_SETUP_MAPPOOL.id).get(true));
            }
        }else if(currentState.equalsIgnoreCase(AdditionalSetUpState.SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.name())) {
            setUpHelper.evalYesOrNo(Translations.CONSOLE_SETUP_SET_DEFAULT_WORLD_INSTEAD_OF_WORLD.id,
                                    AdditionalSetUpState.USE_API_MODE_ONLY,
                                    true,
                                    false);
            if(SetUpHelper.getYesNoFromCommand(event.getIssuedCommand()) != null) {
                if(SetUpHelper.getYesNoFromCommand(event.getIssuedCommand())) {
                    setUpState.setCurrentSetUpState(AdditionalSetUpState.DEFAULT_WORLD_FOR_USAGE.name(), true);
                }else {
                    setUpState.setCurrentSetUpState(AdditionalSetUpState.USE_API_MODE_ONLY.name(), true);
                }
            }
        }else if(currentState.equalsIgnoreCase(AdditionalSetUpState.DEFAULT_WORLD_FOR_USAGE.name())) {
            setUpData.setData(AdditionalSetUpState.DEFAULT_WORLD_FOR_USAGE.name(), event.getIssuedCommand());
            setUpState.setCurrentSetUpState(AdditionalSetUpState.USE_API_MODE_ONLY.name(), true);
        }else if(currentState.equalsIgnoreCase(AdditionalSetUpState.USE_API_MODE_ONLY.name())) {
            setUpHelper.evalYesOrNo(Translations.CONSOLE_SETUP_USE_API_MODE_ONLY.id, DefaultSetUpState.DONE, false, true);
        }
    }

}
