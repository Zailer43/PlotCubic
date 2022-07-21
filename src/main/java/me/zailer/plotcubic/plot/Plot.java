package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.plot.ChatCommand;
import me.zailer.plotcubic.generator.PlotworldGenerator;
import me.zailer.plotcubic.generator.PlotworldSettings;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.SharedConstants.CHUNK_WIDTH;

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
        this.clearPlot(null);
    }

    public void setBorder(BlockState block) {
        PlotworldSettings settings = PlotManager.getInstance().getSettings();
        int plotSize = settings.getPlotSize() + 1;
        int y = settings.getMaxHeight() + 1;
        int x = this.plotID.getXPos();
        int z = this.plotID.getZPos();

        Utils.fillWithDimensions(block, x, y, z, plotSize, 0, 0);
        Utils.fillWithDimensions(block, x, y, z, 0, 0, plotSize);

        x += plotSize;
        z += plotSize;

        Utils.fillWithDimensions(block, x, y, z, -plotSize, 0, 0);
        Utils.fillWithDimensions(block, x, y, z, 0, 0, -plotSize);
    }

    public void clearPlot(@Nullable ServerPlayerEntity player) {
        long startTime = new Date().getTime();
        this.removeEntities();
        this.clearPlotTerrain();

        if (player != null) {
            long timeTaken = new Date().getTime() - startTime;
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.clear.successful", String.valueOf(timeTaken));
        }
    }

    private void clearPlotTerrain() {
        ServerWorld world = PlotCubic.getPlotWorldHandle().asWorld();
        PlayerManager playerManager = PlotCubic.getServer().getPlayerManager();
        PlotworldGenerator plotworldGenerator = (PlotworldGenerator) world.getChunkManager().getChunkGenerator();
        PlotworldSettings settings = PlotManager.getInstance().getSettings();
        PlotManager plotManager = PlotManager.getInstance();
        int plotSizeWithBorder = settings.getPlotSize() + 2;
        int xPos = this.plotID.getXPos();
        int zPos = this.plotID.getZPos();

        Set<Chunk> chunkList = new HashSet<>();

        //Note: this will break if for some reason the plots are not square
        int xChunk = ChunkSectionPos.getSectionCoord(xPos);
        int zChunk = ChunkSectionPos.getSectionCoord(zPos);
        int oppositeCornerXChunk =  ChunkSectionPos.getSectionCoord(xPos + plotSizeWithBorder);

        int plotChunkSize = oppositeCornerXChunk - xChunk;

        //Add all the chunks of the plot to a set
        for (int x = 0; x <= plotChunkSize; x++) {
            for (int z = 0; z <= plotChunkSize; z++) {
                chunkList.add(world.getChunk(xChunk + x, zChunk + z));
            }
        }

        //Go through all the chunks and regenerate them one at a time
        for (var chunk : chunkList) {
            ChunkPos chunkPos = chunk.getPos();
            for (int x = 0; x != CHUNK_WIDTH; x++) {
                for (int z = 0; z != CHUNK_WIDTH; z++) {
                    xPos = chunkPos.getOffsetX(x);
                    zPos = chunkPos.getOffsetZ(z);
                    if (PlotID.isDifferentPlot(this.plotID, xPos, zPos))
                        continue;

                    //FIXME: at no time is the light taken into account, preferably it has to be recalculated
                    // after finishing generating the chunks so that there are no unnecessary updates
                    plotworldGenerator.regen(plotManager, chunk, x, z);
                }
            }
            //Send the chunk to nearby players
            playerManager.sendToAround(null, xPos, 0, zPos, 512, world.getRegistryKey(), new ChunkDataS2CPacket(world.getChunk(chunkPos.x, chunkPos.z), world.getLightingProvider(), null, null, true));
        }
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
            TranslatableText tooltipMessage = new TranslatableText("text.plotcubic.chat_style.warning_empty_plot.tooltip",
                    PlotCommand.COMMAND_ALIAS[0],
                    new ChatCommand().getAlias()[0]
            );

            messageText = MessageUtils.getTranslation("text.plotcubic.chat_style.warning_empty_plot.chat")
                    .append(messageText)
                    .setTooltipMessage(tooltipMessage)
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
