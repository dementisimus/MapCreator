package dev.dementisimus.mapcreator.creator.settings;

import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
import dev.dementisimus.capi.core.creators.InventoryCreator;
import dev.dementisimus.capi.core.creators.ItemCreator;
import dev.dementisimus.capi.core.helpers.Helper;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.api.settings.MapCreationSettings;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.biomes.overworld.DefaultOverworldBiome;
import dev.dementisimus.mapcreator.creator.api.settings.difficulty.DefaultDifficulty;
import dev.dementisimus.mapcreator.creator.api.settings.environment.DefaultWorldEnvironment;
import dev.dementisimus.mapcreator.creator.api.settings.worldtype.DefaultWorldType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreationSettings @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:20:06
 */
public class CustomMapCreationSettings implements MapCreationSettings {

    private final MapCreatorPlugin mapCreatorPlugin;

    @Getter
    @Setter
    private String spawn;
    @Getter
    @Setter
    private DefaultDifficulty difficulty;
    @Getter
    @Setter
    private boolean allowAnimals;
    @Getter
    @Setter
    private boolean allowMonsters;
    @Getter
    @Setter
    private boolean dragonBattle;
    @Getter
    @Setter
    private boolean pvp;
    @Getter
    @Setter
    private DefaultWorldEnvironment environment;
    @Getter
    @Setter
    private DefaultWorldType worldType;
    @Getter
    @Setter
    private DefaultBiome defaultBiome;

    public CustomMapCreationSettings(MapCreatorPlugin mapCreatorPlugin) {
        this.mapCreatorPlugin = mapCreatorPlugin;

        this.spawn = "0, 100, 0";
        this.difficulty = DefaultDifficulty.EASY;
        this.allowAnimals = false;
        this.allowMonsters = false;
        this.dragonBattle = false;
        this.pvp = true;
        this.environment = DefaultWorldEnvironment.OVERWORLD;
        this.worldType = DefaultWorldType.DEFAULT;
        this.defaultBiome = DefaultOverworldBiome.FOREST;
    }

    @Override
    public InventoryCreator createSettingsItems(Player player, InventoryCreator inventoryCreator) {

        for(Items item : Items.values()) {

            ItemCreator itemCreator = new ItemCreator(item.getIcon()).setDisplayName(player, item.getTranslationProperty()).addAllFlags();
            String data = switch(item) {
                case SPAWN -> this.spawn;
                case DIFFICULTY -> this.difficulty.translate(player);
                case ALLOW_ANIMALS -> Helper.getCheckmarksIdentifiedByBoolean(this.allowAnimals);
                case ALLOW_MONSTERS -> Helper.getCheckmarksIdentifiedByBoolean(this.allowMonsters);
                case DRAGON_BATTLE -> Helper.getCheckmarksIdentifiedByBoolean(this.dragonBattle);
                case PVP -> Helper.getCheckmarksIdentifiedByBoolean(this.pvp);
                case ENVIRONMENT -> this.environment.translate(player);
                case WORLD_TYPE -> this.worldType.translate(player);
                case DEFAULT_BIOME -> this.defaultBiome.translate(player);
                default -> null;
            };

            if(data != null) {
                itemCreator.addEmptyLore();
                itemCreator.addLore("§7§l» §7" + data);
                itemCreator.addEmptyLore();
            }

            inventoryCreator.setItem(item.getSlot(), itemCreator.apply());
        }

        return inventoryCreator;
    }

    @Override
    public WorldData toWorldData() {
        WorldData worldData = new WorldData();

        worldData.setDataSource(this.mapCreatorPlugin.getSlimeDataSource());
        worldData.setSpawn(this.spawn);
        worldData.setDifficulty(this.difficulty.name().toLowerCase());
        worldData.setAllowAnimals(this.allowAnimals);
        worldData.setAllowMonsters(this.allowMonsters);
        worldData.setDragonBattle(this.dragonBattle);
        worldData.setPvp(this.pvp);
        worldData.setEnvironment(this.environment.getId());
        worldData.setWorldType(this.worldType.getId());
        worldData.setDefaultBiome(this.defaultBiome.getBiome().getKey().toString());

        return worldData;
    }

    @Override
    public SlimePropertyMap toSlimePropertyMap() {
        return this.toWorldData().toPropertyMap();
    }
}
