package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.commands.plot.admin.AdminClearCommand;
import me.zailer.plotcubic.commands.plot.admin.AdminDeleteCommand;
import me.zailer.plotcubic.commands.plot.admin.ReloadCommand;
import me.zailer.plotcubic.commands.plot.admin.ViewReportsCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdminCommand extends SubcommandAbstract {
    public static final SubcommandAbstract[] SUB_COMMANDS = {
            new AdminClearCommand(),
            new AdminDeleteCommand(),
            new ReloadCommand(),
            new ViewReportsCommand()
    };

    @Override
    public String[] getAlias() {
        return new String[]{"admin", "staff"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        LiteralArgumentBuilder<ServerCommandSource> adminCommand = CommandManager.literal(alias)
                .requires(Permissions.require(this.getCommandPermission()))
                .executes(this::execute);

        for (var subCommand : SUB_COMMANDS)
            subCommand.apply(adminCommand);

        command.then(adminCommand);
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;

        player.sendMessage(this.getValidUsage());

        return 1;
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.admin";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }

}
