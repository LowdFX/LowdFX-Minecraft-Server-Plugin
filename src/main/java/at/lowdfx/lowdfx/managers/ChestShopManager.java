package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.HologramManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.marcpg.libpg.exception.MessagedException;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChestShopManager {
    public record Shop(UUID owner, Location location, ItemStack item, AtomicInteger price, ArrayList<UUID> whitelist) {
        @Override
        public @NotNull ItemStack item() {
            return item.clone();
        }

        public void removeWhitelist(UUID player) {
            whitelist.remove(player);
        }

        public void addWhitelist(UUID player) {
            if (player == owner) return;
            whitelist.add(player);
        }

        public boolean isOwner(@NotNull Player player) {
            return owner.equals(player.getUniqueId()) || Perms.check(player, Perms.Perm.CHEST_SHOP_ADMIN);
        }

        public boolean notAllowed(@NotNull Player player) {
            return !whitelist.contains(player.getUniqueId()) && !isOwner(player);
        }

        /**
         * <strong>Please keep in mind that this does not validate anything and does not remove the item from the chest!</strong>
         * @param player The player to transact to.
         */
        public void transaction(@NotNull Container shopBlock, @NotNull Player player) throws MessagedException {
            Inventory shopInv = shopBlock.getInventory();
            Inventory playerInv = player.getInventory();

            ItemStack payment = new ItemStack(Material.DIAMOND, price.get());

            if (!shopInv.containsAtLeast(item, item.getAmount()))
                throw new MessagedException("Der Shop ist ausverkauft!");
            if (!playerInv.containsAtLeast(payment, price.get()))
                throw new MessagedException("Du hast nicht genug Diamanten, um das zu kaufen.");

            playerInv.removeItem(payment);
            shopInv.addItem(payment);

            playerInv.addItem(item.clone());
            shopInv.removeItem(item.clone());

            startHologramUpdater(location, this);
        }
    }

    public static final Map<UUID, Map<Location, Shop>> SHOPS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(SHOPS, LowdFX.DATA_DIR.resolve("shops.json").toFile());

        // Holograms:
        HologramManager.removeAll();
    }

    public static void load() {
        SHOPS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("shops.json").toFile(), Map.of()));

        // Holograms:
        for (Map<Location, Shop> playerShops : SHOPS.values()) {
            playerShops.forEach(ChestShopManager::spawnHologram);
        }
    }

    public static void registerShop(UUID owner, @NotNull Block block, ItemStack item, int price) {
        registerShop(block, new Shop(owner, block.getLocation(), item, new AtomicInteger(price), new ArrayList<>()));
    }

    public static void registerShop(@NotNull Block block, @NotNull Shop data) {
        Map<Location, Shop> ownerShops = SHOPS.computeIfAbsent(data.owner(), k -> new HashMap<>());
        ownerShops.put(block.getLocation(), data);

        Block connectedChest = Utilities.connectedChest(data.location().getBlock());
        if (connectedChest != null && !ownerShops.containsKey(connectedChest.getLocation())) {
            ownerShops.put(connectedChest.getLocation(), data);
            spawnHologram(connectedChest.getLocation(), data);
        }

        spawnHologram(block.getLocation(), data);
    }

    public static void removeShop(Block block) {
        for (Map.Entry<UUID, Map<Location, Shop>> e : SHOPS.entrySet()) {
            if (e.getValue().remove(block.getLocation()) == null) continue;

            Block connected = Utilities.connectedChest(block);
            if (connected != null)
                HologramManager.remove(connected.getLocation());

            HologramManager.remove(block.getLocation());
            break;
        }
    }

    public static boolean isShop(Location location) {
        if (location == null) return false;
        for (Map.Entry<UUID, Map<Location, Shop>> e : SHOPS.entrySet()) {
            if (e.getValue().containsKey(location)) return true;
        }
        return false;
    }

    public static @Nullable Shop getShop(Location location) {
        if (location == null) return null;
        for (Map.Entry<UUID, Map<Location, Shop>> e : SHOPS.entrySet()) {
            Shop d = e.getValue().get(location);
            if (d != null)
                return d;
        }
        return null;
    }

    public static int stock(@NotNull Shop shop) {
        Block block = shop.location().getBlock();
        if (!(block.getState() instanceof ShulkerBox container)) return 0;

        return Arrays.stream(container.getInventory().getContents())
                .filter(item -> item != null && shop.item().isSimilar(item))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    // ====================================
    // ========== HOLOGRAM STUFF ==========
    // ====================================

    public static void spawnHologram(Location location, @NotNull Shop shop) {
        HologramManager.spawnSafe(location, hologramText(shop));
    }

    public static void startHologramUpdater(Location location, Shop shop) {
        HologramManager.runUpdater(location, 60, () -> hologramText(shop));
    }

    private static @NotNull @Unmodifiable List<Component> hologramText(@NotNull Shop shop) {
        return List.of(
                Component.translatable(shop.item().translationKey()).append(Component.text(" x " + shop.item().getAmount())).color(NamedTextColor.YELLOW),
                Component.text("Stock: " + stock(shop), NamedTextColor.GREEN),
                Component.text("Price: " + shop.price().get() + " Diamonds", NamedTextColor.AQUA)
        );
    }
}
