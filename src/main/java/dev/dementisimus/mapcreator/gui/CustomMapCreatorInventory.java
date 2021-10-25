package dev.dementisimus.mapcreator.gui;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import dev.dementisimus.capi.core.callback.BiCallback;
import dev.dementisimus.capi.core.creators.InventoryCreator;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.creators.infiniteinventory.CustomInfiniteInventory;
import dev.dementisimus.capi.core.creators.infiniteinventory.InfiniteInventoryObject;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.api.MapCreator;
import dev.dementisimus.mapcreator.creator.api.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.templates.interfaces.MapTemplates;
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

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_NO_IMPORTABLE_WORLDS_FOUND;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.CATEGORY_MAPS_MAP_CHOOSE_ACTION;
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
    private final SetupManager setupManager;
    private final Map<Player, CustomMapCreatorMap> currentPlayerMap = new HashMap<>();

    public CustomMapCreatorInventory(CustomMapCreator customMapCreator) {
        this.customMapCreator = customMapCreator;
        this.setupManager = customMapCreator.getSetupManager();
    }

    @Override
    public void open(Player player, Section inventorySection) {
        List<ItemStack> items = new ArrayList<>();

        CustomMapCreatorMap loadedPlayerMap = this.getLoadedPlayerMap(player);

        AtomicReference<List<CustomMapCreatorMap>> importableWorlds = new AtomicReference<>();

        new CustomInfiniteInventory(MapCreatorPlugin.getMapCreatorPlugin(), player, customInfiniteInventory -> {
            customInfiniteInventory.setInfiniteInventoryObject(new InfiniteInventoryObject(player, infiniteInventoryObject -> {
                infiniteInventoryObject.setUseCache(false);
                infiniteInventoryObject.setInventorySize(inventorySection.getInventorySize());
                infiniteInventoryObject.setMaxItemsOnPage(inventorySection.getMaxItemsOnPage());
                infiniteInventoryObject.setTitleTranslationProperty(inventorySection.getTitleTranslationProperty());
                infiniteInventoryObject.setPlaceholderMaterial(inventorySection.getInventoryPlaceholderMaterial());
                try {
                    this.fetch(player, inventorySection, loadedPlayerMap, (fetchedCategories, fetchedItems) -> {
                        for(Document item : fetchedCategories) {
                            String name = item.getString(MapCreatorPlugin.DataSource.NAME);
                            String icon = item.getString(MapCreatorPlugin.DataSource.ICON);
                            ItemCreator categoryItemCreator = new ItemCreator(Material.valueOf(icon));

                            categoryItemCreator.setDisplayName(name);
                            categoryItemCreator.addAllFlags();
                            categoryItemCreator.addEmptyLore();
                            categoryItemCreator.addLore(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_ACTION_SHIFT_CLICK_RENAME);
                            categoryItemCreator.addLore(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_ACTION_SHIFT_CLICK_UPDATE_ICON);

                            items.add(categoryItemCreator.apply());
                        }
                        items.addAll(fetchedItems);

                        if(inventorySection.equals(Section.IMPORTER_WORLDS_AVAILABLE)) {
                            CustomWorldImporter customWorldImporter = this.customMapCreator.getCustomWorldImporter();
                            importableWorlds.set(customWorldImporter.getImportableWorldsByCategory(loadedPlayerMap.getRecentlyViewed().getMapCategory()));

                            for(CustomMapCreatorMap importableWorld : importableWorlds.get()) {
                                items.add(new ItemCreator(Material.PAPER).setDisplayName(importableWorld.getPrettyName()).apply());
                            }
                        }

                        infiniteInventoryObject.setItems(items);
                        customInfiniteInventory.setInfiniteInventoryObject(infiniteInventoryObject.build(customInfiniteInventory));
                        customInfiniteInventory.open(inventoryCreator -> {
                            switch(inventorySection) {
                                case CATEGORIES -> {
                                    inventoryCreator.setItem(49, new ItemCreator(Material.GLOWSTONE_DUST).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).addLores(new String[]{
                                            " ",
                                            new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_1).get(player),
                                            new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP_MAP_ICON_LORE_INSTRUCTIONS_2).get(player)
                                    }).apply());
                                    if(fetchedCategories.isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                }
                                case CATEGORY_MAPS -> {
                                    if(this.setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.WORLD_IMPORTER_REQUIRED).getBoolean()) {
                                        inventoryCreator.setItem(49, new ItemCreator(Material.SUGAR).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_WORLD).apply());
                                    }
                                    inventoryCreator.setItem(50, new ItemCreator(Material.LIME_DYE).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP).apply());
                                    if(fetchedItems.isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                    this.setBackItem(inventoryCreator, 48, player);
                                }
                                case CATEGORY_MAPS_MAP_CHOOSE_ACTION, CATEGORY_MAPS_MAP_MANAGEMENT -> {
                                    CustomMapCreatorMap mapCreatorMap = inventorySection.equals(CATEGORY_MAPS_MAP_CHOOSE_ACTION) ? loadedPlayerMap.getRecentlyViewed() : loadedPlayerMap;
                                    if(mapCreatorMap != null && mapCreatorMap.getMapName() != null) {
                                        for(MapCreator.Action action : MapCreator.Action.values()) {
                                            this.setMapManagementActionItems(player, mapCreatorMap, inventoryCreator, action, action.getTranslationProperty(), action.getActionItemSlot(), action.getActionItemMaterial());
                                        }
                                        for(CustomMapCreator.CustomAction.User userAction : CustomMapCreator.CustomAction.User.values()) {
                                            if(inventorySection.equals(Section.CATEGORY_MAPS_MAP_MANAGEMENT)) {
                                                if(!userAction.equals(CustomMapCreator.CustomAction.User.BACK)) {
                                                    this.setMapManagementActionItems(player, mapCreatorMap, inventoryCreator, userAction, userAction.getTranslationProperty(), userAction.getActionItemSlot(), userAction.getActionItemMaterial());
                                                }
                                            }else {
                                                this.setMapManagementActionItems(player, mapCreatorMap, inventoryCreator, userAction, userAction.getTranslationProperty(), userAction.getActionItemSlot(), userAction.getActionItemMaterial());
                                            }
                                        }
                                        inventoryCreator.setItem(4, new ItemCreator(Material.OAK_SIGN).setDisplayName(mapCreatorMap.getPrettyName()).apply());
                                    }
                                }
                                case IMPORTER_WORLDS_AVAILABLE -> {
                                    if(importableWorlds.get().isEmpty()) {
                                        this.setNothingFoundItem(player, inventoryCreator, inventorySection);
                                    }
                                    this.setBackItem(inventoryCreator, 45, player);
                                }
                                /*
                                 * ToDo: delete this code here when users can create own template maps
                                 * */
                                case MAP_TEMPLATES_CHOOSE_TEMPLATE -> {
                                    CustomMapCreatorMap customMapCreatorMap = new CustomMapCreatorMap(MapTemplates.SIMPLE_TEMPLATE_NAME, MapTemplates.CATEGORY_TEMPLATES);

                                    int blankTemplateSlot = 13;
                                    if(this.setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.SIMPLE_TEMPLATE_MAP_WANTED).getBoolean()) {
                                        inventoryCreator.setItem(14, new ItemCreator(Material.FILLED_MAP).setDisplayName(customMapCreatorMap.getPrettyName()).apply());
                                        blankTemplateSlot = 12;
                                    }
                                    inventoryCreator.setItem(blankTemplateSlot, new ItemCreator(Material.MAP).setDisplayName(player, MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_TEMPLATES_EMPTY).apply());


                                    this.setBackItem(inventoryCreator, 18, player);
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
        Database database = MapCreatorPlugin.getMapCreatorPlugin().getBukkitCoreAPI().getDatabase();
        List<Document> documents = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        switch(inventorySection) {
            case CATEGORIES -> {
                database.setDataSourceProperty(MapCreatorPlugin.DataSource.PROPERTY);
                database.disableCache();

                database.list(fetchedCategories -> fetchedItems.done(fetchedCategories, items));
            }
            case CATEGORY_MAPS -> {
                CustomMapCreatorMap recentlyViewed = currentPlayerMap.getRecentlyViewed();
                for(String world : this.customMapCreator.listMapsByCategory(recentlyViewed.getMapCategory())) {
                    ItemCreator worldItemCreator = new ItemCreator(Material.FILLED_MAP).setDisplayName(world.split(recentlyViewed.getCategoryIdentifier())[1]).addAllFlags();
                    worldItemCreator.addEmptyLore();

                    if(this.worldAlreadyLoadedOnServer(world)) {
                        MapCreatorMap mapCreatorMap = this.getCustomMapCreator().getMapCreatorMap(world);
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
                            if(dataAdded) worldItemCreator.addEmptyLore();
                        }
                    }else {
                        worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_RIGHT_CLICK).get(player));
                        worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_SHIFT_CLICK).get(player));
                        worldItemCreator.addEmptyLore();
                    }
                    worldItemCreator.addLore(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_MAP_ACTION_LEFT_CLICK).get(player));
                    worldItemCreator.addHiddenString(MapCreatorPlugin.getMapCreatorPlugin(), MapCreatorPlugin.ItemDataStorageKeys.CATEGORY, recentlyViewed.getMapCategory());
                    items.add(worldItemCreator.apply());

                    if(player.getWorld().getName().equalsIgnoreCase(world)) {
                        MapCreatorInventory.setMapManagementItem(player);
                    }
                }
                fetchedItems.done(documents, items);
            }
            /*
             * ToDo: use this code here when users can create own template maps
             * */
            /*case MAP_TEMPLATES_CHOOSE_TEMPLATE -> {
                for(String world : this.customMapCreator.listWorldsByCategory(currentPlayerMap.getMapCategory())) {
                    world = world.replace("...", "/").split("/")[1];
                    items.add(new ItemCreator(Material.FILLED_MAP).setDisplayName("§c§l" + world).apply());
                }
                fetchedItems.done(documents, items);
            }*/
            default -> fetchedItems.done(documents, items);
        }
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
    public void setMapManagementActionItems(Player player, CustomMapCreatorMap mapCreatorMap, InventoryCreator inventoryCreator, Enum action, String translationProperty, int actionItemSlot, Material actionItemMaterial) {
        if(!actionItemMaterial.equals(Material.AIR)) {
            ItemCreator actionItemCreator = new ItemCreator(actionItemMaterial).addAllFlags();
            String disabledActionColorCodes = "";
            if(this.worldAlreadyLoadedOnServer(mapCreatorMap.getFileName())) {
                if(action.equals(MapCreator.Action.LOAD)) {
                    disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                }else if(action.equals(CustomMapCreator.CustomAction.User.TELEPORT)) {
                    if(player.getWorld().getName().equals(mapCreatorMap.getFileName())) {
                        disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                    }
                }
            }else {
                if(action.equals(MapCreator.Action.SAVE) || action.equals(MapCreator.Action.LEAVE_WITHOUT_SAVING) || action.equals(CustomMapCreator.CustomAction.User.TELEPORT)) {
                    disabledActionColorCodes = DISABLED_ACTION_COLOR_CODES;
                }
            }
            actionItemCreator.setDisplayName(new BukkitTranslation(translationProperty).get(player, "$disabled$", disabledActionColorCodes));
            inventoryCreator.setItem(actionItemSlot, actionItemCreator.apply());
        }
    }

    @Override
    public void setLoadedPlayerMap(Player player, CustomMapCreatorMap customMapCreatorMap) {
        this.currentPlayerMap.put(player, customMapCreatorMap);
    }

    @Override
    public CustomMapCreatorMap getLoadedPlayerMap(Player player) {
        if(this.currentPlayerMap.get(player) == null) {
            this.currentPlayerMap.put(player, new CustomMapCreatorMap());
        }
        return this.currentPlayerMap.get(player);
    }

    private void setNothingFoundItem(Player player, InventoryCreator inventoryCreator, Section section) {
        String translationProperty = switch(section) {
            case CATEGORIES -> INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND;
            case CATEGORY_MAPS -> INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND;
            case IMPORTER_WORLDS_AVAILABLE -> INVENTORY_SECTION_CATEGORY_MAPS_NO_IMPORTABLE_WORLDS_FOUND;
            default -> null;
        };
        if(translationProperty != null) {
            inventoryCreator.setItem(22, new ItemCreator(Material.BARRIER).setDisplayName(player, translationProperty).apply());
        }
    }

    private void setBackItem(InventoryCreator inventoryCreator, int slot, Player player) {
        inventoryCreator.setItem(slot, new ItemCreator(Material.RED_DYE).setDisplayName(player, MapCreatorPlugin.Translations.BACK).apply());
    }
}
