package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.creators.infiniteinventory.events.InfiniteInventoryClickEvent;
import dev.dementisimus.capi.core.creators.signcreator.SignInputCreator;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
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
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.CATEGORIES;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.CATEGORY_MAPS;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.CATEGORY_MAPS_MAP_CHOOSE_ACTION;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.WORLDS_IMPORTER_CATEGORIES;
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

    @EventHandler
    public void on(InfiniteInventoryClickEvent event) {
        Player player = event.getPlayer();
        String title = event.getCurrentInventoryTitle();
        String displayName = event.getCurrentItemDisplayName();
        boolean isInRange = event.isInRange();

        MapCreatorInventory.Section currentSection = null;

        for(MapCreatorInventory.Section section : MapCreatorInventory.Section.values()) {
            if(new BukkitTranslation(section.getTitleTranslationProperty()).matches(title)) {
                currentSection = section;
                break;
            }
        }

        if(currentSection != null) {
            switch(currentSection) {
                case CATEGORIES -> {
                    if(isInRange) {
                        this.customMapCreatorInventory.getCurrentlyViewedPlayerMap(player).setMapCategory(displayName);
                        this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                    }else {
                        if(new BukkitTranslation(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).matches(displayName)) {
                            Material icon = player.getItemOnCursor().getType().equals(Material.AIR) ? Material.PAPER : player.getItemOnCursor().getType();
                            player.setItemOnCursor(null);
                            this.fetchInput(player, true, newCategory -> {
                                if(newCategory != null) {
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
                                }else {
                                    this.customMapCreatorInventory.open(player, CATEGORIES);
                                }
                            });
                        }
                    }
                }
                case CATEGORY_MAPS -> {
                    CustomMapCreatorMap currentlyViewedPlayerMap = this.customMapCreatorInventory.getCurrentlyViewedPlayerMap(player);

                    if(currentlyViewedPlayerMap != null) {
                        if(isInRange) {
                            this.customMapCreatorInventory.setCurrentlyViewedPlayerMap(player, displayName);

                            MapCreatorInventory.Section section = null;
                            if(event.isRightClick()) {
                                if(!this.customMapCreatorInventory.worldAlreadyLoadedOnServer(currentlyViewedPlayerMap.getFileName())) {
                                    this.handlePerformance(MapCreator.Action.LOAD, true, currentlyViewedPlayerMap, player);
                                }else {
                                    section = MapCreatorInventory.Section.CATEGORY_MAPS_MAP_CHOOSE_ACTION;
                                }
                            }else if(event.isLeftClick()) {
                                section = CATEGORY_MAPS_MAP_CHOOSE_ACTION;
                            }
                            if(section != null) {
                                this.customMapCreatorInventory.open(player, section);
                            }
                        }else {
                            if(new BukkitTranslation(INVENTORY_SECTION_CATEGORY_CREATE_MAP).matches(displayName)) {
                                this.fetchInput(player, true, newMap -> {
                                    if(newMap != null) {
                                        currentlyViewedPlayerMap.setMapName(newMap);
                                        this.handlePerformance(MapCreator.Action.LOAD, true, currentlyViewedPlayerMap, player);
                                    }else {
                                        this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                                    }
                                });
                            }else if(new BukkitTranslation(BACK).matches(displayName)) {
                                this.customMapCreatorInventory.open(player, CATEGORIES);
                            }else if(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_WORLD).matches(displayName)) {
                                this.customMapCreatorInventory.open(player, WORLDS_IMPORTER_CATEGORIES);
                            }
                        }
                    }
                }
                case CATEGORY_MAPS_MAP_CHOOSE_ACTION, CATEGORY_MAPS_MAP_MANAGEMENT -> {
                    MapCreator.Action clickedAction = null;
                    MapCreator.Action.Player clickedPlayerAction = null;

                    CustomMapCreatorMap currentPlayerMap = this.customMapCreatorInventory.getAppropriateSectionPlayerMap(currentSection, player);

                    if(currentPlayerMap.getMapName() != null) {
                        for(MapCreator.Action action : MapCreator.Action.values()) {
                            if(this.matchesAction(player, displayName, action.getTranslationProperty())) {
                                clickedAction = action;
                                break;
                            }
                        }
                        for(MapCreator.Action.Player playerAction : MapCreator.Action.Player.values()) {
                            if(this.matchesAction(player, displayName, playerAction.getTranslationProperty())) {
                                clickedPlayerAction = playerAction;
                                break;
                            }
                        }
                        if(clickedAction != null) {
                            if(clickedAction.equals(MapCreator.Action.DELETE)) {
                                MapCreator.Action finalClickedAction = clickedAction;
                                MapCreatorInventory.Section finalCurrentSection = currentSection;
                                this.fetchInput(player, true, mapInput -> {
                                    if(mapInput != null && mapInput.equals(currentPlayerMap.getMapName())) {
                                        this.handlePerformance(finalClickedAction, false, currentPlayerMap, player);
                                    }else {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                        this.customMapCreatorInventory.open(player, finalCurrentSection);
                                    }
                                });
                            }else {
                                if(this.customMapCreatorInventory.worldAlreadyLoadedOnServer(currentPlayerMap.getFileName())) {
                                    if(clickedAction.equals(MapCreator.Action.SAVE) || clickedAction.equals(MapCreator.Action.LEAVE)) {
                                        this.handlePerformance(clickedAction, true, currentPlayerMap, player);
                                    }else {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    }
                                }else {
                                    if(clickedAction.equals(MapCreator.Action.LOAD)) {
                                        this.handlePerformance(clickedAction, true, currentPlayerMap, player);
                                    }else {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    }
                                }
                            }
                        }else if(clickedPlayerAction != null) {
                            switch(clickedPlayerAction) {
                                case TELEPORT -> {
                                    if(this.customMapCreatorInventory.worldAlreadyLoadedOnServer(currentPlayerMap.getFileName())) {
                                        if(!player.getWorld().getName().equalsIgnoreCase(currentPlayerMap.getFileName())) {
                                            player.sendMessage("teleport to " + currentPlayerMap.getFileName());
                                        }else {
                                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                        }
                                    }else {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    }
                                }
                                case BACK -> this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                            }
                        }
                    }
                }
                case WORLDS_IMPORTER_CATEGORIES -> {
                    CustomWorldImporter customWorldImporter = this.customMapCreator.getCustomWorldImporter();

                    if(isInRange) {
                        if(displayName.contains("/")) {
                            String fileName = ChatColor.stripColor(displayName).replace("/", MapCreatorMap.CATEGORY_MAP_SEPARATOR);
                            CustomMapCreatorMap importableWorld = customWorldImporter.getImportableWorldByFileName(fileName);

                            if(importableWorld != null) {
                                customWorldImporter.importWorld(player, importableWorld);
                            }else {
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                            }
                        }
                    }else {
                        if(new BukkitTranslation(BACK).matches(displayName)) {
                            this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                        }
                    }
                }
            }
        }
    }

    /*
     *
     * ToDo: handle performance failure reasons properly
     * ToDO: handle pre-action & post-action messages/actions
     *
     * */
    private void handlePerformance(MapCreator.Action action, boolean closeInventory, CustomMapCreatorMap customMapCreatorMap, Player player) {
        if(closeInventory) player.closeInventory();

        if(action.isPreActionRequired()) {
            player.sendMessage("pre-action");
        }
        this.customMapCreator.perform(action, customMapCreatorMap, performance -> {
            if(performance.isSuccess()) {
                if(performance.getSlimeWorld() != null) {
                    customMapCreatorMap.setSlimeWorld(performance.getSlimeWorld());
                    this.customMapCreatorInventory.getCurrentlyLoadedPlayerMap(player).setSlimeWorld(performance.getSlimeWorld());
                    customMapCreatorMap.setLoadedBy(player.getName());
                }
                this.customMapCreatorInventory.setCurrentlyLoadedMap(player, customMapCreatorMap);
                performance.performCustomPlayerMapAction(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
                if(!action.equals(MapCreator.Action.LOAD)) {
                    this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                }
                player.sendMessage("post-action");
            }else {
                player.sendMessage("FAILURE_REASON: " + performance.getFailureReason());
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 20, 1);
            }
        });
    }

    private void fetchInput(Player player, boolean allowBlankInput, Callback<String> stringCallback) {
        new SignInputCreator(player, this.mapCreatorPlugin).listen(SignInputCreator.getAdditionalSignLines(player, INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION), newMap -> {
            if(!newMap.isBlank() && !newMap.isEmpty()) {
                stringCallback.done(newMap.replaceAll(" ", ""));
            }else {
                if(allowBlankInput) {
                    stringCallback.done(null);
                }else {
                    this.fetchInput(player, false, stringCallback);
                }
            }
        });
    }

    private boolean matchesAction(Player player, String displayName, String translationProperty) {
        return ChatColor.stripColor(new BukkitTranslation(translationProperty).get(player, "$disabled$", "")).equalsIgnoreCase(ChatColor.stripColor(displayName));
    }
}
