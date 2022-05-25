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
        new ConfirmationGui().open(player, "gui.plotcubic.confirmation.delete.title", List.of("gui.plotcubic.confirmation.delete.info", "gui.plotcubic.confirmation.cant_undone_warning"), () -> {
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.delete.deleting");
            Plot plot = new Plot(player, plotId);
            plot.delete();
            PlotCubic.getDatabaseManager().deletePlot(plotId);
        });
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.delete";
    }
}
