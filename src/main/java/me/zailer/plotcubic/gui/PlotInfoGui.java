package me.zailer.plotcubic.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import me.zailer.plotcubic.plot.PlotPermission;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlotInfoGui {

    public void open(ServerPlayerEntity player, Plot plot) {
        LayeredGui gui = new LayeredGui(ScreenHandlerType.GENERIC_9X6, player, false);
        Layer mainLayer = new Layer(6, 9);

        gui.setTitle(new TranslatableText("gui.plotcubic.info.title"));

        GuiElementBuilder ownerItem = new GuiElementBuilder()
                .setItem(Items.PLAYER_HEAD)
                .setSkullOwner(new GameProfile(UUID.randomUUID(), plot.getOwnerUsername()), player.getServer())
                .setName(new TranslatableText("gui.plotcubic.info.owner.title"))
                .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.owner.value", plot.getOwnerUsername()));
//                .addLoreLine(new MessageUtils("Plot ? of ?", GuiColors.GREEN).get());

        GuiElementBuilder plotIdItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new TranslatableText("gui.plotcubic.info.id.title"))
                .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.id.value", plot.getPlotID().toString()));

        GuiElementBuilder claimedDateItem = new GuiElementBuilder()
                .setItem(Items.CLOCK)
                .setName(new TranslatableText("gui.plotcubic.info.claimed_date.title"))
                .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.claimed_date.value", this.getDateFormatted(plot)));

        GuiElementBuilder trustedItem = new GuiElementBuilder()
                .setItem(Items.TRIPWIRE_HOOK)
                .setName(new TranslatableText("gui.plotcubic.info.trusted.title"))
                .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.trusted.count", String.valueOf(plot.getTrusted().size())))
                .addLoreLine(new TranslatableText("gui.plotcubic.click_for_details"))
                .setCallback((index, type, action) -> this.addPermissionsLayer(gui, plot.getTrusted()));

        GuiElementBuilder deniedItem = new GuiElementBuilder()
                .setItem(Items.BARRIER)
                .setName(new TranslatableText("gui.plotcubic.info.denied.title"))
                .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.denied.count", String.valueOf(plot.getDeniedPlayers().size())))
                .addLoreLine(new TranslatableText("gui.plotcubic.click_for_details"))
                .setCallback((index, type, action) -> this.addDeniedLayer(gui, plot.getDeniedPlayers(), player.getServer()));

        GameMode gameMode = plot.getGameMode();
        Text gameModeMsg = gameMode == null ? new TranslatableText("gui.plotcubic.info.game_mode.default") : gameMode.getTranslatableName();
        GuiElementBuilder gameModeItem = new GuiElementBuilder()
                .setItem(Items.CRAFTING_TABLE)
                .setName(new TranslatableText("gui.plotcubic.info.game_mode.title"))
                .addLoreLine(gameModeMsg);

        GuiElementBuilder closeItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new TranslatableText("gui.plotcubic.cancel"))
                .setCallback((index, type, action) -> gui.close());

        GuiElementBuilder viewInChatItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new TranslatableText("gui.plotcubic.info.view_in_chat"))
                .setCallback((index, type, action) -> {
                    this.viewInChat(player, plot);
                    gui.close();
                });

        mainLayer.setSlot(11, ownerItem);
        mainLayer.setSlot(13, plotIdItem);
        mainLayer.setSlot(15, claimedDateItem);
        mainLayer.setSlot(28, trustedItem);
        mainLayer.setSlot(30, deniedItem);
        mainLayer.setSlot(32, gameModeItem);

        mainLayer.setSlot(45, viewInChatItem);
        GuiUtils.setGlass(mainLayer, 46, 7);
        mainLayer.setSlot(53, closeItem);
        gui.addLayer(mainLayer, 0, 0);

        gui.open();
    }

    public void viewInChat(ServerPlayerEntity player, Plot plot) {
        GameMode gameMode = plot.getGameMode();
        String gameModeTranslationKey = gameMode == null ? "gui.plotcubic.info.game_mode.default" : ((TranslatableText) gameMode.getTranslatableName()).getKey();
        MessageUtils messageUtils = MessageUtils.getTranslation("text.plotcubic.info.claimed_title")
                .append("text.plotcubic.info.plot_id", plot.getPlotID().toString())
                .append("text.plotcubic.info.claimed_date", this.getDateFormatted(plot))
                .append("text.plotcubic.info.owner", plot.getOwnerUsername())
                .append("text.plotcubic.info.trusted", String.join(", ", plot.getTrusted().stream().map(TrustedPlayer::username).toList()))
                .append("text.plotcubic.info.denied", String.join(", ", plot.getDeniedPlayers().stream().map(DeniedPlayer::username).toList()))
                .appendTranslations("text.plotcubic.info.game_mode", gameModeTranslationKey);

        MessageUtils.sendChatMessage(player, messageUtils.get());
    }

    public String getDateFormatted(Plot plot) {
        return new SimpleDateFormat("yyyy/MM/dd").format(plot.getClaimedDate()) + " (yyyy/MM/dd)";
    }

    public void addPermissionsLayer(LayeredGui gui, List<TrustedPlayer> trustedPlayerList) {
        Layer layer = new Layer(6, 9);
        LayerView layerView = gui.addLayer(layer, 0, 0);

        GuiElementBuilder backItem = new GuiElementBuilder()
                .setItem(Items.ARROW)
                .setName(new TranslatableText("gui.plotcubic.back"))
                .setCallback((index, type, action) -> gui.removeLayer(layerView));

        for (int i = 0; i != 54; i++)
            layer.setSlot(i, new GuiElementBuilder().setItem(Items.AIR));

        HashMap<PlotPermission, List<String>> trustedByPermissionHashMap = this.getTrustedByPermission(trustedPlayerList);
        List<PlotPermission> permissionList = trustedByPermissionHashMap.keySet().stream().toList();
        int size = Math.min(permissionList.size(), 45);
        for (int i = 0; i != size; i++) {
            PlotPermission permission = permissionList.get(i);
            GuiElementBuilder permissionItem = new GuiElementBuilder()
                    .setItem(permission.getItem())
                    .setName(permission.getDisplayName());

            if (permission.isHideAttributes())
                permissionItem.hideFlags((byte) ItemStack.TooltipSection.MODIFIERS.getFlag());

            this.addTrustedPlayersToLore(permissionItem, trustedByPermissionHashMap.get(permission));
            layer.setSlot(i, permissionItem);
        }

        GuiUtils.setGlass(layer, 45, 8);
        layer.setSlot(53, backItem);
    }

    private HashMap<PlotPermission, List<String>> getTrustedByPermission(List<TrustedPlayer> trustedPlayerList) {
        HashMap<PlotPermission, List<String>> trustedByPermission = new HashMap<>();

        for (var trustedPlayer : trustedPlayerList) {
            for (var permission : trustedPlayer.permissions()) {
                if (!trustedByPermission.containsKey(permission))
                    trustedByPermission.put(permission, new ArrayList<>());

                trustedByPermission.get(permission).add(trustedPlayer.username());
            }
        }

        return trustedByPermission;
    }

    private void addTrustedPlayersToLore(GuiElementBuilder builder, List<String> trustedPlayers) {
        Collections.sort(trustedPlayers);

        for (int i = 0; i < trustedPlayers.size() / 4 + 1; i++) {
            int index = i * 4;
            String first = getOrDefault(trustedPlayers, index);
            String second = getOrDefault(trustedPlayers, index + 1);
            String third = getOrDefault(trustedPlayers, index + 2);
            String fourth = getOrDefault(trustedPlayers, index + 3);

            builder.addLoreLine(new MessageUtils(String.format("%-15s%-15s%-15s%-15s", first, second, third, fourth), MessageUtils.getHighlight()).get());
        }
    }

    private String getOrDefault(List<String> list, int index) {
        if (list.size() > index) {
            String value = list.get(index);
            if (list.size() - 1 != index)
                value += ",";
            return value;
        }
        return "";
    }

    public void addDeniedLayer(LayeredGui gui, List<DeniedPlayer> deniedList, MinecraftServer server) {
        Layer layer = new Layer(6, 9);
        LayerView layerView = gui.addLayer(layer, 0, 0);

        List<DeniedPlayer> sortedDeniedList = deniedList
                .stream()
                .sorted(Comparator.comparing(DeniedPlayer::username))
                .toList();

        GuiElementBuilder backItem = new GuiElementBuilder()
                .setItem(Items.ARROW)
                .setName(new TranslatableText("gui.plotcubic.back"))
                .setCallback((index, type, action) -> gui.removeLayer(layerView));

        for (int i = 0; i != 54; i++)
            layer.setSlot(i, new GuiElementBuilder().setItem(Items.AIR));

        //TODO: add deny pages
        int size = Math.min(deniedList.size(), 45);
        for (int i = 0; i != size; i++) {
            DeniedPlayer deniedPlayer = sortedDeniedList.get(i);

            GuiElementBuilder deniedHeadItem = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(new TranslatableText("gui.plotcubic.info.denied.name", deniedPlayer.username()))
                    .setSkullOwner(new GameProfile(UUID.randomUUID(), deniedPlayer.username()), server)
                    .addLoreLine(MessageUtils.formatArgs("gui.plotcubic.info.denied.reason", deniedPlayer.reason()));

            layer.setSlot(i, deniedHeadItem);
        }

        GuiUtils.setGlass(layer, 45, 8);
        layer.setSlot(53, backItem);
    }
}
