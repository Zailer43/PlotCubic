package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.PlotID;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeniedRepository {

    private final Connection connection;

    public DeniedRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean exists(PlotID plot_id, String denied_player) throws SQLException {
        return this.exists(plot_id.x(), plot_id.z(), denied_player);
    }

    public boolean exists(int plot_id_x, int plot_id_z, String denied_player) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `denied_username` LIKE ?;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, denied_player);

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    public void add(PlotID plotId, String username, @Nullable String reason) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("INSERT INTO `denied`(`plot_id_x`, `plot_id_z`, `denied_username`, `reason`) VALUES (?, ?, ?, ?)");

        statement.setInt(1, plotId.x());
        statement.setInt(2, plotId.z());
        statement.setString(3, username);
        statement.setString(4, reason);

        statement.executeUpdate();
    }

    public void delete(PlotID plotId, String username) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("DELETE FROM `denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `denied_username` LIKE ?;");

        statement.setInt(1, plotId.x());
        statement.setInt(2, plotId.z());
        statement.setString(3, username);

        statement.executeUpdate();
    }

    @Nullable
    public List<DeniedPlayer> get(PlotID plot_id) throws SQLException {
        return this.get(plot_id.x(), plot_id.z());
    }

    @Nullable
    public List<DeniedPlayer> get(int plot_id_x, int plot_id_z) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("SELECT `denied_username`, `reason` FROM`denied` WHERE `plot_id_x` = ? AND `plot_id_z` = ?;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);

        ResultSet rs = statement.executeQuery();

        List<DeniedPlayer> deniedPlayers = new ArrayList<>();
        while(rs.next())
            deniedPlayers.add(new DeniedPlayer(rs.getString("denied_username"), rs.getString("reason")));

        return deniedPlayers;
    }
}
