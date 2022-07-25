package me.zailer.plotcubic.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.ReportReason;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.ReportedPlot;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;

import java.sql.SQLException;
import java.util.*;

public class ReportGui {
    public void openAddReport(ServerPlayerEntity reportingPlayer, Plot reportedPlot) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, reportingPlayer, false);
        Set<ReportReason> reportReasonsInTrue = new HashSet<>();

        gui.setTitle(new TranslatableText("gui.plotcubic.report.title", reportedPlot.getOwnerUsername()));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.saveReport(reportingPlayer, reportedPlot, reportReasonsInTrue);
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        GuiUtils.setGlass(gui, 0, 9);

        GuiUtils.loadPage(gui, 1, this.getReportReasons(), reportReasonsInTrue);

        gui.setSlot(45, acceptItem);
        GuiUtils.setGlass(gui, 46, 7);
        gui.setSlot(53, cancelItem);

        gui.open();
    }

    private void saveReport(ServerPlayerEntity reportingPlayer, Plot reportedPlot, Set<ReportReason> reportReasonsInTrue) {
        if (reportReasonsInTrue.isEmpty()) {
            MessageUtils.sendChatMessage(reportingPlayer, "error.plotcubic.plot.report.no_reason");
            return;
        }

        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.reportsRepository.add(reportedPlot.getPlotID(), reportingPlayer.getName().getString(), reportReasonsInTrue);
                uow.commit();
                MessageUtils.sendChatMessage(reportingPlayer, "text.plotcubic.plot.report.successful");
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendChatMessage(reportingPlayer, "error.plotcubic.database.report.add");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(reportingPlayer);
        }
    }

    public void openViewReports(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);


        gui.setTitle(new TranslatableText("gui.plotcubic.view_reports.title"));

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        List<ReportedPlot> reportedPlots = new ArrayList<>();

        try (var uow = new UnitOfWork()) {
            reportedPlots.addAll(uow.reportsRepository.getAllReports(false));
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(player);
        }

        int maxReports = Math.min(45, reportedPlots.size());
        for (int i = 0; i != maxReports; i++) {
            ReportedPlot report = reportedPlots.get(i);
            String plotOwner = report.plotOwnerUsername();
            PlotID plotId = report.plotId();

            GuiElementBuilder reportItem = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setSkullOwner(new GameProfile(UUID.randomUUID(), plotOwner), player.getServer())
                    .setName(new TranslatableText("gui.plotcubic.view_reports.reported", plotOwner, plotId.toString()).setStyle(Style.EMPTY.withColor(MessageUtils.getHighlight())))
                    .addLoreLine(new TranslatableText("gui.plotcubic.view_reports.reasons"))
                    .setCallback((index, type, action) -> this.reportItemCallback(type, player, plotId, report));

            for (var reportReason : report.reasons())
                reportItem.addLoreLine(reportReason.getDisplayName());

            reportItem.addLoreLine(LiteralText.EMPTY.copy())
                    .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.view_reports.reporting_player", report.reportingUser()))
                    .addLoreLine(new TranslatableText("gui.plotcubic.view_reports.left_click"))
                    .addLoreLine(new TranslatableText("gui.plotcubic.view_reports.right_click"));

            gui.setSlot(i, reportItem);
        }

        GuiUtils.setGlass(gui, 45, 8);
        gui.setSlot(53, cancelItem);

        gui.open();
    }

    private void reportItemCallback(ClickType clickType, ServerPlayerEntity player, PlotID plotId, ReportedPlot report) {
        if (clickType.isLeft) {
            //Here a bypass is being applied to the permission to visit plots
            // but it is necessary so that the admin can moderate the plot
            Plot plot = Plot.getPlot(plotId);

            if (plot == null)
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.visit.unclaimed");
            else
                plot.visit(player);

        } else if (clickType.isRight) {
            List<String> infoList = List.of("gui.plotcubic.confirmation.report_view.info");
            new ConfirmationGui().open(player, "gui.plotcubic.confirmation.report_view.title", infoList, () -> this.updateReport(report, player));
        }
    }

    private void updateReport(ReportedPlot report, ServerPlayerEntity admin) {
        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.reportsRepository.setModerated(report, admin);
                uow.commit();

                MessageUtils.sendChatMessage(admin, "text.plotcubic.report.moderated", admin.getName().getString(), new Date());
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendChatMessage(admin, "error.plotcubic.database.report.moderated");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(admin);
        }
    }

    private List<ReportReason> getReportReasons() {
        Set<String> reportKeySet = ReportReason.REPORT_REASON_HASH_MAP.keySet();
        List<ReportReason> reportReasonList = new ArrayList<>();
        for (var key : reportKeySet)
            reportReasonList.add(ReportReason.REPORT_REASON_HASH_MAP.get(key));

        return reportReasonList;
    }
}
