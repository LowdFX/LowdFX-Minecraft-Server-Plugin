package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.managers.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
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
public class OPNetheriteAxe {
    public static final ItemStack ITEM = new ItemBuilder(Material.NETHERITE_AXE)
            .name(Component.text("OP Netheritaxt", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(KitManager.OP_LORE)
            .enchant(Enchantment.EFFICIENCY, Short.MAX_VALUE)
            .enchant(Enchantment.FORTUNE, Short.MAX_VALUE)
            .editMeta(m -> {
                m.setUnbreakable(true);
                m.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.attackdamage"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
                m.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.attackspeed"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
                m.addAttributeModifier(Attribute.LUCK, new AttributeModifier(
                        new NamespacedKey("lowdfx", "generic.luck"),
                        Integer.MAX_VALUE,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
            })
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
