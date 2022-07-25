package me.zailer.plotcubic.database;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.database.repositories.*;

import java.sql.Connection;
import java.sql.SQLException;

public class UnitOfWork implements AutoCloseable {
    protected final SQLDatabase context;
    protected Connection connection;

    public DeniedRepository deniedRepository;
    public PlotsRepository plotsRepository;
    public ReportReasonsRepository reportReasonsRepository;
    public ReportsRepository reportsRepository;
    public TrustedRepository trustedRepository;
    public UsersRepository usersRepository;

    public UnitOfWork() throws SQLException {
        this.context = PlotCubic.getDatabaseManager().getDatabase();
        this.connection = this.context.getConnection();

        this.deniedRepository = new DeniedRepository(this.connection);
        this.plotsRepository = new PlotsRepository(this.connection, this);
        this.reportReasonsRepository = new ReportReasonsRepository(this.connection);
        this.reportsRepository = new ReportsRepository(this.connection, this);
        this.trustedRepository = new TrustedRepository(this.connection);
        this.usersRepository = new UsersRepository(this.connection);
    }

    public void beginTransaction() throws SQLException {
        this.connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        if (!this.connection.getAutoCommit())
            this.connection.commit();
    }

    public void rollback() throws SQLException {
        if (!this.connection.getAutoCommit())
            this.connection.rollback();
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }
}
