package me.zailer.plotcubic.database;

import com.mojang.logging.LogUtils;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.enums.PlotPermission;
import me.zailer.plotcubic.enums.ReportReason;
import me.zailer.plotcubic.plot.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.*;
import java.util.Date;


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


            PreparedStatement statement = conn.prepareStatement("INSERT INTO `plots` (`id_x`, `id_z`, `greeting`, `farewall`, `biome`, `music`, `team`, `owner_username`, `date_claimed`, `chat_style_id`) VALUES (?, ?, NULL, NULL, NULL, NULL, NULL, ?, ?, ?);");

            statement.setInt(1, plot_id_x);
            statement.setInt(2, plot_id_z);
            statement.setString(3, username);

            Calendar calendar = Calendar.getInstance();
            Timestamp time = new Timestamp(calendar.getTimeInMillis());
            statement.setTimestamp(4, time);
            statement.setString(5, PlotCubic.getConfig().plotChatStyles()[0].id());

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

            return new Plot(
                    rs.getString("owner_username"),
                    new PlotID(plot_id_x, plot_id_z),
                    this.getAllTrusted(plot_id_x, plot_id_z),
                    this.getDenied(plot_id_x, plot_id_z),
                    rs.getDate("date_claimed"),
                    GameMode.byId(rs.getByte("gamemode_id"), null),
                    PlotChatStyle.byId(rs.getString("chat_style_id"))
            );

        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public void updateGameMode(GameMode gameMode, PlotID plotId) {
        this.updateGameMode(gameMode, plotId.x(), plotId.z());
    }

    public void updateGameMode(GameMode gameMode, int plot_id_x, int plot_id_z) {
        try (Connection conn = database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("UPDATE `plots` SET `gamemode_id` = ? WHERE `id_x` = ? AND `id_z` = ?;");

            statement.setByte(1, (byte) gameMode.getId());
            statement.setInt(2, plot_id_x);
            statement.setInt(3, plot_id_z);

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
        }
    }

    private void addGameMode(byte id) {
        try (Connection conn = database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("""
                        INSERT INTO `gamemodes` (`id`) VALUES (?)
                """);

            statement.setByte(1, id);

            statement.execute();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void updateChatStyle(PlotChatStyle chatStyle, PlotID plotId) {
        this.updateChatStyle(chatStyle, plotId.x(), plotId.z());
    }

    public void updateChatStyle(PlotChatStyle chatStyle, int plot_id_x, int plot_id_z) {
        try (Connection conn = database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("UPDATE `plots` SET `chat_style_id` = ? WHERE `id_x` = ? AND `id_z` = ?;");

            statement.setString(1, chatStyle.id());
            statement.setInt(2, plot_id_x);
            statement.setInt(3, plot_id_z);

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
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

    public void newUser(String username) {
        try (Connection conn = database.getConnection()) {
            if (this.existPlayer(username))
                return;

            PreparedStatement statement = conn.prepareStatement("INSERT INTO `users` (`username`, `plot_chat_enabled`) VALUES (?, FALSE)");

            statement.setString(1, username);

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
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

    @Nullable
    public User getPlayer(String username) {
        try (Connection conn = database.getConnection()) {
            {
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");

                statement.setString(1, username);

                ResultSet rs = statement.executeQuery();

                if (rs.next())
                    return new User(username, rs.getBoolean("plot_chat_enabled"));
            }
        } catch (SQLException e) {
            handleException(e);
        }
        return null;
    }

    public void updatePlotChat(String username, boolean plotChatEnabled) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement = conn.prepareStatement("UPDATE `users` SET `plot_chat_enabled` = ? WHERE `username` = ?;");

            statement.setBoolean(1, plotChatEnabled);
            statement.setString(2, username);

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
        }
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

    public void removeDenied(PlotID plotId, String username) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement;

            statement = conn.prepareStatement("DELETE FROM `denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `denied_username` LIKE ?;");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, username);

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
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

    public void addReport(PlotID plotId, String reportingUser, Set<ReportReason> reportReasonSet) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement = conn.prepareStatement("INSERT INTO `reports`(`plot_id_x`, `plot_id_z`, `reporting_user`) VALUES (?, ?, ?);");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, reportingUser);

            statement.execute();

            statement = conn.prepareStatement("SELECT `id` FROM `reports` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `reporting_user` = ? ORDER BY `date_reported` DESC;");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, reportingUser);

            ResultSet rs = statement.executeQuery();

            if (!rs.next())
                return;

            long id = rs.getLong("id");

            for (var reportReason : reportReasonSet) {
                PreparedStatement insertReasonStatement = conn.prepareStatement("INSERT INTO `reportreasons`(`reason_index`, `report_id`) VALUES (?, ?);");

                insertReasonStatement.setInt(1, reportReason.ordinal());
                insertReasonStatement.setLong(2, id);

                insertReasonStatement.execute();
            }

        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void updateReport(ReportedPlot report, ServerPlayerEntity admin) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement = conn.prepareStatement("UPDATE `reports` SET `is_moderated` = TRUE, `admin_username` = ?, `date_moderated` = CURRENT_TIMESTAMP WHERE `id` = ?;");

            statement.setString(1, admin.getName().getString());
            statement.setLong(2, report.id());

            statement.execute();

        } catch (SQLException e) {
            handleException(e);
        }
    }

    public boolean hasPendingReport(PlotID plotId, String reportingUser) {
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM `reports` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `reporting_user` = ? AND `is_moderated` = FALSE;");

            statement.setInt(1, plotId.x());
            statement.setInt(2, plotId.z());
            statement.setString(3, reportingUser);

            ResultSet rs = statement.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Nullable
    public List<ReportedPlot> getAllReports(boolean isModerated) {
        List<ReportedPlot> reportedPlotList = new ArrayList<>();
        try (Connection conn = database.getConnection()) {

            PreparedStatement statement = conn.prepareStatement("SELECT plots.owner_username, plots.id_x, plots.id_z, reports.id, reports.date_reported, reports.reporting_user FROM `reports` INNER JOIN `plots` ON reports.plot_id_x = plots.id_x AND reports.plot_id_z = plots.id_z WHERE `is_moderated` = ? ORDER BY reports.date_reported ASC;");

            statement.setBoolean(1, isModerated);

            ResultSet resultSetReports = statement.executeQuery();

            while(resultSetReports.next()) {
                String plotOwnerUsername = resultSetReports.getString("owner_username");
                int plot_id_x = resultSetReports.getInt("id_x");
                int plot_id_z = resultSetReports.getInt("id_z");
                long id = resultSetReports.getLong("id");
                Date dateReported = resultSetReports.getDate("date_reported");
                String reportingUser = resultSetReports.getString("reporting_user");
                ReportedPlot reportedPlot = new ReportedPlot(id, new PlotID(plot_id_x, plot_id_z), plotOwnerUsername, reportingUser, null, dateReported, null, isModerated, new HashSet<>());

                PreparedStatement reasonStatement;

                reasonStatement = conn.prepareStatement("SELECT * from `reportreasons` WHERE report_id = ?;");

                reasonStatement.setLong(1, id);

                ResultSet resultSetReasons = reasonStatement.executeQuery();
                ReportReason[] reportReasons = ReportReason.values();

                while (resultSetReasons.next())
                    reportedPlot.addReason(reportReasons[resultSetReasons.getInt("reason_index")]);

                reportedPlotList.add(reportedPlot);
            }

            return reportedPlotList;
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

            //TODO: implement transactions

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
                    CREATE TABLE IF NOT EXISTS `gamemodes` (
                      `id` tinyint NOT NULL
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
                      `gamemode_id` tinyint DEFAULT -1,
                      `date_claimed` timestamp NOT NULL,
                      `chat_style_id` varchar(32) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `reportreasontype` (
                      `index` int(11) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

            conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `reportreasons` (
                      `reason_index` int(11) NOT NULL,
                      `report_id` bigint(20) NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """).execute();

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
                      `username` varchar(16) NOT NULL,
                      `plot_chat_enabled` boolean NOT NULL
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
                    ALTER TABLE `gamemodes`
                      ADD PRIMARY KEY (`id`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `plots`
                      ADD PRIMARY KEY (`id_x`,`id_z`),
                      ADD KEY `fk_plots_biome` (`biome`),
                      ADD KEY `fk_plots_music` (`music`),
                      ADD KEY `fk_plots_team` (`team`),
                      ADD KEY `fk_plots_users` (`owner_username`),
                      ADD KEY `fk_plots_gamemode` (`gamemode_id`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reportreasontype`
                      ADD PRIMARY KEY (`index`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reportreasons`
                      ADD PRIMARY KEY (`reason_index`, `report_id`),
                      ADD KEY `fk_reportreasons_reports` (`report_id`),
                      ADD KEY `fk_reportreasons_reportreasontype` (`reason_index`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reports`
                      ADD PRIMARY KEY (`id`, `plot_id_x`, `plot_id_z`),
                      ADD KEY `fk_reported_plot` (`plot_id_x`,`plot_id_z`),
                      ADD KEY `fk_reporting_user` (`reporting_user`);
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
                      MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
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
                      ADD CONSTRAINT `fk_plots_users` FOREIGN KEY (`owner_username`) REFERENCES `users` (`username`),
                      ADD CONSTRAINT `fk_plots_gamemode` FOREIGN KEY (`gamemode_id`) REFERENCES `gamemodes` (`id`);
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reportreasons`
                      ADD CONSTRAINT `fk_reportreasons_reports` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_reportreasons_reportreasontype` FOREIGN KEY (`reason_index`) REFERENCES `reportreasontype` (`index`) ON DELETE CASCADE ON UPDATE CASCADE;
            """).execute();

            conn.prepareStatement("""
                    ALTER TABLE `reports`
                      ADD CONSTRAINT `fk_reported_plot` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE,
                      ADD CONSTRAINT `fk_reporting_user` FOREIGN KEY (`reporting_user`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
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

            for (var reportReason : ReportReason.values()) {
                PreparedStatement statement = conn.prepareStatement("""
                        INSERT INTO `reportreasontype` (`index`) VALUES (?)
                """);

                statement.setInt(1, reportReason.ordinal());

                statement.execute();
            }

            this.addGameMode((byte) -1); //default

            for (var gameMode : GameMode.values()) {
                this.addGameMode((byte) gameMode.getId());
            }

        } catch (SQLException e) {
            handleException(e);
        }
    }
}
