package at.lowdfx.lowdfx.items.opkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OPStick {
    public static @NotNull ItemStack get() {
        ItemStack item = new ItemStack(Material.STICK, 1);
        item.editMeta(meta -> {
            meta.displayName(Component.text("OP Schlagstock", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            //meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addEnchant(Enchantment.KNOCKBACK, 9999, true);
            meta.setUnbreakable(true);

            meta.lore(List.of(Component.text("OP Kit", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
        });
        return item;
    }
}
