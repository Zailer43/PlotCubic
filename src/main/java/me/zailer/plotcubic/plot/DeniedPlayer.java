package me.zailer.plotcubic.plot;

import javax.annotation.Nullable;

public record DeniedPlayer(String username, @Nullable String reason) {
    public boolean isPlayer(String username) {
        return this.username.equalsIgnoreCase(username);
    }

    public boolean hasReason() {
        return this.reason != null;
    }

    @Override
    public String reason() {
        return this.hasReason() ? this.reason : "Unspecified reason";
    }
}
