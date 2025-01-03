package at.lowdfx.lowdfx.quit;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvents implements Listener {
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        //Globale Leave Nachricht
        event.setQuitMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.YELLOW + "" + lowdfx.config.getString("join.quit") + " " + ChatColor.GOLD + ChatColor.BOLD + event.getPlayer().getName() + ChatColor.YELLOW + " !");
        //Privat Leave Nachricht
        //event.getPlayer().sendMessage(ChatColor.YELLOW + lowdfx.config.getString("welcome.quit"));
    }
}
