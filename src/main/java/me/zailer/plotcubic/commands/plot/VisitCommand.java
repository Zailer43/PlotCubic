package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.*;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public class VisitCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"visit", "v"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::executeValidUsages)
//                        .then(CommandManager.argument("PLOT ID", new PlotIdArgumentType())
//                                        .executes(this::execute)
//                        )
                        .then(CommandManager.argument("OWNER", StringArgumentType.word())
                                        .suggests(CommandSuggestions.ONLINE_PLAYER_SUGGESTION)
                                        .executes(this::execute)
                                        .then(CommandManager.argument("NUMBER", IntegerArgumentType.integer(1))
                                                .executes(this::execute)
                                        )
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;

        MutableText message = Text.translatable("error.plotcubic.unexpected");

        PlotID plotId = Utils.getArg(serverCommandSource, PlotID.class, "PLOT ID");
        String playerToVisit = Utils.getArg(serverCommandSource, String.class, "OWNER");
        if (plotId != null) {
            if (Permissions.check(player, "plotcubic.command.visit.plot_id"))
                message = this.visit(player, plotId);
            else
                message = MessageUtils.getMissingPermissionMsg("permission.plotcubic.command.visit.plot_id");
        } else if (playerToVisit != null) {
            if (Permissions.check(player, "plotcubic.command.visit.username")) {
                Integer index = Utils.getArg(serverCommandSource, Integer.class, "NUMBER");
                message = this.visit(player, playerToVisit, index == null ? 1 : index);
            } else {
                message = MessageUtils.getMissingPermissionMsg("permission.plotcubic.command.visit.username");
            }
        }

        player.sendMessage(message);
        return 1;
    }

    public MutableText visit(ServerPlayerEntity player, PlotID plotId) {
        Plot plot = Plot.getPlot(plotId);
        MutableText message = this.getSuccessfulMsg(plotId);

        if (plot == null && Permissions.check(player, "plotcubic.command.visit.plot_id.unclaimed"))
            message = MessageUtils.getMissingPermissionMsg("permission.plotcubic.command.visit.plot_id.unclaimed");
        else if (plot != null && !plot.visit(player))
            message = this.getDenyMessage();

        return message;
    }

    public MutableText visit(ServerPlayerEntity player, String playerToVisit, int index) {
        List<Plot> plotList;
        MutableText message;
        try (var uow = new UnitOfWork()) {
            plotList = uow.plotsRepository.getAllPlots(playerToVisit);

            if (plotList.isEmpty())
                message = this.getThereAreNoPlotsMsg();
            else if (plotList.size() < index)
                message = this.getOutBoundsErrorMsg(plotList.size());
            else if (!Permissions.check(player, "plotcubic.bypass.deny")
                    && uow.deniedRepository.exists(plotList.get(index - 1).getPlotID(), player.getName().getString()))
                message = this.getDenyMessage();
            else {
                plotList.get(index - 1).visit(player);
                message = this.getSuccessfulMsg(playerToVisit, index, plotList.size());
            }

        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(player);
            return Text.translatable("error.plotcubic.database.connection");
        }
        return message;
    }

    public MutableText getSuccessfulMsg(PlotID plotId) {
        return Text.translatable("text.plotcubic.plot.visit.successful_id", plotId.toString());
    }

    public MutableText getSuccessfulMsg(String player, int index, int plotAmount) {
        return Text.translatable("text.plotcubic.plot.visit.successful_player", index + "/" + plotAmount, player);
    }

    public MutableText getOutBoundsErrorMsg(int plotCount) {
        return Text.translatable("error.plotcubic.plot.visit.out_bounds", plotCount);
    }

    public MutableText getDenyMessage() {
        return Text.translatable("text.plotcubic.plot.you_have_deny");
    }

    public MutableText getThereAreNoPlotsMsg() {
        return Text.translatable("error.plotcubic.plot.visit.has_no_plots");
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.visit";
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot visit <username> <plot number>
        //Command usage: /plot visit <id>
        //ID examples: 5;10, 0;-4 or -30;10

        String commandWithUsername = String.format("/%s %s <%s> <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "username", "plot number");
        String commandWithId = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "id");

        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", commandWithUsername)
                .append("\n")
                .append(MessageUtils.formatArgs("text.plotcubic.help.command_usage.with_plot_id", commandWithId, "5;10, 0;-4, -30;10, -15;-25"));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }
}
