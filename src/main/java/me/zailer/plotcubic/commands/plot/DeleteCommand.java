package me.zailer.plotcubic.commands.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.gui.ConfirmationGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class DeleteCommand extends ClearCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"delete", "dispose"};
    }

    @Override
    public void execute(ServerPlayerEntity player, PlotID plotId) {
        new ConfirmationGui().open(player, "Delete plot", List.of("If you accept the plot will be deleted", "This action can not be undone"), () -> {
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.delete.deleting");
            Plot plot = new Plot(player, plotId);
            plot.delete();
            PlotCubic.getDatabaseManager().deletePlot(plotId);
        });
    }

    @Override
    protected String getHelpDetails() {
        return "Delete the plot and you are no longer the owner";
    }
}
