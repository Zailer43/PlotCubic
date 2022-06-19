package me.zailer.plotcubic.utils;

import com.mojang.brigadier.context.CommandContext;
import me.zailer.plotcubic.PlotCubic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class Utils {
    public static void fillWithPos(BlockState block, int x, int y, int z, int x2, int y2, int z2) {

        if (block.isAir()) {
            fillPosWithAir(x, y, z, x2, y2, z2);
            return;
        }

        ServerWorld world = PlotCubic.getPlotWorldHandle().asWorld();

        for (BlockPos blockPos : BlockPos.iterate(x, y, z, x2, y2, z2)) {
            if (!block.isOf(world.getBlockState(blockPos).getBlock()))
                world.setBlockState(blockPos, block);
        }
    }

    public static void fillPosWithAir(int x, int y, int z, int x2, int y2, int z2) {
        ServerWorld world = PlotCubic.getPlotWorldHandle().asWorld();
        BlockState air = Blocks.AIR.getDefaultState();
        for (int i = y2; i != y; i--) {
            for (BlockPos blockPos : BlockPos.iterate(x, i, z, x2, i, z2)) {
                if (!world.isAir(blockPos))
                    world.setBlockState(blockPos, air);
            }
        }
    }

    public static void fillWithDimensions(BlockState block, int x, int y, int z, int width, int height, int depth) {
        if (width < 0) {
            width = Math.abs(width);
            x -= width;
        }

        if (height < 0) {
            height = Math.abs(height);
            y -= height;
        }

        if (depth < 0) {
            depth = Math.abs(depth);
            z -= depth;
        }

        fillWithPos(block, x, y, z, x + width, y + height, z + depth);
    }

    @Nullable
    public static <T> T getArg(CommandContext<ServerCommandSource> source, Class<T> type, String name) {
        try {
            return source.getArgument(name, type);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
