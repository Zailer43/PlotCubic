package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.CommandSuggestions;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import java.sql.SQLException;
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
                        .requires(Permissions.require(this.getCommandPermission()))
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
            String reason = Utils.getArg(serverCommandSource, String.class, "REASON");

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

            if (reason != null && !Permissions.check(player, "plotcubic.command.deny.add_reason")) {
                MessageUtils.sendMissingPermissionMessage(player, "permission.plotcubic.command.deny.add_reason");
                return 1;
            }

            if (reason != null && reason.length() > 64) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.reason_length");
                return 1;
            }

            try (var uow = new UnitOfWork()) {
                try {
                    if (!uow.usersRepository.exists(deniedUsername)) {
                        MessageUtils.sendChatMessage(player, "error.plotcubic.player_does_not_exist", deniedUsername);
                        return 1;
                    }

                    uow.beginTransaction();

                    if (uow.deniedRepository.exists(plotId, deniedUsername)) {
                        String removeCommand = String.format("/%s %s %s", PlotCommand.COMMAND_ALIAS[0], new RemoveCommand().getAlias()[0], deniedUsername);
                        MessageUtils.sendChatMessage(player, "error.plotcubic.plot.deny.already_has_deny", removeCommand);
                        return 1;
                    }

                    uow.trustedRepository.update(new TrustedPlayer(deniedUsername, Set.of(), plotId));
                    uow.deniedRepository.add(plotId, deniedUsername, reason);
                    uow.plotsRepository.deletePlot(plotId);
                    uow.commit();

                    DeniedPlayer deniedPlayer = new DeniedPlayer(deniedUsername, reason);
                    MessageUtils.sendChatMessage(player, "text.plotcubic.plot.deny_successful", deniedUsername, deniedPlayer.reason());
                    Plot plot = Plot.getLoadedPlot(plotId);
                    if (plot != null)
                        plot.addDenied(deniedPlayer);
                } catch (SQLException e) {
                    uow.rollback();
                    MessageUtils.sendChatMessage(player, "error.plotcubic.database.deny");
                }
            } catch (Exception ignored) {
                MessageUtils.sendDatabaseConnectionError(player);
            }
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
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.deny";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }
}
