package me.zailer.plotcubic.mixin;


import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.plot.PlotID;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;
import java.util.Set;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {

    @Inject(
            method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/util/math/BlockBox;encompassPositions(Ljava/lang/Iterable;)Ljava/util/Optional;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onGenerate(FeatureContext<TreeFeatureConfig> context,
                           CallbackInfoReturnable<Boolean> cir,
                           StructureWorldAccess structureWorldAccess,
                           Random random,
                           BlockPos origin,
                           TreeFeatureConfig treeFeatureConfig,
                           Set<BlockPos> logPositions,
                           Set<BlockPos> leavesPositions,
                           Set<BlockPos> decorationPositions) {

        PlotID originPlotId = PlotID.ofBlockPos(origin.getX(), origin.getZ());
        this.removeBlocksOutOfPlot(decorationPositions, originPlotId);
        this.removeBlocksOutOfPlot(leavesPositions, originPlotId);
        this.removeBlocksOutOfPlot(logPositions, originPlotId);
    }

    public void removeBlocksOutOfPlot(Set<BlockPos> blockPosSet, PlotID originPlotId) {
        //TODO: remove blocks from the tree before they are placed
        ServerWorld world = PlotCubic.getPlotWorldHandle().asWorld();
        for (var pos : blockPosSet) {
            if (PlotID.isDifferentPlot(originPlotId, pos.getX(), pos.getZ()))
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}
