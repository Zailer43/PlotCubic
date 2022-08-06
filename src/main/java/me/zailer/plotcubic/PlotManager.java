package me.zailer.plotcubic;

import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.enums.ZoneType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.List;

public class PlotManager {
    private static PlotManager instance = null;
    //private Config.PlotworldConfig settings;
    private int maxTerrainHeight;
    private int plotSize;
    private int roadSize;
    private int totalSize;
    private int minHeight;
    private int maxHeight;
    private int height;
    private Biome biome;
    private BlockState unclaimedBorderBlock;
    private BlockState claimedBorderBlock;
    private BlockState borderBlock;
    private BlockState roadBlock;
    private List<Layer> layers;

    private PlotManager() {
    }

    public static PlotManager getInstance() {
        if (instance == null)
            instance = new PlotManager();

        return instance;
    }

    public void setSettings(Config.PlotworldConfig settings, MinecraftServer server) {
        this.plotSize = settings.plotSize();
        this.roadSize = settings.roadSize();
        this.totalSize = settings.plotSize() + settings.roadSize() + 2;
        this.biome = server.getRegistryManager().get(Registry.BIOME_KEY).get(new Identifier(settings.biomeId()));
        this.unclaimedBorderBlock = this.getBlock(settings.unclaimedBorderBlock());
        this.claimedBorderBlock = this.getBlock(settings.claimedBorderBlock());
        this.borderBlock = this.getBlock(settings.borderBlock());
        this.roadBlock = this.getBlock(settings.roadBlock());
        this.layers = Arrays.stream(settings.layers()).map(layer -> new Layer(layer.thickness(), this.getBlock(layer.block()))).toList();
    }

    public void setHeight(int minHeight, int height) {
        this.minHeight = minHeight;
        this.maxHeight = height - Math.abs(minHeight);
        this.height = height;
        this.maxTerrainHeight = minHeight - 1;
        for (var layer : this.layers)
            this.maxTerrainHeight += layer.thickness();
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
        int height = this.minHeight;
        for (var layer : this.layers) {
            height += layer.thickness;
            if (height > y)
                return layer.block;
        }
        return Blocks.AIR.getDefaultState();
    }

    private BlockState getRoadBlock(int y) {
        return this.getRoadBlock(y, this.roadBlock);
    }

    private BlockState getRoadBlock(int y, BlockState block) {
        return y > this.maxTerrainHeight ? Blocks.AIR.getDefaultState() : block;
    }

    private BlockState getBorderBlock(int y) {
        if (y == this.maxTerrainHeight + 1)
            return this.unclaimedBorderBlock;
        return this.getRoadBlock(y, this.borderBlock);
    }

    public ZoneType getZone(int x, int z) {
        int plotSize = this.plotSize;
        int size = this.totalSize;
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
        return new BlockPos(0, this.maxTerrainHeight + 2, 0);
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

    public int getMaxTerrainHeight() {
        return this.maxTerrainHeight;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public BlockState getBlock(Config.BlockConfig blockConfig) {
        Block block = Registry.BLOCK.get(new Identifier(blockConfig.id()));
        BlockState blockState = block.getDefaultState();

        StateManager<Block, BlockState> stateManager = blockState.getBlock().getStateManager();

        for (var state : blockConfig.states()) {
            Property<?> property = stateManager.getProperty(state.key());
            if (property == null)
                continue;

            blockState = BlockItem.with(blockState, property, state.value());
        }

        return blockState;
    }

    public int getPlotSize() {
        return this.plotSize;
    }

    public int getRoadSize() {
        return this.roadSize;
    }

    public int getTotalSize() {
        return this.totalSize;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public int getMinHeight() {
        return this.minHeight;
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getClaimedBorderBlock() {
        return this.claimedBorderBlock;
    }

    public BlockState getUnclaimedBorderBlock() {
        return this.unclaimedBorderBlock;
    }

    private record Layer(int thickness, BlockState block) {
    }
}
