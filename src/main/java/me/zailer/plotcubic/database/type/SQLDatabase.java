package me.zailer.plotcubic.database.type;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class SQLDatabase {
    public abstract Connection getConnection() throws SQLException;
    abstract void close();
}
