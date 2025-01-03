package at.lowdfx.lowdfx.items.starterkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class StarterFood {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.BAKED_POTATO, 32);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Starter Essen");


        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Starter Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}