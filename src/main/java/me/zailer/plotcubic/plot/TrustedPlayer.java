package me.zailer.plotcubic.plot;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public record TrustedPlayer(String username, Set<PlotPermission> permissions, PlotID plotId) {

    public boolean isPlayer(String username) {
        return this.username.equalsIgnoreCase(username);
    }

    public boolean hasPermission(PlotPermission permission) {
        return this.permissions.contains(permission);
    }

    public void addPermission(PlotPermission permission) {
        this.permissions.add(permission);
    }

    public void setPermissions(Set<PlotPermission> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    /**
     * Get the difference between two {@link me.zailer.plotcubic.plot.TrustedPlayer}
     * @param trustedPlayer value to compare
     * @return Returns the values that are in this object but not in the one being compared
     */
    public Set<PlotPermission> getDifference(TrustedPlayer trustedPlayer) {
        Set<PlotPermission> deletedPermissions = new HashSet<>();

        for (var permissionKey : PlotPermission.PERMISSION_HASH_MAP.keySet()) {
            PlotPermission permission = PlotPermission.PERMISSION_HASH_MAP.get(permissionKey);
            if (this.hasPermission(permission) && !trustedPlayer.hasPermission(permission))
                deletedPermissions.add(permission);
        }

        return deletedPermissions;
    }
}
