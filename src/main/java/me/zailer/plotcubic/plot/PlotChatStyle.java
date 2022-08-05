package me.zailer.plotcubic.plot;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.PlotCubic;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public record PlotChatStyle(String id, String name, String itemId, String message) {
    private static final String PLOT_ID_PLACEHOLDER = "plot_id";
    private static final String USERNAME_PLACEHOLDER = "username";
    private static final String MESSAGE_PLACEHOLDER = "message";

    public Text getMessage(PlotID plotId, ServerPlayerEntity player, Text message) {
        return this.getMessage(plotId, player.getName().getString(), message);
    }

    public Text getMessage(PlotID plotId, String username, Text message) {
        Map<String, Text> placeHolders = Map.of(
                PLOT_ID_PLACEHOLDER, Text.literal(plotId.toString()),
                USERNAME_PLACEHOLDER, Text.literal(username),
                MESSAGE_PLACEHOLDER, message
        );

        return Placeholders.parseText(TextParserUtils.formatText(this.message), Placeholders.PLACEHOLDER_PATTERN_CUSTOM, placeHolders);
    }

    public Text getExample(PlotID plotId, String username) {
        return this.getMessage(plotId, username, Text.literal("Hello world!"));
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public static PlotChatStyle byId(String id) {
        PlotChatStyle[] chatStyles = PlotCubic.getConfig().plotChatStyles();
        for (var chatStyle : chatStyles) {
            if (chatStyle.isId(id))
                return chatStyle;
        }

        return chatStyles[0];
    }

    public boolean hasPermission(ServerPlayerEntity player) {
        return Permissions.check(player, "plotcubic.chatstyle.style." + this.id);
    }
}