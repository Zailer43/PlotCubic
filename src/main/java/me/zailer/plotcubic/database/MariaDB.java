package me.zailer.plotcubic.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDB extends SQLDatabase {
    private final Hikari hikari;

    @Override
    Connection getConnection() throws SQLException {
        return hikari.getDataSource().getConnection();
    }

    @Override
    void close() {
        hikari.close();
    }

    public MariaDB(String hostname, Integer port, String databaseName, String username, String password) {
        this.hikari = Hikari.createHikari("org.mariadb.jdbc.MariaDbDataSource", hostname, port, databaseName, username, password);
    }
}
