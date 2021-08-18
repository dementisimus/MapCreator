package dev.dementisimus.mapcreator.command;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitCommand;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class TestCommand @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:22:06
 */
@BukkitCommand(name = "test", additionalModulesToInject = {MapCreatorPlugin.class})
public class TestCommand implements CommandExecutor {

    @Inject MapCreatorPlugin mapCreatorPlugin;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            CustomMapCreator customMapCreator = this.mapCreatorPlugin.getCustomMapCreator();
            customMapCreator.getCustomMapCreatorInventory().open(player, MapCreatorInventory.MapCreatorInventorySection.CATEGORIES);
        }
        return false;
    }
}
