package at.lowdfx.lowdfx.items.starterkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class StarterStoneShovel {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.STONE_SHOVEL, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Starter Steinspitzhacke");
        meta.addEnchant(Enchantment.EFFICIENCY, 1, false);
        meta.addEnchant(Enchantment.UNBREAKING, 1, false);
        meta.addEnchant(Enchantment.FORTUNE, 1, false);


        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Starter Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}