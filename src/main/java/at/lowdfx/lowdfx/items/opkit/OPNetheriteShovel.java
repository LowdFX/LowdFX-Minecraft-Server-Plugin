package at.lowdfx.lowdfx.items.opkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class OPNetheriteShovel {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.NETHERITE_SHOVEL, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Netheritschaufel");
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.EFFICIENCY, 999999999, true);
        meta.addEnchant(Enchantment.FORTUNE, 999999999, true);
        AttributeModifier luckAttributeModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.luck"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        );
        meta.addAttributeModifier(Attribute.LUCK, luckAttributeModifier);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}