package me.zailer.plotcubic.events;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.mixin.ExplosionAccessor;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.UserConfig;
import me.zailer.plotcubic.utils.MessageUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.*;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;
import xyz.nucleoid.stimuli.event.player.PlayerChatContentEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;
import xyz.nucleoid.stimuli.event.world.FluidFlowEvent;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;

public class PlotEvents {

    public static void register() {
        registerProtectBlocks();
        registerAvoidEntitiesSpawn();
        registerNewUsers();
        registerPlayersEvents();
        registerChatEvents();
    }

    private static void registerProtectBlocks() {
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> allowAdmin(player, pos, "destroy"));
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> protectMinHeight(world, pos));
        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> protectRoads(pos));

        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> protectRoadOfPiston(world, pos, state, context));
        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> allowAdmin(player, pos, "build"));
        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> protectRoads(pos));

        Stimuli.global().listen(FluidPlaceEvent.EVENT, (world, pos, player, hitResult) -> {
            if (player == null)
                return ActionResult.FAIL;
            return allowAdmin(player, pos, "build");
        });
        Stimuli.global().listen(FluidPlaceEvent.EVENT, (world, pos, player, hitResult) -> {
            if (player == null)
                return ActionResult.FAIL;
            return protectRoads(pos);
        });

        Stimuli.global().listen(FluidFlowEvent.EVENT, (world, fluidPos, fluidBlock, flowDirection, flowTo, flowToBlock) -> protectRoads(flowTo));

        Stimuli.global().listen(ExplosionDetonatedEvent.EVENT, (explosion, particles) -> protectRoads(explosion));
        Stimuli.global().listen(ExplosionDetonatedEvent.EVENT, (explosion, particles) -> protectMinHeight(explosion));
    }

    private static void registerAvoidEntitiesSpawn() {
        Stimuli.global().listen(EntitySpawnEvent.EVENT, PlotEvents::entityWhitelist);
        Stimuli.global().listen(EntitySpawnEvent.EVENT, PlotEvents::entityRoadWhitelist);
    }

    private static void registerNewUsers() {
        ServerPlayConnectionEvents.INIT.register(PlotEvents::onPlayerJoinInit);
    }

    private static void registerPlayersEvents() {
        Stimuli.global().listen(PlayerPlotEvent.ARRIVED, (player, plotId, plot) -> Plot.loadPlot(player, plot));
        Stimuli.global().listen(PlayerPlotEvent.ARRIVED, PlotEvents::denyEvent);
        Stimuli.global().listen(PlayerPlotEvent.ARRIVED, PlotEvents::setArrivedGameMode);
        Stimuli.global().listen(PlayerPlotEvent.LEFT, (player, plotId, plot) -> Plot.unloadPlot(player, plot));
        Stimuli.global().listen(PlayerPlotEvent.LEFT, PlotEvents::setLeftGameMode);
    }

    private static void registerChatEvents() {
        Stimuli.global().listen(PlayerChatContentEvent.EVENT, PlotEvents::sendChatMessage);
    }

    private static ActionResult allowAdmin(ServerPlayerEntity player, BlockPos pos, String action) {
        if (PlotManager.getInstance().isPlot(pos))
            return Permissions.check(player, "plotcubic.bypass.plot." + action) ? ActionResult.SUCCESS : ActionResult.PASS;

        return Permissions.check(player, "plotcubic.bypass.road." + action) ? ActionResult.SUCCESS : ActionResult.PASS;
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

    private static ActionResult entityWhitelist(Entity entity) {
        return PlotCubic.getEntityWhitelist().contains(entity.getType()) ? ActionResult.PASS : ActionResult.FAIL;
    }

    private static ActionResult entityRoadWhitelist(Entity entity) {
        return PlotCubic.getEntityRoadWhitelist().contains(entity.getType())
                || !PlotManager.isOutOfPlot(entity.getBlockPos()) ? ActionResult.PASS : ActionResult.FAIL;
    }

    private static void onPlayerJoinInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        if (!PlotCubic.isModReady()) {
            handler.disconnect(new TranslatableText("error.plotcubic.kick.generating_world"));
            return;
        }

        String username = handler.player.getName().getString();

        try (var uow = new UnitOfWork()) {
            try {
                if (uow.playersRepository.exists(username))
                    return;

                handler.player.teleport(PlotCubic.getPlotWorldHandle().asWorld(), 0, PlotManager.getInstance().getMaxTerrainHeight() + 2, 0, 0, 0);
                uow.beginTransaction();
                uow.playersRepository.add(username);
                uow.commit();
            } catch (SQLException e) {
                uow.rollback();
                MessageUtils.sendChatMessage(handler.player, "error.plotcubic.database.new_user");
            }
        } catch (Exception ignored) {
            MessageUtils.sendDatabaseConnectionError(handler.player);
        }
    }

    private static void denyEvent(ServerPlayerEntity player, PlotID plotId, @Nullable Plot plot) {
        if (plot == null)
            return;

        if (plot.hasDeny(player)) {
            MessageUtils.sendChatMessage(player, "text.plotcubic.plot.you_have_deny");
            BlockPos spawn = PlotManager.getInstance().getPlotworldSpawn();
            player.teleport(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
        }
    }


    public static void fixEntitySpawnBypass() {
        Stimuli.global().listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> fixEntitySpawnBypass(player.getStackInHand(hand), hitResult.getBlockPos()));
        Stimuli.global().listen(DispenserActivateEvent.EVENT, (world, pos, dispenserBlockEntity, slot, stackToDispense) -> fixEntitySpawnBypass(stackToDispense, pos));
    }

    public static ActionResult fixEntitySpawnBypass(ItemStack stack, BlockPos placePos) {
        Item item = stack.getItem();

        if (!stack.hasNbt()) {
            return ActionResult.PASS;
        }
        NbtCompound nbt = stack.getNbt();
        assert nbt != null;

        if (!nbt.contains(EntityType.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE))
            return ActionResult.PASS;

        NbtCompound entityTag = nbt.getCompound(EntityType.ENTITY_TAG_KEY);

        if (item.equals(Items.ARMOR_STAND))
            return fixArmorStandSpawnBypass(entityTag, placePos.getX(), placePos.getZ());

        if (item instanceof ItemFrameItem)
            return fixItemFrameSpawnBypass(entityTag, placePos.getX(), placePos.getZ());

        return ActionResult.PASS;
    }

    private static ActionResult fixArmorStandSpawnBypass(NbtCompound entityTag, int placePosX, int placePosZ) {
        if (!entityTag.contains("Pos", NbtElement.LIST_TYPE))
            return ActionResult.PASS;

        NbtList pos = entityTag.getList("Pos", NbtElement.DOUBLE_TYPE);

        if (pos.size() < 3)
            return ActionResult.PASS;

        boolean isDifferentPlot = PlotID.isDifferentPlot(placePosX, placePosZ, (int) pos.getDouble(0), (int) pos.getDouble(2));
        return isDifferentPlot ? ActionResult.FAIL : ActionResult.PASS;
    }

    private static ActionResult fixItemFrameSpawnBypass(NbtCompound entityTag, int placePosX, int placePosZ) {
        int x = 0;
        int z = 0;
        boolean hasTileTag = false;

        if (entityTag.contains("TileX", NbtElement.INT_TYPE)) {
            hasTileTag = true;
            x = entityTag.getInt("TileX");
        }

        if (entityTag.contains("TileZ", NbtElement.INT_TYPE)) {
            hasTileTag = true;
            z = entityTag.getInt("TileZ");
        }

        if (!hasTileTag)
            return ActionResult.PASS;

        boolean isDifferentPlot = PlotID.isDifferentPlot(placePosX, placePosZ, x, z);
        return isDifferentPlot ? ActionResult.FAIL : ActionResult.PASS;
    }

    private static void setArrivedGameMode(ServerPlayerEntity player, PlotID plotID, Plot plot) {
        if (plot != null && plot.getGameMode() != null)
            player.changeGameMode(plot.getGameMode());
    }

    private static void setLeftGameMode(ServerPlayerEntity player, PlotID plotID, Plot plot) {
        MinecraftServer server = player.getServer();

        if (server == null)
            return;
        if (plot != null && plot.getGameMode() != null)
            player.changeGameMode(server.getDefaultGameMode());
    }

    private static ActionResult sendChatMessage(ServerPlayerEntity sender, PlayerChatContentEvent.MutableMessage message) {
        PlotID plotId = PlotID.ofBlockPos(sender.getBlockX(), sender.getBlockZ());

        if (plotId == null)
            return ActionResult.PASS;

        Plot plot = Plot.getLoadedPlot(plotId);

        if (plot == null)
            return ActionResult.PASS;

        UserConfig userConfig = PlotCubic.getUser(sender);

        if (userConfig == null || !userConfig.isPlotChatEnabled())
            return ActionResult.PASS;

        plot.sendPlotChatMessage(sender, message.getRaw());

        return ActionResult.FAIL;
    }

    private static ActionResult protectRoadOfPiston(ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext context) {
        if (!(state.getBlock() instanceof PistonBlock))
            return ActionResult.PASS;

        // this would be easier if the blockstate had its properties...
        ItemStack stack = context.getStack();
        if (!stack.hasNbt())
            return ActionResult.PASS;

        NbtCompound nbt = stack.getNbt();
        assert nbt != null;

        if (!nbt.contains(BlockItem.BLOCK_STATE_TAG_KEY, NbtElement.COMPOUND_TYPE))
            return ActionResult.PASS;
        NbtCompound stateTag = nbt.getCompound(BlockItem.BLOCK_STATE_TAG_KEY);

        if (!stateTag.contains(PistonBlock.EXTENDED.getName(), NbtElement.STRING_TYPE))
            return ActionResult.PASS;

        Direction direction = state.get(PistonBlock.FACING);

        BlockPos pistonHead = pos.offset(direction);

        if (!PlotManager.isOutOfPlot(pistonHead))
            return ActionResult.PASS;

        BlockState notExtended = state.with(PistonBlock.EXTENDED, false).with(PistonBlock.FACING, direction);
        world.setBlockState(pos, notExtended);

        return ActionResult.FAIL;
    }
}
