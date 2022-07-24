package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class ChatCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"chat", "c"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::executeValidUsages)
                        .then(
                                CommandManager.argument("MESSAGE", StringArgumentType.greedyString())
                                        .executes(this::execute)
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            String message = serverCommandSource.getArgument("MESSAGE", String.class);

            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
            if (plotId == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }
            Plot plot = Plot.getLoadedPlot(plotId);
            if (plot == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }

            plot.sendPlotChatMessage(player, message);

        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.chat";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot chat <message>

        String chatCommand = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "message");

        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", chatCommand);
    }
}
