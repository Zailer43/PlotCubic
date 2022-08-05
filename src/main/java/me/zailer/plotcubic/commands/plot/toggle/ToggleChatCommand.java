package me.zailer.plotcubic.commands.plot.toggle;

import com.mojang.brigadier.context.CommandContext;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.plot.UserConfig;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ToggleChatCommand extends AbstractToggleableCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"chat", "c"};
    }

    @Override
    protected String getToggleTranslationKey(boolean plotChatEnabled) {
        return plotChatEnabled ? "text.plotcubic.plot.chat.enabled" : "text.plotcubic.plot.chat.disabled";
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> serverCommandSource, ServerPlayerEntity player, boolean isEnabled) {
        UserConfig userConfig = PlotCubic.getUser(player);

        if (userConfig == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.null_user_config");
            return 1;
        }

        userConfig.setPlotChat(isEnabled);
        MessageUtils.sendMessage(player, this.getToggleTranslationKey(isEnabled));

        return 0;
    }

    @Override
    public boolean isToggled(CommandContext<ServerCommandSource> serverCommandSource, ServerPlayerEntity player) {
        UserConfig userConfig = PlotCubic.getUser(player);

        if (userConfig == null) {
            MessageUtils.sendMessage(player, "error.plotcubic.null_user_config");
            return false;
        }

        return userConfig.isPlotChatEnabled();
    }

    @Override
    protected String getHelpTranslationKey() {
        return "text.plotcubic.help.chat";
    }

}
