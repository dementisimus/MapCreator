package dev.dementisimus.mapcreator.creator;

import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.api.MapCreationSettings;
import dev.dementisimus.mapcreator.creator.api.settings.DefaultBiome;
import dev.dementisimus.mapcreator.creator.api.settings.DefaultWorldEnvironment;
import dev.dementisimus.mapcreator.creator.api.settings.DefaultWorldType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Difficulty;
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
    private Difficulty difficulty;
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
        this.difficulty = Difficulty.EASY;
        this.allowAnimals = false;
        this.allowMonsters = false;
        this.dragonBattle = false;
        this.pvp = true;
        this.environment = DefaultWorldEnvironment.OVERWORLD;
        this.worldType = DefaultWorldType.DEFAULT;
        this.defaultBiome = DefaultBiome.FOREST;
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
