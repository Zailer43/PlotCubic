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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
                MessageUtils.sendChatMessage(player, new TranslatableText("error.plotcubic.invalid_game_mode"));
                return 1;
            }

            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

            if (plotId == null) {
                MessageUtils.sendChatMessage(player, new TranslatableText("error.plotcubic.requires.plot"));
                return 1;
            }

            if (!Plot.isOwner(player, plotId)) {
                MessageUtils.sendChatMessage(player, new TranslatableText("error.plotcubic.requires.plot_owner"));
                return 1;
            }

            Plot plot = Plot.getLoadedPlot(plotId);
            if (plot == null) {
                MessageUtils.sendChatMessage(player, new TranslatableText("error.plotcubic.plot.not_loaded"));
                return 1;
            }

            plot.setGameMode(gameMode);
            PlotCubic.getDatabaseManager().updateGameMode(gameMode, plotId);
            MessageUtils.sendChatMessage(player, new MessageUtils("Plot game mode changed").get());


        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public Text getValidUsage() {
        //Command usage: /plot gamemode [adventure/creative/spectator/survival]
        String options = String.join("/", Arrays.stream(GameMode.values()).map(GameMode::getName).sorted().toList());
        return new MessageUtils().appendInfo("Command usage: ")
                .append(String.format("/%s %s [%s]", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], options))
                .get();
    }

    @Override
    protected String getHelpDetails() {
        return "Change the game mode of the players on the plot";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

}
