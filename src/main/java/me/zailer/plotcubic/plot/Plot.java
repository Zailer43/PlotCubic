package me.zailer.plotcubic.plot;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.commands.plot.ToggleCommand;
import me.zailer.plotcubic.commands.plot.toggle.ToggleChatCommand;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.generator.PlotworldGenerator;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.TickTracker;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

    public static void claim(ServerPlayerEntity player, Plot plot) {
        plot.setBorder(PlotManager.getInstance().getClaimedBorderBlock());
        Plot.loadPlot(player, plot);
        TickTracker.updatePlot(plot.getPlotID(), plot);
    }

    public void delete() {
        this.setBorder(PlotManager.getInstance().getUnclaimedBorderBlock());
        this.clearPlot(null);
    }

    public void setBorder(BlockState block) {
        PlotManager plotManager = PlotManager.getInstance();
        int plotSize = plotManager.getPlotSize() + 1;
        int y = plotManager.getMaxTerrainHeight() + 1;
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
            MessageUtils.sendMessage(player, "text.plotcubic.plot.clear.successful", String.valueOf(timeTaken));
        }
    }

    private void clearPlotTerrain() {
        ServerWorld world = PlotCubic.getPlotWorldHandle().asWorld();
        PlayerManager playerManager = PlotCubic.getServer().getPlayerManager();
        PlotworldGenerator plotworldGenerator = (PlotworldGenerator) world.getChunkManager().getChunkGenerator();
        int plotSizeWithBorder = PlotManager.getInstance().getPlotSize() + 2;
        int xPos = this.plotID.getXPos();
        int zPos = this.plotID.getZPos();
        int xPosOppositeCorner = xPos + plotSizeWithBorder;
        int zPosOppositeCorner = zPos + plotSizeWithBorder;

        Set<Chunk> chunkList = this.getPlotChunks(world);

        //Go through all the chunks and regenerate them one at a time
        for (var chunk : chunkList) {
            ChunkPos chunkPos = chunk.getPos();
            for (int x = 0; x != CHUNK_WIDTH; x++) {
                xPos = chunkPos.getOffsetX(x);
                // Taking into account that the opposite corner is in positive x or z and that beyond it the plot ends,
                // it is not necessary to follow the for
                if (xPos > xPosOppositeCorner)
                    break;
                for (int z = 0; z != CHUNK_WIDTH; z++) {
                    zPos = chunkPos.getOffsetZ(z);
                    if (zPos > zPosOppositeCorner)
                        break;

                    //FIXME: at no time is the light taken into account, preferably it has to be recalculated
                    // after finishing generating the chunks so that there are no unnecessary updates
                    if (!PlotID.isDifferentPlot(this.plotID, xPos, zPos))
                        plotworldGenerator.regen(chunk, x, z);
                }
            }
            //Send the chunk to nearby players
            playerManager.sendToAround(null, xPos, 0, zPos, 512, world.getRegistryKey(), new ChunkDataS2CPacket(world.getChunk(chunkPos.x, chunkPos.z), world.getLightingProvider(), null, null, true));
        }
    }

    private Set<Chunk> getPlotChunks(ServerWorld world) {
        int xPos = this.plotID.getXPos();
        int zPos = this.plotID.getZPos();
        int plotSizeWithBorder = PlotManager.getInstance().getPlotSize() + 2;
        int xChunk = ChunkSectionPos.getSectionCoord(xPos);
        int zChunk = ChunkSectionPos.getSectionCoord(zPos);
        int oppositeCornerXChunk = ChunkSectionPos.getSectionCoord(xPos + plotSizeWithBorder);
        int oppositeCornerZChunk = ChunkSectionPos.getSectionCoord(zPos + plotSizeWithBorder);

        // There are occasions in which the plot measures different distances in x and z as it is not a multiple of a chunk
        int plotChunkSizeInX = oppositeCornerXChunk - xChunk;
        int plotChunkSizeInZ = oppositeCornerZChunk - zChunk;
        int plotChunkSize = Math.max(plotChunkSizeInX, plotChunkSizeInZ);

        Set<Chunk> chunkList = new HashSet<>();
        for (int x = 0; x <= plotChunkSize; x++) {
            for (int z = 0; z <= plotChunkSize; z++) {
                chunkList.add(world.getChunk(xChunk + x, zChunk + z));
            }
        }
        return chunkList;
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


    public void addTrusted(TrustedPlayer trusted) {
        this.trustedPlayers.add(trusted);
    }


    public List<TrustedPlayer> getTrusted() {
        return this.trustedPlayers;
    }

    @Nullable
    public TrustedPlayer getTrusted(ServerPlayerEntity player) {
        return this.getTrusted(player.getName().getString());
    }

    @Nullable
    public TrustedPlayer getTrusted(String username) {
        for (var trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.isPlayer(username)) {
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
        if (Permissions.check(player, "plotcubic.bypass.deny"))
            return false;

        String username = player.getName().getString();
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

        try (var uow = new UnitOfWork()) {
            return uow.plotsRepository.get(plotId);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean visit(ServerPlayerEntity player) {
        if (this.hasDeny(player))
            return false;

        PlotID plotId = this.getPlotID();
        Config.BlockPos defaultPlotSpawn = PlotCubic.getConfig().general().defaultPlotSpawn();
        float x = plotId.getSpawnOfX() + defaultPlotSpawn.x();
        float y = plotId.getSpawnOfY() + defaultPlotSpawn.y();
        float z = plotId.getSpawnOfZ() + defaultPlotSpawn.z();
        player.teleport(PlotCubic.getPlotWorldHandle().asWorld(), x, y, z, 0, 0);
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

    public void sendPlotChatMessage(ServerPlayerEntity sender, Text message) {
        Text text = this.chatStyle.getMessage(this.plotID, sender, message);
        Config.General config = PlotCubic.getConfig().general();

        if (config.warningUseOfPlotChatInEmptyPlot() && this.players.size() <= 1) {
            MutableText tooltipMessage = Text.translatable("text.plotcubic.chat_style.warning_empty_plot.tooltip",
                    String.format("/%s %s %s", PlotCommand.COMMAND_ALIAS[0], new ToggleCommand().getAlias()[0], new ToggleChatCommand().getAlias()[0])
            );

            text = MessageUtils.getTranslation("text.plotcubic.chat_style.warning_empty_plot.chat")
                    .append(text)
                    .setTooltipMessage(tooltipMessage)
                    .get();
        }

        for (var player : this.players)
            player.sendMessage(text);

        if (config.logPlotChat())
            PlotCubic.log(text.getString());
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
