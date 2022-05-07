package me.zailer.plotcubic.events;

import me.zailer.plotcubic.plot.Plot;
import me.zailer.plotcubic.plot.PlotID;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import javax.annotation.Nullable;

public class PlayerPlotEvent {
    public static final StimulusEvent<Arrived> ARRIVED = StimulusEvent.create(Arrived.class, ctx -> (player, plotId, plot) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onPlayerArrived(player, plotId, plot);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<Left> LEFT = StimulusEvent.create(Left.class, ctx -> (player, plotId, plot) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onPlayerLeft(player, plotId, plot);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public interface Arrived {
        void onPlayerArrived(ServerPlayerEntity player, PlotID plotId, @Nullable Plot plot);
    }

    public interface Left {
        void onPlayerLeft(ServerPlayerEntity player, PlotID plotId, @Nullable Plot plot);
    }
}
