package at.lowdfx.lowdfx.items.starterkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class StarterStonePickaxe {
    public static ItemStack get() {
        ItemStack item = new ItemStack(Material.STONE_PICKAXE, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Starter Steinspitzhacke");
        meta.addEnchant(Enchantment.EFFICIENCY, 1, false);
        meta.addEnchant(Enchantment.UNBREAKING, 1, false);
        meta.addEnchant(Enchantment.FORTUNE, 1, false);

        // ändere Damage
        //meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("generic.attackDamage", 3, AttributeModifier.Operation.ADD_NUMBER));

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Starter Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}