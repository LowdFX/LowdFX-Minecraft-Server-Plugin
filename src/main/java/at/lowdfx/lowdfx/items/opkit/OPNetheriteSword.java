package at.lowdfx.lowdfx.items.opkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class OPNetheriteSword {
    public static final ItemStack ITEM = new ItemStack(Material.NETHERITE_SWORD);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Netheritschwert", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            meta.lore(Lowdfx.OP_LORE);
            meta.setUnbreakable(true);

            // Minecraft benutzt internally signed 16-bit zahlen, was heisst das Short (-32768 bis 32767)'s #MAX_VALUE das maximum ist, ohne zu bugs zu führen.
            meta.addEnchant(Enchantment.LOOTING, Short.MAX_VALUE, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, Short.MAX_VALUE, true);

            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.attackdamage"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            ));
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.attackspeed"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND));
        });
    }

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
