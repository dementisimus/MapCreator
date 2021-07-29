package dev.dementisimus.mapcreator.command;

import com.google.inject.Inject;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitCommand;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import org.bukkit.Bukkit;
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
@BukkitCommand(name = "test", additionalModulesToInject = {MapCreatorPlugin.class, SlimePlugin.class, SlimeLoader.class})
public class TestCommand implements CommandExecutor {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject SlimePlugin slimePlugin;
    @Inject SlimeLoader slimeLoader;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            if(args.length == 3) {
                CustomMapCreator customMapCreator = this.mapCreatorPlugin.getMapCreator();
                CustomMapCreatorMap customMapCreatorMap = new CustomMapCreatorMap(args[2], args[1], this.slimePlugin, this.slimeLoader);
                MapCreator.Action action = null;
                if(args[0].equalsIgnoreCase("create")) {
                    action = MapCreator.Action.CREATE;
                }else if(args[0].equalsIgnoreCase("load")) {
                    action = MapCreator.Action.LOAD;
                }else if(args[0].equalsIgnoreCase("save")) {
                    action = MapCreator.Action.SAVE;
                }else if(args[0].equalsIgnoreCase("delete")) {
                    action = MapCreator.Action.DELETE;
                }else if(args[0].equalsIgnoreCase("leave")) {
                    action = MapCreator.Action.LEAVE;
                }
                if(action != null) {
                    customMapCreator.perform(action, customMapCreatorMap, performance -> {
                        player.sendMessage("PERFORMANCE_ACTION: " + performance.getAction());
                        player.sendMessage("PERFORMANCE_SLIMEWORLD: " + performance.getSlimeWorld());
                        player.sendMessage("PERFORMANCE_SUCCESS: " + performance.isSuccess());
                        player.sendMessage("PERFORMANCE_FAILURE_REASON: " + performance.getFailureReason());
                        if(performance.getSlimeWorld() != null) {
                            player.teleport(Bukkit.getWorld(performance.getSlimeWorld().getName()).getSpawnLocation());
                        }
                    });
                }
            }
        }
        return false;
    }
}
