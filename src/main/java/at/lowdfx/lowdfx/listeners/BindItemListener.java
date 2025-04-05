package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.BindManager;
import at.lowdfx.lowdfx.managers.BindManager.BindingData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BindItemListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Nur in der Main-Hand verarbeiten
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;
        String id = BindManager.getBindId(item);
        if (id == null) return;
        BindingData data = BindManager.getBinding(id);
        if (data == null) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Dieses Bind-Item ist ungültig.", NamedTextColor.RED)));
            return;
        }
        String cmd = data.command;
        if (cmd == null || cmd.isEmpty()) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Kein Command gebunden.", NamedTextColor.RED)));
            return;
        }
        player.performCommand(cmd);
        event.setCancelled(true);
    }
}
