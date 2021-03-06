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
        new ConfirmationGui().open(player, "gui.plotcubic.confirmation.delete.title", List.of("gui.plotcubic.confirmation.admin_delete.info", "gui.plotcubic.confirmation.cant_undone_warning"), () -> {
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.delete.deleting");
            Plot plot = new Plot("", plotId);
            plot.delete();
            PlotCubic.getDatabaseManager().deletePlot(plotId);
        });
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.delete";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}
