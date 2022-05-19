package me.zailer.plotcubic.utils;

import eu.pb4.placeholders.TextParser;
import eu.pb4.placeholders.util.GeneralUtils;
import eu.pb4.placeholders.util.TextParserUtils;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.util.Arrays;

public class MessageUtils {
    private final MutableText message;

    public MessageUtils() {
        this.message = LiteralText.EMPTY.copy();
    }

    public MessageUtils(String message) {
        this(message, CommandColors.NORMAL);
    }

    public MessageUtils(String message, ColorBranch color) {
        this();
        this.append(message, color);
    }

    public MessageUtils(String message, int color) {
        this();
        this.append(message, color);
    }

    public static void registerPlaceHolderColors() {
        registerColor("normal", 0x61C2A2);
        registerColor("icon", 0xCCE0D2);
        registerColor("error", 0x1D617A);
//        registerColor("highlight", 0x2C8395);
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

    public MessageUtils append(String message) {
        return this.append(message, CommandColors.NORMAL);
    }

    public MessageUtils append(Text text) {
        this.message.append(text);
        return this;
    }

    public MessageUtils append(String message, ColorBranch color) {
        this.message.append(color.set(message));
        return this;
    }

    public MessageUtils append(String message, int color) {
        return this.append(new LiteralText(message).setStyle(Style.EMPTY.withColor(color)));
    }

    public MessageUtils appendWarningIcon() {
        return this.appendIcon("⚠");
    }

    public MessageUtils appendInfoIcon() {
        return this.appendIcon("ℹ");
    }

    private MessageUtils appendIcon(String icon) {
        this.message.append(CommandColors.ICON.set(icon));
        return this;
    }

    public MessageUtils appendError(String message) {
        return this.appendWarningIcon().append(" " + message, CommandColors.ERROR);
    }

    public MessageUtils appendInfo(String message) {
        return this.appendInfoIcon().append(" " + message + " ", CommandColors.HIGHLIGHT);
    }

    public MessageUtils appendDoubleInfo(String message) {
        return this.appendInfoIcon().append(" " + message + " ", CommandColors.HIGHLIGHT).appendInfoIcon();
    }

    public MessageUtils append(String key, String info) {
        return this.append("\n▎ " + key + ": ", CommandColors.HIGHLIGHT).append(info);
    }

    public MutableText get() {
        return this.message;
    }

    public MessageUtils setTooltipMessage(Text text) {
        this.message.setStyle(Style.EMPTY.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(text)));
        return this;
    }

    public static MessageUtils getInfo(String message) {
        return new MessageUtils().appendDoubleInfo(message);
    }

    public static void sendChatMessage(ServerPlayerEntity player, Text message) {
        player.sendMessage(message, MessageType.CHAT, player.getUuid());
    }

    public static void sendChatMessage(ServerPlayerEntity player, String key, String... args) {
        Object[] textArgs = new Text[args.length];

        for (int i = 0; i != args.length; i++)
            textArgs[i] = new LiteralText(args[i]).setStyle(Style.EMPTY.withColor(0x2C8395));

        player.sendMessage(new TranslatableText(key, textArgs), MessageType.CHAT, player.getUuid());
    }
}
