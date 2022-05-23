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
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.world.GameMode;

import java.util.Arrays;

public class GameModeCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"gamemode", "gm"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .executes(this::executeValidUsages)
                        .then(
                                CommandManager.argument("GAMEMODE", StringArgumentType.word())
                                        .suggests(CommandSuggestions.GAME_MODE_SUGGESTION)
                                        .executes(this::execute)
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            GameMode gameMode = GameMode.byName(serverCommandSource.getArgument("GAMEMODE", String.class), null);

            if (gameMode == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.invalid_game_mode");
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

            Plot plot = Plot.getLoadedPlot(plotId);
            if (plot == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.not_loaded");
                return 1;
            }

            plot.setGameMode(gameMode);
            PlotCubic.getDatabaseManager().updateGameMode(gameMode, plotId);
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.game_mode.successful");


        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot gamemode [adventure/creative/spectator/survival]

        String options = String.join("/", Arrays.stream(GameMode.values()).map(GameMode::getName).sorted().toList());
        String command = String.format("/%s %s [%s]", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], options);
        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", command);
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.game_mode";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

}
