package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;

public final class UserConfig {
    private final String username;
    private boolean plotChatEnabled;

    public UserConfig(String username, boolean plotChatEnabled) {
        this.username = username;
        this.plotChatEnabled = plotChatEnabled;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isPlotChatEnabled() {
        return this.plotChatEnabled;
    }

    public void setPlotChat(boolean isEnabled) {
        this.plotChatEnabled = isEnabled;
        PlotCubic.getDatabaseManager().updatePlotChat(this.username, this.plotChatEnabled);
    }

}
