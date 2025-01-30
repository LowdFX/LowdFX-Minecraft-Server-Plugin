package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.inventory.ShopData;
import at.lowdfx.lowdfx.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ChestShopEvents implements Listener {
    public static final String ADMIN_PERMISSION = "lowdfx.chestshop.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.chestshop";

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (LowdFX.SHOP_MANAGER.isShop(location)) {
            Player player = event.getPlayer();

            ShopData shop = LowdFX.SHOP_MANAGER.getShop(location).orElse(null);

            if (shop == null) return;

            if (player.getUniqueId().equals(shop.owner())) {
                LowdFX.SHOP_MANAGER.removeShop(location);
                player.sendMessage(Component.text("You destroyed your shop.", NamedTextColor.YELLOW));
                return;
            }

            if (player.hasPermission(ADMIN_PERMISSION)) {
                LowdFX.SHOP_MANAGER.removeShop(location);
                player.sendMessage(Component.text("You destroyed a shop as an admin.", NamedTextColor.YELLOW));
                block.breakNaturally();
                return;
            }

            player.sendMessage(Component.text("You cannot break this shop!", NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Block block = event.getBlock();

        // Prüfen, ob die platzierte Kiste eine CHEST ist
        if (block.getType() == Material.CHEST) {
            Location placedLocation = block.getLocation();
            Set<Location> connectedChests = getConnectedChests(block);

            for (Location adjacentLocation : connectedChests) {
                if (LowdFX.SHOP_MANAGER.isShop(adjacentLocation)) {
                    Player player = event.getPlayer();

                    // Prüfen, ob der Spieler berechtigt ist
                    if (!LowdFX.SHOP_MANAGER.isOwner(player.getUniqueId(), adjacentLocation) &&
                            !LowdFX.SHOP_MANAGER.isWhitelisted(player.getUniqueId(), adjacentLocation)) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("You cannot create a double chest with a shop you don't own!", NamedTextColor.RED));
                    } else {
                        // Spieler ist Besitzer, erweitern der Shop-Kiste
                        LowdFX.SHOP_MANAGER.registerShop(player.getUniqueId(), placedLocation,
                                LowdFX.SHOP_MANAGER.getShop(adjacentLocation).orElseThrow(() -> new IllegalStateException("Shop data not found for adjacent chest.")));

                        // Zentrieren des Hologramms
                        LowdFX.SHOP_MANAGER.updateHologramForDoubleChest(player.getUniqueId(), adjacentLocation);

                        player.sendMessage(Component.text("You have expanded your shop to a double chest.", NamedTextColor.GREEN));
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        event.blockList().removeIf(block -> LowdFX.SHOP_MANAGER.isShop(block.getLocation()));
    }

    @EventHandler
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        event.blockList().removeIf(block -> LowdFX.SHOP_MANAGER.isShop(block.getLocation()));
    }

    @EventHandler
    public void onInventoryMove(@NotNull InventoryMoveItemEvent event) {
        Block sourceBlock = null;
        Block targetBlock = null;

        if (event.getSource().getHolder() instanceof Chest || event.getSource().getHolder() instanceof ShulkerBox) {
            sourceBlock = ((org.bukkit.block.Container) event.getSource().getHolder()).getBlock();
        } else if (event.getDestination().getHolder() instanceof Chest || event.getDestination().getHolder() instanceof ShulkerBox) {
            targetBlock = ((org.bukkit.block.Container) event.getDestination().getHolder()).getBlock();
        }

        if ((sourceBlock != null && LowdFX.SHOP_MANAGER.isShop(sourceBlock.getLocation())) ||
                (targetBlock != null && LowdFX.SHOP_MANAGER.isShop(targetBlock.getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !(block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX"))) return;

        Location location = block.getLocation();

        if (!LowdFX.SHOP_MANAGER.isShop(location)) return;

        ShopData shop = LowdFX.SHOP_MANAGER.getShop(location).orElse(null);
        if (shop == null) return;

        event.setCancelled(true);

        if (player.hasPermission(ADMIN_PERMISSION) || LowdFX.SHOP_MANAGER.isOwner(player.getUniqueId(), location)) {
            if (event.getAction().isLeftClick()) {
                LowdFX.SHOP_MANAGER.removeShop(location);
                player.sendMessage(Component.text("You destroyed the shop as an admin.", NamedTextColor.YELLOW));
                block.breakNaturally();
                return;
            } else if (player.isSneaking() && event.getHand() == EquipmentSlot.HAND) {
                player.openInventory(((org.bukkit.block.Container) block.getState()).getInventory());
                player.sendMessage(Component.text("Admin access granted.", NamedTextColor.GOLD));
                return;
            }
        }

        if (player.getUniqueId().equals(shop.owner())) {
            player.openInventory(((org.bukkit.block.Container) block.getState()).getInventory());
            player.sendMessage(Component.text("You opened your shop.", NamedTextColor.GREEN));
            return;
        }

        Inventory shopInventory = ((org.bukkit.block.Container) block.getState()).getInventory();
        ItemStack itemToSell = shop.item().clone();
        int price = shop.price();

        Inventory playerInventory = player.getInventory();
        ItemStack payment = new ItemStack(Material.DIAMOND, price);

        if (!shopInventory.containsAtLeast(itemToSell, itemToSell.getAmount())) {
            player.sendMessage(Component.text("The shop is out of stock!", NamedTextColor.RED));
            return;
        }

        if (!playerInventory.containsAtLeast(payment, price)) {
            player.sendMessage(Component.text("You don't have enough diamonds to purchase this item.", NamedTextColor.RED));
            return;
        }

        shopInventory.removeItem(itemToSell);
        playerInventory.removeItem(payment);
        playerInventory.addItem(itemToSell);
        shopInventory.addItem(payment);

        player.sendMessage(Component.text("Purchase successful! You bought " + itemToSell.getAmount() + " ").append(itemToSell.displayName()).append(Component.text(" for " + price + " diamonds.").color(NamedTextColor.GREEN)));

        // Schedule stock update after purchase
        scheduleStockUpdate(location, shop);
    }

    @EventHandler
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (LowdFX.SHOP_MANAGER.isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (LowdFX.SHOP_MANAGER.isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void scheduleStockUpdate(Location location, ShopData shop) {
        Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> LowdFX.SHOP_MANAGER.startHologramUpdater(location, shop), 20L);
    }

    private @NotNull Set<Location> getConnectedChests(Block chestBlock) {
        return Utilities.connectedChests(chestBlock);
    }
}
