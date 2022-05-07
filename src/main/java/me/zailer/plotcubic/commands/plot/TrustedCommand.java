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
import me.zailer.plotcubic.gui.PermissionsGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.CommandColors;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TrustedCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"trust", "add", "permissions"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
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
                MessageUtils.sendChatMessage(player, MessageUtils.getError("You cannot modify your own permissions").get());
                return 1;
            }

            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
            if (plotId == null) {
                MessageUtils.sendChatMessage(player, MessageUtils.getError("You are not in a plot").get());
                return 1;
            }

            if (!Plot.isOwner(player, plotId)) {
                MessageUtils.sendChatMessage(player, MessageUtils.getError("You are not the owner of this plot").get());
                return 1;
            }

            DatabaseManager databaseManager = PlotCubic.getDatabaseManager();

            if (!databaseManager.existPlayer(trustedUsername)) {
                MessageUtils.sendChatMessage(player, MessageUtils.getError("The player ")
                        .append(trustedUsername, CommandColors.HIGHLIGHT)
                        .append(" does not exist", CommandColors.ERROR).get());
                return 1;
            }

            if (databaseManager.isDenied(plotId, trustedUsername)) {
                String removeCommand = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], new RemoveCommand().getAlias()[0], trustedUsername);
                MessageUtils.sendChatMessage(player, MessageUtils.getError("The player has deny, to give them trust first you have to use ")
                        .append(removeCommand, CommandColors.HIGHLIGHT).get());
                return 1;
            }

            TrustedPlayer trustedPlayer = databaseManager.getTrusted(plotId, trustedUsername);

            new PermissionsGui().open(player, trustedPlayer);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return "Give some permissions on the plot to a player";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Text getValidUsage() {
        //Command usage: /plot trust <player>

        MessageUtils messageUtils = new MessageUtils().appendInfo("Command usage: ")
                .append(String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "player"));
        return messageUtils.get();
    }
}
