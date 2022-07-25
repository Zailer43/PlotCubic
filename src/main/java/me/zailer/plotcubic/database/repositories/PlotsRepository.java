package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotChatStyle;
import me.zailer.plotcubic.plot.PlotID;
import net.minecraft.world.GameMode;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlotsRepository {
    private final UnitOfWork uow;
    private final Connection connection;

    public PlotsRepository(Connection connection, UnitOfWork uow) {
        this.uow = uow;
        this.connection = connection;
    }

    public void add(PlotID plot_id, String username) throws SQLException {
        this.add(plot_id.x(), plot_id.z(), username);
    }

    public void add(int plot_id_x, int plot_id_z, String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO `plots` (`id_x`, `id_z`, `owner_username`, `date_claimed`) VALUES (?, ?, ?, ?);");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, username);

        Calendar calendar = Calendar.getInstance();
        Timestamp time = new Timestamp(calendar.getTimeInMillis());
        statement.setTimestamp(4, time);

        statement.executeUpdate();
    }

    public List<Plot> getAllPlots(String username) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        PreparedStatement statement;

        statement = connection.prepareStatement("SELECT * FROM `plots` WHERE `owner_username` LIKE ? ORDER BY `date_claimed` ASC;");

        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        while(rs.next()) {
            int plotIdX = rs.getInt("id_x");
            int plotIdZ = rs.getInt("id_z");
            plots.add(new Plot(rs.getString("owner_username"), new PlotID(plotIdX, plotIdZ)));
        }

        return plots;
    }

    @Nullable
    public Plot get(PlotID plot_id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

        statement.setInt(1, plot_id.x());
        statement.setInt(2, plot_id.z());

        ResultSet rs = statement.executeQuery();
        if (!rs.next())
            return null;

        return new Plot(
                rs.getString("owner_username"),
                plot_id,
                this.uow.trustedRepository.get(plot_id),
                this.uow.deniedRepository.get(plot_id),
                rs.getDate("date_claimed"),
                GameMode.byName(rs.getString("gamemode_id"), null),
                PlotChatStyle.byId(rs.getString("chat_style_id"))
        );
    }

    public void updateGameMode(PlotID plot_id, GameMode gameMode) throws SQLException {
        this.updateGameMode(plot_id.x(), plot_id.z(), gameMode);
    }

    public void updateGameMode(int plot_id_x, int plot_id_z, GameMode gameMode) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE `plots` SET `gamemode_id` = ? WHERE `id_x` = ? AND `id_z` = ?;");

        statement.setString(1, gameMode.getName());
        statement.setInt(2, plot_id_x);
        statement.setInt(3, plot_id_z);

        statement.executeUpdate();
    }

    public void updateChatStyle(PlotID plot_id, PlotChatStyle chat_style) throws SQLException {
        this.updateChatStyle(plot_id.x(), plot_id.z(), chat_style);
    }

    public void updateChatStyle(int plot_id_x, int plot_id_z, PlotChatStyle chatStyle) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE `plots` SET `chat_style_id` = ? WHERE `id_x` = ? AND `id_z` = ?;");

        statement.setString(1, chatStyle.id());
        statement.setInt(2, plot_id_x);
        statement.setInt(3, plot_id_z);

        statement.executeUpdate();
    }

    public void deletePlot(PlotID plot_id) throws SQLException {
        this.deletePlot(plot_id.x(), plot_id.z());
    }

    public void deletePlot(int plot_id_x, int plot_id_z) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);

        statement.executeUpdate();
    }

    public boolean exists(PlotID plot_id) throws SQLException {
        return this.exists(plot_id.x(), plot_id.z());
    }

    public boolean exists(int plot_id_x, int plot_id_z) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `plots` WHERE `id_x` = ? AND `id_z` = ?");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }
}
