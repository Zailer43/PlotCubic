package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.commands.plot.toggle.ToggleChatCommand;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class ToggleCommand extends SubcommandAbstract {
    public static final SubcommandAbstract[] SUB_COMMANDS = {
            new ToggleChatCommand(),
    };

    @Override
    public String[] getAlias() {
        return new String[]{"toggle", "t"};
    }

    @Override
    public void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias) {
        LiteralArgumentBuilder<ServerCommandSource> toggleCommand = CommandManager.literal(alias)
                .requires(Permissions.require(this.getCommandPermission()))
                .executes(this::execute);

        for (var subCommand : SUB_COMMANDS)
            subCommand.apply(toggleCommand);

        command.then(toggleCommand);
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
        return "text.plotcubic.help.toggle";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @Override
    public MutableText getValidUsage() {
        //Command usage: /plot toggle <config>
        //Command usage: /plot toggle <config> true/false

        String toggleCommand = String.format("/%s %s <%s>", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "config");
        String toggleCommandWithValue = String.format("/%s %s <%s> %s", PlotCommand.COMMAND_ALIAS[0], this.getAlias()[0], "config", "true/false");

        return MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", toggleCommand)
                .append("\n")
                .append(MessageUtils.formatArgs("text.plotcubic.help.command_usage.generic", toggleCommandWithValue));
    }

}
