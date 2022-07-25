package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.PlotPermission;
import me.zailer.plotcubic.plot.TrustedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TrustedRepository {

    private final Connection connection;

    public TrustedRepository(Connection connection) {
        this.connection = connection;
    }

    public List<TrustedPlayer> get(PlotID plot_id) throws SQLException {
        List<TrustedPlayer> trusted_list = new ArrayList<>();
        HashMap<String, Set<PlotPermission>> players_permissions_hash_map = new HashMap<>();

        PreparedStatement statement;

        statement = connection.prepareStatement("SELECT * FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ?;");

        statement.setInt(1, plot_id.x());
        statement.setInt(2, plot_id.z());

        ResultSet rs = statement.executeQuery();

        while(rs.next()) {
            String username = rs.getString("trusted_username");
            PlotPermission permission = PlotPermission.PERMISSION_HASH_MAP.get(rs.getString("permission_id"));


            if (players_permissions_hash_map.containsKey(username)) {
                players_permissions_hash_map.get(username).add(permission);
            } else {
                players_permissions_hash_map.put(username, new HashSet<>());
                players_permissions_hash_map.get(username).add(permission);
            }
        }

        for (var username : players_permissions_hash_map.keySet())
            trusted_list.add(new TrustedPlayer(username, players_permissions_hash_map.get(username), plot_id));

        return trusted_list;
    }

    public TrustedPlayer get(PlotID plot_id, String username) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("SELECT `permission_id` FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `trusted_username` LIKE ?;");

        statement.setInt(1, plot_id.x());
        statement.setInt(2, plot_id.z());
        statement.setString(3, username);

        ResultSet rs = statement.executeQuery();

        TrustedPlayer trusted_player = new TrustedPlayer(username, new HashSet<>(), plot_id);
        while(rs.next())
            trusted_player.addPermission(PlotPermission.PERMISSION_HASH_MAP.get(rs.getString("permission_id")));

        return trusted_player;
    }

    public void update(TrustedPlayer new_trusted_player) throws SQLException {
        TrustedPlayer old_trusted_player = this.get(new_trusted_player.plotId(), new_trusted_player.username());
        int plot_id_x = new_trusted_player.plotId().x();
        int plot_id_z = new_trusted_player.plotId().z();

        Set<PlotPermission> deleted = old_trusted_player.getDifference(new_trusted_player);
        Set<PlotPermission> added = new_trusted_player.getDifference(old_trusted_player);

        for (var permission : deleted)
            this.delete(plot_id_x, plot_id_z, new_trusted_player.username(), permission);

        for (var permission : added)
            this.add(plot_id_x, plot_id_z, new_trusted_player.username(), permission);
    }

    public void add(int plot_id_x, int plot_id_z, String username, PlotPermission permission) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("INSERT INTO `trusted`(`plot_id_x`, `plot_id_z`, `trusted_username`, `permission_id`) VALUES (?, ?, ?, ?);");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, username);
        statement.setString(4, permission.getId());

        statement.executeUpdate();
    }

    public void delete(PlotID plot_id, String username) throws SQLException {
        this.delete(plot_id.x(), plot_id.z(), username);
    }

    public void delete(int plot_id_x, int plot_id_z, String username) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("DELETE FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `trusted_username` = ?;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, username);

        statement.executeUpdate();
    }

    public void delete(int plot_id_x, int plot_id_z, String username, PlotPermission permission) throws SQLException {
        PreparedStatement statement;

        statement = connection.prepareStatement("DELETE FROM `trusted` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `trusted_username` = ? AND `permission_id` = ?;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, username);
        statement.setString(4, permission.getId());

        statement.executeUpdate();
    }
}
