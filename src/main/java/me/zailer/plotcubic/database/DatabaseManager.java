package me.zailer.plotcubic.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.database.type.Database;
import me.zailer.plotcubic.database.type.SQLDatabase;
import net.fabricmc.loader.api.FabricLoader;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlotCubic Database");
    private final SQLDatabase database;

    // TODO: Handle other database types, for now it only supports H2.
    public DatabaseManager(Config.Database config) {
        // Useless tip since no one will do this: Never use these credentials for ANY public minecraft server.
        DatabaseType type = DatabaseType.getEnum(config.type());

        LOGGER.info("Starting database (" + type.getName() + ")");

        HikariDataSource hikariDataSource = this.getDataSource(config, type);

        LOGGER.info("Migrating database");
        Flyway flyway = Flyway.configure().dataSource(hikariDataSource).load();

        try {
            flyway.migrate();
        } catch (Exception e) {
            flyway.repair();
            throw e;
        }

//        switch (type) {
//            default:
        this.database = new Database(hikariDataSource);
//                break;
//        }
    }

    public SQLDatabase getDatabase() {
        return this.database;
    }

    public HikariDataSource getDataSource(Config.Database config, DatabaseType type) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(type.getDriverClassName());
        hikariConfig.setJdbcUrl(this.getJdbcUrl(type) + config.database());
        hikariConfig.setUsername(config.user());
        hikariConfig.setPassword(config.password());

        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("plotcubicHikariCP");

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        return new HikariDataSource(hikariConfig);
    }
    public String getJdbcUrl(DatabaseType type) {
        return "jdbc:" + type.getName() + ":" + FabricLoader.getInstance().getConfigDir() + "\\";
    }
}
