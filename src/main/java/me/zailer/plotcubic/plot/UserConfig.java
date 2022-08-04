package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.database.UnitOfWork;

import java.sql.SQLException;

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
        try (var uow = new UnitOfWork()) {
            try {
                uow.beginTransaction();
                uow.playersRepository.updatePlotChat(this.username, isEnabled);
                uow.commit();
            } catch (SQLException e) {
                uow.rollback();
            }
        } catch (Exception ignored) {
        }
    }

}
