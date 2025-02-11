package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.LowdFX;
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
public class OPNetheriteBoots {
    public static final ItemStack ITEM = new ItemBuilder(Material.NETHERITE_BOOTS)
            .name(Component.text("OP Netheritstiefel", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(LowdFX.OP_LORE)
            .editMeta(m -> {
                m.setUnbreakable(true);
                m.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.armor"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.FEET));
                m.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.armortoughness"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.FEET));
                m.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.explosionknockbackresistance"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.FEET));
            })
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
