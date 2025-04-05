package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.block.LockableManager;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class LockEvents implements Listener {
    private long lastInteract = 0;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        // Verhindert doppelte Aufrufe bei Türen etc.
        if (lastInteract + 20 > System.currentTimeMillis()) return;
        lastInteract = System.currentTimeMillis();

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (LockableManager.notLockable(block)) return;

        LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
        if (locked == null) return;

        if (locked.notAllowed(player)) {
            // Bei globalen Locks ist Interaktion erlaubt.
            if (locked.isGlobal()) return;
            player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist gesperrt! Du hast keinen Zugriff.", NamedTextColor.RED)));
            Utilities.negativeSound(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        LockableManager.Locked locked = LockableManager.getLocked(event.getBlock().getLocation());
        if (locked == null) return;

        if (locked.isOwner(event.getPlayer())) {
            LockableManager.unlock(event.getBlock().getLocation());
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du hast den gesperrten Block zerstört.", NamedTextColor.YELLOW)));
            return;
        }

        if (locked.isGlobal()) {
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist global gesperrt und darf nicht abgebaut werden!", NamedTextColor.RED)));
            Utilities.negativeSound(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht zerstören, weil er gesperrt ist!", NamedTextColor.RED)));
        Utilities.negativeSound(event.getPlayer());
        event.setCancelled(true);
    }

    // Bestehende Logik für die Erweiterung von Chests (Doppelchest)
    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        // Nur für Kisten (Chest) gilt diese Logik.
        if (!(event.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Chest)) return;

        // Ermitteln, ob diese Kiste an eine bereits bestehende Kiste (Double Chest) anschließt.
        Block connectedChest = Utilities.connectedChest(event.getBlock());
        if (connectedChest == null) return;

        // Hole den Lock der bereits gesperrten Kiste.
        LockableManager.Locked locked = LockableManager.getLocked(connectedChest.getLocation());
        if (locked == null) return;

        // Prüfen, ob der Spieler berechtigt ist.
        if (locked.notAllowed(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    LowdFX.serverMessage(Component.text(
                            "Du kannst keine große Kiste aus einer Kiste, die du nicht besitzt, machen!",
                            NamedTextColor.RED))
            );
        } else {
            // Aktualisiere den bestehenden Lock, falls die neue Kiste (die Erweiterung) noch nicht registriert ist.
            if (!locked.isBlock(SimpleLocation.ofLocation(event.getBlock().getLocation()))) {
                locked.connected = SimpleLocation.ofLocation(event.getBlock().getLocation());
                event.getPlayer().sendMessage(
                        LowdFX.serverMessage(Component.text(
                                "Die Kiste wurde zu einer großen Kiste erweitert und gesperrt.",
                                NamedTextColor.GREEN))
                );
                Utilities.positiveSound(event.getPlayer());
            }
        }
    }


    // Neuer Event-Handler: Automatisches Sperren von Openables und Brauständen beim Platzieren.
    @EventHandler(ignoreCancelled = true)
    public void onAutoLockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        // Überspringe, wenn der Block nicht sperrbar ist.
        if (LockableManager.notLockable(block)) return;
        // Für Chests, die sich mit einem bestehenden verbinden, übernimmt die bestehende Logik.
        if (block.getBlockData() instanceof org.bukkit.block.data.type.Chest) {
            Block connectedChest = Utilities.connectedChest(block);
            if (connectedChest != null) return;
        }
        // Falls bereits gesperrt, nichts tun.
        if (LockableManager.isLocked(block.getLocation())) return;

        Player player = event.getPlayer();
        LockableManager.lock(player.getUniqueId(), block, false);
        player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde automatisch gesperrt.", NamedTextColor.GREEN)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        event.blockList().removeIf(block -> LockableManager.isLocked(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        event.blockList().removeIf(block -> LockableManager.isLocked(block.getLocation()));
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        boolean cancel = false;
        Object srcHolder = event.getSource().getHolder();
        Object destHolder = event.getDestination().getHolder();
        Block srcBlock = null;
        Block destBlock = null;

        if (srcHolder instanceof org.bukkit.block.BlockState) {
            srcBlock = ((org.bukkit.block.BlockState) srcHolder).getBlock();
        }
        if (destHolder instanceof org.bukkit.block.BlockState) {
            destBlock = ((org.bukkit.block.BlockState) destHolder).getBlock();
        }

        // Prüfe, ob es sich um einen Hopper-Transfer handelt:
        // Fall 1: Hopper -> Container (hopperin)
        if (srcHolder instanceof org.bukkit.block.Hopper && destBlock != null) {
            if (LockableManager.isLocked(destBlock.getLocation())) {
                LockableManager.Locked lock = LockableManager.getLocked(destBlock.getLocation());
                if (!lock.isHopperInAllowed()) {
                    cancel = true;
                }
            }
        }
        // Fall 2: Container -> Hopper (hopperout)
        else if (destHolder instanceof org.bukkit.block.Hopper && srcBlock != null) {
            if (LockableManager.isLocked(srcBlock.getLocation())) {
                LockableManager.Locked lock = LockableManager.getLocked(srcBlock.getLocation());
                if (!lock.isHopperOutAllowed()) {
                    cancel = true;
                }
            }
        }
        // Fallback: Wenn keiner der speziellen Fälle greift, und eines der Inventories gesperrt ist, abbrechen.
        else {
            if ((srcBlock != null && LockableManager.isLocked(srcBlock.getLocation())) ||
                    (destBlock != null && LockableManager.isLocked(destBlock.getLocation()))) {
                cancel = true;
            }
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (LockableManager.isLocked(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (LockableManager.isLocked(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
