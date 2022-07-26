package me.zailer.plotcubic;

import me.zailer.plotcubic.enums.ZoneType;
import me.zailer.plotcubic.generator.PlotworldSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

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
        int plotSize = this.settings.getPlotSize();
        int size = this.settings.getTotalSize();
        float halfPlot = plotSize / 2f;
        float xOffset = this.getPlotOffset(x, size) - halfPlot;
        float zOffset = this.getPlotOffset(z, size) - halfPlot;

        if (this.isPointInsideSquare(xOffset, zOffset, halfPlot, 1)) {
            if (this.isPointInsideSquare(xOffset, zOffset, halfPlot, 0))
                return ZoneType.PLOT;
            return ZoneType.BORDER;
        }

        return ZoneType.ROAD;
    }

    private int getPlotOffset(int pos, int size) {
        pos %= size;

        if(pos < 0)
            pos += size;

        return pos;
    }

    private boolean isPointInsideSquare(float posX, float posZ, float halfSquareSize, int enlarge) {
        int x = ((int) -halfSquareSize) - enlarge;
        int z = ((int) -halfSquareSize) - enlarge;
        int x2 = Math.round(halfSquareSize) + enlarge;
        int z2 = Math.round(halfSquareSize) + enlarge;
        return posX > x && posZ > z && posX <= x2 && posZ <= z2;
    }

    public BlockPos getPlotworldSpawn() {
        return new BlockPos(0, this.settings.getMaxHeight() + 2, 0);
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
