package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.kit.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
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
    public static final ItemStack ITEM = new ItemBuilder(Material.NETHERITE_CHESTPLATE)
            .name(Component.text("OP Netheritharnisch", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(KitManager.OP_LORE)
            .editMeta(m -> {
                m.setUnbreakable(true);
                m.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.armor"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST));
                m.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.armortoughness"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST));
                m.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.explosionknockbackresistance"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST));
            })
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
