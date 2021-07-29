package dev.dementisimus.mapcreator.command;

import com.google.inject.Inject;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitCommand;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.MapCreator;
import dev.dementisimus.mapcreator.creator.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.IMapCreator;
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
 * Class COMMAND_test @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:22:06
 */
@BukkitCommand(name = "test", additionalModulesToInject = {MapCreatorPlugin.class, SlimePlugin.class, SlimeLoader.class})
public class COMMAND_test implements CommandExecutor {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject SlimePlugin slimePlugin;
    @Inject SlimeLoader slimeLoader;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            if(args.length == 3) {
                MapCreator mapCreator = this.mapCreatorPlugin.getMapCreator();
                MapCreatorMap mapCreatorMap = new MapCreatorMap(args[2], args[1], this.slimePlugin, this.slimeLoader);
                IMapCreator.Action action = null;
                if(args[0].equalsIgnoreCase("create")) {
                    action = IMapCreator.Action.CREATE;
                }else if(args[0].equalsIgnoreCase("load")) {
                    action = IMapCreator.Action.LOAD;
                }else if(args[0].equalsIgnoreCase("save")) {
                    action = IMapCreator.Action.SAVE;
                }else if(args[0].equalsIgnoreCase("delete")) {
                    action = IMapCreator.Action.DELETE;
                }else if(args[0].equalsIgnoreCase("leave")) {
                    action = IMapCreator.Action.LEAVE;
                }
                if(action != null) {
                    mapCreator.perform(action, mapCreatorMap, performance -> {
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
