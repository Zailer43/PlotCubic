package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotChatStyle;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ChatStylesGui {
    private PlotChatStyle chatStyleSelected;
    private int selectedIndex;
    private SimpleGui gui;

    public void open(ServerPlayerEntity player, Plot plot) {
        this.gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        PlotChatStyle[] chatStyles = PlotCubic.getConfig().plotChatStyles();
        this.chatStyleSelected = plot.getChatStyle();
        this.selectedIndex = 0;

        this.gui.setTitle(new TranslatableText("gui.plotcubic.chat_style.title"));

        for (int i = 0; i != chatStyles.length; i++) {
            PlotChatStyle chatStyle = chatStyles[i];
            Item item = Registry.ITEM.get(new Identifier(chatStyle.itemId()));

            GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(item)
                    .setName(new MessageUtils(chatStyle.name(), MessageUtils.getHighlight()).get())
                    .addLoreLine(chatStyle.getMessage(plot.getPlotID(), player, "Hello world!"))
                    .setCallback((index, type, action) -> this.setCallback(index, chatStyle));

            if (this.chatStyleSelected == chatStyle) {
                builder.glow();
                this.selectedIndex = i;
                builder.addLoreLine(new TranslatableText("gui.plotcubic.chat_style.selected"));
            }

            this.gui.addSlot(builder);
        }

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.save(player, plot);
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> this.gui.close());

        this.gui.setSlot(45, acceptItem);
        GuiUtils.setGlass(this.gui, 46, 8);
        this.gui.setSlot(53, cancelItem);

        this.gui.open();
    }

    private void save(ServerPlayerEntity player, Plot plot) {
        plot.setChatStyle(this.chatStyleSelected);
        PlotCubic.getDatabaseManager().updateChatStyle(this.chatStyleSelected, plot.getPlotID());
        MessageUtils.sendChatMessage(player,"text.plotcubic.plot.chat_style.successful", this.chatStyleSelected.name());
    }

    private void setCallback(int index, PlotChatStyle chatStyle) {
        if (index == this.selectedIndex)
            return;

        this.updateCurrentSelectedItem(index, chatStyle);
        this.updatePreviousSelectedItem(index);
    }

    private void updateCurrentSelectedItem(int index, PlotChatStyle chatStyle) {
        this.chatStyleSelected = chatStyle;
        ItemStack stack = this.gui.getSlot(index).getItemStack();
        stack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 1);
        stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);

        NbtCompound nbt = stack.getNbt();
        assert nbt != null;
        NbtCompound display = nbt.getCompound(ItemStack.DISPLAY_KEY);
        NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        lore.add(NbtString.of(Text.Serializer.toJson(new TranslatableText("gui.plotcubic.chat_style.selected"))));
    }

    private void updatePreviousSelectedItem(int index) {
        ItemStack stack = this.gui.getSlot(this.selectedIndex).getItemStack();
        this.selectedIndex = index;
        if (!stack.hasNbt())
            return;

        NbtCompound nbt = stack.getNbt();
        assert nbt != null;
        nbt.remove(ItemStack.ENCHANTMENTS_KEY);

        NbtCompound display = nbt.getCompound(ItemStack.DISPLAY_KEY);
        NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        lore.remove(lore.size() - 1);
    }
}
