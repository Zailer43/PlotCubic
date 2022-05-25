package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.*;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

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
                        .executes(this::executeValidUsages)
                        .then(CommandManager.argument("PLOT ID", new PlotIdArgumentType())
                                .executes(this::execute)
                        )
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
        ServerPlayerEntity player;
        try {
            player = serverCommandSource.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0;
        }

        try {
            PlotID plotId = serverCommandSource.getArgument("PLOT ID", PlotID.class);
            this.visit(player, plotId);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            String playerToVisit = serverCommandSource.getArgument("OWNER", String.class);

            try {
                Integer index = serverCommandSource.getArgument("NUMBER", Integer.class);
                this.visit(player, playerToVisit, index);
            } catch (IllegalArgumentException ignored) {
                this.visit(player, playerToVisit, 1);
            }
        } catch (IllegalArgumentException ignored) {
        }

        return 1;
    }

    public void visit(ServerPlayerEntity player, PlotID plotId) {
        Plot plot = Plot.getPlot(plotId);
        TranslatableText message = this.getSuccessfulMsg(plotId);

        if (plot != null && !plot.visit(player))
            message = this.getDenyMessage();
        else
            player.teleport(player.getWorld(), plotId.getSpawnOfX(), plotId.getSpawnOfY(), plotId.getSpawnOfZ(), 0, 0);

        MessageUtils.sendChatMessage(player, message.getKey(), message.getArgs());
    }

    public void visit(ServerPlayerEntity player, String playerToVisit) {
        this.visit(player, playerToVisit, 1);
    }

    public void visit(ServerPlayerEntity player, String playerToVisit, int index) {
        List<Plot> plotList = PlotCubic.getDatabaseManager().getAllPlots(playerToVisit, true);
        TranslatableText translation;

        if (plotList == null) {
            translation = new TranslatableText("error.plotcubic.plot.visit.unexpected");
        } else if (plotList.isEmpty()) {
            translation = this.getThereAreNoPlotsMsg();
        } else if (plotList.size() < index) {
            translation = this.getOutBoundsErrorMsg(plotList.size());
        } else {
            // Load the plot with the necessary data, the getAllPlots method only gives the PlotID
            Plot plot = Plot.getPlot(plotList.get(index - 1).getPlotID());
            assert plot != null;

            translation = plot.visit(player) ? this.getSuccessfulMsg(playerToVisit, index, plotList.size()) : this.getDenyMessage();
        }

        MessageUtils.sendChatMessage(player, translation.getKey(), translation.getArgs());
    }

    public TranslatableText getSuccessfulMsg(PlotID plotId) {
        return new TranslatableText("text.plotcubic.plot.visit.successful_id", plotId.toString());
    }

    public TranslatableText getSuccessfulMsg(String player, int index, int plotAmount) {
        return new TranslatableText("text.plotcubic.plot.visit.successful_player", index + "/" + plotAmount, player);
    }

    public TranslatableText getOutBoundsErrorMsg(int plotCount) {
        return new TranslatableText("error.plotcubic.plot.visit.out_bounds", plotCount);
    }

    public TranslatableText getDenyMessage() {
        return new TranslatableText("text.plotcubic.plot.you_have_deny");
    }

    public TranslatableText getThereAreNoPlotsMsg() {
        return new TranslatableText("error.plotcubic.plot.visit.has_no_plots");
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
