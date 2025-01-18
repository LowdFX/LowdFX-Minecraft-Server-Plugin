package at.lowdfx.lowdfx.items.opkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class OPNetheriteChestplate {
    public static final ItemStack ITEM = new ItemStack(Material.NETHERITE_CHESTPLATE);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Netheritharnisch", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            meta.lore(Lowdfx.OP_LORE);
            meta.setUnbreakable(true);

            meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.armor"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET
            ));
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.armortoughness"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET));
            meta.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(
                    new NamespacedKey("lowdfx", "generic.explosionknockbackresistance"),
                    Integer.MAX_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET));
        });
    }

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}