package me.zailer.plotcubic.events;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.mixin.ExplosionAccessor;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.utils.CommandColors;
import me.zailer.plotcubic.utils.MessageUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.FluidPlaceEvent;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;
import xyz.nucleoid.stimuli.event.world.FluidFlowEvent;

import javax.annotation.Nullable;
import java.util.List;

public class PlotEvents {

    public static void register() {
        registerProtectBlocks();
        registerAvoidEntitiesSpawn();
        registerNewUsers();
        registerPlotEvents();
    }

    private static void registerProtectBlocks() {
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> allowAdmin(player));
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> protectMinHeight(world, pos));
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> protectRoads(pos));
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> protectRoads(pos));


        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> allowAdmin(player));
        //FIXME: https://github.com/NucleoidMC/stimuli/issues/19
        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> protectRoads(pos));

        Stimuli.global().listen(FluidPlaceEvent.EVENT, (world, pos, player, hitResult) -> {
            if (player == null)
                return ActionResult.FAIL;
            return allowAdmin(player);
        });
        Stimuli.global().listen(FluidPlaceEvent.EVENT, (world, pos, player, hitResult) -> {
            if (player == null)
                return ActionResult.FAIL;
            return protectRoads(pos);
        });

        Stimuli.global().listen(FluidFlowEvent.EVENT, (world, fluidPos, fluidBlock, flowDirection, flowTo, flowToBlock) -> protectRoads(flowTo));

        Stimuli.global().listen(ExplosionDetonatedEvent.EVENT, (explosion, particles) -> protectRoads(explosion));
        Stimuli.global().listen(ExplosionDetonatedEvent.EVENT, (explosion, particles) -> protectMinHeight(explosion));

        Stimuli.global().listen(ItemUseEvent.EVENT, PlotEvents::protectRoads);
    }

    private static void registerAvoidEntitiesSpawn() {
        Stimuli.global().listen(EntitySpawnEvent.EVENT, PlotEvents::entityWhitelist);
        Stimuli.global().listen(EntitySpawnEvent.EVENT, PlotEvents::entityInRoadBlacklist);
    }

    private static void registerNewUsers() {
        ServerPlayConnectionEvents.INIT.register(PlotEvents::onPlayerJoinInit);
    }

    private static void registerPlotEvents() {
        Stimuli.global().listen(PlayerPlotEvent.ARRIVED, (player, plotId, plot) -> Plot.loadPlot(plot));
        Stimuli.global().listen(PlayerPlotEvent.ARRIVED, PlotEvents::denyEvent);
        Stimuli.global().listen(PlayerPlotEvent.LEFT, (player, plotId, plot) -> Plot.unloadPlot(plot));
    }

    private static ActionResult allowAdmin(ServerPlayerEntity player) {
        return player.hasPermissionLevel(4) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private static ActionResult protectMinHeight(ServerWorld world, BlockPos pos) {
        return pos.getY() == world.getChunkManager().getChunkGenerator().getMinimumY() ? ActionResult.FAIL : ActionResult.PASS;
    }

    private static void protectMinHeight(Explosion explosion) {
        List<BlockPos> affectedBlocks = explosion.getAffectedBlocks();
        World explosionWorld = ((ExplosionAccessor) explosion).getWorld();
        affectedBlocks.removeIf(pos -> protectMinHeight((ServerWorld) explosionWorld, pos) == ActionResult.FAIL);
    }

    private static ActionResult protectRoads(BlockPos pos) {
        if (!PlotManager.getInstance().isPlot(pos))
            return ActionResult.FAIL;

        return ActionResult.PASS;
    }

    private static void protectRoads(Explosion explosion) {
        List<BlockPos> affectedBlocks = explosion.getAffectedBlocks();
        affectedBlocks.removeIf(pos -> !PlotManager.getInstance().isPlot(pos));
    }

    private static TypedActionResult<ItemStack> protectRoads(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        for (var itemUseBlacklist : PlotCubic.ITEM_USE_BLACKLIST) {
            if (item == itemUseBlacklist)
                return TypedActionResult.fail(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private static ActionResult entityWhitelist(Entity entity) {
        for (var entityType : PlotCubic.getEntityWhitelist()) {
            if (entityType == entity.getType())
                return ActionResult.PASS;
        }

        return ActionResult.FAIL;
    }

    private static ActionResult entityInRoadBlacklist(Entity entity) {
        for (var entityType : PlotCubic.ENTITY_IN_ROAD_BLACKLIST) {
            if (entityType == entity.getType() && !PlotManager.getInstance().isPlot(entity.getBlockPos()))
                return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private static void onPlayerJoinInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        if (!PlotCubic.isModReady()) {
            handler.disconnect(CommandColors.ERROR.set("PlotCubic is generating the Plot World, we can't receive players yet. Please try again in a minute or two."));
            return;
        }

        PlotCubic.getDatabaseManager().newUser(handler.player.getName().getString());
    }

    private static void denyEvent(ServerPlayerEntity player, PlotID plotId, @Nullable Plot plot) {
        if (plot == null)
            return;

        if (plot.hasDeny(player)) {
            MessageUtils.sendChatMessage(player, new MessageUtils("You were denied from the plot").get());
            BlockPos spawn = PlotManager.getInstance().getPlotworldSpawn();
            player.teleport(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
        }
    }

}
