package dev.dementisimus.mapcreator.listener;

import com.google.inject.Inject;
import dev.dementisimus.capi.core.actionbar.ActionBar;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.callback.EmptyCallback;
import dev.dementisimus.capi.core.creators.infiniteinventory.events.InfiniteInventoryClickEvent;
import dev.dementisimus.capi.core.creators.input.UserInputFetcher;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.properties.UpdateProperty;
import dev.dementisimus.capi.core.injection.annotations.bukkit.BukkitListener;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.capi.core.pools.BukkitSynchronousExecutor;
import dev.dementisimus.capi.core.pools.ScheduledExecutor;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.CustomPlayerMapActions;
import dev.dementisimus.mapcreator.creator.api.MapCreator;
import dev.dementisimus.mapcreator.creator.api.MapCreatorMap;
import dev.dementisimus.mapcreator.creator.api.MapTemplates;
import dev.dementisimus.mapcreator.creator.api.settings.MapCreationSettings;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.difficulty.DefaultDifficulty;
import dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment;
import dev.dementisimus.mapcreator.creator.api.settings.worldtype.DefaultWorldType;
import dev.dementisimus.mapcreator.creator.importer.CustomWorldImporter;
import dev.dementisimus.mapcreator.creator.settings.CustomMapCreationSettings;
import dev.dementisimus.mapcreator.creator.templates.CustomMapTemplates;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import dev.dementisimus.mapcreator.gui.interfaces.MapCreatorInventory;
import net.md_5.bungee.api.chat.TextComponent;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.dementisimus.mapcreator.MapCreatorPlugin.Translations.*;
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
@BukkitListener(isOptional = true)
public class InfiniteInventoryClickListener implements Listener {

    @Inject MapCreatorPlugin mapCreatorPlugin;
    @Inject CustomMapCreator customMapCreator;
    @Inject CustomMapCreatorInventory customMapCreatorInventory;

