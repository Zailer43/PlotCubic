package me.zailer.plotcubic;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import me.zailer.plotcubic.commands.PlotCommand;
import me.zailer.plotcubic.config.Config;
import me.zailer.plotcubic.config.ConfigManager;
import me.zailer.plotcubic.database.DatabaseManager;
import me.zailer.plotcubic.events.PlayerPlotEvent;
import me.zailer.plotcubic.events.PlotEvents;
import me.zailer.plotcubic.events.PlotPermissionsEvents;
import me.zailer.plotcubic.generator.PlotworldGenerator;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import me.zailer.plotcubic.plot.UserConfig;
import me.zailer.plotcubic.registry.DimensionRegistry;
import me.zailer.plotcubic.utils.MessageUtils;
import me.zailer.plotcubic.utils.TickTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.stimuli.Stimuli;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlotCubic implements ModInitializer {
    public static final String MOD_ID = "plotcubic";
    public static final ImmutableList<EntityType<?>> ENTITY_IN_ROAD_BLACKLIST = ImmutableList.of(
            EntityType.ARMOR_STAND,
            EntityType.BOAT,
            EntityType.CHEST_MINECART,
            EntityType.END_CRYSTAL,
            EntityType.FURNACE_MINECART,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.ITEM_FRAME,
            EntityType.MINECART,
            EntityType.PAINTING,
            EntityType.TNT,
            EntityType.ARROW,
            EntityType.SPECTRAL_ARROW
    );
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
            if (configManager.getConfig() == null) {
                LOGGER.info("[PlotCubic] Configuration is null due to an error, the server will shutdown.");
                server.shutdown();
                return;
            }

            databaseManager = new DatabaseManager(configManager.getConfig(), server);
        });

        PlotCommand.register();
        DimensionRegistry.register();
        MessageUtils.reloadColors();

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            PlotEvents.register();
            PlotPermissionsEvents.register();
            PlotEvents.fixEntitySpawnBypass();

            setupPlotWorld();

            modReady = true;
        });


        ServerLifecycleEvents.SERVER_STARTED.register((server) -> server.addServerGuiTickable(TickTracker::start));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            try (var invokers = Stimuli.select().forEntityAt(player, player.getBlockPos())) {
                PlotID plotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
                invokers.get(PlayerPlotEvent.LEFT).onPlayerLeft(player, plotId, plotId == null ? null : Plot.getLoadedPlot(plotId));
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            String username = handler.player.getName().getString();
            UserConfig userConfig = databaseManager.getPlayer(username);
            playersSet.put(handler.player, userConfig == null ? new UserConfig(username, false) : databaseManager.getPlayer(username));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playersSet.remove(handler.player));
    }

    private void setupPlotWorld() {
        PlotworldGenerator generator = PlotworldGenerator.createPlotGenerator(server);

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_REGISTRY_KEY)
                .setDifficulty(Difficulty.PEACEFUL)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false)
                .setGenerator(generator);
        Fantasy fantasy = Fantasy.get(server);
        plotWorldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(MOD_ID, "plot"), worldConfig);
    }

    public static ImmutableList<EntityType<?>> getEntityWhitelist() {
        List<EntityType<?>> entityList = new ArrayList<>(List.of(
                EntityType.ARROW,
                EntityType.EGG,
                EntityType.ENDER_PEARL,
                EntityType.EXPERIENCE_BOTTLE,
                EntityType.EYE_OF_ENDER,
                EntityType.FIREWORK_ROCKET,
                EntityType.FISHING_BOBBER,
                EntityType.ITEM,
                EntityType.POTION,
                EntityType.SNOWBALL,
                EntityType.SPECTRAL_ARROW,
                EntityType.TRIDENT
        ));

        entityList.addAll(ENTITY_IN_ROAD_BLACKLIST);

        return entityList.stream().collect(ImmutableList.toImmutableList());
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
}
