package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.inventory.LockableData;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class LockEvents implements Listener {
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Container || block.getBlockData() instanceof Openable)) return;

        Location lockLocation = block.getLocation();

        // Überprüfen, ob die Kiste gesperrt ist.
        if (LockableData.isLocked(lockLocation)) {
            // Überprüfen, ob der Spieler berechtigt ist, auf die Kiste zuzugreifen.
            if (LockableData.notOwner(player.getName(), lockLocation) && !LockableData.isPlayerInWhitelist(lockLocation, player.getName()) && !Perms.check(player, Perms.Perm.LOCK_ADMIN)) {
                // Zugriff verweigern, da der Spieler kein Besitzer ist oder nicht auf der Whitelist steht.
                event.setCancelled(true);
                player.sendMessage(LowdFX.serverMessage(Component.text("Diese Kiste ist gesperrt! Du hast keinen Zugriff.", NamedTextColor.RED)));
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(@NotNull InventoryMoveItemEvent event) {
        // Prüfe, ob die Quelle (Hopper zieht Items) oder Ziel (Items werden abgelegt) eine Kiste oder Shulkerkiste ist
        if (event.getSource().getHolder() instanceof Container container) {
            if (LockableData.isLocked(container.getLocation())) {
                if (event.getInitiator().getHolder() instanceof Player player && Perms.check(player, Perms.Perm.LOCK_ADMIN)) return;
                event.setCancelled(true);
            }
        }

        // Genauso bei Ziel (Destination)
        if (event.getDestination().getHolder() instanceof Container container) {
            if (LockableData.isLocked(container.getLocation())) {
                if (event.getInitiator().getHolder() instanceof Player player && Perms.check(player, Perms.Perm.LOCK_ADMIN)) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!(block.getState() instanceof Container || block.getBlockData() instanceof Openable)) return;

        Location chestLocation = block.getLocation();

        // Überprüfen, ob die Kiste gesperrt ist
        if (LockableData.isLocked(chestLocation)) {
            // Überprüfen, ob der Spieler auf der Whitelist oder Besitzer ist
            if (!LockableData.isPlayerInWhitelist(chestLocation, player.getName()) && LockableData.notOwner(player.getName(), chestLocation) && !Perms.check(player, Perms.Perm.LOCK_ADMIN)) {
                // Kiste ist gesperrt und der Spieler ist weder Besitzer noch auf der Whitelist
                event.setCancelled(true);
                player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist gesperrt und du kannst ihn nicht abbauen.", NamedTextColor.RED)));
            } else {
                // Spieler ist Besitzer oder auf der Whitelist, Kiste kann gelöscht werden
                LockableData.removeDestroyedBlock(chestLocation);
                player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block wurde gelöscht.", NamedTextColor.GREEN)));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!(block.getState() instanceof Chest)) return;

        // Prüfen, ob die platzierte Kiste eine Nachbarkiste hat, die gesperrt ist
        Set<Location> connectedChests = Utilities.connectedChests(block);
        Location chestLocation = block.getLocation();
        for (Location adjacentLocation : connectedChests) {
            // Wenn die Kiste gesperrt ist
            if (LockableData.isLocked(adjacentLocation)) {
                LockableData.lockAdjacentChests(chestLocation, player.getName());
                // Verhindern, dass ein unberechtigter Spieler eine Doppelkiste erstellt
                if (!LockableData.isPlayerInWhitelist(adjacentLocation, player.getName()) && !Perms.check(player, Perms.Perm.LOCK_ADMIN)) {
                    event.setCancelled(true);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst keine Doppelkiste mit einer gesperrten Kiste erstellen.", NamedTextColor.RED)));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        try {
            event.blockList().removeAll(explosionBlocks(event.blockList()));
        } catch (Exception e) {
            LowdFX.LOG.error("Couldn't save locked blocks from explosion.", e);
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        try {
            event.blockList().removeAll(explosionBlocks(event.blockList()));
        } catch (Exception e) {
            LowdFX.LOG.error("Couldn't save locked blocks from explosion.", e);
        }
    }

    // Liste von Blöcken, die von der Explosion betroffen sind
    public static @NotNull List<Block> explosionBlocks(@NotNull List<Block> blocks) {
        return blocks.stream()
                .filter(b -> b.getState() instanceof Container || b.getBlockData() instanceof Openable)
                .filter(b -> LockableData.isLocked(b.getLocation()))
                .toList();
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        pistonStuff(event, event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        pistonStuff(event, event.getBlocks());
    }

    public void pistonStuff(BlockPistonEvent event, List<Block> blocks) {
        try {
            // Überprüfe, ob einer der Blöcke gesperrt ist, bevor der Block gezogen/gepusht wird.
            for (Block block : blocks) {
                if (!(block.getState() instanceof Container || block.getBlockData() instanceof Openable)) continue;

                if (LockableData.isLocked(block.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (Exception e) {
            LowdFX.LOG.error("Irgendwas funktioniert nicht.", e);
        }
    }
}