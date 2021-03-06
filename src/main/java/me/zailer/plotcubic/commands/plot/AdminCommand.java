package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.commands.plot.admin.AdminClearCommand;
import me.zailer.plotcubic.commands.plot.admin.AdminDeleteCommand;
import me.zailer.plotcubic.commands.plot.admin.ReloadCommand;
import me.zailer.plotcubic.commands.plot.admin.ViewReportsCommand;
import me.zailer.plotcubic.utils.MessageUtils;
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
                .requires(source -> source.hasPermissionLevel(4))
                .executes(this::execute);

        for (var subCommand : SUB_COMMANDS)
            subCommand.apply(adminCommand);

        command.then(adminCommand);
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();

            MessageUtils.sendChatMessage(player, this.getValidUsage());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
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
