package me.zailer.plotcubic.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends SQLDatabase {
    private final Hikari hikari;

    @Override
    Connection getConnection() throws SQLException {
        return hikari.getDataSource().getConnection();
    }

    @Override
    void close() {
        hikari.close();
    }

    public MySQL(String hostname, Integer port, String database, String username, String password) {
        hikari = Hikari.createHikari("com.mysql.cj.jdbc.MysqlDataSource", hostname, port, database, username, password);
    }
}
