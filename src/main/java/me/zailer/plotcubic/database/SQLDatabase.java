package me.zailer.plotcubic.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class SQLDatabase {
    abstract Connection getConnection() throws SQLException;
    abstract void close();
}
