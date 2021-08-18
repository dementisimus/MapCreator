package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.creators.infiniteinventory.events.InfiniteInventoryClickEvent;
import dev.dementisimus.capi.core.creators.signcreator.SignInputCreator;
import dev.dementisimus.capi.core.databases.DataManagement;
import dev.dementisimus.capi.core.debugging.SysOut;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CurrentPlayerMap;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.MapCreatorInventorySection.CATEGORIES;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.MapCreatorInventorySection.CATEGORY_MAPS;
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
        CustomMapCreator customMapCreator = this.mapCreatorPlugin.getCustomMapCreator();
        CustomMapCreatorInventory customMapCreatorInventory = customMapCreator.getCustomMapCreatorInventory();

        Player player = event.getPlayer();
        String title = event.getCurrentInventoryTitle();
        String displayName = event.getCurrentItemDisplayName();
        boolean isInRange = event.isInRange();

        MapCreatorInventory.MapCreatorInventorySection currentMapCreatorInventorySection = null;
        for(MapCreatorInventory.MapCreatorInventorySection mapCreatorInventorySection : MapCreatorInventory.MapCreatorInventorySection.values()) {
            if(new BukkitTranslation(mapCreatorInventorySection.getTitleTranslationProperty()).matches(title)) {
                currentMapCreatorInventorySection = mapCreatorInventorySection;
                break;
            }
        }
        if(currentMapCreatorInventorySection != null) {
            if(currentMapCreatorInventorySection.equals(CATEGORIES)) {
                if(isInRange) {
                    customMapCreatorInventory.setCurrentPlayerMapCategory(player, displayName);
                    customMapCreatorInventory.open(player, CATEGORY_MAPS);
                }else {
                    if(new BukkitTranslation(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).matches(displayName)) {
                        Material icon = player.getItemOnCursor().getType().equals(Material.AIR) ? Material.PAPER : player.getItemOnCursor().getType();
                        player.setItemOnCursor(null);
                        new SignInputCreator(player, this.mapCreatorPlugin).listen(SignInputCreator.getAdditionalSignLines(player, INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION), newCategory -> {
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
                if(isInRange) {

                }else {
                    if(new BukkitTranslation(INVENTORY_SECTION_CATEGORY_CREATE_MAP).matches(displayName)) {
                        new SignInputCreator(player, this.mapCreatorPlugin).listen(SignInputCreator.getAdditionalSignLines(player, INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION), newMap -> {
                            customMapCreatorInventory.setCurrentPlayerMap(player, newMap);
                            CurrentPlayerMap currentPlayerMap = customMapCreatorInventory.getCurrentPlayerMap(player);
                            this.customMapCreator.perform(MapCreator.Action.CREATE, new CustomMapCreatorMap(currentPlayerMap.getMapName(), currentPlayerMap.getCategoryName()), performance -> {
                                SysOut.debug("PERFORMANCE: " + performance.isSuccess());
                                SysOut.debug("PERFORMANCE_WORLD: " + performance.getSlimeWorld());
                                SysOut.debug("PERFORMANCE_FAILURE_REASON: " + performance.getFailureReason());
                            });
                        });
                    }
                }
            }
        }
    }
}
