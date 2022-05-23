package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
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

            if (PlotCubic.getDatabaseManager().hasPendingReport(plotId, player.getName().getString())) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.report.pending");
                return 1;
            }

            new ReportGui().openAddReport(player, plot);
        } catch (CommandSyntaxException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
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
