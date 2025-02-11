package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.LowdFX;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OPStick {
    public static final ItemStack ITEM = new ItemBuilder(Material.STICK)
            .name(Component.text("OP Schlagstock", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(LowdFX.OP_LORE)
            .enchant(Enchantment.KNOCKBACK, Short.MAX_VALUE)
            .editMeta(m -> m.setUnbreakable(true))
            .build();

    public static @NotNull ItemStack get() {
        return ITEM;
    }
}
