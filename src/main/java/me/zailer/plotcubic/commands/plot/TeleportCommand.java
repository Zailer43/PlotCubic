package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TeleportCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[] { "teleport", "tp" };
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
        ServerWorld world = player.getWorld();
        ServerWorld plotWorld = PlotCubic.getPlotWorldHandle().asWorld();

        if (world == plotWorld) {
            MessageUtils.sendMessage(player, this.getFetchErrorMsg());
            return 0;
        }

        MessageUtils.sendMessage(player, this.getTeleportMsg());

        player.teleport(plotWorld, 0, PlotManager.getInstance().getMaxTerrainHeight() + 2, 0, 0f, 0f);

        return 0;
    }

    private String getFetchErrorMsg() {
        return "error.plotcubic.teleport.already_teleported";
    }

    private String getTeleportMsg() {
        return "text.plotcubic.teleport.teleporting";
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.teleport";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }
}
