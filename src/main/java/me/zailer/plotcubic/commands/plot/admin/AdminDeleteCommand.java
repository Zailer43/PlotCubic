package me.zailer.plotcubic.commands.plot.admin;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.gui.ConfirmationGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class AdminDeleteCommand extends AdminClearCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"delete", "dispose"};
    }

    public void execute(ServerPlayerEntity player, PlotID plotId) {
        new ConfirmationGui().open(player, "Delete plot", List.of("If you accept the user's plot it will be deleted", "This action can not be undone"), () -> {
            MessageUtils.sendChatMessage(player, new MessageUtils("Deleting plot...").get());
            Plot plot = new Plot("", plotId);
            plot.delete();
            PlotCubic.getDatabaseManager().deletePlot(plotId);
        });
    }

    @Override
    protected String getHelpDetails() {
        return "Delete the plot and you are no longer the owner";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}
