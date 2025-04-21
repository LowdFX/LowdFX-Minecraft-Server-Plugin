package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.command.util.CommandHelp;
import net.kyori.adventure.text.Component;

/**
 * API für externe Plugins, um Hilfe-Einträge im LowdFX Custom Help hinzuzufügen.
 */
public final class LowdFXAPI {
    private LowdFXAPI() {}

    /**
     * Registriert einen neuen Help-Eintrag im LowdFX-Hilfesystem.
     *
     * @param command         Kommando-Name ohne führenden Slash
     * @param usage           Kurzinfo (Übersicht)
     * @param detailed        Ausführliche Beschreibung für Spieler
     * @param adminDetailed   Admin-spezifische Zusatzinfo (kann null sein)
     * @param permission      Permission, die benötigt wird, um den Eintrag zu sehen
     * @param adminPermission Optional: zusätzliche Admin-Permission
     */
    public static void registerHelpEntry(
            String command,
            Component usage,
            Component detailed,
            Component adminDetailed,
            String permission,
            String adminPermission
    ) {
        CommandHelp.register(command, usage, detailed, adminDetailed, permission, adminPermission);
    }
}