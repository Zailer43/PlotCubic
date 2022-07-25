package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.gui.ReportGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReportCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"report"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

            if (plotId == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }

            Plot plot = Plot.getPlot(plotId);

            if (plot == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.report.unclaimed");
                return 1;
            }

            if (plot.isOwner(player)) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.report.yourself");
                return 1;
            }

            String reportingPlayer = player.getName().getString();

            try (var uow = new UnitOfWork()) {
                if (uow.reportsRepository.hasUnmoderatedReport(plotId, reportingPlayer)) {
                    MessageUtils.sendChatMessage(player, "error.plotcubic.plot.report.has_unmoderated_report_in_this_plot");
                    return 1;
                }

                int amountOfUnmoderatedReports = uow.reportsRepository.getTotalUnmoderatedReports(reportingPlayer) + 1;
//                int maxReportsUnmoderated = Options.get(player, "plotcubic.command.report.max_reports_unmoderated", 3, Integer::parseInt);
//                MessageUtils.sendChatMessage(player, "maxUnmoderatedReportsAllowed = " + maxReportsUnmoderated);
                if (amountOfUnmoderatedReports > 3) { //maxReportsUnmoderated) {
                    MessageUtils.sendChatMessage(player, "error.plotcubic.plot.report.max_reports_unmoderated");
                    return 1;
                }
            } catch (Exception ignored) {
                MessageUtils.sendDatabaseConnectionError(player);
                return 1;
            }

            new ReportGui().openAddReport(player, plot);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.report";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }
}