    @EventHandler
    public void on(InfiniteInventoryClickEvent event) {
        Database database = this.mapCreatorPlugin.getDatabase();

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
            CustomMapCreatorMap loadedPlayerMap = this.customMapCreatorInventory.getLoadedPlayerMap(player);

            if(loadedPlayerMap.getRecentlyViewed() == null) {
                loadedPlayerMap.setRecentlyViewed(new CustomMapCreatorMap());
            }

            CustomMapCreationSettings mapCreationSettings = (CustomMapCreationSettings) loadedPlayerMap.getRecentlyViewed().getMapCreationSettings();

            switch(currentSection) {
                case CATEGORIES -> {
                    Material icon = player.getItemOnCursor().getType().equals(Material.AIR) ? Material.PAPER : player.getItemOnCursor().getType();

                    if(isInRange) {
                        if(!new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORIES_NOTHING_FOUND).matches(displayName)) {
                            if(event.isShiftClick()) {
                                if(!icon.equals(Material.PAPER)) {
                                    player.setItemOnCursor(null);

                                    UpdateProperty updateProperty = UpdateProperty.of(MapCreatorPlugin.DataSource.NAME, displayName).value(MapCreatorPlugin.DataSource.ICON, icon.name());
                                    this.updateCategory(database, updateProperty, () -> {
                                        this.customMapCreatorInventory.open(player, CATEGORIES);
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
                                    });
                                }else {
                                    this.fetchUpdateInput(player, CATEGORIES, updatedCategoryName -> {
                                        updatedCategoryName = updatedCategoryName.toUpperCase();

                                        UpdateProperty updateProperty = UpdateProperty.of(MapCreatorPlugin.DataSource.NAME, displayName).value(MapCreatorPlugin.DataSource.NAME, updatedCategoryName);
                                        this.updateCategory(database, updateProperty, () -> {
                                            List<String> mapsToRename = this.customMapCreator.listMapsByCategory(displayName);
                                            String categoryName = updateProperty.getValue().toString();

                                            this.renameCategoryMaps(displayName, categoryName, mapsToRename, () -> {
                                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
                                                this.customMapCreatorInventory.open(player, CATEGORIES);
                                            });
                                        });
                                    });
                                }
                            }else {
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
                        }
                    }else {
                        if(new BukkitTranslation(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY).matches(displayName)) {
                            player.setItemOnCursor(null);
                            this.fetchInput(INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_INSTRUCTION, player, true, true, newCategory -> {
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

                                CustomMapCreatorMap loadedMap = (CustomMapCreatorMap) this.customMapCreator.getMapCreatorMap(recentlyViewedMap.getFileName());
                                if(loadedMap != null) {
                                    recentlyViewedMap.setSlimeWorld(loadedMap.getSlimeWorld());
                                }

                                if(event.isShiftClick()) {
                                    if(loadedMap == null) {
                                        this.fetchUpdateInput(player, CATEGORY_MAPS, updatedMapName -> {
                                            CustomMapCreatorMap renameTo = new CustomMapCreatorMap(updatedMapName, recentlyViewedMap.getMapCategory());
                                            recentlyViewedMap.setRenameTo(renameTo);

                                            this.handlePerformance(MapCreator.Action.RENAME, recentlyViewedMap, player);
                                        });
                                    }
                                }else {
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
                            }
                        }else {
                            if(new BukkitTranslation(INVENTORY_SECTION_CATEGORY_CREATE_MAP).matches(displayName)) {
                                List<String> availableTemplates = this.customMapCreator.listMapsByCategory(CustomMapTemplates.CATEGORY_TEMPLATES);

                                if(availableTemplates != null && !availableTemplates.isEmpty()) {
                                    this.customMapCreatorInventory.open(player, MapCreatorInventory.Section.MAP_TEMPLATES_CHOOSE_TEMPLATE);
                                    return;
                                }

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
                            this.fetchInput(INVENTORY_SECTION_CATEGORIES_DELETE_MAP_INSTRUCTION, player, true, true, mapInput -> {
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
                            String fileName = ChatColor.stripColor(displayName).replace("/", MapCreatorMap.CATEGORY_MAP_SEPARATOR);
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
                    CustomMapCreatorMap simpleTemplate = new CustomMapCreatorMap(MapTemplates.SIMPLE_TEMPLATE_NAME, MapTemplates.CATEGORY_TEMPLATES);

                    if(isInRange) {
                        CustomMapCreatorMap cloneFrom = new CustomMapCreatorMap();
                        cloneFrom.setMapCategory(MapTemplates.CATEGORY_TEMPLATES);

                        if(new BukkitTranslation(MapCreatorPlugin.Translations.INVENTORY_SECTION_CATEGORY_MAPS_TEMPLATES_EMPTY).matches(displayName)) {
                            this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_OVERVIEW);
                        }else if(displayName.equalsIgnoreCase(simpleTemplate.getPrettyName())) {
                            cloneFrom.setMapName(MapTemplates.SIMPLE_TEMPLATE_NAME);
                            recentlyViewed.setCloneFrom(cloneFrom);

                            this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_OVERVIEW);
                        }
                    }else {
                        if(new BukkitTranslation(BACK).matches(displayName)) {
                            this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                        }
                    }
                }
                case MAP_CREATION_SETTINGS_OVERVIEW -> {
                    if(isInRange) {
                        MapCreationSettings.Items currentItem = null;

                        for(MapCreationSettings.Items item : MapCreationSettings.Items.values()) {
                            if(new BukkitTranslation(item.getTranslationProperty()).matches(displayName)) {
                                currentItem = item;
                                break;
                            }
                        }

                        if(currentItem != null) {
                            switch(currentItem) {
                                case SPAWN -> {
                                    this.fetchInput(MAP_CREATION_SETTINGS_INSTRUCTION_SPAWN_SET, player, true, false, newSpawn -> {
                                        if(newSpawn != null) {
                                            mapCreationSettings.setSpawn(newSpawn);
                                        }

                                        this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_OVERVIEW);
                                    });
                                    return;
                                }
                                case DIFFICULTY -> {
                                    switch(mapCreationSettings.getDifficulty()) {
                                        case PEACEFUL -> mapCreationSettings.setDifficulty(DefaultDifficulty.EASY);
                                        case EASY -> mapCreationSettings.setDifficulty(DefaultDifficulty.NORMAL);
                                        case NORMAL -> mapCreationSettings.setDifficulty(DefaultDifficulty.HARD);
                                        case HARD -> mapCreationSettings.setDifficulty(DefaultDifficulty.PEACEFUL);
                                    }
                                }
                                case ALLOW_ANIMALS, ALLOW_MONSTERS, DRAGON_BATTLE, PVP -> {
                                    switch(currentItem) {
                                        case ALLOW_ANIMALS -> mapCreationSettings.setAllowAnimals(!mapCreationSettings.isAllowAnimals());
                                        case ALLOW_MONSTERS -> mapCreationSettings.setAllowMonsters(!mapCreationSettings.isAllowMonsters());
                                        case DRAGON_BATTLE -> mapCreationSettings.setDragonBattle(!mapCreationSettings.isDragonBattle());
                                        case PVP -> mapCreationSettings.setPvp(!mapCreationSettings.isPvp());
                                    }
                                }
                                case ENVIRONMENT -> {
                                    switch(mapCreationSettings.getEnvironment()) {
                                        case OVERWORLD -> mapCreationSettings.setEnvironment(DefaultWorldEnvironment.NETHER);
                                        case NETHER -> mapCreationSettings.setEnvironment(DefaultWorldEnvironment.THE_END);
                                        case THE_END -> mapCreationSettings.setEnvironment(DefaultWorldEnvironment.OVERWORLD);
                                    }
                                }
                                case WORLD_TYPE -> {
                                    switch(mapCreationSettings.getWorldType()) {
                                        case DEFAULT -> mapCreationSettings.setWorldType(DefaultWorldType.FLAT);
                                        case FLAT -> mapCreationSettings.setWorldType(DefaultWorldType.LARGE_BIOMES);
                                        case LARGE_BIOMES -> mapCreationSettings.setWorldType(DefaultWorldType.AMPLIFIED);
                                        case AMPLIFIED -> mapCreationSettings.setWorldType(DefaultWorldType.CUSTOMIZED);
                                        case CUSTOMIZED -> mapCreationSettings.setWorldType(DefaultWorldType.DEBUG_ALL_BLOCK_STATES);
                                        case DEBUG_ALL_BLOCK_STATES -> mapCreationSettings.setWorldType(DefaultWorldType.DEFAULT);
                                    }
                                }
                                case DEFAULT_BIOME -> {
                                    this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME);
                                    return;
                                }
                                case CONFIRM -> {
                                    this.handleMapCreation(player, MapCreator.Action.LOAD, loadedPlayerMap.getRecentlyViewed());
                                    return;
                                }
                            }

                            this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_OVERVIEW);
                        }
                    }else {
                        if(new BukkitTranslation(BACK).matches(displayName)) {
                            this.customMapCreatorInventory.open(player, CATEGORY_MAPS);
                        }
                    }
                }
                case MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME -> {
                    DefaultWorldEnvironment worldEnvironment = null;

                    for(DefaultWorldEnvironment environment : DefaultWorldEnvironment.values()) {
                        if(new BukkitTranslation(environment.getTranslationProperty()).matches(displayName)) {
                            worldEnvironment = environment;
                            break;
                        }
                    }

                    if(worldEnvironment != null) {
                        MapCreatorInventory.Section section = switch(worldEnvironment) {
                            case OVERWORLD -> MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_OVERWORLD;
                            case NETHER -> MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_NETHER;
                            case THE_END -> MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_THE_END;
                        };

                        this.customMapCreatorInventory.open(player, section);
                    }
                }
                case MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_OVERWORLD, MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_NETHER, MAP_CREATION_SETTINGS_CHOOSE_DEFAULT_BIOME_THE_END -> {
                    if(isInRange) {
                        DefaultBiome defaultBiome = DefaultBiome.of(displayName);

                        if(defaultBiome != null) {
                            mapCreationSettings.setDefaultBiome(defaultBiome);

                            this.customMapCreatorInventory.open(player, MAP_CREATION_SETTINGS_OVERVIEW);
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
            BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, player :: closeInventory);

            String mapName = action.equals(MapCreator.Action.CLONE) ? customMapCreatorMap.getCloneFrom().getPrettyName() : customMapCreatorMap.getPrettyName();
            String text = new BukkitTranslation(action.getLoadingActionBarActionTranslationProperty()).get(player, "$map$", mapName);

            StringBuilder message = new StringBuilder(" " + text);

            AtomicInteger atomicDotCounter = new AtomicInteger(0);

            scheduledFuture.set(ScheduledExecutor.scheduleWithFixedDelay(0, 375, TimeUnit.MILLISECONDS, () -> {
                int dotCounter = atomicDotCounter.get();

                message.append(".".repeat(Math.max(0, dotCounter)));

                ActionBar.send(player, new TextComponent(message.toString()));

                message.setLength(0);
                message.append(" ").append(text);

                if(dotCounter == 3) {
                    atomicDotCounter.set(0);
                }else {
                    atomicDotCounter.incrementAndGet();
                }
            }));

            if(action.equals(MapCreator.Action.LOAD)) {
                BukkitSynchronousExecutor.execute(this.mapCreatorPlugin, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 10, false, false, false)));
            }
        }

        this.customMapCreator.perform(action, customMapCreatorMap, performance -> {
            if(performance.isSuccess()) {
                if(performance.getSlimeWorld() != null) {
                    customMapCreatorMap.setLoadedBy(player.getName());

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

                ActionBar.send(player, new TextComponent(" "));

                if(action.equals(MapCreator.Action.LOAD)) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
        });
    }

    private void renameCategoryMaps(String displayName, String categoryName, List<String> mapsToRename, EmptyCallback emptyCallback) {
        for(String mapToRename : this.customMapCreator.listMapsByCategory(displayName)) {
            CustomMapCreatorMap customMapCreatorMap = new CustomMapCreatorMap(mapToRename);

            customMapCreatorMap.setRenameTo(new CustomMapCreatorMap(mapToRename).setMapCategory(categoryName));

            this.customMapCreator.perform(MapCreator.Action.RENAME, customMapCreatorMap, performance -> {
                mapsToRename.remove(mapToRename);

                if(mapsToRename.isEmpty()) {
                    emptyCallback.done();
                }
            });
        }
    }

    private void fetchUpdateInput(Player player, MapCreatorInventory.Section section, Callback<String> stringCallback) {
        String translationProperty = section.equals(CATEGORIES) ? INVENTORY_SECTION_CATEGORIES_ADD_CATEGORY_INSTRUCTION : INVENTORY_SECTION_CATEGORIES_CREATE_MAP_INSTRUCTION;

        this.fetchInput(translationProperty, player, true, true, updatedInput -> {
            if(updatedInput != null) {
                stringCallback.done(updatedInput);
            }else {
                this.customMapCreatorInventory.open(player, section);
            }
        });
    }

    private void updateCategory(Database database, UpdateProperty updateProperty, EmptyCallback emptyCallback) {
        database.setDataSourceProperty(MapCreatorPlugin.DataSource.PROPERTY);

        database.setUpdateProperty(updateProperty);
        database.update(success -> emptyCallback.done());
    }

    private void fetchInput(String translationProperty, Player player, boolean allowBlankInput, boolean applyRegex, Callback<String> stringCallback) {
        UserInputFetcher userInputFetcher = new UserInputFetcher(player);

        userInputFetcher.setMessagePrefix(MapCreatorPlugin.Strings.PREFIX);
        userInputFetcher.setMessageTranslationProperty(translationProperty);
        userInputFetcher.fetch(newMap -> {
            if(applyRegex && !newMap.matches("[A-Za-z0-9]+")) {
                stringCallback.done("");
                return;
            }

            if(!newMap.isBlank() && !newMap.isEmpty()) {
                if(applyRegex) newMap = newMap.replaceAll(" ", "");

                stringCallback.done(newMap);
            }else {
                if(allowBlankInput) {
                    stringCallback.done(null);
                }else {
                    this.fetchInput(translationProperty, player, false, applyRegex, stringCallback);
                }
            }
        });
    }

    private void handleMapCreation(Player player, MapCreator.Action action, CustomMapCreatorMap currentPlayerMap) {
        this.fetchInput(INVENTORY_SECTION_CATEGORIES_CREATE_MAP_INSTRUCTION, player, true, true, newMap -> {
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
