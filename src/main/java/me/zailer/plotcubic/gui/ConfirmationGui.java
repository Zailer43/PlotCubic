package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.utils.GuiColors;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.List;

public class ConfirmationGui {

    public void open(ServerPlayerEntity player, String title, List<String> infoList, Runnable accept) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gui.setTitle(new LiteralText(title));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new MessageUtils("Accept", GuiColors.GREEN).get())
                .addLoreLine(new MessageUtils("Shift + right click to accept", GuiColors.BLUE).get())
                .setCallback((index, type, action) -> {
                            if (type == ClickType.MOUSE_RIGHT_SHIFT) {
                                accept.run();
                                gui.close();
                            }
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new MessageUtils("Cancel", GuiColors.RED).get())
                .setCallback((index, type, action) -> gui.close());

        GuiElementBuilder infoItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new MessageUtils("Info", GuiColors.BLUE).get());

        for (var message : infoList)
            infoItem.addLoreLine(new MessageUtils(message, GuiColors.BLUE).get());

        gui.setSlot(11, acceptItem);
        gui.setSlot(13, infoItem);
        gui.setSlot(15, cancelItem);


        gui.open();
    }
}
