package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public record PlotChatStyle(String id, String name, String itemId,
                            String idPrefix, String idSuffix, int idDecor,
                            int idColor, int usernameColor, String messageDelimiter,
                            int delimiterColor, int messageColor) {

    public MutableText getMessage(PlotID plotId, ServerPlayerEntity player, String message) {
        return this.getMessage(plotId, player.getName().getString(), message);
    }
    public MutableText getMessage(PlotID plotId, String username, String message) {
        return new MessageUtils(this.idPrefix, this.idDecor)
                .append(plotId.toString(), this.idColor)
                .append(this.idSuffix, this.idDecor)
                .append(" " + username + " ", this.usernameColor)
                .append(this.messageDelimiter, this.delimiterColor)
                .append(" " + message, this.messageColor)
                .get();
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public static PlotChatStyle byId(String id) {
        for (var chatStyle : PlotCubic.getConfig().plotChatStyles()) {
            if (chatStyle.isId(id))
                return chatStyle;
        }

        return Config.DEFAULT.plotChatStyles()[0];
    }
}