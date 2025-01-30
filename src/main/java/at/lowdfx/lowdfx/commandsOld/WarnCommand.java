package at.lowdfx.lowdfx.commandsOld;

import at.lowdfx.lowdfx.LowdFX;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WarnCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.warn.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.warn";
    public final File warnFolder;

    public WarnCommand() {
        this.warnFolder = LowdFX.DATA_DIR.resolve("WarnSystem").toFile();
        if (warnFolder.mkdirs()) {
            LowdFX.LOG.info("Warn-Ordner erstellt.");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage(LowdFX.serverMessage(Component.text("Fehler! Benutze /warn help um Hilfe zu erhalten.", NamedTextColor.RED)));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("removeall")) {
            handleRemoveCommand(sender, args);
        } else if (subCommand.equals("remove")) {
            handleRemoveCommand(sender, args);
        }
        return true;
    }

    private void handleRemoveCommand(@NotNull CommandSender sender, String[] args) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            if (args.length < 2) {
                sender.sendMessage(LowdFX.serverMessage(Component.text("Benutze: /warn remove <Spieler> | /warn removeall <Spieler>", NamedTextColor.RED)));
                return;
            }

            boolean removeAll = args[0].equalsIgnoreCase("removeall");  // Prüfen, ob "removeall" eingegeben wurde
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            File playerFile = new File(warnFolder, target.getName() + ".yml");
            if (!playerFile.exists()) {
                sender.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler hat keine Verwarnungen.", NamedTextColor.RED)));
                return;
            }

            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                int warns = config.getInt("warns", 0);

                if (removeAll) {
                    if (warns == 0) {  // Überprüfe, ob der Spieler keine Verwarnungen hat
                        sender.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler hat keine Verwarnungen zum Entfernen.", NamedTextColor.RED)));
                        return;
                    }

                    // Alle Verwarnungen entfernen
                    config.set("reasons", null); // Löscht alle Gründe
                    config.set("warned_by", null); // Löscht alle Verwarnenden
                    config.set("warn_date", null); // Löscht alle Datumseinträge
                    config.set("warns", 0);  // Setzt die Gesamtzahl der Verwarnungen auf 0
                    config.save(playerFile);
                    sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Alle Verwarnungen von <b>" + playerName + "</b> wurden gelöscht.")));
                } else if (warns > 0) {
                    config.set("reasons." + warns, null);
                    config.set("warned_by." + warns, null);
                    config.set("warn_date." + warns, null);  // Datum und Uhrzeit entfernen
                    config.set("warns", warns - 1);
                    config.save(playerFile);
                    sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Letzte Verwarnung von <b>" + playerName + "</b> wurde entfernt.")));
                } else {  // Kein Fall für /warn remove, wenn keine Warnung da ist
                    sender.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler hat keine Verwarnungen zum Entfernen.", NamedTextColor.RED)));
                }

                // Spieler vom Namen-Bann entfernen
                Bukkit.getBanList(BanListType.PROFILE).pardon(target.getPlayerProfile());

                // Spieler IP aus den gespeicherten Warn-Daten auslesen und IP-Bann entfernen
                String playerIP = config.getString("ip"); // IP aus den gespeicherten Daten abrufen
                if (playerIP != null && !playerIP.isEmpty()) {
                    try {
                        Bukkit.getBanList(BanListType.IP).pardon(InetAddress.getByName(playerIP));
                    } catch (UnknownHostException ignored) {} // Kann ignoriert werden, weil das einfach heisst, dass der spieler sowieso nicht gebannt war.
                    sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Der IP-Bann für <b>" + playerName + "</b> wurde aufgehoben.")));
                }

            } catch (IOException e) {
                sender.sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Löschen der Verwarnungsdaten.", NamedTextColor.RED)));
            }
        }
    }
}
