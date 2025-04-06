package at.lowdfx.lowdfx.managers.block;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.HologramManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.exception.MessagedException;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChestShopManager {
    public static final class Shop {
        private final UUID owner;
        private final SimpleLocation location;
        public SimpleLocation connected;
        private byte[] serializedItem;
        private final AtomicInteger price;
        private final ArrayList<UUID> whitelist;

        public Shop(UUID owner, SimpleLocation location, SimpleLocation connected, byte[] serializedItem, AtomicInteger price, ArrayList<UUID> whitelist) {
            LowdFX.LOG.info("Shop (byte[]) 1");
            this.owner = owner;
            this.location = location;
            this.connected = connected;
            this.serializedItem = serializedItem;
            this.price = price;
            this.whitelist = whitelist;
            LowdFX.LOG.info("Shop (byte[]) 2");
        }

        public void setItem(@NotNull ItemStack item) {
            this.serializedItem = item.serializeAsBytes();
        }


        public @NotNull ItemStack item() {
            return ItemStack.deserializeBytes(serializedItem).clone();
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

        public boolean isBlock(SimpleLocation loc) {
            return Objects.equals(loc, location) || Objects.equals(loc, connected);
        }

        /**
         * <strong>Please keep in mind that this does not validate anything and does not remove the item from the
         * chest!</strong>
         *
         * @param player The player to transact to.
         */
        public void transaction(@NotNull Container shopBlock, @NotNull Player player) throws MessagedException {
            ItemStack item = ItemStack.deserializeBytes(serializedItem);

            Inventory shopInv = shopBlock.getInventory();
            Inventory playerInv = player.getInventory();

            ItemStack payment = new ItemStack(Material.DIAMOND, price.get());

            if (!shopInv.containsAtLeast(item, item.getAmount()))
                throw new MessagedException("Der Shop ist ausverkauft!");
            if (!playerInv.containsAtLeast(payment, price.get()))
                throw new MessagedException("Du hast nicht genug Diamanten, um das zu kaufen.");

            playerInv.addItem(item.clone());
            shopInv.removeItem(item.clone());

            playerInv.removeItem(payment);
            shopInv.addItem(payment);
        }

        public UUID owner() {
            return owner;
        }

        public SimpleLocation location() {
            return location;
        }

        public AtomicInteger price() {
            return price;
        }

        public ArrayList<UUID> whitelist() {
            return whitelist;
        }
    }

    public static final Map<UUID, ArrayList<Shop>> SHOPS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(SHOPS, LowdFX.DATA_DIR.resolve("shops.json").toFile());
    }

    public static void load() {
        SHOPS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("shops.json").toFile(), Map.of(), new TypeToken<>() {}));

        // Holograms:
        HologramManager.removeAll();
        for (ArrayList<Shop> playerShops : SHOPS.values()) {
            playerShops.forEach(s -> spawnHologram(s.location().asLocation()));
        }
    }

    public static void registerShop(UUID owner, @NotNull Block block, ItemStack item, int price) {
        registerShop(block, new Shop(owner, SimpleLocation.ofLocation(block.getLocation()), null, item.serializeAsBytes(), new AtomicInteger(price), new ArrayList<>()));
    }

    public static void registerShop(@NotNull Block block, @NotNull Shop data) {
        ArrayList<Shop> ownerShops = SHOPS.computeIfAbsent(data.owner(), k -> new ArrayList<>());
        ownerShops.add(data);

        Block connectedChest = Utilities.connectedChest(data.location().asLocation().getBlock());
        if (connectedChest != null && ownerShops.stream().noneMatch(s -> s.isBlock(SimpleLocation.ofLocation(connectedChest.getLocation())))) {
            data.connected = SimpleLocation.ofLocation(connectedChest.getLocation());
            spawnHologram(connectedChest.getLocation());
        }

        spawnHologram(block.getLocation());
    }

    public static void removeShop(Block block) {
        for (Map.Entry<UUID, ArrayList<Shop>> e : SHOPS.entrySet()) {
            if (!e.getValue().removeIf(s -> s.isBlock(SimpleLocation.ofLocation(block.getLocation())))) continue;

            Block connected = Utilities.connectedChest(block);
            if (connected != null)
                HologramManager.remove(connected.getLocation());

            HologramManager.remove(block.getLocation());
            break;
        }
    }

    public static boolean isShop(Location location) {
        return getShop(location) != null;
    }

    public static @Nullable Shop getShop(Location location) {
        if (location == null) return null;
        for (Map.Entry<UUID, ArrayList<Shop>> e : SHOPS.entrySet()) {
            for (Shop s : e.getValue()) {
                if (s.isBlock(SimpleLocation.ofLocation(location)))
                    return s;
            }
        }
        return null;
    }

    public static int stock(@NotNull Shop shop) {
        Block block = shop.location().asLocation().getBlock();
        if (!(block.getState() instanceof Container container)) return 0;

        return Arrays.stream(container.getInventory().getContents())
                .filter(item -> item != null && shop.item().isSimilar(item))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    // ====================================
    // ========== HOLOGRAM STUFF ==========
    // ====================================

    public static void spawnHologram(Location location) {
        HologramManager.spawnSafe(location);
    }

    public static @NotNull @Unmodifiable List<Component> hologramText(@NotNull Shop shop) {
        return List.of(
                Component.translatable(shop.item().translationKey()).append(Component.text(" x " + shop.item().getAmount())).color(NamedTextColor.YELLOW),
                Component.text("Stock: " + stock(shop), NamedTextColor.GREEN),
                Component.text("Price: " + shop.price().get() + " Diamonds", NamedTextColor.AQUA)
        );
    }
}
