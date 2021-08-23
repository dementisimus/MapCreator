package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.ToDo;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.creators.infiniteinventory.events.InfiniteInventoryClickEvent;
import dev.dementisimus.capi.core.creators.signcreator.SignInputCreator;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.BACK;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.MapCreatorInventorySection.CATEGORIES;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.MapCreatorInventorySection.CATEGORY_MAPS;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.MapCreatorInventorySection.CATEGORY_MAPS_MAP_MANAGEMENT;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class InfiniteInventoryClickListener @ MapCreator
 *
 * @author dementisimus
 * @since 31.07.2021:16:58
 */
@BukkitListener(additionalModulesToInject = {MapCreatorPlugin.class, DataManagement.class, CustomMapCreatorInventory.class, CustomMapCreator.class})
public class InfiniteInventoryClickListener implements Listener {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject DataManagement dataManagement;
    @Inject CustomMapCreatorInventory customMapCreatorInventory;
    @Inject CustomMapCreator customMapCreator;

    @ToDo(task = "add field to store viewed maps")
    @EventHandler
    public void on(InfiniteInventoryClickEvent event) {
        Player player = event.getPlayer();
        String title = event.getCurrentInventoryTitle();
        String displayName = event.getCurrentItemDisplayName();
        boolean isInRange = event.isInRange();

        MapCreatorInventory.MapCreatorInventorySection currentMapCreatorInventorySection = null;
        CustomMapCreatorMap customMapCreatorMap = this.customMapCreatorInventory.getCurrentPlayerMap(player);
        String currentPlayerMap = customMapCreatorMap == null || customMapCreatorMap.getMapName() == null ? "" : customMapCreatorMap.getMapName();
        for(MapCreatorInventory.MapCreatorInventorySection mapCreatorInventorySection : MapCreatorInventory.MapCreatorInventorySection.values()) {
            if(new BukkitTranslation(mapCreatorInventorySection.getTitleTranslationProperty()).matches(title, "$map$", currentPlayerMap)) {
                currentMapCreatorInventorySection = mapCreatorInventorySection;
                break;
            }
        }
        if(currentMapCreatorInventorySection != null) {
            if(currentMapCreatorInventorySection.equals(CATEGORIES)) {
                if(isInRange) {
                    this.customMapCreatorInventory.setCurrentPlayerMapCategory(player, displayName);
                    this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                }else {
                    if(new BukkitTranslation(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).matches(displayName)) {
                        Material icon = player.getItemOnCursor().getType().equals(Material.AIR) ? Material.PAPER : player.getItemOnCursor().getType();
                        player.setItemOnCursor(null);
                        this.fetchInput(player, newCategory -> {
                            newCategory = newCategory.toUpperCase();
                            this.dataManagement.setRequirements(MapCreatorPlugin.Storage.CATEGORIES, MapCreatorPlugin.Storage.Categories.NAME, newCategory);

                            Document document = new Document();
                            document.append(MapCreatorPlugin.Storage.Categories.NAME, newCategory);
                            document.append(MapCreatorPlugin.Storage.Categories.ICON, icon.name());

                            this.dataManagement.update(document, updated -> {
                                if(!updated) {
                                    this.dataManagement.write(document, done -> {
                                        this.customMapCreatorInventory.open(player, CATEGORIES);
                                    });
                                }else {
                                    this.customMapCreatorInventory.open(player, CATEGORIES);
                                }
                            });
                        });
                    }
                }
            }else if(currentMapCreatorInventorySection.equals(CATEGORY_MAPS)) {
                if(customMapCreatorMap != null) {
                    if(isInRange) {
                        ItemCreator mapItemCreator = new ItemCreator(event.getCurrentItem());

                        if(customMapCreatorMap.getMapName() == null || customMapCreatorMap.getMapCategory() == null) {
                            this.setCurrentPlayerMapProperties(customMapCreatorMap, displayName, mapItemCreator);
                        }else {
                            if(player.getWorld().getName().equalsIgnoreCase(customMapCreatorMap.getFullMapName())) {
                                /*
                                 *
                                 * -> add field to store viewed maps
                                 *   + use it @ CustomMapCreatorInventory
                                 *
                                 * */
                            }else {
                                this.setCurrentPlayerMapProperties(customMapCreatorMap, displayName, mapItemCreator);
                            }
                        }

                        if(event.isRightClick()) {
                            player.getInventory().close();
                            this.handlePerformance(MapCreator.Action.LOAD, new CustomMapCreatorMap(customMapCreatorMap.getMapName(), customMapCreatorMap.getMapCategory()), player);
                        }else if(event.isLeftClick()) {
                            this.customMapCreatorInventory.open(player, MapCreatorInventory.MapCreatorInventorySection.CATEGORY_MAPS_MAP_MANAGEMENT);
                        }
                    }else {
                        if(new BukkitTranslation(INVENTORY_SECTION_CATEGORY_CREATE_MAP).matches(displayName)) {
                            this.fetchInput(player, newMap -> {
                                this.customMapCreatorInventory.setCurrentPlayerMap(player, newMap);
                                this.handlePerformance(MapCreator.Action.LOAD, new CustomMapCreatorMap(customMapCreatorMap.getMapName(), customMapCreatorMap.getMapCategory()), player);
                            });
                        }else if(new BukkitTranslation(BACK).matches(displayName)) {
                            this.customMapCreatorInventory.open(player, CATEGORIES);
                        }
                    }
                }
            }else if(currentMapCreatorInventorySection.equals(CATEGORY_MAPS_MAP_MANAGEMENT)) {
                MapCreator.Action clickedAction = null;
                if(!currentPlayerMap.isEmpty() && !currentPlayerMap.isBlank()) {
                    for(MapCreator.Action action : MapCreator.Action.values()) {
                        if(ChatColor.stripColor(new BukkitTranslation(action.getTranslationProperty()).get(player, "$disabled$", "")).equalsIgnoreCase(ChatColor.stripColor(displayName))) {
                            clickedAction = action;
                            break;
                        }
                    }
                    if(clickedAction != null) {
                        if(clickedAction.equals(MapCreator.Action.DELETE)) {
                            MapCreator.Action finalClickedAction = clickedAction;
                            this.fetchInput(player, mapInput -> {
                                if(mapInput.equals(currentPlayerMap)) {
                                    this.handlePerformance(finalClickedAction, customMapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    this.customMapCreatorInventory.open(player, CATEGORY_MAPS_MAP_MANAGEMENT);
                                }
                            });
                        }else {
                            if(this.customMapCreatorInventory.worldAlreadyLoadedOnServer(player, customMapCreatorMap.getFullMapName())) {
                                if(clickedAction.equals(MapCreator.Action.SAVE) || clickedAction.equals(MapCreator.Action.LEAVE)) {
                                    this.handlePerformance(clickedAction, customMapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                }
                            }else {
                                if(clickedAction.equals(MapCreator.Action.LOAD)) {
                                    this.handlePerformance(clickedAction, customMapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ToDo(task = "handle performance failure reasons properly")
    private void handlePerformance(MapCreator.Action action, CustomMapCreatorMap customMapCreatorMap, Player player) {
        this.customMapCreator.perform(action, customMapCreatorMap, performance -> {
            if(performance.isSuccess()) {
                performance.performCustomPlayerMapAction(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
            }else {
                player.sendMessage("FAILURE_REASON: " + performance.getFailureReason());
                this.customMapCreatorInventory.open(player, CATEGORY_MAPS_MAP_MANAGEMENT);
            }
        });
    }

    private void fetchInput(Player player, Callback<String> stringCallback) {
        new SignInputCreator(player, this.mapCreatorPlugin).listen(SignInputCreator.getAdditionalSignLines(player, INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION), newMap -> {
            if(!newMap.isBlank() && !newMap.isEmpty()) {
                stringCallback.done(newMap.replaceAll(" ", ""));
            }else {
                this.fetchInput(player, stringCallback);
            }
        });
    }

    private void setCurrentPlayerMapProperties(CustomMapCreatorMap customMapCreatorMap, String displayName, ItemCreator mapItemCreator) {
        customMapCreatorMap.setMapName(displayName);
        customMapCreatorMap.setMapCategory(mapItemCreator.getHiddenString(this.mapCreatorPlugin, MapCreatorPlugin.ItemDataStorageKeys.CATEGORY));
    }
}
