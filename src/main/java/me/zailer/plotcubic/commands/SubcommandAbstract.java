package me.zailer.plotcubic.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class SubcommandAbstract {

    public abstract String[] getAlias();

    public void apply(LiteralArgumentBuilder<ServerCommandSource> command) {
        for (var alias : this.getAlias()) {
            this.apply(command, alias);
        }
    }

    public Text getHelpMessage() {
        String alias = String.join(", ", this.getAlias());
        return MessageUtils.getInfo("Command help")
                .append("Alias", alias)
                .append("Category", this.getCategory().getName())
                .append("Details", this.getHelpDetails())
                .get();
    }

    public abstract void apply(LiteralArgumentBuilder<ServerCommandSource> command, String alias);

    public abstract int execute(CommandContext<ServerCommandSource> serverCommandSource);

    protected abstract String getHelpDetails();

    public abstract CommandCategory getCategory();

    public int executeValidUsages(CommandContext<ServerCommandSource> serverCommandSource) {
        try {
            ServerPlayerEntity player = serverCommandSource.getSource().getPlayer();
            MessageUtils.sendChatMessage(player, this.getValidUsage());

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Text getValidUsage() {
        return null;
    }
}
