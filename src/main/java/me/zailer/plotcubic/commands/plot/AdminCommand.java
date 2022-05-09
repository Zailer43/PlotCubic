package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.commands.plot.admin.ViewReportsCommand;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class AdminCommand extends SubcommandAbstract {
    public static final SubcommandAbstract[] SUB_COMMANDS = {
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

            MessageUtils.sendChatMessage(player, new LiteralText("W I P"));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return "Staff commands :)";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }

}
