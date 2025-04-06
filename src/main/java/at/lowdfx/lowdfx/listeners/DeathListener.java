package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.managers.moderation.DeathLogManager;
import at.lowdfx.lowdfx.managers.DeathMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathListener implements Listener {

    private final DeathMessageManager messageManager;

    public DeathListener(DeathMessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        String deathType = "default";

        if (lastDamage != null) {
            deathType = switch (lastDamage.getCause()) {
                case CONTACT -> "contact";
                case ENTITY_ATTACK -> "entity_attack";
                case PROJECTILE -> "projectile";
                case SUFFOCATION -> "suffocation";
                case FALL -> "fall";
                case FIRE, FIRE_TICK -> "fire";
                case MELTING -> "melting";
                case LAVA -> "lava";
                case DROWNING -> "drowning";
                case BLOCK_EXPLOSION -> "block_explosion";
                case ENTITY_EXPLOSION -> "entity_explosion";
                case VOID -> "void";
                case SUICIDE -> "suicide";
                case MAGIC -> "magic";
                case WITHER -> "wither";
                case FALLING_BLOCK -> "falling_block";
                case THORNS -> "thorns";
                case CUSTOM -> "custom";
                default -> "default";
            };
            DeathLogManager.getInstance().saveDeath(event);
        }

        Component deathMessage = messageManager.getFormattedMessage(deathType, player.getName());
        event.deathMessage(deathMessage);
    }
}
