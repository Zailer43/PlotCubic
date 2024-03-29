package me.zailer.plotcubic.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public abstract class SubcommandAbstract {

    public abstract String[] getAlias();

    public void apply(LiteralArgumentBuilder<ServerCommandSource> command) {
        for (var alias : this.getAlias()) {
            this.apply(command, alias);
        }
    }

    public MutableText getHelpMessage() {
        String alias = String.join(", ", this.getAlias());
        return MessageUtils.getTranslation("text.plotcubic.help.subcommand.help_title")
                .append("text.plotcubic.help.subcommand.alias", alias)
                .append("text.plotcubic.help.subcommand.category", this.getCategory().getName())
                .appendTranslations("text.plotcubic.help.subcommand.details", this.getHelpTranslationKey())
                .get();
    }

    public abstract void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias);

    public abstract int execute(CommandContext<ServerCommandSource> serverCommandSource);

    protected abstract String getHelpTranslationKey();

    public abstract CommandCategory getCategory();

    public int executeValidUsages(CommandContext<ServerCommandSource> serverCommandSource) {
        ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
        if (player == null)
            return 0;

        player.sendMessage(this.getValidUsage());

        return 0;
    }

    public MutableText getValidUsage() {
        return null;
    }

    public String getCommandPermission() {
        return "plotcubic.command." + this.getAlias()[0] + ".use";
    }
}
