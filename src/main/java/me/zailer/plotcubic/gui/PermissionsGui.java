package me.zailer.plotcubic.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotPermission;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermissionsGui {
    private TrustedPlayer trustedPlayer;
    private ServerPlayerEntity ownerPlayer;

    public void open(ServerPlayerEntity ownerPlayer, TrustedPlayer trustedPlayer) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, ownerPlayer, false);
        this.trustedPlayer = trustedPlayer;
        this.ownerPlayer = ownerPlayer;

        gui.setTitle(Text.translatable("gui.plotcubic.permissions.title", this.trustedPlayer.username()));

        GuiElementBuilder acceptItem = new GuiElementBuilder()
                .setItem(Items.EMERALD_BLOCK)
                .setName(Text.translatable("gui.plotcubic.accept"))
                .setCallback((index, type, action) -> {
                            gui.close();
                            this.save();
                        }
                );

        GuiElementBuilder cancelItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(Text.translatable("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        GuiUtils.setGlass(gui, 0, 9);

        GuiUtils.loadPage(gui, 1, this.getPermissions(), this.trustedPlayer.permissions());

        gui.setSlot(45, acceptItem);
        GuiUtils.setGlass(gui, 46, 7);
        gui.setSlot(53, cancelItem);

        gui.open();
    }

    private List<PlotPermission> getPermissions() {
        List<PlotPermission> permissions = new ArrayList<>();

        for (var permissionKey : PlotPermission.PERMISSION_HASH_MAP.keySet()) {
            if (Permissions.check(this.ownerPlayer, PlotPermission.BASE_PERMISSION_KEY + permissionKey))
                permissions.add(PlotPermission.PERMISSION_HASH_MAP.get(permissionKey));
        }

        return permissions;
    }

    private void save() {
        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.trustedRepository.update(this.trustedPlayer);
                uow.commit();

                this.updateLoadedPlot();

                MessageUtils.sendMessage(this.ownerPlayer, "text.plotcubic.plot.permission.successful");
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendMessage(this.ownerPlayer, "error.plotcubic.database.trust");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(this.ownerPlayer);
        }
    }

    private void updateLoadedPlot() {
        Plot plot = Plot.getLoadedPlot(this.trustedPlayer.plotId());

        if (plot != null) {
            TrustedPlayer trustedPlayer = plot.getTrusted(this.trustedPlayer.username());

            if (trustedPlayer != null)
                trustedPlayer.setPermissions(this.trustedPlayer.permissions());
            else
                plot.addTrusted(this.trustedPlayer);
        }
    }
}
