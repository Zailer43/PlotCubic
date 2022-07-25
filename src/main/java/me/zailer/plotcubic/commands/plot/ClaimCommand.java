package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.SQLException;

public class ClaimCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"claim"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::execute)
//                        .then(CommandManager.literal("auto").executes(this::executeAuto))
//                        .then(CommandManager.literal("near).executes(this::executeNear))
                        .then(CommandManager.literal("this").executes(this::execute))
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();

            PlotID plotID = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

            if (plotID == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }

            try (var uow = new UnitOfWork()) {
                try {
                    if (uow.plotsRepository.exists(plotID)) {
                        MessageUtils.sendChatMessage(player, "error.plotcubic.plot.already_claimed");
                        return 1;
                    }

                    uow.beginTransaction();
                    uow.plotsRepository.add(plotID, player.getName().getString());
                    uow.commit();
                    Plot.claim(player, plotID);
                    MessageUtils.sendChatMessage(player, "text.plotcubic.plot.claimed");
                } catch (SQLException e) {
                    uow.rollback();
                    MessageUtils.sendChatMessage(player, "error.plotcubic.database.plot.claim");
                }
            } catch (Exception ignored) {
                MessageUtils.sendDatabaseConnectionError(player);
            }

        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.claim";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

}
