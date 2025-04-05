package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import com.marcpg.libpg.storage.JsonUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bukkit.permissions.PermissionDefault.OP;
import static org.bukkit.permissions.PermissionDefault.TRUE;

public final class Perms {
    public enum Perm {
        ANVIL(              "lowdfx.inv.anvil",         "/anvil",                               OP),
        BACK(               "lowdfx.back",              "/back",                                OP),
        CHEST_SHOP(         "lowdfx.chestshop",         "/shop",                                TRUE),
        CHEST_SHOP_ADMIN(   "lowdfx.chestshop.admin",   "/shop",                                OP),
        ENDERSEE(           "lowdfx.inv.endersee",      "/endersee",                            OP),
        FEED(               "lowdfx.feed",              "/feed",                                OP),
        FLY(                "lowdfx.fly",               "/fly",                                 OP),
        GAME_MODE(          "lowdfx.gm",                "/gm",                                  OP),
        HEAL(               "lowdfx.heal",              "/heal",                                OP),
        HOME(               "lowdfx.home",              "/home",                                TRUE),
        HOME_ADMIN(         "lowdfx.home.admin",        "/home tp_other <player> & mehr",       OP),
        INFO(               "lowdfx.low.info",          "/low info",                            OP),
        INVSEE(             "lowdfx.inv.invsee",        "/invsee",                              OP),
        LOCK(               "lowdfx.lock",              "/lock",                                TRUE),
        LOCK_ADMIN(         "lowdfx.lock.admin",        "/lock",                                OP),
        MUTE(               "lowdfx.mute",              "/mute & /unmute",                      OP),
        OP_KIT(             "lowdfx.kit.op",            "/kit op",                              OP),
        KIT_ADMIN(          "lowdfx.kit.admin",         "/kit <kit> <player>",                  OP),
        PLAYTIME(           "lowdfx.playtime",          "/playtime",                            TRUE),
        PLAYTIME_ADMIN(     "lowdfx.playtime.admin",    "/playtime <player>",                   OP),
        SPAWN(              "lowdfx.spawn",             "/spawn",                               TRUE),
        SPAWN_ADMIN(        "lowdfx.spawn.admin",       "/spawn (andere)",                      OP),
        STARTER_KIT(        "lowdfx.kit.starter",       "/kit starter",                         TRUE),
        TIME(               "lowdfx.time",              "/day & /night",                        OP),
        TPA(                "lowdfx.tpa",               "/tpa",                                 TRUE),
        TPALL(              "lowdfx.tpall",             "/tpall",                               OP),
        TPHERE(             "lowdfx.tphere",            "/tphere",                              OP),
        TRASH(              "lowdfx.trash",             "/trash",                               OP),
        VANISH(             "lowdfx.vanish",            "/vanish",                              OP),
        WARN(               "lowdfx.warn",              "/warn (andere)",                       TRUE),
        WARN_ADMIN(         "lowdfx.warn.admin",        "/warn",                                OP),
        WARP(               "lowdfx.warp",              "/warp",                                TRUE),
        WARP_ADMIN(         "lowdfx.warp.admin",        "/warp (andere)",                       OP),
        WORKBENCH(          "lowdfx.inv.workbench",     "/workbench",                           OP),
        CHAT_CLEAR(         "lowdfx.chat.clear",        "/chat clear",                          OP),
        RELOAD(             "lowdfx.reload",            "/low reload",                          OP),
        GOD(                "lowdfx.god",               "/god <player>",                        OP),
        RTP(                "lowdfx.rtp",               "/rtp",                                 TRUE),
        TP_BYPASS(          "lowdfx.tp.bypass",         "Teleport Delay Bypass",                OP),
        BACK_PREMIUM(       "lowdfx.back.premium",      "Back Premium Cooldown",                OP),
        EMOJIS(             "lowdfx.emojis",            "Chat Emojis",                          TRUE),
        BAN(                "lowdfx.ban",               "/tempban <player> <time> <reason>",    OP),
        ADMINHELP_RECEIVE(  "lowdfx.adminhelp.receive", "Adminhelp Nachricht empfangen",        OP),
        ADMINHELP_SEND(     "lowdfx.adminhelp.send",    "Adminhelp Nachricht senden",           TRUE),
        SCALE(              "lowdfx.scale",             "Verkleinere/vergrößere dich.",         OP),
        DEATHLOG(           "lowdfx.deathlog",          "Nutze den Deathlog Befehl.",           OP),
        CLEARMONSTERS(      "lowdfx.clear.monsters",    "Lösche alle feindlichen Kreaturen.",   OP),
        CLEARITEMS(         "lowdfx.clear.items",       "Lösche alle Items vom Boden.",         OP),
        BIND(               "lowdfx.bind",              "Binde einen Befehl auf ein Item.",     OP),
        SUDO(               "lowdfx.sudo",              "Führe einen Befehl als anderer aus.",  OP),
        COMMANDSIGN(        "lowdfx.commandsign",       "Erstelle ein Commandsign.",  OP);

        private final String permission;
        private final String commands;
        private final PermissionDefault def;

        Perm(String permission, String commands, PermissionDefault def) {
            this.permission = permission;
            this.commands = commands;
            this.def = def;
        }

        public String getPermission() {
            return permission;
        }
    }


    // Lädt die Berechtigungen aus der permissions.json und registriert sie.
    public static void loadPermissions() {
        try {
            if (LowdFX.PLUGIN_DIR.resolve("permissions.json").toFile().createNewFile()) {
                Map<String, Object> data = new LinkedHashMap<>();
                for (Perm perm : Perm.values()) {
                    Map<String, Object> permData = new LinkedHashMap<>();
                    permData.put("description", "Erlaubt die Benutzung von " + perm.commands);
                    permData.put("default", perm.def.name().toLowerCase());
                    data.put(perm.permission, permData);
                }
                JsonUtils.saveMapSafe(data, LowdFX.PLUGIN_DIR.resolve("permissions.json").toFile());
                LowdFX.LOG.info("Permission-Konfiguration erstellt.");
            }
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte Permission-Datei nicht erstellen.");
        }

        PluginManager manager = Bukkit.getPluginManager();
        JsonUtils.loadMapSafe(LowdFX.PLUGIN_DIR.resolve("permissions.json").toFile(), Map.of()).forEach((s, o) -> {
            if (!(o instanceof Map<?, ?> map)) return;
            manager.addPermission(new Permission(s,
                    (String) map.get("description"),
                    PermissionDefault.valueOf(((String) map.get("default")).toUpperCase())));
        });
    }

    public static boolean check(@NotNull Permissible source, @NotNull Perm perm) {
        return source.hasPermission(perm.permission);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean check(@NotNull CommandSourceStack source, @NotNull Perm perm) {
        return check(source.getSender(), perm);
    }
}
