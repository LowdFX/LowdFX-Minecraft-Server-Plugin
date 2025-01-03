package at.lowdfx.lowdfx.items.opkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class OPFood extends JavaPlugin {

    // Item erstellen
    public static ItemStack get() {
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT, 64);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Essen");

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }


}
