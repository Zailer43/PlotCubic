package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
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
        return new String[] { "teleport", "t" };
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
            ServerWorld world = player.getWorld();
            ServerWorld plotWorld = PlotCubic.getPlotWorldHandle().asWorld();

            if (world == plotWorld) {
                MessageUtils.sendChatMessage(player, this.getFetchErrorMsg());
                return 0;
            }

            MessageUtils.sendChatMessage(player, this.getTeleportMsg());

            player.teleport(plotWorld, 0, 52, 0, 0f, 0f);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
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
