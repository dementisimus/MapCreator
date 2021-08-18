package dev.dementisimus.mapcreator.gui;

import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.creators.infiniteinventory.CustomInfiniteInventory;
import dev.dementisimus.capi.core.creators.infiniteinventory.InfiniteInventoryObject;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CurrentPlayerMap;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreatorInventory @ MapCreator
 *
 * @author dementisimus
 * @since 29.07.2021:21:29
 */
public class CustomMapCreatorInventory implements MapCreatorInventory {

    private final CustomMapCreator customMapCreator;
    private final Map<Player, CurrentPlayerMap> currentPlayerMap = new HashMap<>();

    public CustomMapCreatorInventory(CustomMapCreator customMapCreator) {
        this.customMapCreator = customMapCreator;
    }

    @Override
    public void open(Player player, MapCreatorInventorySection inventorySection) {
        List<ItemStack> items = new ArrayList<>();
        new CustomInfiniteInventory(MapCreatorPlugin.getMapCreatorPlugin(), player, customInfiniteInventory -> {
            customInfiniteInventory.setInfiniteInventoryObject(new InfiniteInventoryObject(player, infiniteInventoryObject -> {
                infiniteInventoryObject.setUseCache(false);
                infiniteInventoryObject.setInventorySize(54);
                infiniteInventoryObject.setMaxItemsOnPage(28);
                infiniteInventoryObject.setTitleTranslationProperty(inventorySection.getTitleTranslationProperty());
                infiniteInventoryObject.setPlaceholderMaterial(inventorySection.getInventoryPlaceholderMaterial());
                try {
                    this.fetchCategories(player, inventorySection, (fetchedCategories, fetchedItems) -> {
                        for(Document item : fetchedCategories) {
                            String name = item.getString(MapCreatorPlugin.Storage.Categories.NAME);
                            String icon = item.getString(MapCreatorPlugin.Storage.Categories.ICON);
                            items.add(new ItemCreator(Material.valueOf(icon)).setDisplayName(name).addAllFlags().apply());
                        }
                        items.addAll(fetchedItems);
                        infiniteInventoryObject.setItems(items);
                        customInfiniteInventory.setInfiniteInventoryObject(infiniteInventoryObject.build(customInfiniteInventory));
                        customInfiniteInventory.open(inventoryCreator -> {
                            switch(inventorySection) {
                                case CATEGORIES -> {
                                    inventoryCreator.setItem(49, new ItemCreator(Material.LIME_DYE).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).addLores(new String[]{" ", new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_1).get(player), new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_2).get(player)}).apply());
                                }
                                case CATEGORY_MAPS -> {
                                    inventoryCreator.setItem(49, new ItemCreator(Material.LIME_DYE).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP).apply());
                                }
                            }
                            inventoryCreator.apply(player);
                        });
                    });
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }));
        });
    }

    @Override
    public void fetchCategories(Player player, MapCreatorInventorySection inventorySection, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException {
        DataManagement dataManagement = MapCreatorPlugin.getMapCreatorPlugin().getCoreAPI().getDataManagement();
        List<Document> documents = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        CurrentPlayerMap currentPlayerMap = this.getCurrentPlayerMap(player);

        switch(inventorySection) {
            case CATEGORIES -> {
                dataManagement.setRequirements(MapCreatorPlugin.Storage.CATEGORIES, null, null);
                dataManagement.listDocuments(MapCreatorPlugin.Storage.Categories.NAME, fetchedCategories -> fetchedItems.done(fetchedCategories, items));
            }
            case CATEGORY_MAPS -> {
                String categoryIdentifier = currentPlayerMap.getCategoryName() + MapCreatorMap.CATEGORY_MAP_SEPARATOR;
                for(String world : this.customMapCreator.getSlimeLoader().listWorlds().stream().filter(world -> world.startsWith(categoryIdentifier)).collect(Collectors.toList())) {
                    items.add(new ItemCreator(Material.FILLED_MAP).setDisplayName(world.split(categoryIdentifier)[1]).addAllFlags().apply());
                }
                fetchedItems.done(documents, items);
            }
            default -> fetchedItems.done(documents, items);
        }
    }

    @Override
    public void setCurrentPlayerMapCategory(Player player, String categoryName) {
        this.getCurrentPlayerMap(player).setCategoryName(categoryName);
    }

    @Override
    public void setCurrentPlayerMap(Player player, String mapName) {
        this.getCurrentPlayerMap(player).setMapName(mapName);
    }

    @Override
    public CurrentPlayerMap getCurrentPlayerMap(Player player) {
        return this.currentPlayerMap.computeIfAbsent(player, pl -> new CurrentPlayerMap());
    }

    @Override
    public CustomMapCreator getCustomMapCreator() {
        return this.customMapCreator;
    }
}
