package at.lowdfx.lowdfx.items.opkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class OPApple implements Listener {
    public static final ItemStack ITEM = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Apfel", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            meta.lore(Lowdfx.OP_LORE);
        });
    }

    public static @NotNull ItemStack get() {
        ItemStack item = new ItemStack(ITEM);
        item.setAmount(64);
        return item;
    }

    // Listener für den Konsum des OP Apfels
    @EventHandler
    public void onPlayerConsume(@NotNull PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        // Überprüfen, ob es sich um den benutzerdefinierten OP Apfel handelt
        if (item.isSimilar(ITEM)) {
            // Fügt den Absorptions-Effekt (Level 4 entspricht 20 Absorptionsherzen) mit einer Dauer von 3600 Ticks hinzu
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 6000, 4)); // 3600 Ticks = 3 Minuten
        }
    }
}

