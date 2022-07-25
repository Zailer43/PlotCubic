package me.zailer.plotcubic.database;

import com.mojang.logging.LogUtils;
import me.zailer.plotcubic.config.Config;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SQLDatabase database;
    private final MinecraftServer server;

    // TODO: Handle other database types, for now it only supports MySQL.
    public DatabaseManager(Config config, MinecraftServer server) {
        this.server = server;
        // Useless tip since no one will do this: Never use these credentials for ANY public minecraft server.
        DatabaseType type = DatabaseType.getEnum(config.database().type());

        switch (type) {
            default:
                this.database = new MariaDB(config.database().host(),
                        config.database().port(), config.database().database(),
                        config.database().user(), config.database().password());
                break;
        }

        testDatabase(type.name());
        initialization();
    }

    private void testDatabase(String db) {
        LOGGER.info("[PlotCubic] Using database " + db + ".");
        LOGGER.info("[PlotCubic] Connecting to the database...");

        try (Connection connection = this.database.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT 1;");

            if (rs.next()) {
                LOGGER.info("[PlotCubic] Connection established successfully!");
                return;
            }

            LOGGER.info("[PlotCubic] Something went wrong when connecting to the databse...");
        } catch (SQLException e) {
            LOGGER.error("[PlotCubic] An error occured when connecting to the databse.", e);
            server.shutdown();
        }
    }

    public SQLDatabase getDatabase() {
        return this.database;
    }


    private void initialization() {
        try (Connection conn = database.getConnection()) {

            try {
                // Janky ass solution to prevent table alteration after table creation.
                // TODO: This is a temporary solution, don't leave this as is.

                ResultSet rs = conn.prepareStatement("SHOW TABLES;").executeQuery();
                if (rs.next()) return;

                conn.setAutoCommit(false);
                conn.prepareStatement("START TRANSACTION").execute();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `denied` (
                                  `plot_id_x` int(11) NOT NULL,
                                  `plot_id_z` int(11) NOT NULL,
                                  `denied_username` varchar(16) NOT NULL,
                                  `reason` varchar(64) DEFAULT NULL
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `plots` (
                                  `id_x` int(11) NOT NULL,
                                  `id_z` int(11) NOT NULL,
                                  `greeting` varchar(1024) DEFAULT NULL,
                                  `farewall` varchar(1024) DEFAULT NULL,
                                  `biome` varchar(32) DEFAULT NULL,
                                  `music` varchar(32) DEFAULT NULL,
                                  `team` varchar(32) DEFAULT NULL,
                                  `owner_username` varchar(32) NOT NULL,
                                  `gamemode_id` varchar(24) DEFAULT NULL,
                                  `date_claimed` timestamp NOT NULL,
                                  `chat_style_id` varchar(32) DEFAULT NULL
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `reportreasons` (
                                  `reason_id` varchar(24) NOT NULL,
                                  `report_id` bigint(20) NOT NULL
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `reports` (
                                  `id` bigint(20) NOT NULL,
                                  `plot_id_x` int(11) NOT NULL,
                                  `plot_id_z` int(11) NOT NULL,
                                  `reporting_user` varchar(16) NOT NULL,
                                  `admin_username` varchar(16) DEFAULT NULL,
                                  `date_reported` timestamp DEFAULT CURRENT_TIMESTAMP,
                                  `date_moderated` timestamp DEFAULT CURRENT_TIMESTAMP,
                                  `is_moderated` boolean DEFAULT 0
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `trusted` (
                                  `plot_id_x` int(11) NOT NULL,
                                  `plot_id_z` int(11) NOT NULL,
                                  `trusted_username` varchar(16) NOT NULL,
                                  `permission_id` varchar(32) NOT NULL
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                CREATE TABLE IF NOT EXISTS `users` (
                                  `username` varchar(16) NOT NULL,
                                  `plot_chat_enabled` boolean NOT NULL
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `denied`
                                  ADD PRIMARY KEY (`plot_id_x`,`plot_id_z`,`denied_username`),
                                  ADD KEY `fk_denied_users` (`denied_username`),
                                  ADD KEY `fk_denied_plots` (`plot_id_x`, `plot_id_z`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `plots`
                                  ADD PRIMARY KEY (`id_x`,`id_z`),
                                  ADD KEY `fk_plots_users` (`owner_username`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `reportreasons`
                                  ADD PRIMARY KEY (`reason_id`, `report_id`),
                                  ADD KEY `fk_reportreasons_reports` (`report_id`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `reports`
                                  ADD PRIMARY KEY (`id`, `plot_id_x`, `plot_id_z`),
                                  ADD KEY `fk_reported_plot` (`plot_id_x`,`plot_id_z`),
                                  ADD KEY `fk_reporting_user` (`reporting_user`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `trusted`
                                  ADD PRIMARY KEY (`plot_id_x`,`plot_id_z`,`trusted_username`, `permission_id`),
                                  ADD KEY `fk_trusted_users` (`trusted_username`),
                                  ADD KEY `fk_trusted_plots` (`plot_id_x`, `plot_id_z`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `users`
                                  ADD PRIMARY KEY (`username`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `reports`
                                  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `denied`
                                  ADD CONSTRAINT `fk_denied_users` FOREIGN KEY (`denied_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
                                  ADD CONSTRAINT `fk_denied_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE;
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `plots`
                                  ADD CONSTRAINT `fk_plots_users` FOREIGN KEY (`owner_username`) REFERENCES `users` (`username`);
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `reportreasons`
                                  ADD CONSTRAINT `fk_reportreasons_reports` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `reports`
                                  ADD CONSTRAINT `fk_reported_plot` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE,
                                  ADD CONSTRAINT `fk_reporting_user` FOREIGN KEY (`reporting_user`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
                        """).executeUpdate();

                conn.prepareStatement("""
                                ALTER TABLE `trusted`
                                  ADD CONSTRAINT `fk_trusted_users` FOREIGN KEY (`trusted_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
                                  ADD CONSTRAINT `fk_trusted_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE
                        """).executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                this.server.shutdown();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            this.server.shutdown();
        }
    }
}
