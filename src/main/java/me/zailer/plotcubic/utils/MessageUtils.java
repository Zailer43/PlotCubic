package me.zailer.plotcubic.utils;

import eu.pb4.placeholders.TextParser;
import eu.pb4.placeholders.util.GeneralUtils;
import eu.pb4.placeholders.util.TextParserUtils;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

public class MessageUtils {
    private final MutableText message;

    public MessageUtils() {
        this.message = LiteralText.EMPTY.copy();
    }

    private MessageUtils(TranslatableText translation) {
        this.message = translation;
    }

    public MessageUtils(String message, int color) {
        this();
        this.append(message, color);
    }

    public static void reloadColors() {
        String hexColorRegex = "^[A-Fa-f\\d]{6}$";
        Config.CustomColors customColors = PlotCubic.getConfig().customColors();
        for (var color : customColors.others()) {
            String colorValue = color.color();
            if (colorValue.matches(hexColorRegex))
                registerColor(color.name(), Integer.valueOf(colorValue, 16));
            else
                PlotCubic.error(String.format("[Config] Color \"%s\" does not have a valid hexadecimal value", color.name()));
        }

        String highlight = customColors.highlight();
        if (!highlight.matches(hexColorRegex)) {
            PlotCubic.error("[Config] Highlight color does not have a valid hexadecimal value");
            customColors.setDefaultHighlight();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void registerColor(String identifier, int color) {
        TextParser.register(
                identifier,
                (tag, data, input, handlers, endAt) -> {
                    GeneralUtils.TextLengthPair out = TextParserUtils.recursiveParsing(input, handlers, endAt);
                    out.text().fillStyle(Style.EMPTY.withColor(color));
                    return out;
                }
        );
    }

    public MessageUtils append(Text text) {
        this.message.append(text);
        return this;
    }

    public void append(String message, int color) {
        this.append(new LiteralText(message).setStyle(Style.EMPTY.withColor(color)));
    }

    public MessageUtils append(String key, String info) {
        this.message.append("\n").append(formatArgs(key, info));
        return this;
    }

    public MessageUtils appendTranslations(String key, String key2) {
        MutableText translation = new TranslatableText(key).append(new TranslatableText(key2)
                .setStyle(Style.EMPTY.withColor(getHighlight())));
        this.message.append("\n").append(translation);
        return this;
    }

    public MutableText get() {
        return this.message;
    }

    public MessageUtils setTooltipMessage(Text text) {
        this.message.setStyle(Style.EMPTY.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(text)));
        return this;
    }

    public static MessageUtils getTranslation(String key) {
        return new MessageUtils(new TranslatableText(key));
    }

    public static void sendChatMessage(ServerPlayerEntity player, MutableText text) {
        player.sendMessage(text, MessageType.CHAT, player.getUuid());
    }

    public static void sendChatMessage(ServerPlayerEntity player, TranslatableText text) {
        sendChatMessage(player, text.getKey(), text.getArgs());
    }

    public static void sendChatMessage(ServerPlayerEntity player, String translationKey) {
        player.sendMessage(new TranslatableText(translationKey), MessageType.CHAT, player.getUuid());
    }

    public static void sendChatMessage(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(formatArgs(key, args), MessageType.CHAT, player.getUuid());
    }

    public static void sendDatabaseConnectionError(ServerPlayerEntity player) {
        PlotCubic.log("[PlotCubic] Failed to get database connection");
        sendChatMessage(player, "error.plotcubic.database.connection");
    }

    public static TranslatableText getMissingPermissionMsg(String translationKey) {
        return MessageUtils.formatArgs("error.plotcubic.not_have_permission", new TranslatableText(translationKey));
    }

    public static void sendMissingPermissionMessage(ServerPlayerEntity player, String translationKey) {
        sendChatMessage(player, getMissingPermissionMsg(translationKey));
    }

    public static TranslatableText formatArgs(String key, Object... args) {
        Object[] textArgs = new Text[args.length];

        for (int i = 0; i != args.length; i++) {
            Text text;

            if (args[i] instanceof Text textArg)
                text = textArg;
            else
                text = new LiteralText(args[i].toString());

            textArgs[i] = text.copy().setStyle(Style.EMPTY.withColor(getHighlight()));
        }

        return new TranslatableText(key, textArgs);
    }

    public static int getHighlight() {
        return Integer.valueOf(PlotCubic.getConfig().customColors().highlight(), 16);
    }
}
