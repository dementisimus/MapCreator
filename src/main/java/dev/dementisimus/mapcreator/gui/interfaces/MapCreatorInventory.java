package dev.dementisimus.mapcreator.gui.interfaces;

import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.mapcreator.creator.CurrentPlayerMap;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;
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

    void open(Player player, MapCreatorInventorySection inventorySection);

    void fetchCategories(Player player, MapCreatorInventorySection inventorySection, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException;

    void setCurrentPlayerMapCategory(Player player, String categoryName);

    void setCurrentPlayerMap(Player player, String mapName);

    CurrentPlayerMap getCurrentPlayerMap(Player player);

    CustomMapCreator getCustomMapCreator();

    enum MapCreatorInventorySection {

        CATEGORIES("map.creator.inventory.section.categories", Material.WHITE_STAINED_GLASS_PANE),
        CATEGORY_MAPS("map.creator.inventory.section.category.maps", Material.GREEN_STAINED_GLASS_PANE);

        String titleTranslationProperty;
        Material inventoryPlaceholderMaterial;

        MapCreatorInventorySection(String titleTranslationProperty, Material inventoryPlaceholderMaterial) {
            this.titleTranslationProperty = titleTranslationProperty;
            this.inventoryPlaceholderMaterial = inventoryPlaceholderMaterial;
        }

        public String getTitleTranslationProperty() {
            return this.titleTranslationProperty;
        }

        public Material getInventoryPlaceholderMaterial() {
            return this.inventoryPlaceholderMaterial;
        }
    }

}
