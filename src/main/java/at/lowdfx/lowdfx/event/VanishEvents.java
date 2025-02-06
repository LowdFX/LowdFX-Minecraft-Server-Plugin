package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.moderation.VanishingHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

public class VanishEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        // Nur eine Nachricht wird hier gesendet, wenn der Spieler vanishing hat.
        if (VanishingHandler.getVanishedPlayers().contains(player.getUniqueId())) {
            VanishingHandler.makePlayerInvisible(player);

            // Sende deine eigene Nachricht (nachdem das Setzen von setJoinMessage null verhindert wurde)
            if (!player.hasMetadata("vanishedSent")) {
                // Sende Nachricht nur einmal!
                player.sendMessage(LowdFX.serverMessage(Component.text("Du bist vanished!", NamedTextColor.GREEN)));

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
            VanishingHandler.getVanishedPlayers().add(event.getPlayer().getUniqueId());
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
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !player.hasMetadata("vanished")) return;
        event.setCancelled(true);
        player.openInventory(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().hasMetadata("vanished")) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockData data = block.getBlockData();

        ItemStack item = event.getItem();
        if (item != null && event.getHand() != null) {
            if (data instanceof Waterlogged wl) {
                if (item.getType() == Material.BUCKET && wl.isWaterlogged()) {
                    wl.setWaterlogged(false);
                    event.getPlayer().getInventory().setItem(event.getHand(), new ItemStack(Material.WATER_BUCKET));
                    return;
                } else if (item.getType() == Material.WATER_BUCKET) {
                    wl.setWaterlogged(true);
                    event.getPlayer().getInventory().setItem(event.getHand(), new ItemStack(Material.BUCKET));
                    return;
                }
            }
        }

        if (data instanceof Openable openable) {
            openable.setOpen(!openable.isOpen());
        }
    }
}
