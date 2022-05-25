package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.enums.PlotPermission;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class PermissionsGui {
    private TrustedPlayer trustedPlayer;
    private ServerPlayerEntity ownerPlayer;
    private int page;
    private SimpleGui gui;

    public void open(ServerPlayerEntity ownerPlayer, TrustedPlayer trustedPlayer) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, ownerPlayer, false);
        this.trustedPlayer = trustedPlayer;
        this.ownerPlayer = ownerPlayer;
        this.page = 1;
        this.gui = gui;

        gui.setTitle(new TranslatableText("gui.plotcubic.permissions.title", this.trustedPlayer.username()));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.save();
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        GuiUtils.setGlass(this.gui, 0, 9);
        GuiUtils.setGlass(this.gui, 46, 7);

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
            GuiUtils.setBoolOption(this.gui, slotIndex, permissions[permissionIndex], this.trustedPlayer.permissions());
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

    private void save() {
        boolean successful = PlotCubic.getDatabaseManager().updateTrusted(this.trustedPlayer);
        List<TrustedPlayer> trustedPlayerList = PlotCubic.getDatabaseManager().getAllTrusted(this.trustedPlayer.plotId());
        Plot plot = Plot.getLoadedPlot(this.trustedPlayer.plotId());

        if (plot != null) {
            plot.clearTrusted();
            plot.addTrusted(trustedPlayerList);
        }

        String translationKey = successful ? "text.plotcubic.plot.permission.successful" : "error.plotcubic.permission_gui.saving";
        MessageUtils.sendChatMessage(this.ownerPlayer, translationKey);
    }
}
