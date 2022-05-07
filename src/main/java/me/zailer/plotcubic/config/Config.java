package me.zailer.plotcubic.config;

public record Config(Database database) {

    public static final Config DEFAULT = new Config(
            new Database(
                    "mysql",
                    "localhost",
                    3306,
                    "root",
                    "123",
                    "plotcubic",
                    "p3")
    );

    public record Database(String type, String host, Integer port, String user, String password,
                           String database, String table_name) {
    }
}


