package me.zailer.plotcubic.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.plot.VisitCommand;
import me.zailer.plotcubic.enums.ReportReason;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReportGui {
    public void openAddReport(ServerPlayerEntity reportingPlayer, Plot reportedPlot) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, reportingPlayer, false);
        Set<ReportReason> reportReasonSet = new HashSet<>();

        gui.setTitle(new TranslatableText("gui.plotcubic.report.title", reportedPlot.getOwnerUsername()));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.saveReport(reportingPlayer, reportedPlot, reportReasonSet);
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        ReportReason[] options = ReportReason.values();
        int maxOptions = Math.min(9, options.length);
        for (int i = 0; i != maxOptions; i++)
            GuiUtils.setBoolOption(gui, i, options[i], reportReasonSet);


        gui.setSlot(18, acceptItem);
        GuiUtils.setGlass(gui, 19, 7);
        gui.setSlot(26, cancelItem);

        gui.open();
    }

    private void saveReport(ServerPlayerEntity reportingPlayer, Plot reportedPlot, Set<ReportReason> reportReasonSet) {
        if (reportReasonSet.isEmpty()) {
            MessageUtils.sendChatMessage(reportingPlayer, "error.plotcubic.plot.report.no_reason");
            return;
        }

        PlotCubic.getDatabaseManager().addReport(reportedPlot.getPlotID(), reportingPlayer.getName().getString(), reportReasonSet);
        MessageUtils.sendChatMessage(reportingPlayer, "text.plotcubic.plot.report.successful");
    }

    public void openViewReports(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);


        gui.setTitle(new TranslatableText("gui.plotcubic.view_reports.title"));

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        List<ReportedPlot> reportedPlots = PlotCubic.getDatabaseManager().getAllReports(false);
        if (reportedPlots == null)
            return;

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
            new VisitCommand().visit(player, plotId);
        } else if (clickType.isRight) {
            List<String> infoList = List.of("gui.plotcubic.confirmation.report_view.info");
            new ConfirmationGui().open(player, "gui.plotcubic.confirmation.report_view.title", infoList, () -> this.updateReport(report, player));
        }
    }

    private void updateReport(ReportedPlot report, ServerPlayerEntity admin) {
        PlotCubic.getDatabaseManager().updateReport(report, admin);
    }
}
