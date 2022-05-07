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
import net.minecraft.text.Text;

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
                MessageUtils.sendChatMessage(player, MessageUtils.getError("You are not in a plot").get());
                return 1;
            }

            if (!PlotCubic.getDatabaseManager().claimPlot(plotID.x(), plotID.z(), player.getEntityName())) {
                MessageUtils.sendChatMessage(player, MessageUtils.getError("This plot is already claimed").get());
                return 1;
            }

            Plot.claim(player, plotID);
            MessageUtils.sendChatMessage(player, this.getClaimedMessage());

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return """
                Used to claim parcels
                "this" to get where you are standing
                "auto" to get the closest available to 0;0
                "near" to get the closest available to where you are""";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    public Text getClaimedMessage() {
        return new MessageUtils("Successfully claimed").get();
    }

}
