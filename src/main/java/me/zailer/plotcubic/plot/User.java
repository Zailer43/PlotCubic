package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotCubic;

public final class User {
    private final String username;
    private boolean plotChatEnabled;

    public User(String username, boolean plotChatEnabled) {
        this.username = username;
        this.plotChatEnabled = plotChatEnabled;
    }

    public String getUsername() {
        return username;
    }

    public boolean getPlotChatEnabled() {
        return plotChatEnabled;
    }

    public boolean togglePlotChat() {
        this.plotChatEnabled = !this.plotChatEnabled;
        PlotCubic.getDatabaseManager().updatePlotChat(this.username, this.plotChatEnabled);
        return this.plotChatEnabled;
    }

}
