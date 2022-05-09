package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.generator.PlotworldSettings;
import me.zailer.plotcubic.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
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
    private Date claimedDate;
    private int playerCount;

    public Plot(String username, PlotID plotID, List<TrustedPlayer> trustedPlayers, List<DeniedPlayer> deniedPlayers) {
        this.ownerUsername = username;
        this.plotID = plotID;
        this.trustedPlayers = trustedPlayers;
        this.deniedPlayers = deniedPlayers;
        this.playerCount = 0;
        this.claimedDate = null;
    }

    public Plot(ServerPlayerEntity player, PlotID plotID) {
        this(player.getName().getString(), plotID);
    }

    public Plot(String ownerUsername, PlotID plotID) {
        this(ownerUsername, plotID, new ArrayList<>(), new ArrayList<>());
    }

    public static void claim(ServerPlayerEntity player, PlotID plotID) {
        Plot plot = new Plot(player, plotID);
        plot.setBorder(PlotManager.getInstance().getSettings().getClaimedBorderBlock());
        loadedPlots.add(plot);
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

    public static void loadPlot(@Nullable Plot plot) {
        if (plot == null)
            return;

        plot.playerCount++;
        if (Plot.loadedPlotsContains(plot))
            return;

        Plot.loadedPlots.add(plot);
    }

    public static void unloadPlot(@Nullable Plot plot) {
        if (plot == null)
            return;

        if (plot.playerCount > 1)
            plot.playerCount--;
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

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
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

}
