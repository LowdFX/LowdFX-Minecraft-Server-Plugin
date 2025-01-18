package at.lowdfx.lowdfx.commands;

import at.lowdfx.lowdfx.Lowdfx;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class WarnCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.warn.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.warn";
    public final File warnFolder;

    public WarnCommand() {
        this.warnFolder = Lowdfx.DATA_DIR.resolve("WarnSystem").toFile();
        if (warnFolder.mkdirs()) {
            Lowdfx.LOG.info("Warn-Ordner erstellt.");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler! Benutze /warn help um Hilfe zu erhalten.", NamedTextColor.RED)));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info" -> handleInfoCommand(sender, args);
            case "removeall" ->  handleRemoveCommand(sender, args); // TODO: Remove All!
            case "remove" -> handleRemoveCommand(sender, args);
            default -> handleWarnCommand(sender, args);
        }
        return true;
    }

    private void handleWarnCommand(@NotNull CommandSender sender, String[] args) {
        // Überprüfen, ob der Sender die erforderliche Berechtigung hat
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.", NamedTextColor.RED)));
            return;
        }

        // Überprüfen, ob ausreichend Argumente vorhanden sind
        if (args.length < 2) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler! Benutze /warn help um Hilfe zu erhalten.", NamedTextColor.RED)));
            return;
        }

        // Namen des Spielers und Grund für die Verwarnung extrahieren
        String playerName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Überprüfen, ob der angegebene Spieler existiert
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden.", NamedTextColor.RED)));
            return;
        }

        try {
            File playerFile = new File(warnFolder, target.getName() + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            // Warns erhöhen und neuen Grund speichern
            int warns = config.getInt("warns", 0) + 1;
            String currentDate = LocalDateTime.now().format(Lowdfx.TIME_FORMAT);
            config.set("warns", warns);
            config.set("reasons." + warns, reason);
            config.set("warned_by." + warns, sender.getName());
            config.set("warn_date." + warns, currentDate); // Datum und Uhrzeit der Verwarnung speichern
            config.set("uuid", target.getUniqueId().toString());

            if (target.isOnline()) { // Stattdessen die IP speichern, wenn online.
                // noinspection DataFlowIssue
                config.set("ip", target.getPlayer().getAddress().getAddress().getHostAddress());
            }
            config.save(playerFile);

            sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + playerName + "</b> wurde verwarnt.")));
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Aktuelle Punkte: ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));
            // Temporärer Bann bei 2 Verwarnungen
            if (warns == 2) {
                long banDuration = Lowdfx.CONFIG.getLong("warnsystem.tempban-duration", 86400);
                banPlayer(target, buildBanMessage(config), banDuration);
                sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + playerName + "</b> wurde für <red>" + (banDuration / 3600) + " <green>Stunden temporär gebannt.")));
            }
            // Permanenter Bann ab 3 Verwarnungen
            else if (warns >= 3) {
                sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + playerName + "</b> wurde <red>permanent <green> gebannt.")));
                banPlayer(target, buildBanMessage(config), -1); // Unbefristeter Bann
            }
        } catch (IOException e) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler beim Speichern der Verwarnungsdaten.", NamedTextColor.RED)));
        }
    }

    private void handleInfoCommand(CommandSender sender, String @NotNull [] args) {
        // Wenn der Befehl einen Spielernamen benötigt
        if (args.length > 1 ) {
            // Überprüfen, ob der Sender die Admin-Berechtigung hat
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.", NamedTextColor.RED)));
                return;
            }

            // Hole den OfflinePlayer anhand des Spielernamens
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            // Spieler existiert nicht oder ist ungültig
            if (!target.hasPlayedBefore()) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden.", NamedTextColor.RED)));
                return;
            }

            // Der Spieler hat die entsprechende Berechtigung, nun die Warnung auslesen
            try {
                File playerFile = new File(warnFolder, target.getName() + ".yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                int warns = config.getInt("warns", 0);
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Verwarnungen von " + target.getName() + ": ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));

                // Alle Verwarnungen auslesen und anzeigen
                sendWarnInfo(sender, config, warns);
            } catch (Exception e) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Keine Verwarnungsdaten gefunden.", NamedTextColor.RED)));
            }
        }
        // Wenn kein Spielername angegeben wird
        else {
            // Überprüfen, ob der Sender die Spieler-Berechtigung hat
            if (!sender.hasPermission(PLAYER_PERMISSION)) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen.", NamedTextColor.RED)));
                return;
            }

            // Wenn der Sender der Spieler selbst ist, dann auf sich selbst zugreifen
            OfflinePlayer target = (OfflinePlayer) sender;

            // Hier kannst du ähnlich verfahren wie bei oben und seine Verwarnungen anzeigen
            try {
                File playerFile = new File(warnFolder, target.getName() + ".yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                int warns = config.getInt("warns", 0);
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Deine Verwarnungen: ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));

                sendWarnInfo(sender, config, warns);
            } catch (Exception e) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Keine Verwarnungsdaten gefunden.", NamedTextColor.RED)));
            }
        }
    }

    private void sendWarnInfo(CommandSender sender, YamlConfiguration config, int warns) {
        for (int i = 1; i <= warns; i++) {
            String warnDate = config.getString("warn_date." + i);  // Datum und Uhrzeit anzeigen
            sender.sendMessage(Component.text("➽ " + i + ". Grund: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("reasons." + i)), NamedTextColor.RED))
                    .append(Component.text(", von: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("warned_by." + i)), NamedTextColor.GOLD)))
                    .append(Component.text(", am: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(warnDate), NamedTextColor.WHITE))));
        }
    }

    private void handleRemoveCommand(@NotNull CommandSender sender, String[] args) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            if (args.length < 2) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Benutze: /warn remove <Spieler> | /warn removeall <Spieler>", NamedTextColor.RED)));
                return;
            }

            boolean removeAll = args[0].equalsIgnoreCase("removeall");  // Prüfen, ob "removeall" eingegeben wurde
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            File playerFile = new File(warnFolder, target.getName() + ".yml");
            if (!playerFile.exists()) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spieler hat keine Verwarnungen.", NamedTextColor.RED)));
                return;
            }

            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                int warns = config.getInt("warns", 0);

                if (removeAll) {
                    if (warns == 0) {  // Überprüfe, ob der Spieler keine Verwarnungen hat
                        sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spieler hat keine Verwarnungen zum Entfernen.", NamedTextColor.RED)));
                        return;
                    }

                    // Alle Verwarnungen entfernen
                    config.set("reasons", null); // Löscht alle Gründe
                    config.set("warned_by", null); // Löscht alle Verwarnenden
                    config.set("warn_date", null); // Löscht alle Datumseinträge
                    config.set("warns", 0);  // Setzt die Gesamtzahl der Verwarnungen auf 0
                    config.save(playerFile);
                    sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Alle Verwarnungen von <b>" + playerName + "</b> wurden gelöscht.")));
                } else if (warns > 0) {
                    config.set("reasons." + warns, null);
                    config.set("warned_by." + warns, null);
                    config.set("warn_date." + warns, null);  // Datum und Uhrzeit entfernen
                    config.set("warns", warns - 1);
                    config.save(playerFile);
                    sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Letzte Verwarnung von <b>" + playerName + "</b> wurde entfernt.")));
                } else {  // Kein Fall für /warn remove, wenn keine Warnung da ist
                    sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spieler hat keine Verwarnungen zum Entfernen.", NamedTextColor.RED)));
                }

                // Spieler vom Namen-Bann entfernen
                Bukkit.getBanList(BanListType.PROFILE).pardon(target.getPlayerProfile());

                // Spieler IP aus den gespeicherten Warn-Daten auslesen und IP-Bann entfernen
                String playerIP = config.getString("ip"); // IP aus den gespeicherten Daten abrufen
                if (playerIP != null && !playerIP.isEmpty()) {
                    try {
                        Bukkit.getBanList(BanListType.IP).pardon(InetAddress.getByName(playerIP));
                    } catch (UnknownHostException ignored) {} // Kann ignoriert werden, weil das einfach heisst, dass der spieler sowieso nicht gebannt war.
                    sender.sendMessage(Lowdfx.serverMessage(MiniMessage.miniMessage().deserialize("<green>Der IP-Bann für <b>" + playerName + "</b> wurde aufgehoben.")));
                }

            } catch (IOException e) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler beim Löschen der Verwarnungsdaten.", NamedTextColor.RED)));
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void banPlayer(@NotNull OfflinePlayer target, Component reason, long duration) {
        Bukkit.getBanList(BanListType.PROFILE).addBan(target.getPlayerProfile(), LegacyComponentSerializer.legacySection().serialize(reason), duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");

        // Ban based on IP (if the player is online)
        if (target.isOnline()) {
            Bukkit.getBanList(BanListType.IP).addBan(target.getPlayer().getAddress().getAddress(), LegacyComponentSerializer.legacySection().serialize(reason), duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");
            target.getPlayer().kick(reason);
        }
    }

    private @NotNull Component buildBanMessage(@NotNull YamlConfiguration config) {
        long banDuration = Lowdfx.CONFIG.getLong("warnsystem.tempban-duration", 86400);
        TextComponent.Builder banMessage = Component.text().append(MiniMessage.miniMessage().deserialize("""
                    <red>Bei <b>2</b> Verwarnungen hast du einen temporären Ban für <hours> Stunden!
                    Bei <b>3</b> Verwarnungen hast du einen permanenten Ban!
                    ------------------------------------------------------------------
                    """, Placeholder.unparsed("hours", String.valueOf(banDuration / 3600))));

        int warns = config.getInt("warns", 0);

        for (int i = 1; i <= warns; i++) {
            String warnDate = config.getString("warn_date." + i);
            banMessage.append(Component.text("➽ " + i + ". Grund: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("reasons." + i)), NamedTextColor.RED))
                    .append(Component.text(", von: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("warned_by." + i)), NamedTextColor.GOLD)))
                    .append(Component.text(", am: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(warnDate), NamedTextColor.WHITE)))
                    .appendNewline().appendNewline());
        }
        return banMessage.build();
    }
}
