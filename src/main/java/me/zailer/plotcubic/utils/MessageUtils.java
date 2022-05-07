package me.zailer.plotcubic.utils;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

    public MessageUtils append(String message) {
        return this.append(message, CommandColors.NORMAL);
    }

    public MessageUtils append(String message, ColorBranch color) {
        this.message.append(color.set(message));
        return this;
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

    public static MessageUtils getError(String message) {
        return new MessageUtils().appendError(message);
    }

    public static MessageUtils getInfo(String message) {
        return new MessageUtils().appendDoubleInfo(message);
    }

    public static void sendChatMessage(ServerPlayerEntity player, Text message) {
        player.sendMessage(message, MessageType.CHAT, player.getUuid());
    }

    public static boolean isNumber(String str){
        return str != null && str.matches("[0-9.]+");
    }
}
