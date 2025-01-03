package at.lowdfx.lowdfx.commands;

import at.lowdfx.lowdfx.lowdfx;
import com.google.gson.*;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class WarnCommand implements CommandExecutor {
    public static final String adminPermission = "lowdfx.warn.admin";
    public static final String playerPermission = "lowdfx.warn";
    public final File warnFolder;


    public WarnCommand(File dataFolder) {
        this.warnFolder = new File(dataFolder, "WarnSystem");
        if (!warnFolder.exists()) {
            warnFolder.mkdirs();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler! Benutze /warn help um Hilfe zu erhalten.");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
            case "info":
                handleInfoCommand(sender, args);
                break;
            case "removeall":  // hier den Befehl "removeall" hinzufügen
                handleRemoveCommand(sender, args);
                break;
            case "remove":
                handleRemoveCommand(sender, args);
                break;
            default:
                handleWarnCommand(sender, args);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        sender.sendMessage(title + ChatColor.BOLD + "------- Help: Warn -------");
        if (sender.hasPermission(playerPermission)) {
            sender.sendMessage(commandColor + "/warn info" + arrow + color + " Zeigt deine Verwarnungen an.");
        }
        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(commandColor + "/warn <Spieler> <Grund>" + arrow + color + " Verwarnt einen Spieler.");
            sender.sendMessage(commandColor + "/warn info <Spieler>" + arrow + color + " Zeigt die Verwarnungen eines anderen Spielers.");
            sender.sendMessage(commandColor + "/warn remove <Spieler>" + arrow + color + " Entfernt den letzten Verwarnpunkt eines Spielers.");
            sender.sendMessage(commandColor + "/warn removeall <Spieler>" + arrow + color + " Entfernt alle Verwarnungen eines Spielers.");
        }
    }

    private void handleWarnCommand(CommandSender sender, String[] args) {
        // Überprüfen, ob der Sender die erforderliche Berechtigung hat
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.");
            return;
        }

        // Überprüfen, ob ausreichend Argumente vorhanden sind
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler! Benutze /warn help um Hilfe zu erhalten.");
            return;
        }

        // Namen des Spielers und Grund für die Verwarnung extrahieren
        String playerName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Überprüfen, ob der angegebene Spieler existiert
        if (target == null) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden.");
            return;
        }

        try {
            File playerFile = new File(warnFolder, target.getName() + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);


                // Warns erhöhen und neuen Grund speichern
                int warns = config.getInt("warns", 0) + 1;
                String currentDate = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(new Date());
                config.set("warns", warns);
                config.set("reasons." + warns, reason);
                config.set("warned_by." + warns, sender.getName());
                config.set("warn_date." + warns, currentDate);  // Datum und Uhrzeit der Verwarnung speichern
                config.set("uuid", target.getUniqueId().toString());
            if (target.isOnline()) {
                config.set("ip", target.getPlayer().getAddress().getAddress().getHostAddress());
            } // Stattdessen die IP speichern, wenn online.
                config.save(playerFile);

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Spieler " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurde verwarnt.");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Aktuelle Punkte: " + ChatColor.RED + ChatColor.BOLD + warns);
            // Temporärer Bann bei 2 Verwarnungen
            if (warns == 2) {
                long banDuration = Bukkit.getPluginManager().getPlugin("lowdfx").getConfig().getLong("warnsystem.tempban-duration", 86400);
                banPlayer(target, buildBanMessage(config), banDuration);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Spieler " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurde für " + ChatColor.RED + banDuration / 3600 + " Stunden " + ChatColor.GREEN + "temporär gebannt.");
            }
            // Permanenter Bann ab 3 Verwarnungen
            else if (warns >= 3) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Spieler " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurde " + ChatColor.RED + "permanent" + ChatColor.GREEN +" gebannt.");
                banPlayer(target, buildBanMessage(config), -1); // Unbefristeter Bann
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler beim Speichern der Verwarnungsdaten.");
        }
    }


    private void handleInfoCommand(CommandSender sender, String[] args) {
        // Wenn der Befehl einen Spielernamen benötigt
        if (args.length > 1 ) {
            // Überprüfen, ob der Sender die Admin-Berechtigung hat
            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.");
                return;
            }

            // Hole den OfflinePlayer anhand des Spielernamens
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            // Spieler existiert nicht oder ist ungültig
            if (target == null) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden oder ungültige Nutzung des Befehls.");
                return;
            }

            // Der Spieler hat das entsprechende Berechtigung, nun die Warnung auslesen
            try {
                File playerFile = new File(warnFolder, target.getName() + ".yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                int warns = config.getInt("warns", 0);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Verwarnungen von " + ChatColor.BOLD + target.getName() + ChatColor.GREEN + ": " + ChatColor.BOLD + ChatColor.RED + warns);

                // Alle Verwarnungen auslesen und anzeigen
                for (int i = 1; i <= warns; i++) {
                    String warnDate = config.getString("warn_date." + i);  // Datum und Uhrzeit anzeigen
                    sender.sendMessage(ChatColor.GRAY + "➽ " + i + ". Grund: " + ChatColor.RED + config.getString("reasons." + i)
                            + ChatColor.GRAY + ", von: " + ChatColor.GOLD + config.getString("warned_by." + i)
                            + ChatColor.GRAY + ", am: " + ChatColor.WHITE + warnDate);  // Datum und Uhrzeit in der Ausgabe
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Keine Verwarnungsdaten gefunden.");
            }
        }
        // Wenn kein Spielername angegeben wird
        else {
            // Überprüfen, ob der Sender die Spieler-Berechtigung hat
            if (!sender.hasPermission(playerPermission)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.");
                return;
            }

            // Wenn der Sender der Spieler selbst ist, dann auf sich selbst zugreifen
            OfflinePlayer target = (OfflinePlayer) sender;

            // Hier kannst du ähnlich verfahren wie bei oben und seine Verwarnungen anzeigen
            try {
                File playerFile = new File(warnFolder, target.getName() + ".yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                int warns = config.getInt("warns", 0);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Deine Verwarnungen: " + ChatColor.RED + ChatColor.BOLD + warns);

                for (int i = 1; i <= warns; i++) {
                    String warnDate = config.getString("warn_date." + i);  // Datum und Uhrzeit anzeigen
                    sender.sendMessage(ChatColor.GRAY + "➽ " + i + ". Grund: " + ChatColor.RED + config.getString("reasons." + i)
                            + ChatColor.GRAY +", von: " + ChatColor.GOLD + config.getString("warned_by." + i)
                            + ChatColor.GRAY + ", am: " + ChatColor.WHITE + warnDate);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Keine Verwarnungsdaten gefunden.");
            }
        }
    }



    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (sender.hasPermission(adminPermission)) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Benutze: /warn remove <Spieler> | /warn removeall <Spieler>");
                return;
            }

            boolean removeAll = args[0].equalsIgnoreCase("removeall");  // Prüfen, ob "removeall" eingegeben wurde
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            File playerFile = new File(warnFolder, target.getName() + ".yml");
            if (!playerFile.exists()) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Spieler hat keine Verwarnungen.");
                return;
            }

            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                int warns = config.getInt("warns", 0);

                if (removeAll) {
                    if (warns == 0) {  // Überprüfe, ob der Spieler keine Verwarnungen hat
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Spieler hat keine Verwarnungen zum Entfernen.");
                        return;
                    }

                    // Alle Verwarnungen entfernen
                    config.set("reasons", null); // Löscht alle Gründe
                    config.set("warned_by", null); // Löscht alle Verwarnenden
                    config.set("warn_date", null); // Löscht alle Datumseinträge
                    config.set("warns", 0);  // Setzt die Gesamtzahl der Verwarnungen auf 0
                    config.save(playerFile);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Alle Verwarnungen von " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurden gelöscht.");
                } else if (warns > 0) {
                    config.set("reasons." + warns, null);
                    config.set("warned_by." + warns, null);
                    config.set("warn_date." + warns, null);  // Datum und Uhrzeit entfernen
                    config.set("warns", warns - 1);
                    config.save(playerFile);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Letzte Verwarnung von " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurde entfernt.");
                } else {  // Kein Fall für /warn remove, wenn keine Warnung da ist
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Spieler hat keine Verwarnungen zum Entfernen.");
                }

                // Spieler vom Namen-Bann entfernen
                Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());

                // Spieler IP aus den gespeicherten Warn-Daten auslesen und IP-Bann entfernen
                String playerIP = config.getString("ip"); // IP aus den gespeicherten Daten abrufen
                if (playerIP != null && !playerIP.isEmpty()) {
                    removeIPBan(playerIP, sender);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Der IP-Bann für " + ChatColor.BOLD + playerName + ChatColor.GREEN + " wurde aufgehoben.");
                }

            } catch (IOException e) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler beim Löschen der Verwarnungsdaten.");
            }
        }
    }



    private void removeIPBan(String playerIP, CommandSender sender) {
        // Versuchen, die IP aus der banned-ips.json zu entfernen
        try {
            File bannedIPsFile = new File("banned-ips.json");
            if (!bannedIPsFile.exists()) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Die Datei banned-ips.json wurde nicht gefunden.");
                return;
            }

            // Lade die JSON-Datei
            Gson gson = new Gson();
            JsonArray bannedIPs = gson.fromJson(new FileReader(bannedIPsFile), JsonArray.class);

            boolean ipFound = false;
            for (int i = 0; i < bannedIPs.size(); i++) {
                JsonObject entry = bannedIPs.get(i).getAsJsonObject();
                if (entry.get("ip").getAsString().equals(playerIP)) {
                    bannedIPs.remove(i);
                    ipFound = true;
                    break;
                }
            }

            if (ipFound) {
                // Speichere die Änderungen zurück in die Datei
                try (FileWriter writer = new FileWriter(bannedIPsFile)) {
                    gson.toJson(bannedIPs, writer);
                }
                //sender.sendMessage("[WarnSystem] Der IP-Ban für " + playerIP + " wurde entfernt.");
                Bukkit.getBanList(BanList.Type.IP).pardon(playerIP);  // Entferne IP aus Bukkit Ban-List
            }
        } catch (IOException e) {
            //sender.sendMessage("[WarnSystem] Fehler beim Bearbeiten der banned-ips.json: " + e.getMessage());
        }
    }







    private void banPlayer(OfflinePlayer target, String reason, long duration) {
        // Ban based on Name
        BanList banListName = Bukkit.getBanList(BanList.Type.NAME);
        banListName.addBan(target.getName(), reason, duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");

        // Ban based on UUID (by using the string representation of UUID)
       // BanList banListUUID = Bukkit.getBanList(BanList.Type.NAME); // Use NAME because we ban by name or UUID as string
        //banListUUID.addBan(target.getUniqueId().toString(), reason, duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");

        // Ban based on IP (if the player is online)
        if (target.isOnline()) {
            String playerIP = target.getPlayer().getAddress().getAddress().getHostAddress();
            BanList banListIP = Bukkit.getBanList(BanList.Type.IP);
            banListIP.addBan(playerIP, reason, duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");
        }

        // If the player is online, kick them
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(reason);
        }
    }



    private String buildBanMessage(YamlConfiguration config) {
        long banDuration = Bukkit.getPluginManager().getPlugin("lowdfx").getConfig().getLong("warnsystem.tempban-duration", 86400);
        // Berechne Stunden
        long hours = banDuration / 3600; // 1 Stunde = 3600 Sekunden
        StringBuilder banMessage = new StringBuilder(
                ChatColor.RED + "Bei " + ChatColor.BOLD + "2" + ChatColor.RED + " Verwarnungen hast du einen temporären Ban für " + hours + " Stunden!" + "\n" +
                ChatColor.RED + "Bei " + ChatColor.BOLD + "3" + ChatColor.RED + " Verwarnungen  hast du einen permanenten Ban!" + "\n" +
                ChatColor.DARK_RED + "------------------------------------------------------------------"+ "\n");
        int warns = config.getInt("warns", 0);

        for (int i = 1; i <= warns; i++) {
            String warnDate = config.getString("warn_date." + i); // Datum und Uhrzeit einfügen
            banMessage.append(ChatColor.GRAY).append("➽ ").append(i)
                    .append(". Grund: ").append(ChatColor.RED).append(config.getString("reasons." + i))
                    .append(ChatColor.GRAY).append(", von: ").append(ChatColor.GREEN).append(config.getString("warned_by." + i))
                    .append(ChatColor.GRAY).append(", am: ").append(ChatColor.WHITE).append(warnDate).append("\n").append("\n"); // Datum und Uhrzeit anfügen
        }
        return banMessage.toString();
    }
}
