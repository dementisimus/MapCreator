package dev.dementisimus.mapcreator.creator.api.settings;

import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
import dev.dementisimus.capi.core.creators.InventoryCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapCreationSettings @ MapCreator
 *
 * @author dementisimus
 * @since 01.11.2021:20:06
 */
public interface MapCreationSettings {

    InventoryCreator createSettingsItems(Player player, InventoryCreator inventoryCreator);

    WorldData toWorldData();

    SlimePropertyMap toSlimePropertyMap();

    Document toDocument(String fileName);

    MapCreationSettings fromDocument(Document document);

    @AllArgsConstructor
    enum Items {

        SPAWN("map.creation.settings.items.spawn", Material.SUNFLOWER, 10),
        DIFFICULTY("map.creation.settings.items.difficulty", Material.DIAMOND, 11),
        ALLOW_ANIMALS("map.creation.settings.items.allow.animals", Material.FEATHER, 12),
        ALLOW_MONSTERS("map.creation.settings.items.allow.monsters", Material.ROTTEN_FLESH, 13),
        DRAGON_BATTLE("map.creation.settings.items.dragon.battle", Material.DRAGON_BREATH, 14),
        PVP("map.creation.settings.items.pvp", Material.CROSSBOW, 15),
        ENVIRONMENT("map.creation.settings.items.environment", Material.GRASS, 16),
        WORLD_TYPE("map.creation.settings.items.world.type", Material.GRASS_BLOCK, 21),
        DEFAULT_BIOME("map.creation.settings.items.default.biome", Material.HONEYCOMB, 23),
        CONFIRM("map.creation.settings.items.confirm", Material.GREEN_DYE, 41);

        @Getter private final String translationProperty;
        @Getter private final Material icon;
        @Getter private final int slot;
    }
}
