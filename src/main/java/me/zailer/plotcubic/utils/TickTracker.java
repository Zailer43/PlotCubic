package me.zailer.plotcubic.utils;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.PlotManager;
import me.zailer.plotcubic.database.UnitOfWork;
import me.zailer.plotcubic.enums.ZoneType;
import me.zailer.plotcubic.events.PlayerPlotEvent;
import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.stimuli.Stimuli;

import javax.annotation.Nullable;
import java.util.*;

public class TickTracker {
    private static int tickCount = 0;
    private static HashMap<ServerPlayerEntity, PlotID> playerPlotHashMap = new HashMap<>();
    private static HashMap<PlotID, Plot> plotHashMap = new HashMap<>();

    public static void start() {
        if (++tickCount % 20 == 0) {
            tickCount = 0;

            removeEntitiesFromRoad();
            updatePlayerPlotEvent();
        }
    }

    private static void removeEntitiesFromRoad() {
        for (var entityType : PlotCubic.ENTITY_IN_ROAD_BLACKLIST)
            removeEntitiesFromRoad(entityType);
    }

    private static void removeEntitiesFromRoad(EntityType<?> entityType) {
        List<? extends Entity> entityList = PlotCubic.getPlotWorldHandle().asWorld().getEntitiesByType(entityType, entity -> true);
        PlotManager plotManager = PlotManager.getInstance();
        for (var entity : entityList) {
            ZoneType zoneType = plotManager.getZone(entity.getBlockX(), entity.getBlockZ());

            if (zoneType != ZoneType.PLOT)
                entity.kill();
        }

    }

    private static void updatePlayerPlotEvent() {
        List<ServerPlayerEntity> currentPlayerList = PlotCubic.getPlotWorldHandle().asWorld().getPlayers();
        clearUnusedPlayers(currentPlayerList);
        clearInactivePlots();

        for (var player : currentPlayerList) {
            PlotID currentPlotId = PlotID.ofBlockPos(player.getBlockX(), player.getBlockZ());
            Plot currentPlot = getPlot(currentPlotId);
            PlotID previousPlotId = playerPlotHashMap.getOrDefault(player, null);
            Plot previousPlot = getPlot(previousPlotId);

            try (var invokers = Stimuli.select().forEntityAt(player, player.getBlockPos())) {
                boolean equals = Objects.equals(currentPlotId, previousPlotId);

                if (!equals && currentPlotId != null)
                    invokers.get(PlayerPlotEvent.ARRIVED).onPlayerArrived(player, currentPlotId, currentPlot);

                if (!equals && previousPlotId != null)
                    invokers.get(PlayerPlotEvent.LEFT).onPlayerLeft(player, previousPlotId, previousPlot);
            }
            playerPlotHashMap.put(player, currentPlotId);
            plotHashMap.put(currentPlotId, currentPlot);
        }
    }

    @Nullable
    private static Plot getPlot(@Nullable PlotID plotId) {
        if (plotId == null)
            return null;

        if (plotHashMap.containsKey(plotId))
            return plotHashMap.get(plotId);

        try (var uow = new UnitOfWork()) {
            return uow.plotsRepository.get(plotId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void clearUnusedPlayers(List<ServerPlayerEntity> currentPlayerList) {
        HashMap<ServerPlayerEntity, PlotID> previousHashMap = new HashMap<>(playerPlotHashMap);
        playerPlotHashMap = new HashMap<>();
        for (var player : currentPlayerList) {
            playerPlotHashMap.put(player, null);

            if (previousHashMap.containsKey(player))
                playerPlotHashMap.put(player, previousHashMap.get(player));
        }
    }

    private static void clearInactivePlots() {
        HashMap<PlotID, Plot> previousHashMap = new HashMap<>(plotHashMap);
        plotHashMap = new HashMap<>();
        for (var player : playerPlotHashMap.keySet()) {
            PlotID plotId = playerPlotHashMap.get(player);

            if (plotId == null)
                break;

            if (previousHashMap.containsKey(plotId))
                plotHashMap.put(plotId, previousHashMap.get(plotId));
        }
    }

}
