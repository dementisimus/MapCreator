package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class PlayerInteractListener @ MapCreator
 *
 * @author dementisimus
 * @since 23.08.2021:12:26
 */
@BukkitListener(additionalModulesToInject = {MapCreatorPlugin.class, CustomMapCreatorInventory.class})
public class PlayerInteractListener implements Listener {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject CustomMapCreatorInventory customMapCreatorInventory;

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();
        CustomMapCreatorMap customMapCreatorMap = this.customMapCreatorInventory.getLoadedPlayerMap(player);

        if(item != null && item.getItemMeta() != null) {
            String displayName = item.getItemMeta().getDisplayName();
            if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
                if(customMapCreatorMap.getMapName() != null) {
                    if(new BukkitTranslation(MapCreatorInventory.Section.CATEGORY_MAPS_MAP_MANAGEMENT.getTitleTranslationProperty()).matches(displayName)) {
                        this.customMapCreatorInventory.open(player, MapCreatorInventory.Section.CATEGORY_MAPS_MAP_MANAGEMENT);
                    }
                }
            }
        }
    }
}
