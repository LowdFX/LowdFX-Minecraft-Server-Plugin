package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.managers.BindManager;
import at.lowdfx.lowdfx.managers.BindManager.BindingData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class BindItemGiveListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        // Für jedes registrierte Binding prüfen
        for (String id : BindManager.getBindings().keySet()) {
            BindingData data = BindManager.getBinding(id);
            if (data == null) continue;
            // Nur wenn das Binding in der gleichen Welt erstellt wurde
            if (!world.getName().equalsIgnoreCase(data.world)) continue;
            boolean hasItem = false;
            for (ItemStack item : player.getInventory().getContents()) {
                String itemId = BindManager.getBindId(item);
                if (itemId != null && itemId.equals(id)) {
                    hasItem = true;
                    break;
                }
            }
            if (!hasItem) {
                player.getInventory().addItem(BindManager.createBindItem(id));
            }
        }
    }
}
