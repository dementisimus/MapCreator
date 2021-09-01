package dev.dementisimus.mapcreator.creator.templates;

import dev.dementisimus.capi.core.dependencies.FileDownloader;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.interfaces.MapCreator;
import dev.dementisimus.mapcreator.creator.templates.interfaces.MapTemplates;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapTemplates @ MapCreator
 *
 * @author dementisimus
 * @since 01.09.2021:11:48
 */
public class CustomMapTemplates implements MapTemplates {

    @Getter private final MapCreatorPlugin mapCreatorPlugin;
    @Getter private final FileDownloader fileDownloader;
    @Getter private final CustomMapCreator customMapCreator;
    @Getter private final SetupManager setupManager;

    public CustomMapTemplates(MapCreatorPlugin mapCreatorPlugin) {
        this.mapCreatorPlugin = mapCreatorPlugin;
        this.fileDownloader = mapCreatorPlugin.getCoreAPI().getFileDownloader();
        this.customMapCreator = mapCreatorPlugin.getCustomMapCreator();
        this.setupManager = mapCreatorPlugin.getSetupManager();
    }

    @Override
    public void downloadSimpleTemplate() {
        if(!this.setupManager.getSetupState(MapCreatorPlugin.ExtraSetupStates.SIMPLE_TEMPLATE_MAP_WANTED).getBoolean()) return;

        CustomMapCreatorMap importableTemplate = new CustomMapCreatorMap();
        importableTemplate.setMapCategory(CATEGORY_TEMPLATES);
        importableTemplate.setMapName(SIMPLE_TEMPLATE_NAME);

        if(!importableTemplate.exists()) {
            this.fileDownloader.setRequirements(this.mapCreatorPlugin.getName());
            this.fileDownloader.downloadZipFile(MapTemplates.SIMPLE_TEMPLATE_URL, this.getTemporaryTemplate(SIMPLE_TEMPLATE_NAME_ZIP), this.getTemporaryTemplatesFolder(), success -> {
                if(success) {
                    importableTemplate.setImportableWorldFile(this.getTemporaryTemplate(SIMPLE_TEMPLATE_NAME));

                    this.customMapCreator.perform(MapCreator.Action.IMPORT, importableTemplate, performance -> {
                        if(performance.isSuccess()) {
                            this.mapCreatorPlugin.getLogger().log(Level.INFO, new Translation(MapCreatorPlugin.Translations.TEMPLATES_IMPORT_SUCCESS).get("$template$", SIMPLE_TEMPLATE_NAME, true));
                            try {
                                FileUtils.deleteDirectory(importableTemplate.getImportableWorldFile());
                            }catch(IOException ignored) {}
                        }else {
                            performance.announceFailure();
                        }
                    });
                }else {
                    this.mapCreatorPlugin.getLogger().log(Level.WARNING, new Translation(MapCreatorPlugin.Translations.TEMPLATES_DOWNLOAD_FAILURE).get("$template$", SIMPLE_TEMPLATE_NAME, true));
                }
            });
        }
    }

    @Override
    public File getTemporaryTemplatesFolder() {
        File temporaryTemplates = new File(this.mapCreatorPlugin.getDataFolder(), "temporaryTemplates");
        if(!temporaryTemplates.exists()) {
            temporaryTemplates.mkdirs();
        }
        return temporaryTemplates;
    }

    @Override
    public File getTemporaryTemplate(String templateName) {
        return new File(this.getTemporaryTemplatesFolder(), templateName);
    }
}
