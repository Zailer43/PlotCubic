package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.database.UnitOfWork;
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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.sql.SQLException;

public class ChatStylesGui {
    private PlotChatStyle chatStyleSelected;
    private int selectedIndex;
    private SimpleGui gui;

    public void open(ServerPlayerEntity player, Plot plot) {
        this.gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        this.chatStyleSelected = plot.getChatStyle();
        this.selectedIndex = 0;

        this.gui.setTitle(Text.translatable("gui.plotcubic.chat_style.title"));

        this.addStyles(player, plot);

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(Text.translatable("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.save(player, plot);
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(Text.translatable("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> this.gui.close());

        this.gui.setSlot(45, acceptItem);
        GuiUtils.setGlass(this.gui, 46, 8);
        this.gui.setSlot(53, cancelItem);

        this.gui.open();
    }

    public void addStyles(ServerPlayerEntity player, Plot plot) {
        PlotChatStyle[] chatStyles = PlotCubic.getConfig().plotChatStyles();
        for (int i = 0; i != chatStyles.length; i++) {
            PlotChatStyle chatStyle = chatStyles[i];
            Item item = Registry.ITEM.get(new Identifier(chatStyle.itemId()));

            boolean hasPermission = chatStyle.hasPermission(player);
            GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(hasPermission ? item : Items.GRAY_DYE)
                    .setName(new MessageUtils(chatStyle.name(), MessageUtils.getHighlight()).get())
                    .addLoreLine(chatStyle.getExample(plot.getPlotID(), player.getName().getString()))
                    .addLoreLine(Text.empty())
                    .setCallback((index, type, action) -> this.setCallback(player, index, chatStyle));

            if (this.chatStyleSelected == chatStyle) {
                builder.glow();
                this.selectedIndex = i;
                builder.addLoreLine(Text.translatable("gui.plotcubic.chat_style.selected"));
            } else {
                builder.addLoreLine(Text.translatable("gui.plotcubic.chat_style." + (hasPermission ? "has_permission" : "not_has_permission")));
            }

            this.gui.addSlot(builder);
        }
    }

    private void save(ServerPlayerEntity player, Plot plot) {
        plot.setChatStyle(this.chatStyleSelected);
        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.plotsRepository.updateChatStyle(plot.getPlotID(), this.chatStyleSelected);
                uow.commit();
                MessageUtils.sendMessage(player, "text.plotcubic.plot.chat_style.successful", this.chatStyleSelected.name());
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendMessage(player, "error.plotcubic.database.plot.update_chat_style");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(player);
        }
    }

    private void setCallback(ServerPlayerEntity player, int index, PlotChatStyle chatStyle) {
        if (index == this.selectedIndex || !chatStyle.hasPermission(player))
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
        lore.remove(lore.size() - 1);
        lore.add(NbtString.of(Text.Serializer.toJson(
                Text.translatable("gui.plotcubic.chat_style.selected")
                        .setStyle(Style.EMPTY.withItalic(false)))
        ));
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
        //Note: if the user stops having permission of this style
        // and changes to another one he will temporarily see until he reopens the gui that they have permissions
        lore.add(NbtString.of(Text.Serializer.toJson(
                Text.translatable("gui.plotcubic.chat_style.has_permission")
                        .setStyle(Style.EMPTY.withItalic(false)))
        ));
    }
}
