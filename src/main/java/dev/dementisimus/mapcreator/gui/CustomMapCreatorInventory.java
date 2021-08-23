package dev.dementisimus.mapcreator.gui;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.creators.infiniteinventory.CustomInfiniteInventory;
import dev.dementisimus.capi.core.creators.infiniteinventory.InfiniteInventoryObject;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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

    public static final String DISABLED_ACTION_COLOR_CODES = "§7§l§m";

    private final CustomMapCreator customMapCreator;
    private final Map<Player, CustomMapCreatorMap> currentPlayerMaps = new HashMap<>();

    public CustomMapCreatorInventory(CustomMapCreator customMapCreator) {
        this.customMapCreator = customMapCreator;
    }

    @Override
    public void open(Player player, MapCreatorInventorySection inventorySection) {
        List<ItemStack> items = new ArrayList<>();
        CustomMapCreatorMap currentPlayerMap = this.getCurrentPlayerMap(player);
        new CustomInfiniteInventory(MapCreatorPlugin.getMapCreatorPlugin(), player, customInfiniteInventory -> {
            customInfiniteInventory.setInfiniteInventoryObject(new InfiniteInventoryObject(player, infiniteInventoryObject -> {
                infiniteInventoryObject.setUseCache(false);
                infiniteInventoryObject.setInventorySize(inventorySection.getInventorySize());
                infiniteInventoryObject.setMaxItemsOnPage(inventorySection.getMaxItemsOnPage());
                infiniteInventoryObject.setTitleTranslationProperty(inventorySection.getTitleTranslationProperty());
                if(currentPlayerMap != null && currentPlayerMap.getMapName() != null) {
                    infiniteInventoryObject.setTitleTranslationTargets(new String[]{"$map$"});
                    infiniteInventoryObject.setTitleTranslationReplacements(new String[]{this.getCurrentPlayerMap(player).getMapName()});
                }
                infiniteInventoryObject.setPlaceholderMaterial(inventorySection.getInventoryPlaceholderMaterial());
                try {
                    this.fetch(player, inventorySection, (fetchedCategories, fetchedItems) -> {
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
                                    inventoryCreator.setItem(48, new ItemCreator(Material.RED_DYE).setDisplayName(player, MapCreatorPlugin.Translations.BACK).apply());
                                    inventoryCreator.setItem(50, new ItemCreator(Material.LIME_DYE).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP).apply());
                                }
                                case CATEGORY_MAPS_MAP_MANAGEMENT -> {
                                    if(currentPlayerMap != null && currentPlayerMap.getMapName() != null) {
                                        for(MapCreator.Action action : MapCreator.Action.values()) {
                                            String disabledActionColorCodes = "";
                                            if(this.worldAlreadyLoadedOnServer(player, currentPlayerMap.getFullMapName())) {
                                                if(action.equals(MapCreator.Action.LOAD)) {
                                                    disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                                                }
                                            }else {
                                                if(action.equals(MapCreator.Action.SAVE) || action.equals(MapCreator.Action.LEAVE)) {
                                                    disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                                                }
                                            }
                                            String displayName = new BukkitTranslation(action.getTranslationProperty()).get(player, "$disabled$", disabledActionColorCodes);
                                            inventoryCreator.setItem(action.getActionItemSlot(), new ItemCreator(action.getActionItemMaterial()).setDisplayName(displayName).addAllFlags().apply());
                                        }
                                    }
                                }
                            }
                            inventoryCreator.apply(player);
                        });
                    });
                }catch(IOException | UnknownWorldException e) {
                    e.printStackTrace();
                }
            }));
        });
    }

    @Override
    public void fetch(Player player, MapCreatorInventorySection inventorySection, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException, UnknownWorldException {
        DataManagement dataManagement = MapCreatorPlugin.getMapCreatorPlugin().getCoreAPI().getDataManagement();
        List<Document> documents = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        CustomMapCreatorMap customMapCreatorMap = this.getCurrentPlayerMap(player);

        switch(inventorySection) {
            case CATEGORIES -> {
                dataManagement.setRequirements(MapCreatorPlugin.Storage.CATEGORIES, null, null);
                dataManagement.listDocuments(MapCreatorPlugin.Storage.Categories.NAME, fetchedCategories -> fetchedItems.done(fetchedCategories, items));
            }
            case CATEGORY_MAPS -> {
                for(String world : this.customMapCreator.getSlimeLoader().listWorlds().stream().filter(world -> world.startsWith(customMapCreatorMap.getCategoryIdentifier())).collect(Collectors.toList())) {
                    ItemCreator worldItemCreator = new ItemCreator(Material.FILLED_MAP).setDisplayName(world.split(customMapCreatorMap.getCategoryIdentifier())[1]).addAllFlags();
                    worldItemCreator.addLore(" ");
                    if(!this.worldAlreadyLoadedOnServer(player, world)) {
                        worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_RIGHT_CLICK).get(player));
                    }
                    worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_LEFT_CLICK).get(player));
                    worldItemCreator.addHiddenString(MapCreatorPlugin.getMapCreatorPlugin(), MapCreatorPlugin.ItemDataStorageKeys.CATEGORY, customMapCreatorMap.getMapCategory());
                    items.add(worldItemCreator.apply());

                    if(player.getWorld().getName().equalsIgnoreCase(world)) {
                        MapCreatorInventory.setMapManagementItem(player, customMapCreatorMap.getMapName());
                    }
                }
                fetchedItems.done(documents, items);
            }
            default -> fetchedItems.done(documents, items);
        }
    }

    @Override
    public void setCurrentPlayerMapCategory(Player player, String categoryName) {
        this.getCurrentPlayerMap(player).setMapCategory(categoryName);
    }

    @Override
    public void setCurrentPlayerMap(Player player, String mapName) {
        this.getCurrentPlayerMap(player).setMapName(mapName);
    }

    @Override
    public CustomMapCreatorMap getCurrentPlayerMap(Player player) {
        return this.currentPlayerMaps.computeIfAbsent(player, pl -> new CustomMapCreatorMap());
    }

    @Override
    public CustomMapCreator getCustomMapCreator() {
        return this.customMapCreator;
    }

    @Override
    public boolean worldAlreadyLoadedOnServer(Player player, String playerMap) {
        boolean worldAlreadyLoadedOnServer = false;
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equalsIgnoreCase(playerMap)) {
                worldAlreadyLoadedOnServer = true;
            }
        }
        return worldAlreadyLoadedOnServer;
    }
}
