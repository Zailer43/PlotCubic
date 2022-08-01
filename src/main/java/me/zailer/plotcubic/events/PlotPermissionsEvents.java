package me.zailer.plotcubic.events;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.plot.PlotPermission;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.TrustedPlayer;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.block.FluidPlaceEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;

import java.util.Set;

public class PlotPermissionsEvents {

    public static Set<Block> EXPLOSIVE_BLOCKS = Set.of(Blocks.TNT, Blocks.RESPAWN_ANCHOR);
    public static Set<Item> BOATS_ITEMS = Set.of(
            Items.OAK_BOAT,
            Items.ACACIA_BOAT,
            Items.BIRCH_BOAT,
            Items.DARK_OAK_BOAT,
            Items.JUNGLE_BOAT,
            Items.SPRUCE_BOAT
    );
    public static Set<Item> SPAWN_ENTITY_ITEMS = Set.of(
            Items.ARMOR_STAND,
            Items.CHEST_MINECART,
            Items.END_CRYSTAL,
            Items.FURNACE_MINECART,
            Items.GLOW_ITEM_FRAME,
            Items.ITEM_FRAME,
            Items.MINECART,
            Items.PAINTING,
            Items.AXOLOTL_BUCKET,
            Items.COD_BUCKET,
            Items.PUFFERFISH_BUCKET,
            Items.SALMON_BUCKET,
            Items.TROPICAL_FISH_BUCKET
        );

    public static void register() {
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> {
            BlockEntity blockEntity = PlotCubic.getPlotWorldHandle().asWorld().getBlockEntity(pos);
            if (blockEntity instanceof LockableContainerBlockEntity)
                return hasPermission(player, blockEntity.getPos(), PlotPermission.OPEN_CONTAINER);

            return hasPermission(player, pos, PlotPermission.BREAK_BLOCKS);
        });

        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> {
            if (hasPermission(player, pos, PlotPermission.PLACE_BLOCKS) != ActionResult.PASS)
                return ActionResult.FAIL;

            if (EXPLOSIVE_BLOCKS.contains(state.getBlock()))
                return hasPermission(player, pos, PlotPermission.PLACE_EXPLOSIVES);

            return ActionResult.PASS;
        });

        Stimuli.global().listen(PlayerC2SPacketEvent.EVENT, (player, packet) -> {
            if (!(packet instanceof PlayerInteractBlockC2SPacket packetInteract))
                return ActionResult.PASS;

            if (player.hasPermissionLevel(4))
                return ActionResult.PASS;

            Direction side = packetInteract.getBlockHitResult().getSide();
            BlockPos pos = packetInteract.getBlockHitResult().getBlockPos().offset(side);

            ActionResult result = hasPermission(player, pos, PlotPermission.PLACE_BLOCKS);

            if (result == ActionResult.FAIL)
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.getWorld(), pos));

            return result;
        });

        Stimuli.global().listen(FluidPlaceEvent.EVENT, (world, pos, player, hitResult) -> hasPermission(player, pos, PlotPermission.PLACE_FLUIDS));

        //FIXME: https://github.com/FabricMC/fabric/issues/1870
        Stimuli.global().listen(EntityUseEvent.EVENT, (player, entity, hand, hitResult) -> {
            if (entity instanceof AbstractMinecartEntity)
                return hasPermission(player, entity.getBlockPos(), PlotPermission.USE_MINECART);

            if (entity instanceof BoatEntity)
                return hasPermission(player, entity.getBlockPos(), PlotPermission.USE_BOATS);

            return ActionResult.PASS;
        });

        Stimuli.global().listen(PlayerAttackEntityEvent.EVENT, (attacker, hand, attacked, hitResult) -> hasPermission(attacker, attacked.getBlockPos(), PlotPermission.DAMAGE_ENTITIES));

        Stimuli.global().listen(ItemUseEvent.EVENT, (player, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (BOATS_ITEMS.contains(stack.getItem()))
                return new TypedActionResult<>(hasPermission(player, player.getBlockPos(), PlotPermission.SPAWN_ENTITIES), stack);

            if (stack.getItem() == Items.MAP)
                return new TypedActionResult<>(hasPermission(player, player.getBlockPos(), PlotPermission.FILL_MAP), stack);

            return TypedActionResult.pass(stack);
        });

        Stimuli.global().listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (SPAWN_ENTITY_ITEMS.contains(stack.getItem()) || stack.getItem() instanceof SpawnEggItem)
                return hasPermission(player, hitResult.getBlockPos(), PlotPermission.SPAWN_ENTITIES);
            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity blockEntity = PlotCubic.getPlotWorldHandle().asWorld().getBlockEntity(pos);
            Block block = PlotCubic.getPlotWorldHandle().asWorld().getBlockState(pos).getBlock();

            if (blockEntity instanceof BedBlockEntity)
                return hasPermission(player, pos, PlotPermission.SLEEP);

            if (blockEntity instanceof LockableContainerBlockEntity)
                return hasPermission(player, pos, PlotPermission.OPEN_CONTAINER);

            if (block instanceof AbstractButtonBlock)
                return hasPermission(player, pos, PlotPermission.USE_BUTTONS);

            if (block instanceof LeverBlock)
                return hasPermission(player, pos, PlotPermission.USE_LEVER);

            return ActionResult.PASS;
        });
    }

    public static ActionResult hasPermission(ServerPlayerEntity player, BlockPos pos, PlotPermission permission) {
        if (player == null)
            return ActionResult.PASS;

        PlotID plotId = PlotID.ofBlockPos(pos.getX(), pos.getZ());
        if (plotId == null)
            return ActionResult.FAIL;

        for (var plot : Plot.loadedPlots) {
            if (plot.isId(plotId)) {
                return hasPermission(plot, player, permission);
            }
        }
        return ActionResult.FAIL;
    }

    public static ActionResult hasPermission(Plot plot, ServerPlayerEntity player, PlotPermission permission) {
        if (plot.isOwner(player))
            return ActionResult.PASS;

        TrustedPlayer trustedPlayer = plot.getTrusted(player);
        if (trustedPlayer == null)
            return ActionResult.FAIL;

        return trustedPlayer.hasPermission(permission) ? ActionResult.PASS : ActionResult.FAIL;
    }
}
