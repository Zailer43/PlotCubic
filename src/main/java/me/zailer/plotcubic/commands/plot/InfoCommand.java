package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.gui.PlotInfoGui;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class InfoCommand extends SubcommandAbstract {

    @Override
    public String[] getAlias() {
        return new String[]{"info", "i"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::execute)
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;

        PlotID plotID = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());

        if (plotID == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.requires.plot");
            return 0;
        }

        Plot plot = Plot.getPlot(plotID);

        if (plot == null)
            player.sendMessage(this.getUnclaimedMessage(plotID));
        else if (!plot.isOwner(player) && !Permissions.check(player, "plotcubic.command.info.use_without_owner"))
            MessageUtils.sendMissingPermissionMessage(player, "permission.plotcubic.command.info.use_without_owner");
        else
            new PlotInfoGui().open(player, plot);

        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.info";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    public MutableText getUnclaimedMessage(PlotID plotID) {
        MessageUtils messageUtils = MessageUtils.getTranslation("text.plotcubic.info.unclaimed_title")
                .append("text.plotcubic.info.plot_id", plotID.toString());

        return messageUtils.get();
    }

}
