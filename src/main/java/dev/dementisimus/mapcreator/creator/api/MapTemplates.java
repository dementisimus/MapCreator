package dev.dementisimus.mapcreator.creator.api;

import java.io.File;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class MapTemplates @ MapCreator
 *
 * @author dementisimus
 * @since 01.09.2021:11:48
 */
public interface MapTemplates {

    String SIMPLE_TEMPLATE_NAME = "simple";
    String SIMPLE_TEMPLATE_NAME_ZIP = SIMPLE_TEMPLATE_NAME + ".zip";

    String SIMPLE_TEMPLATE_URL = "https://dementisimus.dev/files/MapCreator/templates/" + SIMPLE_TEMPLATE_NAME + ".zip";

    String CATEGORY_TEMPLATES = "TEMPLATES";

    void downloadSimpleTemplate();

    File getTemporaryTemplatesFolder();

    File getTemporaryTemplate(String templateName);

}
