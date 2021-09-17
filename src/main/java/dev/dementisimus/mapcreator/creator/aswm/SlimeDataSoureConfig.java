package dev.dementisimus.mapcreator.creator.aswm;

import com.google.common.reflect.TypeToken;
import com.grinderwolf.swm.internal.ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import com.grinderwolf.swm.plugin.config.DatasourcesConfig;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.interfaces.IDatabase;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.logger.CoreAPILogger;
import dev.dementisimus.capi.core.setup.MainSetupStates;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class SlimeDataSoureConfig @ MapCreator
 *
 * @author dementisimus
 * @since 08.09.2021:09:58
 */
public class SlimeDataSoureConfig {

    @Getter private final File sourcesFile = new File(new File("plugins", "SlimeWorldManager"), "sources.yml");
    @Getter private final YAMLConfigurationLoader dataSourceLoader = YAMLConfigurationLoader.builder().setFlowStyle(DumperOptions.FlowStyle.BLOCK).setPath(this.sourcesFile.toPath()).build();
    @Getter private final Database database;
    @Getter private final SetupManager setupManager;

    public SlimeDataSoureConfig(Database database, SetupManager setupManager) {
        this.database = database;
        this.setupManager = setupManager;
    }

    @SneakyThrows
    public void modify() {
        DatasourcesConfig datasourcesConfig = new DatasourcesConfig();

        IDatabase.Type type = this.database.getType();
        boolean supportedDatabase = true;

        if(type.equals(IDatabase.Type.MONGODB)) {
            DatasourcesConfig.MongoDBConfig mongoDBConfig = new DatasourcesConfig.MongoDBConfig();

            mongoDBConfig.setEnabled(true);
            mongoDBConfig.setUri(this.setupManager.getSetupState(MainSetupStates.MONGODB_URI).getString());
            mongoDBConfig.setDatabase(this.setupManager.getSetupState(MainSetupStates.DATABASE).getString());

            datasourcesConfig.setMongoDbConfig(mongoDBConfig);
        }else if(type.equals(IDatabase.Type.MARIADB)) {
            DatasourcesConfig.MysqlConfig mysqlConfig = new DatasourcesConfig.MysqlConfig();

            mysqlConfig.setEnabled(true);
            mysqlConfig.setHost(this.setupManager.getSetupState(MainSetupStates.MARIADB_HOST).getString());
            mysqlConfig.setPort(this.setupManager.getSetupState(MainSetupStates.MARIADB_PORT).getInteger());
            mysqlConfig.setUsername(this.setupManager.getSetupState(MainSetupStates.MARIADB_USER).getString());
            mysqlConfig.setPassword(this.setupManager.getSetupState(MainSetupStates.MARIADB_PASSWORD).getString());
            mysqlConfig.setDatabase(this.setupManager.getSetupState(MainSetupStates.DATABASE).getString());

            datasourcesConfig.setMysqlConfig(mysqlConfig);
        }else {
            CoreAPILogger.error(new Translation(MapCreatorPlugin.Translations.ASWM_INIT_UNSUPPORTED_DATABASE).get(new String[]{
                    "$prefix$", "$database$"
            }, new String[]{MapCreatorPlugin.Strings.PREFIX, type.name()}, true));
            supportedDatabase = false;
        }

        if(!this.sourcesFile.getParentFile().exists()) {
            this.sourcesFile.getParentFile().mkdirs();
        }

        if(!this.sourcesFile.exists()) {
            this.sourcesFile.createNewFile();
        }

        this.dataSourceLoader.save(this.dataSourceLoader.createEmptyNode().setValue(TypeToken.of(DatasourcesConfig.class), datasourcesConfig));

        if(supportedDatabase) {
            CoreAPILogger.info(new Translation(MapCreatorPlugin.Translations.ASWM_INIT_CONFIG_DONE).get("$prefix$", MapCreatorPlugin.Strings.PREFIX, true));
        }
    }
}
