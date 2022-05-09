package me.zailer.plotcubic.utils;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.zailer.plotcubic.PlotCubic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class Utils {
    public static void fillWithPos(BlockState block, int x, int y, int z, int x2, int y2, int z2) {

        if (block.isAir()) {
            fillPosWithAir(x, y, z, x2, y2, z2);
            return;
        }

        for (BlockPos blockPos : BlockPos.iterate(x, y, z, x2, y2, z2)) {
            if (!block.isOf(PlotCubic.getPlotWorldHandle().asWorld().getBlockState(blockPos).getBlock()))
                PlotCubic.getPlotWorldHandle().asWorld().setBlockState(blockPos, block);
        }
    }

    public static void fillPosWithAir(int x, int y, int z, int x2, int y2, int z2) {
        for (int i = y2; i != y; i--) {
            for (BlockPos blockPos : BlockPos.iterate(x, i, z, x2, i, z2)) {
                if (!PlotCubic.getPlotWorldHandle().asWorld().isAir(blockPos))
                    PlotCubic.getPlotWorldHandle().asWorld().setBlockState(blockPos, Blocks.AIR.getDefaultState());
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
}
