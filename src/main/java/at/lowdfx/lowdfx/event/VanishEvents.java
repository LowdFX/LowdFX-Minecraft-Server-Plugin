package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.moderation.VanishManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VanishEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Nur eine Nachricht wird hier gesendet, wenn der Spieler vanishing hat.
        if (VanishManager.getVanishedPlayers().contains(player.getUniqueId())) {
            event.joinMessage(null);

            VanishManager.makePlayerInvisible(player);

            // Sende deine eigene Nachricht (nachdem das Setzen von setJoinMessage null verhindert wurde)
            if (!player.hasMetadata("vanishedSent")) {
                // Sende Nachricht nur einmal!
                player.sendMessage(LowdFX.serverMessage(Component.text("Du bist vanished!", NamedTextColor.GREEN)));
                Utilities.positiveSound(player);

                // Markiere, dass die Nachricht für diesen Spieler gesendet wurde, um Duplikate zu vermeiden
                player.setMetadata("vanishedSent", new FixedMetadataValue(LowdFX.PLUGIN, true));
            }
        }

        // Verstecke vanished-Spieler vor dem beigetretenen Spieler.
        // Für alle online-spieler, falls jemand un-vanished, während der Spieler offline ist.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.hasMetadata("vanished")) {
                if (!Perms.check(player, Perms.Perm.VANISH)) {
                    player.hidePlayer(LowdFX.PLUGIN, target);
                } else {
                    player.showPlayer(LowdFX.PLUGIN, target);
                }
            } else {
                player.showPlayer(LowdFX.PLUGIN, target);
            }
        }

    }

    @EventHandler
    public void onSculkSensorRedstone(BlockRedstoneEvent event) {
        if (event.getBlock().getType() != Material.SCULK_SENSOR) return;

        for (Player player : event.getBlock().getWorld().getPlayers()) {
            if (player.hasMetadata("vanished") &&
                    player.getLocation().distance(event.getBlock().getLocation()) <= 8) {
                event.setNewCurrent(0); // Signal unterdrücken
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrivateMessage(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player sender = event.getPlayer();

        // Nur /msg, /tell, /w abfangen (kannst du beliebig erweitern)
        if (message.toLowerCase().startsWith("/msg ") ||
                message.toLowerCase().startsWith("/tell ") ||
                message.toLowerCase().startsWith("/w ")||
                message.toLowerCase().startsWith("/reply ")||
                message.toLowerCase().startsWith("/adminhelp")) {

            String[] args = message.split(" ");
            if (args.length < 2) return; // Kein Ziel angegeben

            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);

            if (target != null && target.hasMetadata("vanished")) {
                // Prüfen ob der Sender die VANISH-Permission hat (über dein Enum-System)
                if (!Perms.check(sender, Perms.Perm.VANISH)) {
                    sender.sendMessage(Component.text("Es wurde kein Spieler gefunden", NamedTextColor.RED));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();

        // Prüfe, ob der Spieler vanished ist
        if (joined.hasMetadata("vanished")) {
            // Sende Nachricht an alle Spieler mit VANISH-Permission
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (Perms.check(online, Perms.Perm.VANISH)) {
                    online.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler ")
                            .append(Component.text(joined.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" ist vanished gejoint!", NamedTextColor.GOLD))));
                }
            }
        }
    }


    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        if (event.getPlayer().hasMetadata("vanished")) {
            VanishManager.getVanishedPlayers().add(event.getPlayer().getUniqueId());
            event.quitMessage(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(@NotNull EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && player.hasMetadata("vanished"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && player.hasMetadata("vanished"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(@NotNull FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && player.hasMetadata("vanished"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && player.hasMetadata("vanished") && !player.isSneaking())
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPaperServerListPing(@NotNull PaperServerListPingEvent event) {
        int removed = Utilities.removeIf(event.getListedPlayers(), p -> VanishManager.getVanishedPlayers().contains(p.id()));
        event.setNumPlayers(event.getNumPlayers() - removed);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && player.hasMetadata("vanished"))
            tempSpectator(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !player.hasMetadata("vanished")) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockData data = block.getBlockData();
        Material type = block.getType();

        // Nur du siehst das Enderchest-Inventar
        if (type == Material.ENDER_CHEST) {
            GameMode oldGameMode = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);
            player.setVelocity(new Vector(0, 0, 0));

            // Öffne die Enderchest manuell im nächsten Tick
            Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> {
                player.openInventory(player.getEnderChest());
                player.setGameMode(oldGameMode);
                VanishManager.makePlayerInvisible(player);
            }, 1L);
            return;
        }



        ItemStack item = event.getItem();
        if (item != null && event.getHand() != null) {
            if (data instanceof Waterlogged wl) {
                if (item.getType() == Material.BUCKET && wl.isWaterlogged()) {
                    wl.setWaterlogged(false);
                    player.getInventory().setItem(event.getHand(), new ItemStack(Material.WATER_BUCKET));
                    return;
                } else if (item.getType() == Material.WATER_BUCKET) {
                    wl.setWaterlogged(true);
                    player.getInventory().setItem(event.getHand(), new ItemStack(Material.BUCKET));
                    return;
                }
            }
        }

        if (data instanceof Openable openable) {
            openable.setOpen(!openable.isOpen());
        }

        if (block.getState() instanceof Container)
            tempSpectator(player);
    }



    private void tempSpectator(@NotNull Player player) {
        GameMode before = player.getGameMode();

        player.setGameMode(GameMode.SPECTATOR);
        player.setVelocity(new Vector(0, 0, 0));

        Bukkit.getScheduler().runTask(LowdFX.PLUGIN, () -> {
            player.setGameMode(before);
            VanishManager.makePlayerInvisible(player);
        });
    }
}
