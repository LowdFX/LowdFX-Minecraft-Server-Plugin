package at.lowdfx.lowdfx.items.opkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class OPApple implements Listener {

    // Diese Methode gibt den OP Apfel als Item zurück
    public static ItemStack get() {
        ItemStack item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64);
        ItemMeta meta = item.getItemMeta();

        // Custom-Name für den Apfel
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Apfel");

        // Lore für den Apfel
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    // Listener für den Konsum des OP Apfels
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        // Überprüfen, ob es sich um den benutzerdefinierten OP Apfel handelt
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Apfel")) {
                // Fügt den Absorptions-Effekt (Level 4 entspricht 20 Absorptionsherzen) mit einer Dauer von 3600 Ticks hinzu
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 6000, 4)); // 3600 Ticks = 3 Minuten
            }
        }
    }
}

