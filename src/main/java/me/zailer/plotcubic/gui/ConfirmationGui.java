package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class ConfirmationGui {

    public void open(ServerPlayerEntity player, String titleTranslationKey, List<String> infoTranslationList, Runnable accept) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gui.setTitle(new TranslatableText(titleTranslationKey));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.accept"))
                .addLoreLine(new TranslatableText("gui.plotcubic.confirmation.shift_right_click"))
                .setCallback((index, type, action) -> {
                            if (type == ClickType.MOUSE_RIGHT_SHIFT) {
                                accept.run();
                                gui.close();
                            }
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        GuiElementBuilder infoItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new TranslatableText("gui.plotcubic.confirmation.info"));

        for (var translationKey : infoTranslationList)
            infoItem.addLoreLine(new TranslatableText(translationKey));

        gui.setSlot(11, acceptItem);
        gui.setSlot(13, infoItem);
        gui.setSlot(15, cancelItem);


        gui.open();
    }
}
