package at.lowdfx.lowdfx.kit.op;

import at.lowdfx.lowdfx.managers.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class OPFood extends JavaPlugin {
    public static final ItemStack ITEM = new ItemBuilder(Material.GOLDEN_CARROT)
            .name(Component.text("OP Essen", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .lore(KitManager.OP_LORE)
            .amount(64)
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}
