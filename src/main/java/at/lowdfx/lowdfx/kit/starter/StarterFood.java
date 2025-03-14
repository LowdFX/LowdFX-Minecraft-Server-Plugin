package at.lowdfx.lowdfx.kit.starter;

import at.lowdfx.lowdfx.kit.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterFood {
    public static final ItemStack ITEM = new ItemBuilder(Material.BAKED_POTATO)
            .name(Component.text("Starter Essen", NamedTextColor.RED))
            .lore(KitManager.STARTER_LORE)
            .amount(64)
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}