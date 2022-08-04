package me.zailer.plotcubic.database.type;

import com.zaxxer.hikari.HikariDataSource;
import me.zailer.plotcubic.database.Hikari;

import java.sql.Connection;
import java.sql.SQLException;

public class Database extends SQLDatabase {
    private final Hikari hikari;

    @Override
    public Connection getConnection() throws SQLException {
        return hikari.getDataSource().getConnection();
    }

    @Override
    public void close() {
        hikari.close();
    }

    public Database(HikariDataSource hikariDataSource) {
        this.hikari = new Hikari(hikariDataSource);
    }
}
