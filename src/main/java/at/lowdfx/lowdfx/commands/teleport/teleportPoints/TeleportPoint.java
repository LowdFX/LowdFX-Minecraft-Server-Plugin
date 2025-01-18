package at.lowdfx.lowdfx.commands.teleport.teleportPoints;

import at.lowdfx.lowdfx.Lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record TeleportPoint(Location location) {
    public void teleport(@NotNull Entity entity, Lowdfx plugin) {
        if (entity.getVehicle() != null) {
            Entity vehicle = entity.getVehicle();
            Bukkit.getScheduler().runTask(plugin, () -> {
                vehicle.eject();
                vehicle.teleport(location);
                vehicle.addPassenger(entity);
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> entity.teleport(location));
        }
    }
}
