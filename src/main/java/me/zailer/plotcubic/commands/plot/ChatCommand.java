package me.zailer.plotcubic.commands.plot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.commands.CommandCategory;
import me.zailer.plotcubic.commands.SubcommandAbstract;
import me.zailer.plotcubic.plot.UserConfig;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ChatCommand extends SubcommandAbstract {
    @Override
    public String[] getAlias() {
        return new String[]{"chat", "c"};
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

            UserConfig userConfig = PlotCubic.getUser(player);

            if (userConfig == null) {
                MessageUtils.sendChatMessage(player, new TranslatableText("error.plotcubic.null_user_config"));
                return 1;
            }
            boolean plotChatEnabled = userConfig.togglePlotChat();
            MessageUtils.sendChatMessage(player, this.getToggleMessage(plotChatEnabled));

        } catch (CommandSyntaxException ignored) {
        }
        return 1;
    }

    @Override
    protected String getHelpDetails() {
        return "Toggle plot chat";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @SuppressWarnings("ConstantConditions")
    private Text getToggleMessage(boolean plotChatEnabled) {
        MessageUtils messageUtils = new MessageUtils()
                .append("Plot chat ");

        if (plotChatEnabled)
            messageUtils.append("enabled", Formatting.GREEN.getColorValue());
        else
            messageUtils.append("disabled", Formatting.RED.getColorValue());

        return messageUtils.get();
    }
}
