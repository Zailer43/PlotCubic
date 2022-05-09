package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.enums.ReportReason;

import java.util.Date;
import java.util.Set;

public record ReportedPlot(long id, PlotID plotId, String plotOwnerUsername, String reportingUser, String adminUsername, Date reportedDate, Date moderatedDate, boolean isModerated, Set<ReportReason> reasons) {

    public void addReason(ReportReason reason) {
        this.reasons.add(reason);
    }
}
