package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.creators.infiniteinventory.events.InfiniteInventoryClickEvent;
import dev.dementisimus.capi.core.creators.signcreator.SignInputCreator;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.properties.UpdateProperty;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.pools.ScheduledExecutor;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.CustomPlayerMapActions;
import dev.dementisimus.mapcreator.creator.api.MapCreator;
import dev.dementisimus.mapcreator.creator.api.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.creator.templates.interfaces.MapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.BACK;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION;
import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_CREATE_MAP;
import static dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory.Section.*;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class InfiniteInventoryClickListener @ MapCreator
 *
 * @author dementisimus
 * @since 31.07.2021:16:58
 */
@BukkitListener(additionalModulesToInject = {MapCreatorPlugin.class, CustomMapCreatorInventory.class, CustomMapCreator.class})
public class InfiniteInventoryClickListener implements Listener {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject CustomMapCreatorInventory customMapCreatorInventory;
    @Inject CustomMapCreator customMapCreator;

    @EventHandler
    public void on(InfiniteInventoryClickEvent event) {
        Player player = event.getPlayer();
        String title = event.getCurrentInventoryTitle();
        String displayName = event.getCurrentItemDisplayName();
        boolean isInRange = event.isInRange();
        Database database = this.mapCreatorPlugin.getDatabase();

        MapCreatorInventory.Section currentSection = null;

        for(MapCreatorInventory.Section section : MapCreatorInventory.Section.values()) {
            if(new BukkitTranslation(section.getTitleTranslationProperty()).matches(title)) {
                currentSection = section;
                break;
            }
        }

        if(currentSection != null) {
            CustomMapCreatorMap loadedPlayerMap = this.customMapCreatorInventory.getLoadedPlayerMap(player);

            switch(currentSection) {
                case CATEGORIES -> {
                    if(isInRange) {
                        if(!new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND).matches(displayName)) {
                            if(loadedPlayerMap.getMapCategory() == null) {
                                loadedPlayerMap.setMapCategory(displayName);
                            }
                            CustomMapCreatorMap recentlyViewed = loadedPlayerMap.getRecentlyViewed();
                            if(recentlyViewed == null) {
                                recentlyViewed = new CustomMapCreatorMap();
                            }
                            loadedPlayerMap.setRecentlyViewed(recentlyViewed.setMapCategory(displayName));
                            this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                        }
                    }else {
                        if(new BukkitTranslation(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).matches(displayName)) {
                            Material icon = player.getItemOnCursor().getType().equals(Material.AIR) ? Material.PAPER : player.getItemOnCursor().getType();
                            player.setItemOnCursor(null);
                            this.fetchInput(player, true, newCategory -> {
                                if(newCategory != null) {
                                    if(!newCategory.isEmpty()) {
                                        if(!newCategory.equalsIgnoreCase(MapTemplates.CATEGORY_TEMPLATES)) {
                                            newCategory = newCategory.toUpperCase();

                                            database.setDataSourceProperty(MapCreatorPlugin.DataSource.PROPERTY);

                                            Document document = new Document();
                                            document.append(MapCreatorPlugin.DataSource.NAME, newCategory);
                                            document.append(MapCreatorPlugin.DataSource.ICON, icon.name());

                                            database.setDocument(document);
                                            database.setUpdateProperty(UpdateProperty.of(MapCreatorPlugin.DataSource.NAME, newCategory).value(MapCreatorPlugin.DataSource.ICON, icon.name()));
                                            database.writeOrUpdate(success -> {
                                                this.customMapCreatorInventory.open(player, CATEGORIES);
                                            });
                                        }else {
                                            player.playSound(player.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_ANGRY, 10, 1);
                                            this.customMapCreatorInventory.open(player, CATEGORIES);
                                        }
                                    }else {
                                        this.customMapCreatorInventory.open(player, CATEGORIES);
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    }
                                }else {
                                    this.customMapCreatorInventory.open(player, CATEGORIES);
                                }
                            });
                        }
                    }
                }
                case CATEGORY_MAPS -> {
                    CustomMapCreatorMap recentlyViewedMap = loadedPlayerMap.getRecentlyViewed();
                    if(recentlyViewedMap != null) {
                        if(isInRange) {
                            if(!new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_NOTHING_FOUND).matches(displayName)) {
                                recentlyViewedMap.setMapName(displayName);

                                MapCreatorMap loadedMap = this.customMapCreator.getMapCreatorMap(recentlyViewedMap.getFileName());
                                if(loadedMap != null) {
                                    recentlyViewedMap.setSlimeWorld(loadedMap.getSlimeWorld());
                                }

                                MapCreatorInventory.Section section = null;
                                if(event.isRightClick()) {
                                    if(!this.customMapCreatorInventory.worldAlreadyLoadedOnServer(recentlyViewedMap.getFileName())) {
                                        this.handlePerformance(MapCreator.Action.LOAD, recentlyViewedMap, player);
                                    }else {
                                        section = MapCreatorInventory.Section.CATEGORY_MAPS_MAP_CHOOSE_ACTION;
                                    }
                                }else if(event.isLeftClick()) {
                                    section = CATEGORY_MAPS_MAP_CHOOSE_ACTION;
                                }
                                if(section != null) {
                                    this.customMapCreatorInventory.open(player, section);
                                }
                            }
                        }else {
                            if(new BukkitTranslation(INVENTORY_SECTION_CATEGORY_CREATE_MAP).matches(displayName)) {
                                try {
                                    List<String> availableTemplates = this.customMapCreator.listWorldsByCategory(CustomMapTemplates.CATEGORY_TEMPLATES);
                                    if(availableTemplates != null && !availableTemplates.isEmpty()) {
                                        this.customMapCreatorInventory.open(player, MapCreatorInventory.Section.MAP_TEMPLATES_CHOOSE_TEMPLATE);
                                        return;
                                    }
                                }catch(IOException ignored) {}

                                this.handleMapCreation(player, MapCreator.Action.LOAD, recentlyViewedMap);
                            }else if(new BukkitTranslation(BACK).matches(displayName)) {
                                this.customMapCreatorInventory.open(player, CATEGORIES);
                            }else if(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_IMPORT_WORLD).matches(displayName)) {
                                this.customMapCreatorInventory.open(player, IMPORTER_WORLDS_AVAILABLE);
                            }
                        }
                    }
                }
                case CATEGORY_MAPS_MAP_CHOOSE_ACTION, CATEGORY_MAPS_MAP_MANAGEMENT -> {
                    CustomMapCreatorMap mapCreatorMap = currentSection.equals(CATEGORY_MAPS_MAP_CHOOSE_ACTION) ? loadedPlayerMap.getRecentlyViewed() : loadedPlayerMap;

                    MapCreator.Action clickedAction = null;
                    CustomMapCreator.CustomAction.User clickedUserAction = null;

                    for(MapCreator.Action action : MapCreator.Action.values()) {
                        if(this.matchesAction(player, displayName, action.getTranslationProperty())) {
                            clickedAction = action;
                            break;
                        }
                    }
                    for(CustomMapCreator.CustomAction.User userAction : CustomMapCreator.CustomAction.User.values()) {
                        if(this.matchesAction(player, displayName, userAction.getTranslationProperty())) {
                            clickedUserAction = userAction;
                            break;
                        }
                    }
                    if(clickedAction != null) {
                        mapCreatorMap.setRecentlyViewed(new CustomMapCreatorMap(mapCreatorMap.getMapName(), mapCreatorMap.getMapCategory()));

                        if(clickedAction.equals(MapCreator.Action.DELETE)) {
                            MapCreator.Action finalClickedAction = clickedAction;
                            MapCreatorInventory.Section finalCurrentSection = currentSection;
                            this.fetchInput(player, true, mapInput -> {
                                if(mapInput != null && mapInput.equals(mapCreatorMap.getMapName())) {
                                    this.handlePerformance(finalClickedAction, mapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                    this.customMapCreatorInventory.open(player, finalCurrentSection);
                                }
                            });
                        }else {
                            if(this.customMapCreatorInventory.worldAlreadyLoadedOnServer(mapCreatorMap.getFileName())) {
                                if(clickedAction.equals(MapCreator.Action.SAVE) || clickedAction.equals(MapCreator.Action.LEAVE_WITHOUT_SAVING)) {
                                    this.handlePerformance(clickedAction, mapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                }
                            }else {
                                if(clickedAction.equals(MapCreator.Action.LOAD)) {
                                    this.handlePerformance(clickedAction, mapCreatorMap, player);
                                }else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                                }
                            }
                        }
                    }else if(clickedUserAction != null) {
                        switch(clickedUserAction) {
                            case TELEPORT -> {
                                if(this.customMapCreatorInventory.worldAlreadyLoadedOnServer(mapCreatorMap.getFileName())) {
                                    if(!player.getWorld().getName().equalsIgnoreCase(mapCreatorMap.getFileName())) {
                                        this.customMapCreatorInventory.setLoadedPlayerMap(player, mapCreatorMap);

                                        CustomPlayerMapActions customPlayerMapActions = new CustomPlayerMapActions(player, mapCreatorMap.getSlimeWorld());
                                        customPlayerMapActions.load();

                                        player.playSound(player.getLocation(), Sound.ENTITY_STRIDER_HAPPY, 10, 1);
                                        clickedUserAction.sendActionMessage(player, mapCreatorMap);
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
                case IMPORTER_WORLDS_AVAILABLE -> {
                    CustomWorldImporter customWorldImporter = this.customMapCreator.getCustomWorldImporter();

                    if(isInRange) {
                        if(displayName.contains("/")) {
                            String fileName = ChatColor.stripColor(displayName).replace("/", CustomMapCreatorMap.CATEGORY_MAP_SEPARATOR);
                            CustomMapCreatorMap importableWorld = customWorldImporter.getImportableWorldByFileName(fileName);

                            if(importableWorld != null) {
                                this.handlePerformance(MapCreator.Action.IMPORT, importableWorld, player);
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
                case MAP_TEMPLATES_CHOOSE_TEMPLATE -> {
                    CustomMapCreatorMap recentlyViewed = loadedPlayerMap.getRecentlyViewed();

                    if(isInRange) {
                        CustomMapCreatorMap cloneFrom = new CustomMapCreatorMap();
                        cloneFrom.setMapCategory(MapTemplates.CATEGORY_TEMPLATES);
                        if(!new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_TEMPLATES_EMPTY).matches(displayName)) {
                            cloneFrom.setMapName(MapTemplates.SIMPLE_TEMPLATE_NAME);

                            recentlyViewed.setCloneFrom(cloneFrom);
                            this.handleMapCreation(player, MapCreator.Action.CLONE, recentlyViewed);
                        }else {
                            this.handleMapCreation(player, MapCreator.Action.LOAD, recentlyViewed);
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

    private void handlePerformance(MapCreator.Action action, CustomMapCreatorMap customMapCreatorMap, Player player) {
        if(action.isPreActionRequired()) {
            action.sendActionMessage(player, customMapCreatorMap, "", false);
        }

        long started = System.currentTimeMillis();

        AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<>();

        if(action.isUseLoadingActionBar()) {
            player.closeInventory();

            String mapName = action.equals(MapCreator.Action.CLONE) ? customMapCreatorMap.getCloneFrom().getPrettyName() : customMapCreatorMap.getPrettyName();
            Component message = Component.text(new BukkitTranslation(action.getLoadingActionBarActionTranslationProperty()).get(player, "$map$", mapName));

            AtomicInteger atomicDotCounter = new AtomicInteger(0);
            TextComponent dotText = Component.text(".");

            scheduledFuture.set(ScheduledExecutor.scheduleWithFixedDelay(0, 375, TimeUnit.MILLISECONDS, () -> {
                int dotCounter = atomicDotCounter.get();
                TextComponent dots = Component.empty();

                for(int i = 0; i < dotCounter; i++) {
                    dots = dots.append(dotText);
                }

                player.sendActionBar(message.append(dots));

                if(dotCounter == 3) {
                    atomicDotCounter.set(0);
                }else {
                    atomicDotCounter.incrementAndGet();
                }
            }));

            if(action.equals(MapCreator.Action.LOAD)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 10, false, false, false));
            }
        }

        this.customMapCreator.perform(action, customMapCreatorMap, performance -> {
            if(performance.isSuccess()) {
                if(performance.getSlimeWorld() != null) {
                    customMapCreatorMap.setSlimeWorld(performance.getSlimeWorld());
                    customMapCreatorMap.setLoadedBy(player.getName());
                    customMapCreatorMap.setLoadedSince(new Date());

                    this.customMapCreatorInventory.setLoadedPlayerMap(player, customMapCreatorMap);
                }

                if(action.equals(MapCreator.Action.IMPORT) || action.equals(MapCreator.Action.DELETE)) {
                    this.customMapCreator.getCustomWorldImporter().scanForImportableWorlds();
                }

                performance.performCustomPlayerMapAction(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
                if(!action.equals(MapCreator.Action.LOAD) && !action.equals(MapCreator.Action.CLONE)) {
                    this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                }

                String elapsed = String.format("%.3fs", (System.currentTimeMillis() - started) / 1000.0f);
                action.sendActionMessage(player, customMapCreatorMap, elapsed, true);
            }else {
                performance.announceFailure(player);
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 20, 1);
            }

            if(action.isUseLoadingActionBar() || !performance.isSuccess()) {
                scheduledFuture.get().cancel(true);

                player.sendActionBar(Component.empty());

                if(action.equals(MapCreator.Action.LOAD)) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
        });
    }

    private void fetchInput(Player player, boolean allowBlankInput, Callback<String> stringCallback) {
        new SignInputCreator(player, this.mapCreatorPlugin).listen(SignInputCreator.getAdditionalSignLines(player, INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_SIGN_INSTRUCTION), newMap -> {
            if(newMap.matches("[A-Za-z0-9]+")) {
                if(!newMap.isBlank() && !newMap.isEmpty()) {
                    stringCallback.done(newMap.replaceAll(" ", ""));
                }else {
                    if(allowBlankInput) {
                        stringCallback.done(null);
                    }else {
                        this.fetchInput(player, false, stringCallback);
                    }
                }
            }else {
                stringCallback.done("");
            }
        });
    }

    private void handleMapCreation(Player player, MapCreator.Action action, CustomMapCreatorMap currentPlayerMap) {
        this.fetchInput(player, true, newMap -> {
            if(newMap != null && !newMap.isEmpty()) {
                currentPlayerMap.setMapName(newMap);
                this.handlePerformance(action, currentPlayerMap, player);
            }else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                this.customMapCreatorInventory.open(player, MAP_TEMPLATES_CHOOSE_TEMPLATE);
            }
        });
    }

    private boolean matchesAction(Player player, String displayName, String translationProperty) {
        return ChatColor.stripColor(new BukkitTranslation(translationProperty).get(player, "$disabled$", "")).equalsIgnoreCase(ChatColor.stripColor(displayName));
    }
}
