package at.lowdfx.lowdfx.commands.chestLock;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChestLockListener implements Listener {
    private final lowdfx plugin;

    public ChestLockListener(lowdfx plugin) {
        this.plugin = plugin;
    }

    // Hilfsfunktion zur Abfrage der verbundenen Kisten
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


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return; // Wenn der Block null ist, gehe zurück, ohne etwas zu tun
        }
        // Überprüfen, ob der Block existiert und eine Kiste ist
        if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
            Location chestLocation = block.getLocation();
            ChestData data = plugin.getChestData();

            // Überprüfen, ob die Kiste gesperrt ist
            if (data.isChestLocked(chestLocation)) {
                // Überprüfen, ob der Spieler berechtigt ist, auf die Kiste zuzugreifen
                if (!data.isOwner(player.getName(), chestLocation) && !data.isPlayerInWhitelist(chestLocation, player.getName()) && !player.hasPermission(ChestLockCommand.adminPermission)) {
                    // Zugriff verweigern, da der Spieler kein Besitzer ist oder nicht auf der Whitelist steht
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Diese Kiste ist gesperrt! Du hast keinen Zugriff.");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Prüfe, ob die Quelle (Hopper zieht Items) oder Ziel (Items werden abgelegt) eine Kiste oder Shulkerkiste ist
        if (event.getSource().getHolder() instanceof BlockState) {
            BlockState blockState = (BlockState) event.getSource().getHolder();
            if (blockState.getBlock().getType().name().endsWith("CHEST") || blockState.getBlock().getType().name().endsWith("SHULKER_BOX")) {

                // Hole die Location von der Quelle
                Location chestLocation = blockState.getLocation();

                if (chestLocation == null) return; // Falls keine Location ermittelt wurde

                ChestData data = plugin.getChestData();
                if (data.isChestLocked(chestLocation)) {
                    // Überprüfe, ob ein Spieler die Aktion ausführt und die erforderliche Permission hat
                    Player player = null;

                    // Versuche, einen Player-Interagierer zu ermitteln
                    if (event.getInitiator().getHolder() instanceof Player) {
                        player = (Player) event.getInitiator().getHolder();
                    }

                    // Abbrechen, falls die Kiste gesperrt ist und der Spieler keine Permission hat
                    if (player == null || !player.hasPermission(ChestLockCommand.adminPermission)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Genauso bei Ziel (Destination)
        if (event.getDestination().getHolder() instanceof BlockState) {
            BlockState blockState = (BlockState) event.getDestination().getHolder();
            if (blockState.getBlock().getType().name().endsWith("CHEST") || blockState.getBlock().getType().name().endsWith("SHULKER_BOX")) {

                // Hole die Location von der Ziel-Box
                Location chestLocation = blockState.getLocation();

                if (chestLocation == null) return; // Falls keine Location ermittelt wurde

                ChestData data = plugin.getChestData();
                if (data.isChestLocked(chestLocation)) {
                    // Überprüfe, ob ein Spieler die Aktion ausführt und die erforderliche Permission hat
                    Player player = null;

                    // Versuche, einen Player-Interagierer zu ermitteln
                    if (event.getInitiator().getHolder() instanceof Player) {
                        player = (Player) event.getInitiator().getHolder();
                    }

                    // Abbrechen, falls die Kiste gesperrt ist und der Spieler keine Permission hat
                    if (player == null || !player.hasPermission(ChestLockCommand.adminPermission)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }




    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        // Prüfe, ob der Block eine Kiste oder Shulker-Kiste ist
        if (type == Material.CHEST || type.name().endsWith("SHULKER_BOX")) {
            // Hole die Position der Kiste
            Location chestLocation = block.getLocation();
            ChestData data = plugin.getChestData();

            // Überprüfen, ob die Kiste gesperrt ist
            if (data.isChestLocked(chestLocation)) {
                // Überprüfen, ob der Spieler auf der Whitelist oder Besitzer ist
                if (!data.isPlayerInWhitelist(chestLocation, player.getName()) && !data.isOwner(player.getName(), chestLocation) && !player.hasPermission(ChestLockCommand.adminPermission)) {
                    // Kiste ist gesperrt und der Spieler ist weder Besitzer noch auf der Whitelist
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Diese Kiste ist gesperrt und du kannst sie nicht abbauen.");
                } else {
                    // Spieler ist Besitzer oder auf der Whitelist, Kiste kann gelöscht werden
                    data.removeDestroyedChest(chestLocation);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Diese Kiste wurde gelöscht.");
                }
            }
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Wenn die platzierte Kiste nicht vom Typ CHEST ist, brechen wir ab.
        if (block.getType() != Material.CHEST && !block.getType().name().endsWith("SHULKER_BOX")) {
            return;
        }

        ChestData data = plugin.getChestData();

        // Prüfen, ob die platzierte Kiste eine Nachbarkiste hat, die gesperrt ist
        Set<Location> connectedChests = getConnectedChests(block);
        Location chestLocation = block.getLocation();
        for (Location adjacentLocation : connectedChests) {
            // Wenn die Kiste gesperrt ist
            if (data.isChestLocked(adjacentLocation)) {
                data.lockAdjacentChests(chestLocation, player.getName());
                // Verhindern, dass ein unberechtigter Spieler eine Doppelkiste erstellt
                if (!data.isPlayerInWhitelist(adjacentLocation, player.getName()) &&
                        !player.hasPermission(ChestLockCommand.adminPermission)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst keine Doppelkiste mit einer gesperrten Kiste erstellen.");
                    return;
                }
            }
        }
    }


    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        try {
            // Überprüfen, ob ChestData geladen ist
            if (plugin.getChestData() == null) {
                return;
            }

            // Überprüfe, ob es sich um eine Explosion handelt, die durch TNT, Creeper, etc. ausgelöst wurde
            if (event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Creeper || event.getEntity() instanceof Explosive || event.getEntity() instanceof ExplosiveMinecart) {
                // Durch alle Blöcke in der Explosion iterieren
                List<Block> blocksToRemove = new ArrayList<>();
                for (Block block : event.blockList()) {
                    // Wenn der Block eine Kiste oder Endertruhen ist
                    if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                        Location chestLocation = block.getLocation();
                        ChestData data = plugin.getChestData();

                        // Wenn die Kiste gesperrt ist
                        if (data.isChestLocked(chestLocation)) {
                            // Block zur Liste der zu entfernenden Blöcke hinzufügen
                            blocksToRemove.add(block);
                        }
                    }
                }

                // Alle gesperrten Kisten aus der Explosionsliste entfernen
                event.blockList().removeAll(blocksToRemove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        try {
            // Überprüfen, ob ChestData geladen ist
            if (plugin.getChestData() == null) {
                return;
            }

            // Liste von Blöcken, die von der Explosion betroffen sind
            List<Block> blocksToRemove = new ArrayList<>();
            for (Block block : event.blockList()) {
                // Wenn es sich um eine Kiste handelt
                if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                    Location chestLocation = block.getLocation();
                    ChestData data = plugin.getChestData();

                    // Wenn die Kiste gesperrt ist, verhindern wir die Zerstörung
                    if (data.isChestLocked(chestLocation)) {
                        // Block zur Liste der zu entfernenden Blöcke hinzufügen
                        blocksToRemove.add(block);
                    }
                }
            }

            // Alle gesperrten Kisten aus der Explosionsliste entfernen
            event.blockList().removeAll(blocksToRemove);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        try {
            if (plugin.getChestData() == null) {
                return;  // Frühes Abbrechen, falls `chestData` nicht verfügbar ist
            }

            // Überprüfe jedes Block in der Block-Liste der Pistonen-Erweiterung
            for (Block block : event.getBlocks()) {
                if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                    // Wenn die Kiste gesperrt ist, verhindere die Pistonen-Erweiterung
                    if (plugin.getChestData().isChestLocked(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        try {
            if (plugin.getChestData() == null) {
                return;  // Frühes Abbrechen, falls `chestData` nicht verfügbar ist
            }

            // Überprüfe, ob einer der Blocke gesperrt ist, bevor der Block zurückgezogen wird
            for (Block block : event.getBlocks()) {
                if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                    // Wenn die Kiste gesperrt ist, verhindere das Zurückziehen
                    if (plugin.getChestData().isChestLocked(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}