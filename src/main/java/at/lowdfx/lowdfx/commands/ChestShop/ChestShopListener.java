package at.lowdfx.lowdfx.commands.ChestShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class ChestShopListener implements Listener {

    private final ChestShopManager shopManager;

    public static final String adminPermission = "lowdfx.chestshop.admin";
    public static final String playerPermission = "lowdfx.chestshop";

    public ChestShopListener(ChestShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (shopManager.isShop(location)) {
            Player player = event.getPlayer();

            ShopData shop = shopManager.getShop(location).orElse(null);

            if (shop == null) return;

            if (player.getUniqueId().equals(shop.getOwner())) {
                shopManager.removeShop(location);
                player.sendMessage(ChatColor.YELLOW + "You destroyed your shop.");
                return;
            }

            if (player.hasPermission(adminPermission)) {
                shopManager.removeShop(location);
                player.sendMessage(ChatColor.YELLOW + "You destroyed a shop as an admin.");
                block.breakNaturally();
                return;
            }

            player.sendMessage(ChatColor.RED + "You cannot break this shop!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        // Prüfen, ob die platzierte Kiste eine CHEST ist
        if (block.getType() == Material.CHEST) {
            Location placedLocation = block.getLocation();
            Set<Location> connectedChests = getConnectedChests(block);

            for (Location adjacentLocation : connectedChests) {
                if (shopManager.isShop(adjacentLocation)) {
                    Player player = event.getPlayer();

                    // Prüfen, ob der Spieler berechtigt ist
                    if (!shopManager.isOwner(player.getUniqueId(), adjacentLocation) &&
                            !shopManager.isWhitelisted(player.getUniqueId(), adjacentLocation)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot create a double chest with a shop you don't own!");
                        return;
                    } else {
                        // Spieler ist Besitzer, erweitern der Shop-Kiste
                        shopManager.registerShop(player.getUniqueId(), placedLocation,
                                shopManager.getShop(adjacentLocation).orElseThrow(() ->
                                        new IllegalStateException("Shop data not found for adjacent chest.")));

                        // Zentrieren des Hologramms
                        Location centerLocation = placedLocation.add(adjacentLocation).multiply(0.5);
                        shopManager.updateHologramForDoubleChest(player.getUniqueId(), adjacentLocation);

                        player.sendMessage(ChatColor.GREEN + "You have expanded your shop to a double chest.");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> shopManager.isShop(block.getLocation()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> shopManager.isShop(block.getLocation()));
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        Block sourceBlock = null;
        Block targetBlock = null;

        if (event.getSource().getHolder() instanceof Chest || event.getSource().getHolder() instanceof ShulkerBox) {
            sourceBlock = ((org.bukkit.block.Container) event.getSource().getHolder()).getBlock();
        } else if (event.getDestination().getHolder() instanceof Chest || event.getDestination().getHolder() instanceof ShulkerBox) {
            targetBlock = ((org.bukkit.block.Container) event.getDestination().getHolder()).getBlock();
        }

        if ((sourceBlock != null && shopManager.isShop(sourceBlock.getLocation())) ||
                (targetBlock != null && shopManager.isShop(targetBlock.getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !(block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX"))) {
            return;
        }

        Location location = block.getLocation();

        if (!shopManager.isShop(location)) {
            return;
        }

        ShopData shop = shopManager.getShop(location).orElse(null);
        if (shop == null) return;

        event.setCancelled(true);

        if (player.hasPermission(adminPermission) || shopManager.isOwner(player.getUniqueId(), location)) {
            if (event.getAction().isLeftClick()) {
                shopManager.removeShop(location);
                player.sendMessage(ChatColor.YELLOW + "You destroyed the shop as an admin.");
                block.breakNaturally();
                return;
            } else if (player.isSneaking() && event.getHand() == EquipmentSlot.HAND) {
                player.openInventory(((org.bukkit.block.Container) block.getState()).getInventory());
                player.sendMessage(ChatColor.GOLD + "Admin access granted.");
                return;
            }
        }

        if (player.getUniqueId().equals(shop.getOwner())) {
            player.openInventory(((org.bukkit.block.Container) block.getState()).getInventory());
            player.sendMessage(ChatColor.GREEN + "You opened your shop.");
            return;
        }

        Inventory shopInventory = ((org.bukkit.block.Container) block.getState()).getInventory();
        ItemStack itemToSell = shop.getItem().clone();
        int price = shop.getPrice();

        Inventory playerInventory = player.getInventory();
        ItemStack payment = new ItemStack(Material.DIAMOND, price);

        if (!shopInventory.containsAtLeast(itemToSell, itemToSell.getAmount())) {
            player.sendMessage(ChatColor.RED + "The shop is out of stock!");
            return;
        }

        if (!playerInventory.containsAtLeast(payment, price)) {
            player.sendMessage(ChatColor.RED + "You don't have enough diamonds to purchase this item.");
            return;
        }

        shopInventory.removeItem(itemToSell);
        playerInventory.removeItem(payment);
        playerInventory.addItem(itemToSell);
        shopInventory.addItem(payment);

        player.sendMessage(ChatColor.GREEN + "Purchase successful! You bought " + itemToSell.getAmount() + " " + itemToSell.getType() + " for " + price + " diamonds.");

        // Schedule stock update after purchase
        scheduleStockUpdate(location, shopInventory, shop);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (shopManager.isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (shopManager.isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void scheduleStockUpdate(Location location, Inventory inventory, ShopData shop) {
        new BukkitRunnable() {
            @Override
            public void run() {
                shopManager.startHologramUpdater(location, shop);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("lowdfx"), 20L);
    }

    private Set<Location> getConnectedChests(Block chestBlock) {
        Set<Location> connectedChests = new HashSet<>();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chestBlock.getRelative(face);
            if (relativeBlock.getType() == Material.CHEST) {
                connectedChests.add(relativeBlock.getLocation());
            }
        }
        return connectedChests;
    }
}
