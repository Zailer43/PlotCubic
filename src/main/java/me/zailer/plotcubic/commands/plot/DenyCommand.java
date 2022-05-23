package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.CommandSuggestions;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.database.DatabaseManager;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import java.util.Set;

public class DenyCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"deny"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .executes(this::executeValidUsages)
                        .then(CommandManager.argument("PLAYER", StringArgumentType.word())
                                .suggests(CommandSuggestions.ONLINE_PLAYER_SUGGESTION)
                                .executes(this::execute)
                                .then(CommandManager.argument("REASON", StringArgumentType.greedyString())
                                        .executes(this::execute)
                                )
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            String deniedUsername = serverCommandSource.getArgument("PLAYER", String.class);
            String reason = null;

            try {
                reason = serverCommandSource.getArgument("REASON", String.class);
            } catch (IllegalArgumentException ignored) {
            }

            if (deniedUsername.equalsIgnoreCase(player.getName().getString())) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.yourself");
                return 1;
            }

            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
            if (plotId == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }

            if (!Plot.isOwner(player, plotId)) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot_owner");
                return 1;
            }

            if (reason != null && reason.length() > 64) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.reason_length");
                return 1;
            }

            DatabaseManager databaseManager = PlotCubic.getDatabaseManager();
            if (!databaseManager.existPlayer(deniedUsername)) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.player_does_not_exist", deniedUsername);
                return 1;
            }

            if (databaseManager.isDenied(plotId, deniedUsername)) {
                String removeCommand = String.format("/%s %s %s", PlotCommand.COMMAND_ALIAS[0], new RemoveCommand().getAlias()[0], deniedUsername);
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.already_has_deny", removeCommand);
                return 1;
            }

            boolean removeTrustedSuccessful = databaseManager.updateTrusted(new TrustedPlayer(deniedUsername, Set.of(), plotId));
            boolean deniedSuccessful = databaseManager.addDenied(plotId, deniedUsername, reason);
            DeniedPlayer deniedPlayer = new DeniedPlayer(deniedUsername, reason);

            if (removeTrustedSuccessful && deniedSuccessful) {
                MessageUtils.sendChatMessage(player, "text.plotcubic.plot.deny_successful", deniedUsername, reason);
            } else {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.unexpected");
            }
            Plot plot = Plot.getLoadedPlot(plotId);

            if (plot != null)
                plot.addDenied(deniedPlayer);

        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot deny <player>
        //Command usage: /plot deny <player> <reason>

        String denyCommand = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "player");
        String denyCommandWithReason = String.format("/%s %s <%s> <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "player", "reason");

        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", denyCommand)
                .append("\n")
                .append(MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", denyCommandWithReason));
    }

    @Override
    protected String getHelpDetails() {
        return "Ban a player from entering your plot";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }
}
