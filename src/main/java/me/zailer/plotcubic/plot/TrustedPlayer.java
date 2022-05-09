package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.enums.PlotPermission;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

public record TrustedPlayer(String username, Set<PlotPermission> permissions, PlotID plotId) {

    public boolean isPlayer(ServerPlayerEntity player) {
        return this.isPlayer(player.getName().getString());
    }

    public boolean isPlayer(String username) {
        return this.username.equalsIgnoreCase(username);
    }

    public boolean hasPermission(PlotPermission permission) {
        return this.permissions.contains(permission);
    }

    public void addPermission(PlotPermission permission) {
        this.permissions.add(permission);
    }

}
