package dev.dementisimus.mapcreator.creator;

import dev.dementisimus.capi.core.database.interfaces.IDatabase;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SlimeDataSource @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:22:57
 */
public class SlimeDataSource {

    public static final String MONGODB = "mongodb";
    public static final String MYSQL = "mysql";
    public static final String REDIS = "redis";
    public static final String FILE = "file";

    public static String of(String dataSource) {
        String slimeDataSource = null;

        if(dataSource.equalsIgnoreCase(IDatabase.Type.MARIADB.name())) {
            slimeDataSource = MYSQL;
        }else if(dataSource.equalsIgnoreCase(IDatabase.Type.MONGODB.name())) {
            slimeDataSource = MONGODB;
        }

        return slimeDataSource;
    }

}
