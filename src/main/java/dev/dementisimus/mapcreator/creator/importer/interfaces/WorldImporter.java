package dev.dementisimus.mapcreator.creator.importer.interfaces;

import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class WorldImporter @ MapCreator
 *
 * @author dementisimus
 * @since 31.08.2021:22:39
 */
public interface WorldImporter {

    void scanForImportableWorlds();

    List<CustomMapCreatorMap> getImportableWorldsByCategory(String categoryName);

    @Nullable CustomMapCreatorMap getImportableWorldByFileName(String fileName);

}
