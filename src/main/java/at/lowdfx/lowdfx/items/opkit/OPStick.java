package at.lowdfx.lowdfx.items.opkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class OPStick {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Schlagstock");
        //meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addEnchant(Enchantment.KNOCKBACK, 9999, true);
        meta.setUnbreakable(true);


        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
