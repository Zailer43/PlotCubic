package me.zailer.plotcubic.utils;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.Set;

public class GuiUtils {
    public static void setGlass(SlotHolder gui, int index, int count) {
        int finalSlot = index + count;
        for (int i = index; i != finalSlot; i++) {
            gui.setSlot(i, new GuiElementBuilder()
                    .setItem(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(LiteralText.EMPTY.copy())
            );
        }
    }

    public static <BooleanOption extends IBooleanOption> void setBoolOption(SlotHolder gui, int index, BooleanOption option, Set<BooleanOption> trueOptions) {
        gui.setSlot(index, getInfoItem(option));
        gui.setSlot(index + 9, getBoolItem(option, trueOptions));
    }

    private static GuiElementBuilder getInfoItem(IBooleanOption option) {
        GuiElementBuilder builder = new GuiElementBuilder()
                .setItem(option.getItem())
                .setName(new MessageUtils(option.getName(), GuiColors.BLUE).get())
                .setCount(option.getCount());

        if (option.isHideAttributes())
            builder.hideFlags((byte) ItemStack.TooltipSection.MODIFIERS.getFlag());

        if (option.hasHeadValue())
            builder.setSkullOwner(option.getHeadValue());

        return builder;
    }

    private static <BooleanOption extends IBooleanOption> GuiElementBuilder getBoolItem(BooleanOption option, Set<BooleanOption> trueOptions) {
        boolean isOptionTrue = trueOptions.contains(option);

        return new GuiElementBuilder()
                .setItem(getBooleanItem(isOptionTrue))
                .setName(getBooleanName(isOptionTrue))
                .setCallback((index, type, action, gui) -> getBoolCallback(gui, index, option, trueOptions));
    }

    private static <BooleanOption extends IBooleanOption> void getBoolCallback(SlotHolder gui, int index, BooleanOption option, Set<BooleanOption> trueOptions) {
        boolean isOptionTrue = trueOptions.contains(option);
        if (isOptionTrue)
            trueOptions.remove(option);
        else
            trueOptions.add(option);

        ItemStack stack = getBooleanItem(!isOptionTrue).getDefaultStack();
        stack.setCustomName(getBooleanName(!isOptionTrue));

        gui.setSlot(index, stack, (index2, type2, action2, gui2) -> getBoolCallback(gui, index, option, trueOptions));
    }

    private static Item getBooleanItem(boolean value) {
        return value ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
    }

    private static MutableText getBooleanName(boolean value) {
        MessageUtils message = value ? new MessageUtils("True", GuiColors.GREEN) : new MessageUtils("False", GuiColors.RED);
        return message.get();
    }
}
