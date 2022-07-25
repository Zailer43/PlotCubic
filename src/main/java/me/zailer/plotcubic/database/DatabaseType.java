package me.zailer.plotcubic.database;

import java.util.Locale;

public enum DatabaseType {
    MARIADB, POSTGRESQL, H2;

    public static DatabaseType getEnum(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "postgresql" -> DatabaseType.POSTGRESQL;
            case "h2" -> DatabaseType.H2;
            default -> DatabaseType.MARIADB;
        };
    }
}
