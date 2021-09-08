package dev.dementisimus.mapcreator.creator.aswm;

import com.google.common.reflect.TypeToken;
import com.grinderwolf.swm.internal.ninja.leaping.configurate.objectmapping.Setting;
import com.grinderwolf.swm.internal.ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import com.grinderwolf.swm.internal.ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import dev.dementisimus.capi.core.database.Database;
import dev.dementisimus.capi.core.database.interfaces.IDatabase;
import dev.dementisimus.capi.core.language.Translation;
import dev.dementisimus.capi.core.logger.CoreAPILogger;
import dev.dementisimus.capi.core.setup.MainSetupStates;
import dev.dementisimus.capi.core.setup.SetupManager;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import lombok.Getter;
import lombok.Setter;
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
        Sources sources = new Sources();
        IDatabase.Type type = this.database.getType();
        boolean supportedDatabase = true;

        if(type.equals(IDatabase.Type.MONGODB)) {
            Sources.MongoDBConfig mongoDBConfig = new Sources.MongoDBConfig();

            mongoDBConfig.setEnabled(true);
            mongoDBConfig.setUri(this.setupManager.getSetupState(MainSetupStates.MONGODB_URI).getString());
            mongoDBConfig.setDatabase(this.setupManager.getSetupState(MainSetupStates.DATABASE).getString());

            sources.setMongoDbConfig(mongoDBConfig);
        }else if(type.equals(IDatabase.Type.MARIADB)) {
            Sources.MysqlConfig mysqlConfig = new Sources.MysqlConfig();

            mysqlConfig.setEnabled(true);
            mysqlConfig.setHost(this.setupManager.getSetupState(MainSetupStates.MARIADB_HOST).getString());
            mysqlConfig.setPort(this.setupManager.getSetupState(MainSetupStates.MARIADB_PORT).getInteger());
            mysqlConfig.setUsername(this.setupManager.getSetupState(MainSetupStates.MARIADB_USER).getString());
            mysqlConfig.setPassword(this.setupManager.getSetupState(MainSetupStates.MARIADB_PASSWORD).getString());
            mysqlConfig.setDatabase(this.setupManager.getSetupState(MainSetupStates.DATABASE).getString());

            sources.setMysqlConfig(mysqlConfig);
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

        this.dataSourceLoader.load().setValue(TypeToken.of(Sources.class), sources);
        this.dataSourceLoader.save(this.dataSourceLoader.createEmptyNode().setValue(TypeToken.of(Sources.class), sources));

        if(supportedDatabase) {
            CoreAPILogger.info(new Translation(MapCreatorPlugin.Translations.ASWM_INIT_CONFIG_DONE).get("$prefix$", MapCreatorPlugin.Strings.PREFIX, true));
        }
    }

    /*
     * Copyright (c) by GitHub.com/Paul19988/Advanced-Slime-World-Manager & Team,
     * modified by GitHub.com/dementisimus
     * */
    @Getter
    @ConfigSerializable
    public static class Sources {

        @Setting("file")
        @Setter
        private FileConfig fileConfig = new FileConfig();
        @Setting("mysql")
        @Setter
        private MysqlConfig mysqlConfig = new MysqlConfig();
        @Setting("mongodb")
        @Setter
        private MongoDBConfig mongoDbConfig = new MongoDBConfig();
        @Setting("redis")
        @Setter
        private RedisConfig redisConfig = new RedisConfig();

        @Getter
        @ConfigSerializable
        public static class MysqlConfig {

            @Setting("enabled")
            @Setter
            private boolean enabled = false;

            @Setting("host")
            @Setter
            private String host = "127.0.0.1";
            @Setting("port")
            @Setter
            private int port = 3306;

            @Setting("username")
            @Setter
            private String username = "slimeworldmanager";
            @Setting("password")
            @Setter
            private String password = "";

            @Setting("database")
            @Setter
            private String database = "slimeworldmanager";

            @Setting("usessl")
            @Setter
            private boolean usessl = false;

            @Setting("sqlUrl")
            @Setter
            private String sqlUrl = "jdbc:mysql://{host}:{port}/{database}?autoReconnect=true&allowMultiQueries=true&useSSL={usessl}";
        }

        @Getter
        @ConfigSerializable
        public static class MongoDBConfig {

            @Setting("enabled")
            @Setter
            private boolean enabled = false;

            @Setting("host")
            @Setter
            private String host = "127.0.0.1";
            @Setting("port")
            @Setter
            private int port = 27017;

            @Setting("auth")
            @Setter
            private String authSource = "admin";
            @Setting("username")
            @Setter
            private String username = "slimeworldmanager";
            @Setting("password")
            @Setter
            private String password = "";

            @Setting("database")
            @Setter
            private String database = "slimeworldmanager";
            @Setting("collection")
            @Setter
            private String collection = "worlds";

            @Setting("uri")
            @Setter
            private String uri = "";
        }

        @Getter
        @ConfigSerializable
        public static class FileConfig {

            @Setting("path")
            @Setter
            private String path = "slime_worlds";
        }

        @Getter
        @ConfigSerializable
        public static class RedisConfig {

            @Setting("enabled")
            @Setter
            private boolean enabled = false;
            @Setting("uri")
            @Setter
            private String uri = "redis://127.0.0.1/";
        }
    }
}
