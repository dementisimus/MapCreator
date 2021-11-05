package dev.dementisimus.mapcreator.gui.interfaces;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.InventoryCreator;
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
import java.text.SimpleDateFormat;
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

    SimpleDateFormat LOADED_SINCE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

    static void setMapManagementItem(Player player) {
        player.getInventory().setItem(8, new ItemCreator(Material.MOJANG_BANNER_PATTERN).setDisplayName(new BukkitTranslation(Section.CATEGORY_MAPS_MAP_MANAGEMENT.getTitleTranslationProperty()).get(player)).addAllFlags().apply());
    }

    void open(Player player, Section inventorySection);

    void fetch(Player player, Section inventorySection, CustomMapCreatorMap currentPlayerMap, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException, UnknownWorldException;

    CustomMapCreator getCustomMapCreator();

    boolean worldAlreadyLoadedOnServer(String playerMap);

    void setMapManagementActionItems(Player player, CustomMapCreatorMap mapCreatorMap, InventoryCreator inventoryCreator, Enum action, String translationProperty, int actionItemSlot, Material actionItemMaterial);

    void setLoadedPlayerMap(Player player, CustomMapCreatorMap customMapCreatorMap);

    CustomMapCreatorMap getLoadedPlayerMap(Player player);

    enum CurrentMapInfoDataState {
        LOADED,
        VIEWED
    }

    enum Section {

        CATEGORIES("map.creator.inventory.section.categories", Material.WHITE_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        CATEGORY_MAPS("map.creator.inventory.section.category.maps", Material.GREEN_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        CATEGORY_MAPS_MAP_CHOOSE_ACTION("player.inventory.map.choose.action", Material.ORANGE_STAINED_GLASS_PANE, 27, 28, CurrentMapInfoDataState.VIEWED),
        CATEGORY_MAPS_MAP_MANAGEMENT("player.inventory.map.management", Material.ORANGE_STAINED_GLASS_PANE, 27, 28, CurrentMapInfoDataState.LOADED),
        IMPORTER_WORLDS_AVAILABLE("importer.worlds.available", Material.YELLOW_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        MAP_TEMPLATES_CHOOSE_TEMPLATE("map.templates.choose.template", Material.CYAN_STAINED_GLASS_PANE, 27, 28, CurrentMapInfoDataState.VIEWED),
        MAP_CREATION_SETTINGS_OVERVIEW("map.creation.settings.overview", Material.RED_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME("map.creation.settings.choose.default.biome", Material.GRAY_STAINED_GLASS_PANE, 27, 28, CurrentMapInfoDataState.VIEWED),
        MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_OVERWORLD("map.creation.settings.choose.default.biome.overworld", Material.GRAY_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_NETHER("map.creation.settings.choose.default.biome.nether", Material.GRAY_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED),
        MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_THE_END("map.creation.settings.choose.default.biome.the.end", Material.GRAY_STAINED_GLASS_PANE, 54, 28, CurrentMapInfoDataState.VIEWED);

        @Getter String titleTranslationProperty;
        @Getter Material inventoryPlaceholderMaterial;
        @Getter int inventorySize;
        @Getter int maxItemsOnPage;
        @Getter CurrentMapInfoDataState currentMapInfoDataState;

        Section(String titleTranslationProperty, Material inventoryPlaceholderMaterial, int inventorySize, int maxItemsOnPage, CurrentMapInfoDataState currentMapInfoDataState) {
            this.titleTranslationProperty = titleTranslationProperty;
            this.inventoryPlaceholderMaterial = inventoryPlaceholderMaterial;
            this.inventorySize = inventorySize;
            this.maxItemsOnPage = maxItemsOnPage;
            this.currentMapInfoDataState = currentMapInfoDataState;
        }
    }
}
