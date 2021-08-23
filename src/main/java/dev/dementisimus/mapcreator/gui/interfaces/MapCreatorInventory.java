package dev.dementisimus.mapcreator.gui.interfaces;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.PLAYER_INVENTORY_MAP_MANAGEMENT;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreatorInventory @ MapCreator
 *
 * @author dementisimus
 * @since 29.07.2021:21:29
 */
public interface MapCreatorInventory {

    static void setMapManagementItem(Player player, String mapName) {
        player.getInventory().setItem(8, new ItemCreator(Material.MOJANG_BANNER_PATTERN).setDisplayName(new BukkitTranslation(PLAYER_INVENTORY_MAP_MANAGEMENT).get(player, "$map$", mapName)).addAllFlags().apply());
    }

    void open(Player player, MapCreatorInventorySection inventorySection);

    void fetch(Player player, MapCreatorInventorySection inventorySection, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException, UnknownWorldException;

    void setCurrentPlayerMapCategory(Player player, String categoryName);

    void setCurrentPlayerMap(Player player, String mapName);

    CustomMapCreatorMap getCurrentPlayerMap(Player player);

    CustomMapCreator getCustomMapCreator();

    boolean worldAlreadyLoadedOnServer(Player player, String playerMap);

    enum MapCreatorInventorySection {

        CATEGORIES("map.creator.inventory.section.categories", Material.WHITE_STAINED_GLASS_PANE, 54, 28),
        CATEGORY_MAPS("map.creator.inventory.section.category.maps", Material.GREEN_STAINED_GLASS_PANE, 54, 28),
        CATEGORY_MAPS_MAP_MANAGEMENT("player.inventory.map.management", Material.ORANGE_STAINED_GLASS_PANE, 27, 28);

        @Getter String titleTranslationProperty;
        @Getter Material inventoryPlaceholderMaterial;
        @Getter int inventorySize;
        @Getter int maxItemsOnPage;

        MapCreatorInventorySection(String titleTranslationProperty, Material inventoryPlaceholderMaterial, int inventorySize, int maxItemsOnPage) {
            this.titleTranslationProperty = titleTranslationProperty;
            this.inventoryPlaceholderMaterial = inventoryPlaceholderMaterial;
            this.inventorySize = inventorySize;
            this.maxItemsOnPage = maxItemsOnPage;
        }
    }
}
