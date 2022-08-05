package me.zailer.plotcubic.utils;

import eu.pb4.placeholders.api.node.parent.ColorNode;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.placeholders.impl.textparser.TextParserImpl;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.config.Config;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

public class MessageUtils {
    private final MutableText message;

    public MessageUtils() {
        this.message = Text.empty();
    }

    private MessageUtils(MutableText translation) {
        this.message = translation;
    }

    public MessageUtils(String message, int color) {
        this();
        this.append(message, color);
    }

    public static void reloadColors(Config.CustomColors customColors) {
        String hexColorRegex = "^[A-Fa-f\\d]{6}$";
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
    private static void registerColor(String name, int color) {
        TextParserV1.registerDefault(
                TextParserV1.TextTag.of(
                        name,
                        "color",
                        (tag, data, input, tags, endAt) -> {
                            var out = TextParserImpl.recursiveParsing(input, tags, endAt);
                            return new TextParserV1.TagNodeValue(new ColorNode(out.nodes(), TextColor.fromRgb(color)), out.length());
                        }
                )
        );
    }

    public MessageUtils append(Text text) {
        this.message.append(text);
        return this;
    }

    public void append(String message, int color) {
        this.append(Text.literal(message).setStyle(Style.EMPTY.withColor(color)));
    }

    public MessageUtils append(String key, String info) {
        this.message.append("\n").append(formatArgs(key, info));
        return this;
    }

    public MessageUtils appendTranslations(String key, String key2) {
        MutableText translation = Text.translatable(key).append(Text.translatable(key2)
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
        return new MessageUtils(Text.translatable(key));
    }

    public static void sendMessage(ServerPlayerEntity player, String translationKey) {
        player.sendMessage(Text.translatable(translationKey));
    }

    public static void sendMessage(ServerPlayerEntity player, String translationKey, Object... args) {
        player.sendMessage(formatArgs(translationKey, args));
    }

    public static void sendDatabaseConnectionError(ServerPlayerEntity player) {
        PlotCubic.log("[PlotCubic] Failed to get database connection");
        player.sendMessage(getTranslation("error.plotcubic.database.connection").get());
    }

    public static MutableText getMissingPermissionMsg(String translationKey) {
        return MessageUtils.formatArgs("error.plotcubic.not_have_permission", Text.translatable(translationKey));
    }

    public static void sendMissingPermissionMessage(ServerPlayerEntity player, String translationKey) {
        player.sendMessage(getMissingPermissionMsg(translationKey));
    }

    public static MutableText formatArgs(String key, Object... args) {
        Object[] textArgs = new Text[args.length];

        for (int i = 0; i != args.length; i++) {
            Text text = args[i] instanceof Text textArg ? textArg : Text.literal(args[i].toString());

            textArgs[i] = text.copyContentOnly().setStyle(Style.EMPTY.withColor(getHighlight()));
        }

        return Text.translatable(key, textArgs);
    }

    public static int getHighlight() {
        return Integer.valueOf(PlotCubic.getConfig().customColors().highlight(), 16);
    }
}
