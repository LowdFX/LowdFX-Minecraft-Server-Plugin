package at.lowdfx.lowdfx.items.opkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class OPNetheriteBoots {
    public static ItemStack get(){
        ItemStack item = new ItemStack(Material.NETHERITE_BOOTS, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "OP Netheritstiefel");
        meta.setUnbreakable(true);

        AttributeModifier armorModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.armor"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.FEET
        );
        AttributeModifier armorToughnessModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.armortoughness"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.FEET
        );
        AttributeModifier explosionKnockBackModifier = new AttributeModifier(
                new NamespacedKey("lowdfx", "generic.explosionknockbackresistance"),
                999999999,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.FEET
        );

        meta.addAttributeModifier(Attribute.ARMOR, armorModifier);
        meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, armorToughnessModifier);
        meta.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, explosionKnockBackModifier);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "OP Kit");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

}
