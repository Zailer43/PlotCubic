package me.zailer.plotcubic.utils;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.zailer.plotcubic.gui.IBooleanOption;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;

import java.util.List;
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

    public static <BooleanOption extends IBooleanOption> void setBoolOption(SlotHolder gui, int index, BooleanOption option, Set<BooleanOption> optionsInTrue) {
        gui.setSlot(index, getInfoItem(option));
        gui.setSlot(index + 9, getBoolItem(option, optionsInTrue));
    }

    private static GuiElementBuilder getInfoItem(IBooleanOption option) {
        GuiElementBuilder builder = new GuiElementBuilder()
                .setItem(option.getItem())
                .setName(option.getDisplayName())
                .setCount(option.getCount());

        for (var description : option.getDescription())
            builder.addLoreLine(description);

        if (option.isHideAttributes())
            builder.hideFlags((byte) ItemStack.TooltipSection.MODIFIERS.getFlag());

        if (option.hasGlow())
            builder.glow();

        if (option.hasHeadValue())
            builder.setSkullOwner(option.getHeadValue());

        return builder;
    }

    private static <BooleanOption extends IBooleanOption> GuiElementBuilder getBoolItem(BooleanOption option, Set<BooleanOption> optionsInTrue) {
        boolean isOptionTrue = optionsInTrue.contains(option);

        return new GuiElementBuilder()
                .setItem(getBooleanItem(isOptionTrue))
                .setName(getBooleanName(isOptionTrue))
                .setCallback((index, type, action, gui) -> getBoolCallback(gui, index, option, optionsInTrue));
    }

    private static <BooleanOption extends IBooleanOption> void getBoolCallback(SlotHolder gui, int index, BooleanOption option, Set<BooleanOption> optionsInTrue) {
        boolean isOptionTrue = optionsInTrue.contains(option);
        if (isOptionTrue)
            optionsInTrue.remove(option);
        else
            optionsInTrue.add(option);

        ItemStack stack = getBooleanItem(!isOptionTrue).getDefaultStack();
        stack.setCustomName(getBooleanName(!isOptionTrue));

        gui.setSlot(index, stack, (index2, type2, action2, gui2) -> getBoolCallback(gui, index, option, optionsInTrue));
    }

    private static Item getBooleanItem(boolean value) {
        return value ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
    }

    private static MutableText getBooleanName(boolean value) {
        return new TranslatableText(value ? "gui.plotcubic.true" : "gui.plotcubic.false").setStyle(Style.EMPTY.withItalic(false));
    }

    public static <BooleanOption extends IBooleanOption> void loadPage(SlotHolder gui, int pageIndex, List<BooleanOption> allOptions, Set<BooleanOption> optionsInTrue) {
        for (int i = 9; i != 45; i++)
            gui.clearSlot(i);

        for (int i = 0; i != 18; i++) {
            int optionsIndex = i * pageIndex;
            if (optionsIndex >= allOptions.size())
                return;

            int slotIndex = i + (i > 8 ? 18 : 9);
            GuiUtils.setBoolOption(gui, slotIndex, allOptions.get(optionsIndex), optionsInTrue);
        }
    }
}
