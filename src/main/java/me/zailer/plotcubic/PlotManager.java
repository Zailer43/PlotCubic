package me.zailer.plotcubic;

import me.zailer.plotcubic.enums.ZoneType;
import me.zailer.plotcubic.generator.PlotworldSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class PlotManager {
    private static PlotManager instance = null;
    private PlotworldSettings settings;

    private PlotManager() {
    }

    public static PlotManager getInstance() {
        if (instance == null)
            instance = new PlotManager();

        return instance;
    }

    public void setSettings(PlotworldSettings settings) {
        this.settings = settings;
    }

    public PlotworldSettings getSettings() {
        return this.settings;
    }

    public BlockState getBlock(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        ZoneType zone = this.getZone(x, z);

        return switch (zone) {
            case PLOT -> this.getPlotBlock(y);
            case ROAD -> this.getRoadBlock(y);
            case BORDER -> this.getBorderBlock(y);
        };
    }

    private BlockState getPlotBlock(int y) {
        int height = 0;
        for (var layer : this.settings.getLayers()) {
            height += layer.getThickness();
            if (height > y)
                return layer.getBlockState();
        }
        return Blocks.AIR.getDefaultState();
    }

    private BlockState getRoadBlock(int y) {
        return y > this.settings.getMaxHeight() ? Blocks.AIR.getDefaultState() : this.settings.getRoadBlock();
    }

    private BlockState getBorderBlock(int y) {
        if (y == this.settings.getMaxHeight() + 1)
            return this.settings.getUnclaimedBorderBlock();
        return this.getRoadBlock(y);
    }

    public ZoneType getZone(int x, int z) {
        int roadSize = this.settings.getRoadSize() + 1;
        int plotSize = this.settings.getPlotSize() + 1;
        int offset = roadSize / 2;
        int size = plotSize + roadSize;
        int plotAndHalfRoad = plotSize + offset;
        int xOffset = this.getPlotOffset(x, offset, size, false);
        int zOffset = this.getPlotOffset(z, offset, size, false);
        int xOffsetInvested = this.getPlotOffset(x, offset, size, true);
        int zOffsetInvested = this.getPlotOffset(z, offset, size, true);

        if (this.isSquare(offset, plotAndHalfRoad, xOffset, zOffset, xOffsetInvested, zOffsetInvested, 1)) {
            if (this.isSquare(offset, plotAndHalfRoad, xOffset, zOffset, xOffsetInvested, zOffsetInvested, 0))
                return ZoneType.PLOT;
            return ZoneType.BORDER;
        }

        return ZoneType.ROAD;
    }

    private int getPlotOffset(int pos, int offset, int size, boolean invested) {
        pos -= offset;
        pos %= size;

        if(pos < 0)
            pos += size;

        if (invested)
            pos = Math.abs(pos - size);

        return pos;
    }

    private boolean isSquare(int pos1, int pos2, int x, int z, int x2, int z2, int enlarge) {
        x += enlarge;
        z -= enlarge;
        x2 += enlarge;
        z2 -= enlarge;
        return ++pos1 <= x && pos1 < x2 && pos2 >= z && pos2 > z2;
    }

    public BlockPos getPlotworldSpawn() {
        return new BlockPos(0, this.settings.getMaxHeight() + 2, 0);
    }

    public TeleportTarget getSpawnTeleportTarget() {
        return new TeleportTarget(new Vec3d(0, getSettings().getMaxHeight() + 1, 0), Vec3d.ZERO, 0, 0);
    }

    public boolean isPlot(BlockPos pos) {
        return this.isPlot(pos.getX(), pos.getZ());
    }

    public boolean isPlot(int x, int z) {
        ZoneType zone = this.getZone(x, z);
        return zone == ZoneType.PLOT;
    }

    public static boolean isOutOfPlot(BlockPos pos) {
        return PlotManager.getInstance().getZone(pos.getX(), pos.getZ()) != ZoneType.PLOT;
    }

}
