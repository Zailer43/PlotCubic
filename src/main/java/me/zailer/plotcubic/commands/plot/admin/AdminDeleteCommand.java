package me.zailer.plotcubic.commands.plot.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.plot.DeleteCommand;
import me.zailer.plotcubic.gui.ConfirmationGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class AdminDeleteCommand extends DeleteCommand {

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require("plotcubic.command.admin_delete.use"))
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;
        PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

        if (plotId == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.requires.plot");
            return 1;
        }

        this.execute(player, plotId);

        return 1;
    }

    public void execute(ServerPlayerEntity player, PlotID plotId) {
        new ConfirmationGui().open(player, "gui.plotcubic.confirmation.delete.title", List.of("gui.plotcubic.confirmation.admin_delete.info", "gui.plotcubic.confirmation.cant_undone_warning"), () -> {
            MessageUtils.sendMessage(player, "text.plotcubic.plot.delete.deleting");
            Plot plot = new Plot("", plotId);
            plot.delete();
            this.deletePlot(player, plotId);
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
