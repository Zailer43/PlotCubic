package me.zailer.plotcubic.gui;

import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;

public interface IBooleanOption {

    String getTranslationKey();

    Text getDisplayName();

    Item getItem();

    int getCount();

    boolean isHideAttributes();

    boolean hasHeadValue();

    @Nullable
    String getHeadValue();
}
