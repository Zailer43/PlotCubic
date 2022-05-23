package me.zailer.plotcubic.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import me.zailer.plotcubic.enums.PlotPermission;
import me.zailer.plotcubic.plot.DeniedPlayer;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.TrustedPlayer;
import me.zailer.plotcubic.utils.GuiColors;
import me.zailer.plotcubic.utils.GuiUtils;
import me.zailer.plotcubic.utils.MessageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlotInfoGui {

    public void open(ServerPlayerEntity player, Plot plot) {
        LayeredGui gui = new LayeredGui(ScreenHandlerType.GENERIC_9X6, player, false);
        Layer mainLayer = new Layer(6, 9);

        gui.setTitle(new LiteralText("Plot info"));

        GuiElementBuilder ownerItem = new GuiElementBuilder()
                .setItem(Items.PLAYER_HEAD)
                .setSkullOwner(new GameProfile(UUID.randomUUID(), plot.getOwnerUsername()), player.getServer())
                .setName(new MessageUtils("Plot owner", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils("Username: " + plot.getOwnerUsername(), GuiColors.GREEN).get())
                .addLoreLine(new MessageUtils("Plot ? of ?", GuiColors.GREEN).get());

        GuiElementBuilder plotIdItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new MessageUtils("Plot ID", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils(plot.getPlotID().toString(), GuiColors.GREEN).get());

        GuiElementBuilder claimedDateItem = new GuiElementBuilder()
                .setItem(Items.CLOCK)
                .setName(new MessageUtils("Claimed date", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils(this.getDateFormatted(plot), GuiColors.GREEN).get());

        String trustedPlayerCountFormatted = String.format("Trusted: %s players", plot.getTrusted().size());
        GuiElementBuilder trustedItem = new GuiElementBuilder()
                .setItem(Items.TRIPWIRE_HOOK)
                .setName(new MessageUtils("Trusted", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils("Click for details", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils(trustedPlayerCountFormatted, GuiColors.GREEN).get())
                .setCallback((index, type, action) -> this.addPermissionsLayer(gui, plot.getTrusted()));

        String deniedPlayerCountFormatted = String.format("Denied: %s players", plot.getDeniedPlayers().size());
        GuiElementBuilder deniedItem = new GuiElementBuilder()
                .setItem(Items.BARRIER)
                .setName(new MessageUtils("Denied", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils("Click for details", GuiColors.BLUE).get())
                .addLoreLine(new MessageUtils(deniedPlayerCountFormatted, GuiColors.GREEN).get())
                .setCallback((index, type, action) -> this.addDeniedLayer(gui, plot.getDeniedPlayers(), player.getServer()));

        GameMode gameMode = plot.getGameMode();
        Text gameModeMsg = gameMode == null ? new MessageUtils("Default", GuiColors.GREEN).get() : gameMode.getTranslatableName().copy().setStyle(Style.EMPTY.withColor(GuiColors.GREEN.getColor()));
        GuiElementBuilder gameModeItem = new GuiElementBuilder()
                .setItem(Items.CRAFTING_TABLE)
                .setName(new MessageUtils("Game mode", GuiColors.BLUE).get())
                .addLoreLine(gameModeMsg);

        GuiElementBuilder closeItem = new GuiElementBuilder()
                .setItem(Items.REDSTONE_BLOCK)
                .setName(new MessageUtils("Cancel", GuiColors.RED).get())
                .setCallback((index, type, action) -> gui.close());

        GuiElementBuilder viewInChatItem = new GuiElementBuilder()
                .setItem(Items.PAPER)
                .setName(new MessageUtils("View in chat", GuiColors.BLUE).get())
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
        MessageUtils messageUtils = MessageUtils.getTranslation("text.plotcubic.info.claimed_title")
                .append("text.plotcubic.info.plot_id", plot.getPlotID().toString())
                .append("text.plotcubic.info.claimed_date", this.getDateFormatted(plot))
                .append("text.plotcubic.info.owner", plot.getOwnerUsername())
                .append("text.plotcubic.info.trusted", String.join(", ", plot.getTrusted().stream().map(TrustedPlayer::username).toList()))
                .append("text.plotcubic.info.denied", String.join(", ", plot.getDeniedPlayers().stream().map(DeniedPlayer::username).toList()))
                .append("text.plotcubic.info.game_mode", gameMode == null ? "Default" : gameMode.getName());

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
                .setName(new MessageUtils("Back", GuiColors.RED).get())
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
                    .setName(new MessageUtils(permission.getName(), GuiColors.BLUE).get());

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

            builder.addLoreLine(new MessageUtils(String.format("%-15s%-15s%-15s%-15s", first, second, third, fourth), GuiColors.GREEN).get());
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
                .setName(new MessageUtils("Back", GuiColors.RED).get())
                .setCallback((index, type, action) -> gui.removeLayer(layerView));

        for (int i = 0; i != 54; i++)
            layer.setSlot(i, new GuiElementBuilder().setItem(Items.AIR));

        //TODO: add deny pages
        int size = Math.min(deniedList.size(), 45);
        for (int i = 0; i != size; i++) {
            DeniedPlayer deniedPlayer = sortedDeniedList.get(i);
            String deniedReason = String.format("Reason: %s", deniedPlayer.reason());

            GuiElementBuilder deniedHeadItem = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(new MessageUtils(deniedPlayer.username(), GuiColors.BLUE).get())
                    .setSkullOwner(new GameProfile(UUID.randomUUID(), deniedPlayer.username()), server)
                    .addLoreLine(new MessageUtils(deniedReason, GuiColors.GREEN).get());

            layer.setSlot(i, deniedHeadItem);
        }

        GuiUtils.setGlass(layer, 45, 8);
        layer.setSlot(53, backItem);
    }
}
