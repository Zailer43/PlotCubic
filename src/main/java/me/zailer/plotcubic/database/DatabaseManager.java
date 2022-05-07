package me.zailer.plotcubic.database;

import com.mojang.logging.LogUtils;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.enums.PlotPermission;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.*;


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
                this.database = new MySQL(config.database().host(),
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


    public boolean claimPlot(int plot_id_x, int plot_id_z, String username) {
        try (Connection conn = database.getConnection()) {
            if (!this.isUnclaimed(plot_id_x, plot_id_z))
                return false;


            PreparedStatement statement = conn.prepareStatement("INSERT INTO `plots` (`id_x`, `id_z`, `greeting`, `farewall`, `biome`, `music`, `team`, `owner_username`, `gamemode`, `date_claimed`) VALUES (?, ?, NULL, NULL, NULL, NULL, NULL, ?, NULL, ?);");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);
            statement.setString(3, username);

            Calendar calendar = Calendar.getInstance();
            Timestamp time = new Timestamp(calendar.getTimeInMillis());
            statement.setTimestamp(4, time);

            statement.execute();

            return true;

        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Nullable
    public List<Plot> getAllPlots(String username, boolean order) {
        List<Plot> plots = new ArrayList<>();

        try (Connection conn = database.getConnection()) {
            PreparedStatement statement;

            if (order)
                statement = conn.prepareStatement("SELECT * FROM `plots` WHERE `owner_username` LIKE ? ORDER BY `date_claimed` ASC;");
            else
                statement = conn.prepareStatement("SELECT * FROM `plots` WHERE `owner_username` LIKE ?;");

            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                int plotIdX = rs.getInt("id_x");
                int plotIdZ = rs.getInt("id_z");
                plots.add(new Plot(rs.getString("owner_username"), new PlotID(plotIdX, plotIdZ)));
            }

            return plots;
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Nullable
    public List<TrustedPlayer> getAllTrusted(PlotID plotId) {
        return this.getAllTrusted(plotId.x(), plotId.z());
    }
    @Nullable
    public List<TrustedPlayer> getAllTrusted(int plot_id_x, int plot_id_z) {
        List<TrustedPlayer> trustedList = new ArrayList<>();
        HashMap<String, Set<PlotPermission>> playersPermissionsHashMap = new HashMap<>();

        try (Connection conn = database.getConnection()) {
            PreparedStatement statement;

            statement = conn.prepareStatement("SELECT * FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ?;");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);

            ResultSet rs = statement.executeQuery();
            PlotPermission[] plotPermissions = PlotPermission.values();

            while(rs.next()) {
                String username = rs.getString("trusted_username");
                PlotPermission permission = plotPermissions[rs.getInt("permission_index")];
                if (playersPermissionsHashMap.containsKey(username)) {
                    playersPermissionsHashMap.get(username).add(permission);
                } else {
                    playersPermissionsHashMap.put(username, new HashSet<>());
                    playersPermissionsHashMap.get(username).add(permission);
                }
            }

            for (var username : playersPermissionsHashMap.keySet())
                trustedList.add(new TrustedPlayer(username, playersPermissionsHashMap.get(username), new PlotID(plot_id_x, plot_id_z)));

            return trustedList;
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Nullable
    public TrustedPlayer getTrusted(PlotID plotId, String username) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(username, new HashSet<>(), plotId);

        try (Connection conn = database.getConnection()) {
            PreparedStatement statement;

            statement = conn.prepareStatement("SELECT `permission_index` FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `trusted_username` LIKE ?;");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, username);

            ResultSet rs = statement.executeQuery();
            PlotPermission[] plotPermissions = PlotPermission.values();

            while(rs.next()) {
                trustedPlayer.addPermission(plotPermissions[rs.getInt("permission_index")]);
            }

            return trustedPlayer;
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public boolean updateTrusted(TrustedPlayer trustedPlayer) {
        try (Connection conn = database.getConnection()) {

            int plot_id_x = trustedPlayer.plotId().x();
            int plot_id_z = trustedPlayer.plotId().z();
            String trusted_username = trustedPlayer.username();

            PreparedStatement deleteStatement;

            deleteStatement = conn.prepareStatement("DELETE FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `trusted_username` = ?;");

            deleteStatement.setInt(1, plot_id_x);
            deleteStatement.setInt(2, plot_id_z);
            deleteStatement.setString(3, trusted_username);

            deleteStatement.execute();

            for (var permission : trustedPlayer.permissions()) {
                PreparedStatement statement;

                statement = conn.prepareStatement("INSERT INTO `trusted`(`plot_id_x`, `plot_id_z`, `trusted_username`, `permission_index`) VALUES (?, ?, ?, ?);");

                statement.setInt(1, plot_id_x);
                statement.setInt(2, plot_id_z);
                statement.setString(3, trusted_username);
                statement.setInt(4, permission.ordinal());

                statement.execute();
            }

            return true;
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Nullable
    public Plot getPlot(PlotID plotID) {
        return this.getPlot(plotID.x(), plotID.z());
    }

    @Nullable
    public Plot getPlot(int plot_id_x, int plot_id_z) {
        try (Connection conn = database.getConnection()) {
            if (this.isUnclaimed(plot_id_x, plot_id_z))
                return null;

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);

            ResultSet rs = statement.executeQuery();
            rs.next();

            String username = rs.getString("owner_username");
            Plot plot = new Plot(username, new PlotID(plot_id_x, plot_id_z));

            plot.addTrusted(this.getAllTrusted(plot_id_x, plot_id_z));
            plot.addDenied(this.getDenied(plot_id_x, plot_id_z));
            plot.setClaimedDate(rs.getDate("date_claimed"));
            return plot;

        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public void deletePlot(PlotID plotID) {
        this.deletePlot(plotID.x(), plotID.z());
    }

    public void deletePlot(int plot_id_x, int plot_id_z) {
        try (Connection conn = database.getConnection()) {
            if (this.isUnclaimed(plot_id_x, plot_id_z))
                return;

            PreparedStatement statement = conn.prepareStatement("DELETE FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);

            statement.execute();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public boolean isUnclaimed(int plot_id_x, int plot_id_z) {
        try (Connection conn = database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) return false;
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
        return true;
    }

    public boolean newUser(String username) {
        try (Connection conn = database.getConnection()) {
            if (this.existPlayer(username))
                return false;

            PreparedStatement statement = conn.prepareStatement("INSERT INTO `users` (`username`) VALUES (?)");

            statement.setString(1, username);

            statement.execute();

            return true;
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public boolean existPlayer(String username) {
        try (Connection conn = database.getConnection()) {
            {
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");

                statement.setString(1, username);

                ResultSet rs = statement.executeQuery();

                if (rs.next())
                    return true;
            }
        } catch (SQLException e) {
            handleException(e);
        }
        return false;
    }

    public boolean isDenied(PlotID plotId, String denied_player) {
        return this.isDenied(plotId.x(), plotId.z(), denied_player);
    }

    public boolean isDenied(int plot_id_x, int plot_id_z, String denied_player) {
        try (Connection conn = database.getConnection()) {
            {
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM `denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `denied_username` LIKE ?;");

                statement.setInt(1, plot_id_x);
                statement.setInt(2, plot_id_z);
                statement.setString(3, denied_player);

                ResultSet rs = statement.executeQuery();

                if (rs.next())
                    return true;
            }
        } catch (SQLException e) {
            handleException(e);
        }
        return false;
    }

    public boolean addDenied(PlotID plotId, String username, @Nullable String reason) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement;

            statement = conn.prepareStatement("INSERT INTO `denied`(`plot_id_x`, `plot_id_z`, `denied_username`, `reason`) VALUES (?, ?, ?, ?)");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, username);
            statement.setString(4, reason);

            statement.execute();

            return true;
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public boolean removeDenied(PlotID plotId, String username) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement;

            statement = conn.prepareStatement("DELETE FROM `denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `denied_username` LIKE ?;");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, username);

            statement.execute();

            return true;
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Nullable
    public List<DeniedPlayer> getDenied(int plot_id_x, int plot_id_z) {
        List<DeniedPlayer> deniedPlayers = new ArrayList<>();
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement;

            statement = conn.prepareStatement("SELECT `denied_username`, `reason` FROM`denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ?;");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);

            ResultSet rs = statement.executeQuery();

            while(rs.next())
                deniedPlayers.add(new DeniedPlayer(rs.getString("denied_username"), rs.getString("reason")));

            return deniedPlayers;
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    private void handleException(SQLException e) {
        LOGGER.error("[PlotCubic] An error occured while executing a SQLQuery, to prevent a hell door from opening, your server will shutdown.", e);
        server.shutdown();
    }


    private void initialization() {
        try (Connection conn = database.getConnection()) {

            // Janky ass solution to prevent table alteration after table creation.
            // TODO: This is a temporary solution, don't leave this as is.

            ResultSet rs = conn.prepareStatement("SHOW TABLES;").executeQuery();
            if (rs.next()) return;


            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `biomes` (
                      `name` varchar(32) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `denied` (
                      `plot_id_x` int(11) NOT NULL,
                      `plot_id_z` int(11) NOT NULL,
                      `denied_username` varchar(16) NOT NULL,
                      `reason` varchar(64) DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `music` (
                      `name` varchar(32) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `plots` (
                      `id_x` int(11) NOT NULL,
                      `id_z` int(11) NOT NULL,
                      `greeting` varchar(1024) DEFAULT NULL,
                      `farewall` varchar(1024) DEFAULT NULL,
                      `biome` varchar(32) DEFAULT NULL,
                      `music` varchar(32) DEFAULT NULL,
                      `team` varchar(32) DEFAULT NULL,
                      `owner_username` varchar(32) DEFAULT NULL,
                      `gamemode` enum('creative','survival','adventure') DEFAULT NULL,
                      `date_claimed` timestamp NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `reportreasons` (
                      `name` varchar(255) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `reports` (
                      `id` int(11) NOT NULL,
                      `details` varchar(255) DEFAULT NULL,
                      `report_reason` varchar(255) DEFAULT NULL,
                      `plot_id_x` int(11) DEFAULT NULL,
                      `plot_id_z` int(11) DEFAULT NULL,
                      `reporting_user` varchar(16) DEFAULT NULL,
                      `admin_username` varchar(16) DEFAULT NULL,
                      `punishment_type` enum('clear','delete','warning','ban','temporal_ban','ban_ip', 'invalid') DEFAULT NULL,
                      `date_reported` timestamp NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `teams` (
                      `name` varchar(32) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `permissions` (
                      `index` int(11) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `trusted` (
                      `plot_id_x` int(11) NOT NULL,
                      `plot_id_z` int(11) NOT NULL,
                      `trusted_username` varchar(16) NOT NULL,
                      `permission_index` int(11) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `users` (
                      `username` varchar(16) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `biomes`
                      ADD PRIMARY KEY (`name`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `denied`
                      ADD PRIMARY KEY (`plot_id_x`,`plot_id_z`,`denied_username`),
                      ADD KEY `fk_denied_users` (`denied_username`),
                      ADD KEY `fk_denied_plots` (`plot_id_x`, `plot_id_z`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `music`
                      ADD PRIMARY KEY (`name`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `plots`
                      ADD PRIMARY KEY (`id_x`,`id_z`),
                      ADD KEY `fk_plots_biome` (`biome`),
                      ADD KEY `fk_plots_music` (`music`),
                      ADD KEY `fk_plots_team` (`team`),
                      ADD KEY `fk_plots_users` (`owner_username`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reportreasons`
                      ADD PRIMARY KEY (`name`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reports`
                      ADD PRIMARY KEY (`id`),
                      ADD KEY `fk_reports_plots` (`plot_id_x`,`plot_id_z`),
                      ADD KEY `fk_reports_reportreasons` (`report_reason`),
                      ADD KEY `fk_reports_users_1` (`reporting_user`),
                      ADD KEY `fk_reports_users_2` (`admin_username`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `teams`
                      ADD PRIMARY KEY (`name`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `permissions`
                      ADD PRIMARY KEY (`index`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `trusted`
                      ADD PRIMARY KEY (`plot_id_x`,`plot_id_z`,`trusted_username`, `permission_index`),
                      ADD KEY `fk_trusted_users` (`trusted_username`),
                      ADD KEY `fk_trusted_plots` (`plot_id_x`, `plot_id_z`),
                      ADD KEY `fk_trusted_permissions` (`permission_index`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `users`
                      ADD PRIMARY KEY (`username`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reports`
                      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `denied`
                      ADD CONSTRAINT `fk_denied_users` FOREIGN KEY (`denied_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_denied_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE;
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `plots`
                      ADD CONSTRAINT `fk_plots_biome` FOREIGN KEY (`biome`) REFERENCES `biomes` (`name`),
                      ADD CONSTRAINT `fk_plots_music` FOREIGN KEY (`music`) REFERENCES `music` (`name`),
                      ADD CONSTRAINT `fk_plots_team` FOREIGN KEY (`team`) REFERENCES `teams` (`name`),
                      ADD CONSTRAINT `fk_plots_users` FOREIGN KEY (`owner_username`) REFERENCES `users` (`username`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reports`
                      ADD CONSTRAINT `fk_reports_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_reports_reportreasons` FOREIGN KEY (`report_reason`) REFERENCES `reportreasons` (`name`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_reports_users_1` FOREIGN KEY (`reporting_user`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_reports_users_2` FOREIGN KEY (`admin_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `trusted`
                      ADD CONSTRAINT `fk_trusted_users` FOREIGN KEY (`trusted_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_trusted_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_trusted_permissions` FOREIGN KEY (`permission_index`) REFERENCES `permissions` (`index`) ON DELETE CASCADE ON UPDATE CASCADE;
            """).execute();

            for (var permission : PlotPermission.values()) {
                PreparedStatement statement = conn.prepareStatement("""
                        INSERT INTO `permissions` (`index`) VALUES (?)
                """);

                statement.setInt(1, permission.ordinal());

                statement.execute();
            }

            conn.prepareStatement("""
                    COMMIT;
            """).execute();

        } catch (SQLException e) {
            handleException(e);
        }
    }
}
