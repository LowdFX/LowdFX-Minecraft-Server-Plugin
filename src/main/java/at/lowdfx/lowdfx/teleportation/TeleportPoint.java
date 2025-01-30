package at.lowdfx.lowdfx.teleportation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record TeleportPoint(Location location) {
    public void teleport(@NotNull Entity entity) {
        TeleportManager.update(entity);

        // Pretty sure the teleportation options for RETAIN_VEHICLE and RETAIN_PASSENGERS would work as well,
        // but I'm assuming this has some kind of reason to use the outdated version.

        if (entity.getVehicle() != null) {
            Entity vehicle = entity.getVehicle();
            Bukkit.getScheduler().runTask(LowdFX.PLUGIN, () -> {
                vehicle.eject();
                vehicle.teleport(location);
                vehicle.addPassenger(entity);
            });
        } else {
            Bukkit.getScheduler().runTask(LowdFX.PLUGIN, () -> entity.teleport(location));
        }
    }
}
