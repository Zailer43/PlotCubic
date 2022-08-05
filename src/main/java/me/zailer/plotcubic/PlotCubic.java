package me.zailer.plotcubic;

import com.mojang.logging.LogUtils;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.config.ConfigManager;
import me.zailer.plotcubic.database.DatabaseManager;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.events.PlayerPlotEvent;
import me.zailer.plotcubic.events.PlotEvents;
import me.zailer.plotcubic.events.PlotPermissionsEvents;
import me.zailer.plotcubic.events.ReloadEvent;
import me.zailer.plotcubic.generator.PlotworldGenerator;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.PlotPermission;
import me.zailer.plotcubic.plot.UserConfig;
import me.zailer.plotcubic.registry.DimensionRegistry;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.TickTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.stimuli.Stimuli;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PlotCubic implements ModInitializer {
    public static final String MOD_ID = "plotcubic";
    public static final RegistryKey<DimensionType> PLOTWORLD_DIMENSION_TYPE = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MOD_ID, "plotworld"));
    private static Set<EntityType<?>> ENTITY_WHITELIST = Set.of();
    private static Set<EntityType<?>> ENTITY_ROAD_WHITELIST = Set.of();
    private static Set<EntityType<?>> ENTITY_ROAD_BLACKLIST = Set.of();
    private static final HashMap<ServerPlayerEntity, UserConfig> playersSet = new HashMap<>();
    public static RuntimeWorldHandle plotWorldHandle;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean modReady = false;
    private static MinecraftServer server;
    private static DatabaseManager databaseManager;
    private static ConfigManager configManager;

    @Nullable
    public static RuntimeWorldHandle getPlotWorldHandle() {
        return plotWorldHandle;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static boolean isModReady() {
        return modReady;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
    public static Config getConfig() {
        return configManager.getConfig();
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void onInitialize() {
        configManager = new ConfigManager();

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            PlotCubic.server = server;
            Config config = configManager.getConfig();
            if (config == null) {
                LOGGER.info("[PlotCubic] Configuration is null due to an error, the server will shutdown.");
                server.shutdown();
                return;
            }

            databaseManager = new DatabaseManager(config.database());
        });

        PlotCommand.register();
        DimensionRegistry.register();
        PlotPermission.register();

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            PlotEvents.register();
            PlotPermissionsEvents.register();
            PlotEvents.fixEntitySpawnBypass();

            try (var invokers = Stimuli.select().at(server.getOverworld(), new BlockPos(0, 0, 0))) {
                invokers.get(ReloadEvent.EVENT).onReload(PlotCubic.getConfig());
            }

            this.setupPlotWorld();

            modReady = true;
        });

        Stimuli.global().listen(ReloadEvent.EVENT, config -> {
            PlotManager.getInstance().setSettings(config.PlotWorld(), server);
            MessageUtils.reloadColors(config.customColors());
            reloadEntitiesWhitelist(config.general());
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> server.addServerGuiTickable(TickTracker::start));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            try (var invokers = Stimuli.select().forEntityAt(player, player.getBlockPos())) {
                PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
                invokers.get(PlayerPlotEvent.LEFT).onPlayerLeft(player, plotId, plotId == null ? null : Plot.getLoadedPlot(plotId));
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (configManager.getConfig().general().autoTeleport()) newPlayer.teleport(plotWorldHandle.asWorld(), 0, PlotManager.getInstance().getMaxTerrainHeight() + 2, 0, 0, 0);
        });

        ServerPlayConnectionEvents.JOIN.register(this::addPlayer);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playersSet.remove(handler.player));
    }

    private void addPlayer(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        player.changeGameMode(GameMode.CREATIVE);
        String username = player.getName().getString();
        UserConfig userConfig;
        try (var uow = new UnitOfWork()) {
            userConfig = uow.playersRepository.get(username);
        } catch (Exception ignored) {
            userConfig = null;
            MessageUtils.sendDatabaseConnectionError(player);
        }
        playersSet.put(player, userConfig == null ? new UserConfig(username, false) : userConfig);
    }

    private void setupPlotWorld() {
        PlotworldGenerator generator = PlotworldGenerator.createPlotGenerator(server);

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, PLOTWORLD_DIMENSION_TYPE.getValue()))
                .setDifficulty(Difficulty.PEACEFUL)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false)
                .setGenerator(generator);
        Fantasy fantasy = Fantasy.get(server);
        plotWorldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(MOD_ID, "plot"), worldConfig);
    }

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static UserConfig getUser(ServerPlayerEntity player) {
        return playersSet.get(player);
    }

    private static void reloadEntitiesWhitelist(Config.General config) {
        ENTITY_WHITELIST = parseEntities(config.entityWhitelist());
        ENTITY_ROAD_WHITELIST = parseEntities(config.entityRoadWhitelist());
        ENTITY_ROAD_BLACKLIST = parseEntities(config.entityWhitelist());
        ENTITY_ROAD_BLACKLIST.removeAll(ENTITY_ROAD_WHITELIST);
    }

    private static HashSet<EntityType<?>> parseEntities(String[] entityList) {
        HashSet<EntityType<?>> entityTypes = new HashSet<>();
        for (var entityId : entityList) {
            var optionalEntity = EntityType.get(entityId);

            if (optionalEntity.isPresent())
                entityTypes.add(optionalEntity.get());
            else
                LOGGER.warn("Could not parse entity '{}' from configuration", entityId);
        }
        return entityTypes;
    }

    public static Set<EntityType<?>> getEntityWhitelist() {
        return ENTITY_WHITELIST;
    }

    public static Set<EntityType<?>> getEntityRoadWhitelist() {
        return ENTITY_ROAD_WHITELIST;
    }

    public static Set<EntityType<?>> getEntityRoadBlacklist() {
        return ENTITY_ROAD_BLACKLIST;
    }
}
