package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.block.LockableManager;
import at.lowdfx.lowdfx.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
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
        // Prevents duplicates, as the event is called twice for doors and other blocks if cancelled.
        if (lastInteract + 20 > System.currentTimeMillis()) return;
        lastInteract = System.currentTimeMillis();

        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        if (LockableManager.notLockable(block)) return;

        LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
        if (locked == null) return;

        if (locked.notAllowed(player)) {
            // Erlaube Interaktion bei global lock
            if (locked.isGlobal()) return;

            player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist gesperrt! Du hast keinen Zugriff.", NamedTextColor.RED)));
            Utilities.negativeSound(player);
            event.setCancelled(true);
            return;
        }



        player.sendMessage(LowdFX.serverMessage(Component.text("Du hast einen gesperrten Block geöffnet.", NamedTextColor.GREEN)));
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

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (!(event.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Chest)) return;
        Block connectedChest = Utilities.connectedChest(event.getBlock());
        if (connectedChest == null) return;

        LockableManager.Locked locked = LockableManager.getLocked(connectedChest.getLocation());
        if (locked == null) return;

        // Prüfen, ob der Spieler berechtigt ist
        if (locked.notAllowed(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Du kannst keine große Kiste aus einer Kiste, den du nicht besitzt, machen!", NamedTextColor.RED)));
        } else {
            LockableManager.lock(event.getBlock(), locked);
            event.getPlayer().sendMessage(LowdFX.serverMessage(Component.text("Die Kiste wurde zu einer großen Kiste erweitert.", NamedTextColor.GREEN)));
            Utilities.positiveSound(event.getPlayer());
        }
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
    public void onInventoryMoveItem(@NotNull InventoryMoveItemEvent event) {
        if (LockableManager.isLocked(event.getSource().getLocation()) || LockableManager.isLocked(event.getDestination().getLocation()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (LockableManager.isLocked(block.getLocation())) {  // Nur abbrechen, wenn der Block gesperrt ist.
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