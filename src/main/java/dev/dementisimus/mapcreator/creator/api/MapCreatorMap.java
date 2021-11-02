package dev.dementisimus.mapcreator.creator.api;

import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.api.settings.MapCreationSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreatorMap @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:21:52
 */
public interface MapCreatorMap {

    /**
     * The string used to separate a map's CATEGORY and NAME
     */
    String CATEGORY_MAP_SEPARATOR = "...";

    /**
     * Creates a new {@link MapCreatorMap} object for further use in the map creation process
     *
     * @param mapName     The map name {@link MapCreatorMap} object should represent
     * @param mapCategory The map category {@link MapCreatorMap} object should represent
     * @param readOnly    If the map will be treated as readOnly (if false, no other server is able to use this map after loading it on one server)
     */
    static MapCreatorMap of(String mapName, String mapCategory, boolean readOnly) {
        return new CustomMapCreatorMap(mapName, mapCategory, readOnly);
    }

    /**
     * Creates a new {@link MapCreatorMap} object for further use in the map creation process
     *
     * @param fullMap  The map full map name ('CATEGORY + {@link #CATEGORY_MAP_SEPARATOR} + MapName' {@link MapCreatorMap} object should represent
     * @param readOnly If the map will be treated as readOnly (if false, no other server is able to use this map after loading it on one server)
     */
    static MapCreatorMap of(String fullMap, boolean readOnly) {
        return new CustomMapCreatorMap(fullMap, readOnly);
    }

    /**
     * Checks if a map is locked in the data source
     *
     * @return true if isLocked, false otherwise
     */
    boolean isLocked();

    /**
     * Checks if a map exists in the data source
     *
     * @return true if isLocked, false otherwise
     */
    boolean exists();

    /**
     * Gets the full map name (CATEGORY + {@link #CATEGORY_MAP_SEPARATOR} + mapName)
     *
     * @return the full map name (CATEGORY + {@link #CATEGORY_MAP_SEPARATOR} + mapName)
     */
    String getFileName();

    /**
     * Gets the full map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     *
     * @return the full map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     */
    String getPrettyFileName();

    /**
     * Gets the full map name (CATEGORY + {@link #CATEGORY_MAP_SEPARATOR} + mapName)
     *
     * @return the full map name (CATEGORY + {@link #CATEGORY_MAP_SEPARATOR} + mapName)
     */
    String getFileName(String mapName);

    /**
     * Gets the full map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     *
     * @return the full map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     */
    String getPrettyFileName(String mapName);

    /**
     * Gets the full colored map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     *
     * @return the full colored map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     */
    String getPrettyName();

    /**
     * Gets the full colored map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     *
     * @return the full colored map name, but with a `/` instead of {@link #CATEGORY_MAP_SEPARATOR}
     */
    String getPrettyName(String mapName);

    /**
     * Gets the loader of a map
     * Note: the loader may be null, but the map could still be loaded (by API, for example)
     *
     * @return the loader of a map
     */
    @Nullable String getLoadedBy();

    /**
     * Gets the date the map has been loaded
     *
     * @return the date the map has been loaded
     */
    @Nullable Date getLoadedSince();

    /**
     * Checks if a map is read-only
     *
     * @return true if read-only, false otherwise
     */
    boolean isReadOnly();

    /**
     * Gets the settings for map creation
     *
     * @return {@link MapCreationSettings} map creation
     */
    MapCreationSettings getMapCreationSettings();

    /**
     * Teleports a player to the map
     *
     * @param player the player who will be teleported to the map
     */
    void teleportTo(Player player);

    /**
     * Teleports a player to a location
     *
     * @param player   the player who will be teleported to the map
     * @param location the location the player will be teleported to
     */
    void teleportTo(Player player, Location location);

    /**
     * Teleports a player to a location key in the current map
     *
     * @param player      the player who will be teleported to the map
     * @param locationKey the location key the player will be teleported to
     */
    void teleportTo(Player player, String locationKey);
}
