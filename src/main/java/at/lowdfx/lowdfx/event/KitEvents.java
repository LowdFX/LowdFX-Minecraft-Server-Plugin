package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.command.LowCommand;
import at.lowdfx.lowdfx.kit.op.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        event.getPlayer().getPersistentDataContainer().remove(LowCommand.STARTERKIT_KEY);
    }


    // Listener für den Konsum des OP Apfels
    @EventHandler
    public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        // Überprüfen, ob es sich um den benutzerdefinierten OP Apfel handelt
        if (OPApple.ITEM.isSimilar(event.getItem())) {
            // Fügt den Absorptions-Effekt (Level 4 entspricht 20 Absorptionsherzen) mit einer Dauer von 3600 Ticks hinzu
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 6000, 4)); // 3600 Ticks = 3 Minuten
        }
    }

    @EventHandler
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        // Überprüfen, ob das Entity ein Spieler ist
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        // Überprüfen, ob der Spieler Rüstungsteile aus Netherite trägt
        if (isMaterial(helmet, Material.NETHERITE_HELMET) ||
                isMaterial(chestplate, Material.NETHERITE_CHESTPLATE) ||
                isMaterial(leggings, Material.NETHERITE_LEGGINGS) ||
                isMaterial(boots, Material.NETHERITE_BOOTS)) {

            // Prüfen, ob das Rüstungsteil ItemMeta und Attribut-Modifikatoren hat
            if (OPNetheriteHelmet.ITEM.isSimilar(helmet) ||
                    OPNetheriteChestplate.ITEM.isSimilar(chestplate) ||
                    OPNetheriteLeggings.ITEM.isSimilar(leggings) ||
                    OPNetheriteBoots.ITEM.isSimilar(boots)) {
                event.setCancelled(true);
                // Optional: Nachricht an den Spieler senden
                //player.sendMessage("Deine spezielle Netherite-Rüstung hat den Schaden blockiert!");
            }
        }
    }

    // Methode, die prüft, ob ein Item aus Netherite besteht und spezielle Rüstungsmodifikatoren hat
    private boolean isMaterial(@Nullable ItemStack item, Material material) {
        return item != null && item.getType() == material;
    }
}
