package at.lowdfx.lowdfx.kit;

import at.lowdfx.lowdfx.LowdFX;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.InvUI;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.AbstractWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.function.BiConsumer;

public class Items {
    public static final ItemStack WHITE_BACKGROUND = new ItemBuilder(Material.WHITE_STAINED_GLASS).name(Component.empty()).build();
    public static final ItemStack BLACK_BACKGROUND = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).build();

    public static class LiveItem extends AbstractItem {
        protected final Player target;
        protected final BiConsumer<PlayerInventory, ItemStack> setter;
        protected final ItemProvider provider;
        protected BukkitTask task;

        public LiveItem(Player target, BiConsumer<PlayerInventory, ItemStack> setter, SimpleItemProvider provider, boolean main) {
            this.target = target;
            this.setter = setter;
            this.provider = provider;

            if (main) {
                Bukkit.getScheduler().runTaskTimer(LowdFX.PLUGIN, r -> {
                    if (getWindows().stream().anyMatch(Window::isOpen)) {
                        notifyWindows(); // Damit Änderungen vom Inventar live sind.
                    } else {
                        r.cancel();
                    }
                }, 10, 10);
            }
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

        public void start() {
            if (task != null) task.cancel();
            task = Bukkit.getScheduler().runTaskTimer(InvUI.getInstance().getPlugin(), this::notifyWindows, 0, 20);
        }

        public void cancel() {
            task.cancel();
            task = null;
        }

        @Override
        public void addWindow(AbstractWindow window) {
            super.addWindow(window);
            if (task == null) start();
        }

        @Override
        public void removeWindow(AbstractWindow window) {
            super.removeWindow(window);
            if (getWindows().isEmpty() && task != null) cancel();
        }
    }

    @FunctionalInterface
    public interface SimpleItemProvider extends ItemProvider {
        @NotNull ItemStack get();
        default @NotNull ItemStack get(String lang) { return get(); }
    }
}
