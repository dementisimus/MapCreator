package dev.dementisimus.mapcreator.gui;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.InventoryCreator;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.creators.infiniteinventory.CustomInfiniteInventory;
import dev.dementisimus.capi.core.creators.infiniteinventory.InfiniteInventoryObject;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_NO_IMPORTABLE_WORLDS_FOUND;
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
    private final Map<Player, CustomMapCreatorMap> currentlyLoadedPlayerMaps = new HashMap<>();
    private final Map<Player, CustomMapCreatorMap> currentlyViewedPlayerMaps = new HashMap<>();

    public CustomMapCreatorInventory(CustomMapCreator customMapCreator) {
        this.customMapCreator = customMapCreator;
    }

    @Override
    public void open(Player player, Section inventorySection) {
        List<ItemStack> items = new ArrayList<>();

        CustomMapCreatorMap currentPlayerMap = this.getAppropriateSectionPlayerMap(inventorySection, player);
        SetupManager setupManager = MapCreatorPlugin.getMapCreatorPlugin().getSetupManager();
        AtomicReference<List<CustomMapCreatorMap>> importableWorlds = new AtomicReference<>();

        new CustomInfiniteInventory(MapCreatorPlugin.getMapCreatorPlugin(), player, customInfiniteInventory -> {
            customInfiniteInventory.setInfiniteInventoryObject(new InfiniteInventoryObject(player, infiniteInventoryObject -> {
                infiniteInventoryObject.setUseCache(false);
                infiniteInventoryObject.setInventorySize(inventorySection.getInventorySize());
                infiniteInventoryObject.setMaxItemsOnPage(inventorySection.getMaxItemsOnPage());
                infiniteInventoryObject.setTitleTranslationProperty(inventorySection.getTitleTranslationProperty());
                infiniteInventoryObject.setPlaceholderMaterial(inventorySection.getInventoryPlaceholderMaterial());
                try {
                    this.fetch(player, inventorySection, currentPlayerMap, (fetchedCategories, fetchedItems) -> {

                        for(Document item : fetchedCategories) {
                            String name = item.getString(MapCreatorPlugin.Storage.Categories.NAME);
                            String icon = item.getString(MapCreatorPlugin.Storage.Categories.ICON);
                            items.add(new ItemCreator(Material.valueOf(icon)).setDisplayName(name).addAllFlags().apply());
                        }
                        items.addAll(fetchedItems);

                        if(inventorySection.equals(Section.WORLDS_IMPORTER_CATEGORIES)) {
                            CustomWorldImporter customWorldImporter = this.customMapCreator.getCustomWorldImporter();
                            importableWorlds.set(customWorldImporter.getImportableWorldsByCategory(currentPlayerMap.getMapCategory()));

                            for(CustomMapCreatorMap importableWorld : importableWorlds.get()) {
                                items.add(new ItemCreator(Material.PAPER).setDisplayName("§c§l" + importableWorld.getMapCategory() + "§7/§7§l" + importableWorld.getMapName()).apply());
                            }
                        }

                        infiniteInventoryObject.setItems(items);
                        customInfiniteInventory.setInfiniteInventoryObject(infiniteInventoryObject.build(customInfiniteInventory));
                        customInfiniteInventory.open(inventoryCreator -> {
                            switch(inventorySection) {
                                case CATEGORIES -> {
                                    inventoryCreator.setItem(49, new ItemCreator(Material.GLOWSTONE_DUST).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).addLores(new String[]{" ", new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_1).get(player), new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_2).get(player)}).apply());
                                    if(fetchedCategories.isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                }
                                case CATEGORY_MAPS -> {
                                    inventoryCreator.setItem(48, new ItemCreator(Material.RED_DYE).setDisplayName(player, MapCreatorPlugin.Translations.BACK).apply());
                                    if(setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.WORLD_IMPORTER_REQUIRED).getBoolean()) {
                                        inventoryCreator.setItem(49, new ItemCreator(Material.SUGAR).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_WORLD).apply());
                                    }
                                    inventoryCreator.setItem(50, new ItemCreator(Material.LIME_DYE).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP).apply());
                                    if(fetchedItems.isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                }
                                case CATEGORY_MAPS_MAP_CHOOSE_ACTION, CATEGORY_MAPS_MAP_MANAGEMENT -> {
                                    if(currentPlayerMap != null && currentPlayerMap.getMapName() != null) {
                                        for(MapCreator.Action action : MapCreator.Action.values()) {
                                            if(!action.equals(MapCreator.Action.IMPORT)) {
                                                this.setMapManagementActionItems(player, currentPlayerMap, inventoryCreator, action, action.getTranslationProperty(), action.getActionItemSlot(), action.getActionItemMaterial());
                                            }
                                        }
                                        for(MapCreator.Action.Player playerAction : MapCreator.Action.Player.values()) {
                                            if(inventorySection.equals(Section.CATEGORY_MAPS_MAP_MANAGEMENT)) {
                                                if(!playerAction.equals(MapCreator.Action.Player.BACK)) {
                                                    this.setMapManagementActionItems(player, currentPlayerMap, inventoryCreator, playerAction, playerAction.getTranslationProperty(), playerAction.getActionItemSlot(), playerAction.getActionItemMaterial());
                                                }
                                            }else {
                                                this.setMapManagementActionItems(player, currentPlayerMap, inventoryCreator, playerAction, playerAction.getTranslationProperty(), playerAction.getActionItemSlot(), playerAction.getActionItemMaterial());
                                            }
                                        }
                                        inventoryCreator.setItem(4, new ItemCreator(Material.OAK_SIGN).setDisplayName(currentPlayerMap.getPrettyFileName()).apply());
                                    }
                                }
                                case WORLDS_IMPORTER_CATEGORIES -> {
                                    if(importableWorlds.get().isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                    inventoryCreator.setItem(45, new ItemCreator(Material.RED_DYE).setDisplayName(player, MapCreatorPlugin.Translations.BACK).apply());
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
    public void fetch(Player player, Section inventorySection, CustomMapCreatorMap currentPlayerMap, BiCallback<List<Document>, List<ItemStack>> fetchedItems) throws IOException, UnknownWorldException {
        DataManagement dataManagement = MapCreatorPlugin.getMapCreatorPlugin().getCoreAPI().getDataManagement();
        List<Document> documents = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        switch(inventorySection) {
            case CATEGORIES -> {
                dataManagement.setRequirements(MapCreatorPlugin.Storage.CATEGORIES, null, null);
                dataManagement.listDocuments(MapCreatorPlugin.Storage.Categories.NAME, fetchedCategories -> fetchedItems.done(fetchedCategories, items));
            }
            case CATEGORY_MAPS -> {
                for(String world : this.customMapCreator.getSlimeLoader().listWorlds().stream().filter(world -> world.startsWith(currentPlayerMap.getCategoryIdentifier())).collect(Collectors.toList())) {
                    ItemCreator worldItemCreator = new ItemCreator(Material.FILLED_MAP).setDisplayName(world.split(currentPlayerMap.getCategoryIdentifier())[1]).addAllFlags();
                    worldItemCreator.addLore(" ");

                    if(this.worldAlreadyLoadedOnServer(world)) {
                        CustomMapCreatorMap mapCreatorMap = this.getCustomMapCreator().getMapCreatorMap(world);
                        if(mapCreatorMap != null) {
                            boolean dataAdded = false;
                            if(mapCreatorMap.getLoadedBy() != null) {
                                worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_LOADED_BY).get(player, "$loadedBy$", mapCreatorMap.getLoadedBy()));
                                dataAdded = true;
                            }
                            if(mapCreatorMap.getLoadedSince() != null) {
                                worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_LOADED_SINCE).get(player, "$loadedSince$", LOADED_SINCE_DATE_FORMAT.format(mapCreatorMap.getLoadedSince())));
                                dataAdded = true;
                            }
                            if(dataAdded) worldItemCreator.addLore(" ");
                        }
                    }else {
                        worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_RIGHT_CLICK).get(player));
                    }
                    worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_LEFT_CLICK).get(player));
                    worldItemCreator.addHiddenString(MapCreatorPlugin.getMapCreatorPlugin(), MapCreatorPlugin.ItemDataStorageKeys.CATEGORY, currentPlayerMap.getMapCategory());
                    items.add(worldItemCreator.apply());

                    if(player.getWorld().getName().equalsIgnoreCase(world)) {
                        MapCreatorInventory.setMapManagementItem(player);
                    }
                }
                fetchedItems.done(documents, items);
            }
            default -> fetchedItems.done(documents, items);
        }
    }

    @Override
    public void setCurrentlyLoadedPlayerMap(Player player, String mapName) {
        this.getCurrentlyLoadedPlayerMap(player).setMapName(mapName);
    }

    @Override
    public CustomMapCreatorMap getCurrentlyLoadedPlayerMap(Player player) {
        return this.currentlyLoadedPlayerMaps.computeIfAbsent(player, pl -> new CustomMapCreatorMap());
    }

    @Override
    public CustomMapCreator getCustomMapCreator() {
        return this.customMapCreator;
    }

    @Override
    public boolean worldAlreadyLoadedOnServer(String playerMap) {
        boolean worldAlreadyLoadedOnServer = false;
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equalsIgnoreCase(playerMap)) {
                worldAlreadyLoadedOnServer = true;
            }
        }
        return worldAlreadyLoadedOnServer;
    }

    @Override
    public CustomMapCreatorMap getCurrentlyViewedPlayerMap(Player player) {
        return this.currentlyViewedPlayerMaps.computeIfAbsent(player, pl -> new CustomMapCreatorMap());
    }

    @Override
    public void setCurrentlyViewedPlayerMap(Player player, String mapName) {
        this.getCurrentlyViewedPlayerMap(player).setMapName(mapName);
    }

    @Override
    public void setCurrentlyLoadedMap(Player player, CustomMapCreatorMap mapCreatorMap) {
        this.currentlyLoadedPlayerMaps.put(player, mapCreatorMap);
    }

    @Override
    public CustomMapCreatorMap getAppropriateSectionPlayerMap(Section inventorySection, Player player) {
        return inventorySection.isRequiresViewableMapInfo() ? this.getCurrentlyViewedPlayerMap(player) : this.getCurrentlyLoadedPlayerMap(player);
    }

    @Override
    public void setMapManagementActionItems(Player player, CustomMapCreatorMap mapCreatorMap, InventoryCreator inventoryCreator, Enum action, String translationProperty, int actionItemSlot, Material actionItemMaterial) {
        ItemCreator actionItemCreator = new ItemCreator(actionItemMaterial).addAllFlags();
        String disabledActionColorCodes = "";
        if(this.worldAlreadyLoadedOnServer(mapCreatorMap.getFileName())) {
            if(action.equals(MapCreator.Action.LOAD)) {
                disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
            }else if(action.equals(MapCreator.Action.Player.TELEPORT)) {
                if(player.getWorld().getName().equals(mapCreatorMap.getFileName())) {
                    disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                }
            }
        }else {
            if(action.equals(MapCreator.Action.SAVE) || action.equals(MapCreator.Action.LEAVE) || action.equals(MapCreator.Action.Player.TELEPORT)) {
                disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
            }
        }
        actionItemCreator.setDisplayName(new BukkitTranslation(translationProperty).get(player, "$disabled$", disabledActionColorCodes));
        inventoryCreator.setItem(actionItemSlot, actionItemCreator.apply());
    }

    private void setNothingFoundItem(Player player, InventoryCreator inventoryCreator, Section section) {
        String translationProperty = switch(section) {
            case CATEGORIES -> INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND;
            case CATEGORY_MAPS -> INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND;
            case WORLDS_IMPORTER_CATEGORIES -> INVENTORY_SECTION_CATEGORY_MAPS_NO_IMPORTABLE_WORLDS_FOUND;
            default -> null;
        };
        if(translationProperty != null) {
            inventoryCreator.setItem(22, new ItemCreator(Material.BARRIER).setDisplayName(player, translationProperty).apply());
        }
    }
}
