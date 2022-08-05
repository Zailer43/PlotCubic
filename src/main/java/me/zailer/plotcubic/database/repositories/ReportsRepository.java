package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.ReportReason;
import me.zailer.plotcubic.plot.ReportedPlot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportsRepository {
    private final UnitOfWork uow;
    private final Connection connection;

    public ReportsRepository(Connection connection, UnitOfWork uow) {
        this.uow = uow;
        this.connection = connection;
    }

    public void add(PlotID plot_id, String reporting_user, Set<ReportReason> report_reasons) throws SQLException {
        this.add(plot_id.x(), plot_id.z(), reporting_user, report_reasons);
    }

    public void add(int plot_id_x, int plot_id_z, String reporting_user, Set<ReportReason> report_reasons) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO `reports`(`plot_id_x`, `plot_id_z`, `reporting_user`) VALUES (?, ?, ?);");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, reporting_user);

        statement.executeUpdate();

        long id = this.getLastReport(plot_id_x, plot_id_z, reporting_user);

        this.uow.reportReasonsRepository.add(id, report_reasons);
    }

    public long getLastReport(int plot_id_x, int plot_id_z, String reporting_user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `reports` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `reporting_user` = ? ORDER BY `date_reported` DESC;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, reporting_user);

        ResultSet rs = statement.executeQuery();

        if (!rs.next())
            throw new SQLException();

        return rs.getLong("id");
    }

    public void setModerated(ReportedPlot report, ServerPlayerEntity admin) throws SQLException {
        this.setModerated(report.id(), admin.getName().getString());
    }

    public void setModerated(long report_id, String admin_username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE `reports` SET `is_moderated` = TRUE, `admin_username` = ?, `date_moderated` = CURRENT_TIMESTAMP WHERE `id` = ?;");

        statement.setString(1, admin_username);
        statement.setLong(2, report_id);

        statement.executeUpdate();
    }

    public boolean hasUnmoderatedReport(PlotID plot_id, String reporting_player) throws SQLException {
        return this.hasUnmoderatedReport(plot_id.x(), plot_id.z(), reporting_player);
    }

    public boolean hasUnmoderatedReport(int plot_id_x, int plot_id_z, String reporting_player) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `reports` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `reporting_user` = ? AND `is_moderated` = FALSE;");

        statement.setInt(1, plot_id_x);
        statement.setInt(2, plot_id_z);
        statement.setString(3, reporting_player);

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    public int getTotalUnmoderatedReports(String reportingUser) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `reports` WHERE `reporting_user` = ? AND `is_moderated` = FALSE;");

        statement.setString(1, reportingUser);

        ResultSet rs = statement.executeQuery();

        int total = 0;
        while (rs.next())
            total++;
        return total;
    }

    public List<ReportedPlot> getAllReports(boolean is_moderated) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT plots.owner_username, plots.id_x, plots.id_z, reports.id, reports.date_reported, reports.reporting_user FROM `reports` INNER JOIN `plots` ON reports.plot_id_x = plots.id_x AND reports.plot_id_z = plots.id_z WHERE `is_moderated` = ? ORDER BY reports.date_reported ASC;");

        statement.setBoolean(1, is_moderated);

        ResultSet resultSetReports = statement.executeQuery();

        List<ReportedPlot> reported_plot_list = new ArrayList<>();

        while(resultSetReports.next()) {
            int plot_id_x = resultSetReports.getInt("id_x");
            int plot_id_z = resultSetReports.getInt("id_z");
            long id = resultSetReports.getLong("id");
            ReportedPlot reported_plot = new ReportedPlot(
                    id,
                    new PlotID(plot_id_x, plot_id_z),
                    resultSetReports.getString("owner_username"),
                    resultSetReports.getString("reporting_user"),
                    null,
                    resultSetReports.getDate("date_reported"),
                    null,
                    is_moderated,
                    this.uow.reportReasonsRepository.get(id)
            );

            reported_plot_list.add(reported_plot);
        }
        return reported_plot_list;
    }
}
