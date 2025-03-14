package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.moderation.VanishManager;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
                player.hidePlayer(LowdFX.PLUGIN, target);
            } else {
                player.showPlayer(LowdFX.PLUGIN, target);
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
