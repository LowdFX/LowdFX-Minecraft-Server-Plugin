package at.lowdfx.lowdfx.items.opkit;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArmorProtectionListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Überprüfen, ob das Entity ein Spieler ist
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Überprüfe alle Rüstungsteile (Helm, Brustplatte, Hose, Schuhe)
            ItemStack helmet = player.getInventory().getHelmet();
            ItemStack chestplate = player.getInventory().getChestplate();
            ItemStack leggings = player.getInventory().getLeggings();
            ItemStack boots = player.getInventory().getBoots();

            // Überprüfen, ob der Spieler Rüstungsteile aus Netherite trägt
            if (isNetheriteArmor(helmet) || isNetheriteArmor(chestplate) || isNetheriteArmor(leggings) || isNetheriteArmor(boots)) {

                // Prüfen, ob das Rüstungsteil ItemMeta und Attribut-Modifikatoren hat
                if (hasSpecialArmorModifications(helmet) || hasSpecialArmorModifications(chestplate) || hasSpecialArmorModifications(leggings) || hasSpecialArmorModifications(boots)) {
                    event.setCancelled(true);
                    // Optional: Nachricht an den Spieler senden
                    //player.sendMessage("Deine spezielle Netherite-Rüstung hat den Schaden blockiert!");
                }
            }
        }
    }

    // Methode, die prüft, ob ein Item aus Netherite besteht und spezielle Rüstungsmodifikatoren hat
    private boolean isNetheriteArmor(ItemStack item) {
        return item != null && (item.getType() == Material.NETHERITE_HELMET ||
                item.getType() == Material.NETHERITE_CHESTPLATE ||
                item.getType() == Material.NETHERITE_LEGGINGS ||
                item.getType() == Material.NETHERITE_BOOTS);
    }

    // Methode zur Überprüfung, ob ein Rüstungsteil Attribut-Modifikatoren enthält
    private boolean hasSpecialArmorModifications(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasAttributeModifiers()) {
                Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
                for (Attribute attribute : modifiers.keySet()) {
                    // Nur "GENERIC_ARMOR" Modifikatoren prüfen
                    if (attribute == Attribute.ARMOR) {
                        for (AttributeModifier modifier : modifiers.get(attribute)) {
                            // Den Wert des Attribut-Modifikators holen und mit 999999999 vergleichen
                            if (modifier.getAmount() == 999999999) {
                                return true; // Spezielle Armor-Menge gefunden
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
