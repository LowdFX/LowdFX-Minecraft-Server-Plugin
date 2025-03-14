package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.managers.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OPApple {
    public static final ItemStack ITEM = new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE)
            .name(Component.text("OP Apfel", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(KitManager.OP_LORE)
            .amount(64)
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
