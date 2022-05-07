package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.utils.CommandColors;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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
                MessageUtils.sendChatMessage(player, getFetchErrorMsg());
                return 0;
            }

            MessageUtils.sendChatMessage(player, getTeleportMsg());

            player.teleport(plotWorld, 0, 52, 0, 0f, 0f);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Text getFetchErrorMsg() {
        return CommandColors.ERROR.set("You are already in the Plot World.");
    }

    private MutableText getTeleportMsg() {
        return CommandColors.NORMAL.set("Teleporting to the Plot World...");
    }

    @Override
    protected String getHelpDetails() {
        return "Teleports you to the Plot World dimension.";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }
}
