package me.zailer.plotcubic.mixin.piston;

import me.zailer.plotcubic.PlotManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PistonHandler.class)
public class PistonHandlerMixin {

    @Shadow @Final private Direction motionDirection;

    @Shadow @Final private BlockPos posTo;

    @Inject(
            method = "tryMove(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/block/BlockState;isAir()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void tryMoveBlock(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir, BlockState blockState) {
        if (PlotManager.isOutOfPlot(pos))
            cir.setReturnValue(false);
    }

    @Inject(
            method = "tryMove(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 1, target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void tryMoveAdjacentBlock(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir, BlockState blockState, int i, BlockPos blockPos, BlockState blockState2) {
        if (this.isIllegalMove(blockPos))
            cir.setReturnValue(false);
    }

    @Inject(
            method = "tryMove(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 2, target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void tryMoveMultipleBlocks(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir, BlockState blockState, int i, int j, int k, BlockPos blockPos2) {
        if (this.isIllegalMove(blockPos2))
            cir.setReturnValue(false);
    }

    private boolean isIllegalMove(BlockPos pos) {
        return PlotManager.isOutOfPlot(pos) || PlotManager.isOutOfPlot(pos.offset(this.motionDirection));
    }
}
