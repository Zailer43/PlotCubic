package me.zailer.plotcubic.commands.plot.toggle;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractToggleableCommand extends SubcommandAbstract {

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        command.then(
                CommandManager.literal(alias)
                        .requires(Permissions.require(this.getCommandPermission()))
                        .executes(this::execute)
                        .then(
                                CommandManager.literal("true")
                                        .executes(context -> this.execute(context, context.getSource().getPlayerOrThrow(), true))
                        ).then(
                                CommandManager.literal("false")
                                        .executes(context -> this.execute(context, context.getSource().getPlayerOrThrow(), false))
                        )
        );
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;

        boolean isToggled = this.isToggled(serverCommandSource, player);
        this.execute(serverCommandSource, player, !isToggled);

        return 1;
    }

    public abstract int execute(CommandContext<ServerCommandSource> serverCommandSource, ServerPlayerEntity player, boolean isEnabled);

    public abstract boolean isToggled(CommandContext<ServerCommandSource> serverCommandSource, ServerPlayerEntity player);

    protected abstract String getToggleTranslationKey(boolean isEnabled);

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @Override
    public String getCommandPermission() {
        return "plotcubic.command.toggle." + this.getAlias()[0] + ".use";
    }
}
