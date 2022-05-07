package me.zailer.plotcubic.utils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;

public enum GuiColors implements ColorBranch{
    RED(0xA06767),
    GREEN(0x67A067),
    BLUE(0x6767A0),
    YELLOW(0xA0A067);

    private final int color;

    GuiColors(int color) {
        this.color = color;
    }

    @Override
    public MutableText set(String message) {
        return new LiteralText(message).setStyle(Style.EMPTY.withColor(this.color));
    }
}
