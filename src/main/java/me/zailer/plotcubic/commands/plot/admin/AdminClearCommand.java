package me.zailer.plotcubic.commands.plot.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.plot.ClearCommand;
import me.zailer.plotcubic.gui.ConfirmationGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class AdminClearCommand extends ClearCommand {

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require("plotcubic.command.admin_clear.use"))
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

            if (plotId == null) {
                MessageUtils.sendChatMessage(player, "error.plotcubic.requires.plot");
                return 1;
            }

            this.execute(player, plotId);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    public void execute(ServerPlayerEntity player, PlotID plotId) {
        new ConfirmationGui().open(player, "gui.plotcubic.confirmation.clear.title", List.of("gui.plotcubic.confirmation.admin_clear.info", "gui.plotcubic.confirmation.cant_undone_warning"), () -> {
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.clear.cleaning");
            Plot plot = new Plot(player, plotId);
            plot.clearPlot(player);
        });
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}
