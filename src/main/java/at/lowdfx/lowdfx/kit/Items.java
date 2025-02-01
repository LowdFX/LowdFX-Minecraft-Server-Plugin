package at.lowdfx.lowdfx.kit;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.function.BiConsumer;

public class Items {
    public static final ItemStack WHITE_BACKGROUND = new ItemBuilder(Material.WHITE_STAINED_GLASS).name(Component.empty()).build();
    public static final ItemStack GRAY_BACKGROUND = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).build();
    public static final ItemStack BLACK_BACKGROUND = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).build();

    public static class LiveItem extends AbstractItem {
        protected final Player target;
        protected final BiConsumer<PlayerInventory, ItemStack> setter;
        protected final ItemProvider provider;

        public LiveItem(Player target, BiConsumer<PlayerInventory, ItemStack> setter, SimpleItemProvider provider) {
            this.target = target;
            this.setter = setter;
            this.provider = provider;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (WHITE_BACKGROUND.isSimilar(event.getCurrentItem())) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);
            Bukkit.getScheduler().runTask(LowdFX.PLUGIN, () -> setter.accept(target.getInventory(), event.getCurrentItem()));
        }

        @Override
        public ItemProvider getItemProvider() {
            return provider;
        }
    }

    @FunctionalInterface
    public interface SimpleItemProvider extends ItemProvider {
        @NotNull ItemStack get();
        default @NotNull ItemStack get(String lang) { return get(); }
    }
}
