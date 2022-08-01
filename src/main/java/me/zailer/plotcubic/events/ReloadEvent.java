package me.zailer.plotcubic.events;

import me.zailer.plotcubic.config.Config;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface ReloadEvent {
    StimulusEvent<ReloadEvent> EVENT = StimulusEvent.create(ReloadEvent.class, ctx -> (config) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onReload(config);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    void onReload(Config newConfig);
}
