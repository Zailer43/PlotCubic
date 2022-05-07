package me.zailer.plotcubic.database;


import com.zaxxer.hikari.HikariDataSource;

public class Hikari {

    private final HikariDataSource dataSource;

    public Hikari(HikariDataSource hikari) {
        this.dataSource = hikari;
    }

    public void close() {
        dataSource.close();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public static Hikari createHikari(String dsClassName, String hostname, Integer port, String database, String username, String password) {
        HikariDataSource hikari  = new HikariDataSource();

        hikari.setMaximumPoolSize(10);
        hikari.setDataSourceClassName(dsClassName);

        hikari.addDataSourceProperty("serverName", hostname);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", database);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);

        hikari.setMinimumIdle(5);
        hikari.setLeakDetectionThreshold(15000);
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setConnectionTimeout(1000);

        return new Hikari(hikari);
    }

}
