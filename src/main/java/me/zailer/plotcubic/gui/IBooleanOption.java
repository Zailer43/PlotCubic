package me.zailer.plotcubic.gui;

import net.minecraft.item.Item;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public interface IBooleanOption {

    String getId();

    Text getDisplayName();

    Item getItem();

    int getCount();

    boolean isHideAttributes();

    boolean hasHeadValue();

    @Nullable
    String getHeadValue();
}
