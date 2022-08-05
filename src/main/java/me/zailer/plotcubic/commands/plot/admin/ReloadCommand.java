package me.zailer.plotcubic.commands.plot.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.events.ReloadEvent;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.stimuli.Stimuli;

import java.io.IOException;

public class ReloadCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"reload"};
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
        String translationKey;
        try {
            PlotCubic.getConfigManager().reload();
            try (var invokers = Stimuli.select().at(PlotCubic.getPlotWorldHandle().asWorld(), player.getBlockPos())) {
                invokers.get(ReloadEvent.EVENT).onReload(PlotCubic.getConfig());
            }
            translationKey = "text.plotcubic.config.reloaded";
        } catch (IOException e) {
            e.printStackTrace();
            translationKey = "error.plotcubic.reloading_config";
        }
        MessageUtils.sendMessage(player, translationKey);

        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.admin.reload";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }
}
