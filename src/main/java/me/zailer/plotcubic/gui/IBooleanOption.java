package me.zailer.plotcubic.gui;

import net.minecraft.item.Item;

import javax.annotation.Nullable;

public interface IBooleanOption {

    String getName();

    Item getItem();

    int getCount();

    boolean isHideAttributes();

    boolean hasHeadValue();

    @Nullable
    String getHeadValue();
}
