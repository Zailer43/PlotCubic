package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClaimCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"claim"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
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

            if (!PlotCubic.getDatabaseManager().claimPlot(plotID.x(), plotID.z(), player.getEntityName())) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.plot.claimed");
                return 1;
            }

            Plot.claim(player, plotID);
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.claimed");

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
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
