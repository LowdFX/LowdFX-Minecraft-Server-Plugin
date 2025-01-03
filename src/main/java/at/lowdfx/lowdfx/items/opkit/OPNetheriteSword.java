package at.lowdfx.lowdfx.items.opkit;

import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
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

public class OPNetheriteSword {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Netheritschwert");
        //meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addEnchant(Enchantment.LOOTING, 900, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 900, true);
        meta.setUnbreakable(true);


        AttributeModifier attackDamageModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.attackdamage"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        );
        AttributeModifier attackSpeedModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.attackspeed"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        );
        // ändere Damage
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, attackDamageModifier);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, attackSpeedModifier);


        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
