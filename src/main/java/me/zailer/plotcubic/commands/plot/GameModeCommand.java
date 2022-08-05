package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.CommandSuggestions;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.world.GameMode;

import java.sql.SQLException;
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
                        .requires(Permissions.require(this.getCommandPermission()))
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
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;
        GameMode gameMode = GameMode.byName(serverCommandSource.getArgument("GAMEMODE", String.class), null);

        if (gameMode == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.invalid_game_mode");
            return 1;
        }

        if (!Permissions.check(player, "plotcubic.command.gamemode." + gameMode.getName())) {
            MessageUtils.sendMissingPermissionMessage(player, "permission.plotcubic.command.gamemode.game_mode");
            return 1;
        }

        PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

        if (plotId == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.requires.plot");
            return 1;
        }

        if (!Plot.isOwner(player, plotId)) {
            MessageUtils.sendMessage(player, "error.plotcubic.requires.plot_owner");
            return 1;
        }

        Plot plot = Plot.getLoadedPlot(plotId);
        if (plot == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.plot.not_loaded");
            return 1;
        }

        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.plotsRepository.updateGameMode(plotId, gameMode);
                uow.commit();

                plot.setGameMode(gameMode);
                MessageUtils.sendMessage(player, "text.plotcubic.plot.game_mode.successful");
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendMessage(player, "error.plotcubic.database.game_mode");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(player);
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
