package at.lowdfx.lowdfx.command.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandHelp {

    public static class HelpEntry {
        private final String command;
        private final Component usage;         // Kurzinfo (wird in der Übersicht angezeigt)
        private final Component detailed;      // Ausführliche Beschreibung für normale Spieler
        private final Component adminDetailed; // Zusätzliche Beschreibung für Admins (optional)
        private final String permission;       // Basis-Permission für diesen Command
        private final String adminPermission;  // Zusätzliche Permission (optional)

        public HelpEntry(String command, Component usage, Component detailed, Component adminDetailed, String permission, String adminPermission) {
            this.command = command.toLowerCase();
            this.usage = usage;
            this.detailed = detailed;
            this.adminDetailed = adminDetailed;
            this.permission = permission;
            this.adminPermission = adminPermission;
        }

        public String getCommand() {
            return command;
        }

        public Component getUsage() {
            return usage;
        }

        public Component getDetailed() {
            return detailed;
        }

        public Component getAdminDetailed() {
            return adminDetailed;
        }

        public String getPermission() {
            return permission;
        }

        public String getAdminPermission() {
            return adminPermission;
        }
    }

    // Hilfseinträge in einer LinkedHashMap (Reihenfolge bleibt erhalten)
    private static final Map<String, HelpEntry> helpEntries = new LinkedHashMap<>();

    /**
     * Registriert einen Help-Eintrag für einen Command.
     *
     * @param command         Der Name des Commands (ohne führenden Slash).
     * @param usage           Kurzinfo (wird in der Übersicht angezeigt).
     * @param detailed        Ausführliche Beschreibung für normale Spieler.
     * @param adminDetailed   Zusätzliche Beschreibung für Admins (optional, kann null sein).
     * @param permission      Die Basis-Permission, die benötigt wird, um den Command zu sehen.
     * @param adminPermission Die zusätzliche Admin-Permission (optional, kann null sein).
     */
    public static void register(String command, Component usage, Component detailed, Component adminDetailed, String permission, String adminPermission) {
        helpEntries.put(command.toLowerCase(), new HelpEntry(command, usage, detailed, adminDetailed, permission, adminPermission));
    }

    /**
     * Liefert den Help-Eintrag für den angegebenen Command.
     *
     * @param command Der Name des Commands.
     * @return Der registrierte Help-Eintrag oder null, falls nicht vorhanden.
     */
    public static HelpEntry getHelpEntry(String command) {
        return helpEntries.get(command.toLowerCase());
    }

    /**
     * Gibt alle registrierten Help-Einträge zurück.
     *
     * @return Eine Collection aller HelpEntry-Objekte.
     */
    public static Collection<HelpEntry> getHelpEntries() {
        return helpEntries.values();
    }

    /**
     * Baut eine Übersicht aller registrierten Commands auf,
     * zeigt jedoch nur jene an, für die der CommandSender die Basis-Permission besitzt.
     *
     * @param sender Der CommandSender, dessen Permissions geprüft werden.
     * @return Eine Component mit den zugänglichen Help-Einträgen.
     */
    public static Component getAllHelp(CommandSender sender) {
        TextComponent.Builder builder = Component.text();
        builder.append(MiniMessage.miniMessage().deserialize("<gold><bold>--- Befehlsübersicht ---</bold></gold>\n\n"));
        for (HelpEntry entry : helpEntries.values()) {
            if (sender.hasPermission(entry.getPermission())) {
                builder.append(MiniMessage.miniMessage().deserialize("<blue>» </blue><green>/" + entry.getCommand() + "</green> "))
                        .append(Component.text("- "))
                        .append(entry.getUsage())
                        .append(Component.newline());
            }
        }
        return builder.build();
    }
}
