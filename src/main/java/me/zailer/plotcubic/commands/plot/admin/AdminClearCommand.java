package me.zailer.plotcubic.commands.plot.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.gui.ConfirmationGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class AdminClearCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"clear"};
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
            PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

            if (plotId == null) {
                MessageUtils.sendChatMessage(player, MessageUtils.getError("You are not in a plot").get());
                return 1;
            }

            this.execute(player, plotId);
        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    public void execute(ServerPlayerEntity player, PlotID plotId) {
        new ConfirmationGui().open(player, "Clear plot", List.of("If you accept the user's plot it will be cleaned", "This action can not be undone"), () -> {
            MessageUtils.sendChatMessage(player, new MessageUtils("Cleaning plot...").get());
            Plot plot = new Plot(player, plotId);
            plot.clearPlot();
        });
    }

    @Override
    protected String getHelpDetails() {
        return "Clean the plot and leave it as new";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}