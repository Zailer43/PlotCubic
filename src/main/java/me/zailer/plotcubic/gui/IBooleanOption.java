package me.zailer.plotcubic.gui;

import net.minecraft.item.Item;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public interface IBooleanOption {

    String getId();

    Text getDisplayName();

    List<Text> getDescription();

    Item getItem();

    int getCount();

    boolean isHideAttributes();

    boolean hasHeadValue();

    boolean hasGlow();

    @Nullable
    String getHeadValue();
}
