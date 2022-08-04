package me.zailer.plotcubic.database;

public enum DatabaseType {
    H2("h2", "org.h2.Driver");

    private final String name;
    private final String driverClassName;

    DatabaseType(String name, String driverClassName) {
        this.name = name;
        this.driverClassName = driverClassName;
    }

    public String getName() {
        return this.name;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public static DatabaseType getEnum(String value) {
        return DatabaseType.H2;
    }
}
