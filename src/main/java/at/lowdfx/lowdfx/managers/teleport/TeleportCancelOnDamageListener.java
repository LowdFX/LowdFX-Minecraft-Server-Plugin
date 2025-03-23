package at.lowdfx.lowdfx.managers.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Configuration;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class TeleportCancelOnDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!Configuration.SAFE_TELEPORT_ENABLED) return;

        // Nur abbrechen, wenn wirklich ein Teleport aussteht
        if (TeleportManager.hasPendingTeleport(player)) {
            TeleportManager.cancelPendingTeleport(player);
            player.sendMessage(LowdFX.serverMessage(Component.text("§cTeleportation abgebrochen — du hast Schaden erlitten!")));
        }
    }
}

