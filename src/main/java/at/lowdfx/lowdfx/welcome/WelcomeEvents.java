package at.lowdfx.lowdfx.welcome;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WelcomeEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        //Globale Join Nachricht
        event.setJoinMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.YELLOW + "" + lowdfx.config.getString("join.welcome") + " " + ChatColor.GOLD + ChatColor.BOLD + event.getPlayer().getName() + ChatColor.YELLOW + " !");
        //Privat Nachricht
        //event.getPlayer().sendMessage(ChatColor.YELLOW + "Guten Tag!");
    }
}
