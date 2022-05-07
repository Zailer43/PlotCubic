package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.gui.PlotInfoGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class InfoCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"info", "i"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            this.execute(player);

        } catch (CommandSyntaxException ignored) {
        }

        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return "Used to get information about the plot you are on";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    public void execute(ServerPlayerEntity player) {
        PlotID plotID = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

        if (plotID == null) {
            MessageUtils.sendChatMessage(player, MessageUtils.getError("You are not in a plot").get());
            return;
        }

        Plot plot = PlotCubic.getDatabaseManager().getPlot(plotID);

        if (plot == null)
            MessageUtils.sendChatMessage(player, this.getUnclaimedMessage(plotID));
        else
            new PlotInfoGui().open(player, plot);
    }

    public Text getUnclaimedMessage(PlotID plotID) {
        MessageUtils messageUtils = MessageUtils.getInfo("Plot info (Unclaimed)")
                .append("Plot ID", plotID.toString());

        return messageUtils.get();
    }

}
