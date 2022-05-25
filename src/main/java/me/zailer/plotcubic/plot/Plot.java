package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.plot.ChatCommand;
import me.zailer.plotcubic.generator.PlotworldSettings;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Plot {
    public static final List<Plot> loadedPlots = new ArrayList<>();
    private final String ownerUsername;
    private final PlotID plotID;
    private final List<TrustedPlayer> trustedPlayers;
    private final List<DeniedPlayer> deniedPlayers;
    private final List<ServerPlayerEntity> players;
    private final Date claimedDate;
    private GameMode gameMode;
    private PlotChatStyle chatStyle;

    public Plot(String username, PlotID plotID, List<TrustedPlayer> trustedPlayers, List<DeniedPlayer> deniedPlayers, Date claimedDate, GameMode gameMode, PlotChatStyle chatStyle) {
        this.ownerUsername = username;
        this.plotID = plotID;
        this.trustedPlayers = trustedPlayers;
        this.deniedPlayers = deniedPlayers;
        this.players = new ArrayList<>();
        this.claimedDate = claimedDate;
        this.gameMode = gameMode;
        this.chatStyle = chatStyle;
    }

    public Plot(ServerPlayerEntity player, PlotID plotID) {
        this(player.getName().getString(), plotID);
    }

    public Plot(String ownerUsername, PlotID plotID) {
        this(ownerUsername, plotID, new ArrayList<>(), new ArrayList<>(), null, null, PlotCubic.getConfig().plotChatStyles()[0]);
    }

    public static void claim(ServerPlayerEntity player, PlotID plotID) {
        Plot plot = new Plot(player, plotID);
        plot.setBorder(PlotManager.getInstance().getSettings().getClaimedBorderBlock());
        Plot.loadPlot(player, plot);
    }

    public void delete() {
        this.setBorder(PlotManager.getInstance().getSettings().getUnclaimedBorderBlock());
        this.clearPlot();
    }

    public void setBorder(BlockState block) {
        new Thread(() -> {
            PlotworldSettings settings = PlotManager.getInstance().getSettings();
            int plotSize = settings.getPlotSize() + 1;
            int y = settings.getMaxHeight() + 1;
            int x = this.plotID.getXPos() - plotSize;
            int z = this.plotID.getZPos() - plotSize + 1;

            Utils.fillWithDimensions(block, ++x, y, z, plotSize, 0, 0);
            Utils.fillWithDimensions(block, x, y, z, 0, 0, plotSize);

            x += plotSize;
            z += plotSize;

            Utils.fillWithDimensions(block, x, y, z, -plotSize, 0, 0);
            Utils.fillWithDimensions(block, x, y, z, 0, 0, -plotSize);
        }).start();
    }

    public void clearPlot() {
        new Thread(() -> {
            this.removeEntities();
            PlotworldSettings settings = PlotManager.getInstance().getSettings();
            ChunkGenerator chunkGenerator = PlotCubic.getPlotWorldHandle().asWorld().getChunkManager().getChunkGenerator();
            int plotSize = settings.getPlotSize() - 1;
            int y = chunkGenerator.getWorldHeight();
            int x = this.plotID.getXPos() - plotSize;
            int z = this.plotID.getZPos() - plotSize;
            List<FlatChunkGeneratorLayer> layers = new ArrayList<>(List.copyOf(settings.getLayers()));

            int airHeight = chunkGenerator.getWorldHeight() - settings.getMaxHeight();
            Utils.fillWithDimensions(Blocks.AIR.getDefaultState(), x, y, z, plotSize, -airHeight, plotSize);
            y -= airHeight;


            for (int i = layers.size() - 1; i != -1; i--) {
                Utils.fillWithDimensions(layers.get(i).getBlockState(), x, y, z, plotSize, -(layers.get(i).getThickness() - 1), plotSize);
                y -= layers.get(i).getThickness();
            }

        }).start();
    }

    public boolean isId(@NotNull PlotID id) {
        return this.plotID.equals(id);
    }

    public boolean isOwner(ServerPlayerEntity player) {
        return this.ownerUsername.equals(player.getName().getString());
    }

    public static boolean isOwner(ServerPlayerEntity player, PlotID plotId) {
        Plot plot = getPlot(plotId);

        if (plot == null)
            return false;

        return plot.isOwner(player);
    }

    public PlotID getPlotID() {
        return this.plotID;
    }

    public String getOwnerUsername() {
        return this.ownerUsername;
    }

    public void addTrusted(List<TrustedPlayer> usernameList) {
        this.trustedPlayers.addAll(usernameList);
    }

    public void clearTrusted() {
        this.trustedPlayers.clear();
    }

    public List<TrustedPlayer> getTrusted() {
        return this.trustedPlayers;
    }

    @Nullable
    public TrustedPlayer getTrustedPermissions(ServerPlayerEntity player) {
        for (var trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.isPlayer(player)) {
                return trustedPlayer;
            }
        }

        return null;
    }

    public static void loadPlot(ServerPlayerEntity player, @Nullable Plot plot) {
        if (plot == null)
            return;

        plot.addPlayer(player);
        if (Plot.loadedPlotsContains(plot))
            return;

        Plot.loadedPlots.add(plot);
    }

    public static void unloadPlot(ServerPlayerEntity player, @Nullable Plot plot) {
        if (plot == null)
            return;

        if (plot.getPlayersCount() > 1)
            plot.removePlayer(player);
        else
            loadedPlots.remove(plot);
    }

    public static boolean loadedPlotsContains(Plot plot) {
        return loadedPlots.contains(plot);
    }

    @Nullable
    public static Plot getLoadedPlot(PlotID plotId) {
        for (var plot : loadedPlots) {
            if (plot.plotID.equals(plotId))
                return plot;
        }

        return null;
    }

    public Date getClaimedDate() {
        return this.claimedDate;
    }

    public void addDenied(List<DeniedPlayer> deniedPlayerList) {
        this.deniedPlayers.addAll(deniedPlayerList);
    }

    public void addDenied(DeniedPlayer deniedPlayer) {
        this.deniedPlayers.add(deniedPlayer);
    }

    public List<DeniedPlayer> getDeniedPlayers() {
        return this.deniedPlayers;
    }


    public boolean hasDeny(ServerPlayerEntity player) {
        return this.hasDeny(player.getName().getString());
    }

    public boolean hasDeny(String username) {
        for (var deniedPlayer : this.deniedPlayers) {
            if (deniedPlayer.isPlayer(username))
                return true;
        }
        return false;
    }

    public boolean removeTrust(String username) {
        for (var trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.isPlayer(username)) {
                this.trustedPlayers.remove(trustedPlayer);
                return true;
            }
        }
        return false;
    }

    public boolean removeDeny(String username) {
        for (var deniedPlayer : this.deniedPlayers) {
            if (deniedPlayer.isPlayer(username)) {
                this.deniedPlayers.remove(deniedPlayer);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Plot getPlot(PlotID plotId) {
        for (var plot : loadedPlots) {
            if (plot.plotID.equals(plotId))
                return plot;
        }

        return PlotCubic.getDatabaseManager().getPlot(plotId);
    }

    public boolean visit(ServerPlayerEntity player) {
        if (this.hasDeny(player))
            return false;

        PlotID plotId = this.getPlotID();
        player.teleport(player.getWorld(), plotId.getSpawnOfX(), plotId.getSpawnOfY(), plotId.getSpawnOfZ(), 0, 0);
        return true;
    }

    @Nullable
    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;

        for (var player : this.players)
            player.changeGameMode(gameMode);
    }

    public void removePlayer(ServerPlayerEntity player) {
        this.players.remove(player);
    }

    public void addPlayer(ServerPlayerEntity player) {
        this.players.add(player);
    }

    public int getPlayersCount() {
        return this.players.size();
    }

    public void sendPlotChatMessage(ServerPlayerEntity sender, String message) {
        boolean isPlotEmpty = this.players.size() <= 1;
        MutableText messageText = this.chatStyle.getMessage(this.plotID, sender, message);

        if (isPlotEmpty) {
            String tooltipMessage = String.format(
                    """
                            The plot is currently empty
                            No one else can read the message you sent
                            If you want everyone to read you, you can use /%s %s""",
                    PlotCommand.COMMAND_ALIAS[0],
                    new ChatCommand().getAlias()[0]
            );

            messageText = MessageUtils.getTranslation("text.plotcubic.chat_style.warning_empty_plot")
                    .append(messageText)
                    .setTooltipMessage(new LiteralText(tooltipMessage))
                    .get();
        }

        for (var player : this.players)
            MessageUtils.sendChatMessage(player, messageText);

        PlotCubic.log(messageText.getString());
    }

    public void setChatStyle(PlotChatStyle chatStyle) {
        this.chatStyle = chatStyle;
    }

    public PlotChatStyle getChatStyle() {
        return this.chatStyle;
    }

    public void removeEntities() {
        Iterable<Entity> entities = PlotCubic.getPlotWorldHandle().asWorld().iterateEntities();

        for (var entity : entities) {
            if (entity instanceof ServerPlayerEntity)
                continue;

            PlotID plotId = PlotID.ofBlockPos(entity.getBlockX(), entity.getBlockZ());

            if (plotId == null)
                continue;

            if (plotId.equals(this.plotID))
                entity.kill();
        }
    }

}
