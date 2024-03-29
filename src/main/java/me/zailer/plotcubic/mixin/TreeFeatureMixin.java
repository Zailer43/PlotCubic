package me.zailer.plotcubic.mixin;


import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.plot.PlotID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

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
                           Set<BlockPos> rootPositions,
                           Set<BlockPos> logPositions,
                           Set<BlockPos> leavesPositions,
                           Set<BlockPos> decorationPositions) {

        RuntimeWorldHandle handle = PlotCubic.getPlotWorldHandle();

        if (handle != null) {
            ServerWorld world = handle.asWorld();
            PlotManager plotManager = PlotManager.getInstance();

            PlotID originPlotId = PlotID.ofBlockPos(origin.getX(), origin.getZ());
            this.removeBlocksOutOfPlot(plotManager, decorationPositions, originPlotId, world);
            this.removeBlocksOutOfPlot(plotManager, rootPositions, originPlotId, world);
            this.removeBlocksOutOfPlot(plotManager, logPositions, originPlotId, world);
            this.removeBlocksOutOfPlot(plotManager, leavesPositions, originPlotId, world);
        }
    }

    public void removeBlocksOutOfPlot(PlotManager plotManager, Set<BlockPos> blockPosSet, PlotID originPlotId, ServerWorld world) {
        //TODO: remove blocks from the tree before they are placed
        for (var pos : blockPosSet) {
            if (PlotID.isDifferentPlot(originPlotId, pos.getX(), pos.getZ()))
                world.setBlockState(pos, plotManager.getBlock(pos));
        }
    }
}
