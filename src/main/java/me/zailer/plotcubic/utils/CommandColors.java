package me.zailer.plotcubic.utils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;

public enum CommandColors implements ColorBranch{
    // https://www.canva.com/colors/color-palettes/swiftness-of-water/
    NORMAL(0x61C2A2),
    ICON(0xCCE0D2),
    ERROR(0x1D617A),
    HIGHLIGHT(0x2C8395);

    private final int color;

    CommandColors(int color) {
        this.color = color;
    }

    @Override
    public MutableText set(String message) {
        return new LiteralText(message).setStyle(Style.EMPTY.withColor(this.color));
    }
}
