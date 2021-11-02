package dev.dementisimus.mapcreator.creator;

import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.api.PlayerMapActions;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomPlayerMapActions @ MapCreator
 *
 * @author dementisimus
 * @since 22.08.2021:22:44
 */
public class CustomPlayerMapActions implements PlayerMapActions {

    @Getter private final Player player;
    @Getter private final SlimeWorld slimeWorld;
    @Getter private final CustomMapCreatorMap customMapCreatorMap;

    public CustomPlayerMapActions(Player player, SlimeWorld slimeWorld) {
        this.player = player;
        this.slimeWorld = slimeWorld;
        this.customMapCreatorMap = MapCreatorPlugin.getMapCreatorPlugin().getCustomMapCreator().getCustomMapCreatorInventory().getLoadedPlayerMap(player);
        /*
         * ToDo: check if player is in slimeWorld, if false, do not clear inventory
         * */
        this.getPlayer().getInventory().clear();
    }

    @Override
    public void load() {
        World world = Bukkit.getWorld(this.slimeWorld.getName());
        SlimePropertyMap slimePropertyMap = this.getSlimeWorld().getPropertyMap();
        int spawnX = slimePropertyMap.getValue(SlimeProperties.SPAWN_X);
        int spawnY = slimePropertyMap.getValue(SlimeProperties.SPAWN_Y);
        int spawnZ = slimePropertyMap.getValue(SlimeProperties.SPAWN_Z);
        if(world != null) {
            MapCreatorInventory.setMapManagementItem(this.player);
            this.player.setGameMode(GameMode.CREATIVE);
            this.player.teleport(new Location(world, spawnX, spawnY, spawnZ));
            this.player.setAllowFlight(true);
            this.player.setFlying(true);
        }
    }

    @Override
    public void save() {

    }

    @Override
    public void delete() {

    }

    @Override
    public void leave() {

    }

    @Override
    public void worldImport() {

    }
}
