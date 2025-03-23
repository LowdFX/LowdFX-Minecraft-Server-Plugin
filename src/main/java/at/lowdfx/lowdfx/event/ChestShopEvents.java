package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.block.ChestShopManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.marcpg.libpg.exception.MessagedException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Lidded;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class ChestShopEvents implements Listener {
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Lidded && block.getState() instanceof Container container)) return;

        ChestShopManager.Shop shop = ChestShopManager.getShop(block.getLocation());
        if (shop == null) return;

        if (player.isSneaking() && event.getHand() == EquipmentSlot.HAND && Perms.check(player, Perms.Perm.CHEST_SHOP_ADMIN)) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast den Shop als Admin geöffnet.", NamedTextColor.GOLD)));
            return;
        }

        if (shop.isOwner(player)) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast deinen Shop geöffnet.", NamedTextColor.GREEN)));
            return;
        }

        event.setCancelled(true);

        try {
            shop.transaction(container, player);
            player.sendMessage(LowdFX.serverMessage(Component.text("Erfolgreich! Du hast " + shop.item().getAmount() + " ").append(Component.translatable(shop.item().translationKey())).append(Component.text(" für " + shop.price() + " Diamanten gekauft.").color(NamedTextColor.GREEN))));
            Utilities.positiveSound(event.getPlayer());
        } catch (MessagedException e) {
            player.sendMessage(e.message());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        ChestShopManager.Shop shop = ChestShopManager.getShop(event.getBlock().getLocation());
        if (shop == null) return;

        if (shop.isOwner(event.getPlayer())) {
            ChestShopManager.removeShop(event.getBlock());
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du hast den Shop zerstört.", NamedTextColor.YELLOW)));
            return;
        }

        event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Shop nicht zerstören!", NamedTextColor.RED)));
        Utilities.negativeSound(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Chest)) return;
        Block connectedChest = Utilities.connectedChest(event.getBlock());
        if (connectedChest == null) return;

        ChestShopManager.Shop shop = ChestShopManager.getShop(connectedChest.getLocation());
        if (shop == null) return;

        // Prüfen, ob der Spieler berechtigt ist
        if (shop.notAllowed(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du kannst keine große Kiste aus einem Shop, den du nicht besitzt, machen!", NamedTextColor.RED)));
            Utilities.negativeSound(event.getPlayer());
        } else {
            ChestShopManager.registerShop(event.getBlock(), shop);
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Dein Shop wurde zu einer großen Kiste erweitert.", NamedTextColor.GREEN)));
            Utilities.positiveSound(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        event.blockList().removeIf(block -> ChestShopManager.isShop(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        event.blockList().removeIf(block -> ChestShopManager.isShop(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(@NotNull InventoryMoveItemEvent event) {
        ChestShopManager.Shop sourceShop = ChestShopManager.getShop(event.getSource().getLocation());
        if (sourceShop != null)
            event.setCancelled(true);

        ChestShopManager.Shop destinationShop = ChestShopManager.getShop(event.getDestination().getLocation());
        if (destinationShop != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (ChestShopManager.isShop(block.getLocation())) {  // Nur abbrechen, wenn es ein Shop ist.
                event.setCancelled(true);
                return;
            }
        }
    }



    @EventHandler
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (ChestShopManager.isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }


}
