package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.enums.PlotPermission;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.GuiColors;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.List;

public class PermissionsGui {
    private TrustedPlayer trustedPlayer;
    private ServerPlayerEntity ownerPlayer;
    private int page;
    private SimpleGui gui;

    public PermissionsGui() {
        this.trustedPlayer = null;
    }

    public void open(ServerPlayerEntity ownerPlayer, TrustedPlayer trustedPlayer) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, ownerPlayer, false);
        this.trustedPlayer = trustedPlayer;
        this.ownerPlayer = ownerPlayer;
        this.page = 1;
        this.gui = gui;

        gui.setTitle(new LiteralText("Permissions of " + this.trustedPlayer.username()));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new MessageUtils("Accept", GuiColors.GREEN).get())
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.save();
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new MessageUtils("Cancel", GuiColors.RED).get())
                .setCallback((index, type, action) -> gui.close());

        Utils.setGlass(this.gui, 0, 9);
        Utils.setGlass(this.gui, 46, 7);

        loadPage();

        gui.setSlot(45, acceptItem);
        gui.setSlot(53, cancelItem);

        gui.open();
    }

    private void loadPage() {
        for (int i = 9; i != 45; i++)
            gui.clearSlot(i);

        PlotPermission[] permissions = this.getBooleanPermissions();
        for (int i = 0; i != 18; i++) {
            int permissionIndex = i * this.page;
            if (permissionIndex >= permissions.length)
                return;

            int slotIndex = i + (i > 8 ? 18 : 9);
            this.setOption(slotIndex, permissions[permissionIndex]);
        }

    }

    private PlotPermission[] getBooleanPermissions() {
        return new PlotPermission[]{
                PlotPermission.PLACE_BLOCKS,
                PlotPermission.BREAK_BLOCKS,
                PlotPermission.PLACE_FLUIDS,
                PlotPermission.PLACE_EXPLOSIVES,
                PlotPermission.DAMAGE_ENTITIES,
                PlotPermission.SPAWN_ENTITIES,
                PlotPermission.USE_BOATS,
                PlotPermission.USE_MINECART,
                PlotPermission.SLEEP,
                PlotPermission.OPEN_CONTAINER,
                PlotPermission.DESTROY_CONTAINER,
                PlotPermission.USE_BUTTONS,
                PlotPermission.USE_LEVER,
//                PlotPermission.USE_PRESSURE_PLATE,
                PlotPermission.FILL_MAP
        };
    }

    private void setOption(int index, PlotPermission permission) {
        this.gui.setSlot(index, this.getInfoItem(permission.getItem(), permission.getName()));
        this.gui.setSlot(index + 9, this.getInfoItem(permission));
    }

    private GuiElementBuilder getInfoItem(Item item, String name) {
        return new GuiElementBuilder()
                .setItem(item)
                .setName(new MessageUtils(name, GuiColors.BLUE).get());
    }

    private GuiElementBuilder getInfoItem(PlotPermission permission) {
        boolean hasPermission = this.trustedPlayer.hasPermission(permission);
        GuiElementBuilder builder = new GuiElementBuilder()
                .setItem(this.getBooleanItem(hasPermission))
                .setName(this.getBooleanName(hasPermission))
                .setCallback((index, type, action, gui) -> this.permissionChangeCallback(index, gui, permission));

        if (permission.isHideAttributes())
            builder.hideFlags((byte) 2);

        return builder;
    }

    private void permissionChangeCallback(int index, SlotGuiInterface gui, PlotPermission permission) {
        permissionChangeCallback(permission);
        boolean hasPermission = this.trustedPlayer.hasPermission(permission);

        ItemStack stack = this.getBooleanItem(hasPermission).getDefaultStack();
        stack.setCustomName(this.getBooleanName(hasPermission));

        gui.setSlot(index, stack, (index2, type2, action2, gui2) -> this.permissionChangeCallback(index, gui, permission));
    }

    private Item getBooleanItem(boolean value) {
        return value ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
    }

    private MutableText getBooleanName(boolean value) {
        MessageUtils message = value ? new MessageUtils("True", GuiColors.GREEN) : new MessageUtils("False", GuiColors.RED);
        return message.get();
    }

    private void permissionChangeCallback(PlotPermission permission) {
        if (this.trustedPlayer.hasPermission(permission))
            this.trustedPlayer.removePermission(permission);
        else
            this.trustedPlayer.addPermission(permission);
    }

    private void save() {
        boolean successful = PlotCubic.getDatabaseManager().updateTrusted(this.trustedPlayer);
        List<TrustedPlayer> trustedPlayerList = PlotCubic.getDatabaseManager().getAllTrusted(this.trustedPlayer.plotId());
        Plot plot = Plot.getLoadedPlot(this.trustedPlayer.plotId());

        if (plot != null) {
            plot.clearTrusted();
            plot.addTrusted(trustedPlayerList);
        }

        MessageUtils message = successful ? new MessageUtils("Player permissions saved") : MessageUtils.getError("Error saving player permissions");
        MessageUtils.sendChatMessage(this.ownerPlayer, message.get());
    }
}
