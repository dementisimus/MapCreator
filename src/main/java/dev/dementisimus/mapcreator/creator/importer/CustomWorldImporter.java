package dev.dementisimus.mapcreator.creator.importer;

import com.google.common.base.CharMatcher;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.importer.interfaces.WorldImporter;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreatorMap;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomWorldImporter @ MapCreator
 *
 * @author dementisimus
 * @since 31.08.2021:22:38
 */
public class CustomWorldImporter implements WorldImporter {

    @Getter private final MapCreatorPlugin mapCreatorPlugin;
    @Getter private final SlimeLoader slimeLoader;
    @Getter private final CustomMapCreator customMapCreator;
    @Getter private final CustomMapCreatorInventory customMapCreatorInventory;
    @Getter private final SetupManager setupManager;
    @Getter private final boolean importerRequired;
    @Getter private final File worldImportFolder;
    @Getter private final List<CustomMapCreatorMap> importableWorlds = new ArrayList<>();

    public CustomWorldImporter(MapCreatorPlugin mapCreatorPlugin) {
        this.mapCreatorPlugin = mapCreatorPlugin;
        this.slimeLoader = mapCreatorPlugin.getSlimeLoader();
        this.customMapCreator = mapCreatorPlugin.getCustomMapCreator();
        this.customMapCreatorInventory = this.customMapCreator.getCustomMapCreatorInventory();
        this.setupManager = mapCreatorPlugin.getSetupManager();

        this.importerRequired = this.setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.WORLD_IMPORTER_REQUIRED).getBoolean();
        this.worldImportFolder = this.setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.WORLD_IMPORTER_FOLDER_LOCATION).getFile();
    }

    @Override
    public void scanForImportableWorlds() {
        if(!this.importerRequired) return;

        File[] fileList = this.worldImportFolder.listFiles();

        if(fileList != null) {
            this.importableWorlds.clear();

            for(File listCategory : fileList) {
                if(listCategory.isDirectory() && CharMatcher.javaUpperCase().matchesAllOf(listCategory.getName())) {
                    File[] worldsInCategory = listCategory.listFiles();
                    if(worldsInCategory != null) {
                        for(File listWorld : worldsInCategory) {
                            File[] worldFiles = listWorld.listFiles();
                            if(listWorld.isDirectory() && worldFiles != null && !Arrays.asList(worldFiles).isEmpty()) {
                                String categoryName = listCategory.getName();
                                String worldName = listWorld.getName();
                                String fullWorldName = categoryName + MapCreatorMap.CATEGORY_MAP_SEPARATOR + worldName;
                                try {
                                    if(!this.slimeLoader.worldExists(fullWorldName)) {
                                        CustomMapCreatorMap customMapCreatorMap = new CustomMapCreatorMap(worldName, categoryName);
                                        customMapCreatorMap.setImportableWorldFile(listWorld);
                                        this.importableWorlds.add(customMapCreatorMap);
                                    }
                                }catch(IOException ignored) {}
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<CustomMapCreatorMap> getImportableWorldsByCategory(String categoryName) {
        List<CustomMapCreatorMap> importableWorldsByCategory = new ArrayList<>();

        for(CustomMapCreatorMap importableWorld : this.importableWorlds) {
            if(importableWorld.getMapCategory().equalsIgnoreCase(categoryName)) {
                importableWorldsByCategory.add(importableWorld);
            }
        }

        return importableWorldsByCategory;
    }

    @Override
    public @Nullable CustomMapCreatorMap getImportableWorldByFileName(String fileName) {
        CustomMapCreatorMap importableWorldByPrettyFileName = null;

        for(CustomMapCreatorMap importableWorld : this.importableWorlds) {
            if(importableWorld.getFileName().equalsIgnoreCase(fileName)) {
                importableWorldByPrettyFileName = importableWorld;
            }
        }

        return importableWorldByPrettyFileName;
    }
}
