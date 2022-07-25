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
import me.zailer.plotcubic.gui.PermissionsGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class TrustedCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"trust", "add", "permissions"};
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
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            String trustedUsername = serverCommandSource.getArgument("PLAYER", String.class);

            if (trustedUsername.equalsIgnoreCase(player.getName().getString())) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.trust.yourself");
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

            TrustedPlayer trustedPlayer;
            try (var uow = new UnitOfWork()) {
                if (!uow.usersRepository.exists(trustedUsername)) {
                    MessageUtils.sendChatMessage(player, "error.plotcubic.player_does_not_exist", trustedUsername);
                    return 1;
                }

                if (uow.deniedRepository.exists(plotId, trustedUsername)) {
                    String removeCommand = String.format("/%s %s %s", PlotCommand.COMMAND_ALIAS[0], new RemoveCommand().getAlias()[0], trustedUsername);
                    MessageUtils.sendChatMessage(player, "error.plotcubic.plot.trust.has_deny", removeCommand);
                    return 1;
                }

                trustedPlayer = uow.trustedRepository.get(plotId, trustedUsername);

            } catch (Exception ignored) {
                MessageUtils.sendDatabaseConnectionError(player);
                return 1;
            }

            new PermissionsGui().open(player, trustedPlayer);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.trust";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot trust <player>

        String command = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "player");
        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", command);
    }
}
