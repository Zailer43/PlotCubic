package me.zailer.plotcubic.plot;

import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import eu.pb4.placeholders.util.TextParserUtils;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.regex.Matcher;

public record PlotChatStyle(String id, String name, String itemId, String message) {
    private static final String PLOT_ID_PLACEHOLDER = "plot_id";
    private static final String USERNAME_PLACEHOLDER = "username";
    private static final String MESSAGE_PLACEHOLDER = "message";

    public MutableText getMessage(PlotID plotId, ServerPlayerEntity player, String message) {
        return this.getMessage(plotId, player.getName().getString(), message);
    }

    public MutableText getMessage(PlotID plotId, String username, String message) {
        message = this.escapeFormatting(message);

        Map<String, Text> placeHolders = Map.of(
                PLOT_ID_PLACEHOLDER, new LiteralText(plotId.toString()),
                USERNAME_PLACEHOLDER, new LiteralText(username),
                MESSAGE_PLACEHOLDER, new LiteralText(message)
        );

        return (MutableText) PlaceholderAPI.parsePredefinedText(TextParser.parse(this.message), PlaceholderAPI.PLACEHOLDER_PATTERN_CUSTOM, placeHolders);
    }

    @SuppressWarnings("UnstableApiUsage")
    public String escapeFormatting(String message) {
        Matcher matcher = TextParserUtils.STARTING_PATTERN.matcher(message);

        while (matcher.find()) {
            String match = matcher.group(0);
            String replace = match.replace("\\", "");
            replace = replace.replace(">", "\\>");
            message = message.replace(match, replace);
        }

        return message;
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