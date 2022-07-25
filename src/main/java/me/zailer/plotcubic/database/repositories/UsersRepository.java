package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.plot.UserConfig;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersRepository {
    private final Connection connection;

    public UsersRepository(Connection connection) {
        this.connection = connection;
    }

    public void add(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO `users` (`username`, `plot_chat_enabled`) VALUES (?, FALSE)");

        statement.setString(1, username);

        statement.executeUpdate();
    }

    public boolean exists(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");

        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    @Nullable
    public UserConfig get(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `username` = ?");

        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        if (rs.next())
            return new UserConfig(
                    username,
                    rs.getBoolean("plot_chat_enabled")
            );

        return null;
    }

    public void updatePlotChat(String username, boolean value) throws SQLException {
            PreparedStatement statement = connection.prepareStatement("UPDATE `users` SET `plot_chat_enabled` = ? WHERE `username` = ?");

            statement.setBoolean(1, value);
            statement.setString(2, username);

            statement.executeUpdate();
    }
}
