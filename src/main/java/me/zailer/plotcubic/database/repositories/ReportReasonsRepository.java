package me.zailer.plotcubic.database.repositories;

import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.plot.ReportReason;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ReportReasonsRepository {
    private final Connection connection;

    public ReportReasonsRepository(Connection connection) {
        this.connection = connection;
    }

    public void add(long report_id, ReportReason report_reason) throws SQLException {
        PreparedStatement insertReasonStatement = connection.prepareStatement("INSERT INTO `reportreasons`(`reason_id`, `report_id`) VALUES (?, ?);");

        insertReasonStatement.setString(1, report_reason.getId());
        insertReasonStatement.setLong(2, report_id);

        insertReasonStatement.executeUpdate();
    }

    public void add(long report_id, Set<ReportReason> reasons) throws SQLException {
        for (var reason : reasons)
            this.add(report_id, reason);
    }

    public Set<ReportReason> get(long report_id) throws SQLException {
        PreparedStatement reasonStatement;

        reasonStatement = connection.prepareStatement("SELECT * from `reportreasons` WHERE report_id = ?;");

        reasonStatement.setLong(1, report_id);

        ResultSet rs = reasonStatement.executeQuery();

        Set<ReportReason> reasons = new HashSet<>();
        while (rs.next()) {
            String id = rs.getString("reason_id");
            reasons.add(ReportReason.byId(id, new ReportReason(id, 0, new Config.ItemConfig("stone", 1, false, false, ""))));
        }

        return reasons;
    }
}
