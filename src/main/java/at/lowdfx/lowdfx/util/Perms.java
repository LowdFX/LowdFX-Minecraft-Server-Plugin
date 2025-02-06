package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bukkit.permissions.PermissionDefault.OP;
import static org.bukkit.permissions.PermissionDefault.TRUE;

public final class Perms {
    public static final Yaml YAML = new Yaml();

    // Can use the LowdFX.DATA_DIR, because Java loads classes at runtime, meaning this will only
    // be executed, once the Permissions class actually gets accessed for the first time.
    public static final File FILE = LowdFX.DATA_DIR.resolve("permissions.yml").toFile();

    public enum Perm {
        ANVIL(          "lowdfx.inv.anvil",         "/anvil",               OP),
        BACK(           "lowdfx.back",              "/back",                OP),
        ENDERSEE(       "lowdfx.inv.endersee",      "/endersee",            OP),
        FEED(           "lowdfx.feed",              "/feed",                OP),
        FLY(            "lowdfx.fly",               "/fly",                 OP),
        GAME_MODE(      "lowdfx.gm",                "/gm",                  OP),
        HEAL(           "lowdfx.heal",              "/heal",                OP),
        HOME(           "lowdfx.home",              "/home",                TRUE),
        HOME_ADMIN(     "lowdfx.home.admin",        "/home (andere)",       OP),
        INFO(           "lowdfx.low.info",          "/low info",            OP),
        INVSEE(         "lowdfx.inv.invsee",        "/invsee",              OP),
        LOCK(           "lowdfx.lock",              "/lock",                TRUE),
        LOCK_ADMIN(     "lowdfx.lock.admin",        "/lock",                OP),
        MUTE(           "lowdfx.mute",              "/mute & /unmute",      OP),
        OP_KIT(         "lowdfx.kit.op",            "/kit op",              OP),
        PLAYTIME(       "lowdfx.playtime",          "/playtime",            TRUE),
        PLAYTIME_ADMIN( "lowdfx.playtime.admin",    "/playtime (andere)",   OP),
        SPAWN(          "lowdfx.spawn",             "/spawn",               TRUE),
        SPAWN_ADMIN(    "lowdfx.spawn.admin",       "/spawn (andere)",      OP),
        STARTER_KIT(    "lowdfx.kit.starter",       "/kit starter",         TRUE),
        TIME(           "lowdfx.time",              "/day & /night",        OP),
        TPA(            "lowdfx.tpa",               "/tpa",                 TRUE),
        TPALL(          "lowdfx.tpall",             "/tpall",               OP),
        TPHERE(         "lowdfx.tphere",            "/tphere",              OP),
        TRASH(          "lowdfx.trash",             "/trash",               OP),
        VANISH(         "lowdfx.vanish",            "/vanish",              OP),
        WARN(           "lowdfx.warn",              "/warn (limitiert)",    TRUE),
        WARN_ADMIN(     "lowdfx.warn.admin",        "/warn",                OP),
        WARP(           "lowdfx.warp",              "/warp",                TRUE),
        WARP_ADMIN(     "lowdfx.warp.admin",        "/warp (andere)",       OP),
        WORKBENCH(      "lowdfx.inv.workbench",     "/workbench",           OP);

        private final String permission;
        private final String commands;
        private final PermissionDefault def;

        Perm(String permission, String commands, PermissionDefault def) {
            this.permission = permission;
            this.commands = commands;
            this.def = def;
        }
    }

    // Lädt die Berechtigungen aus der permissions.yml und registriert sie.
    public static void loadPermissions() throws IOException {
        if (FILE.createNewFile()) {
            Map<String, Object> data = new LinkedHashMap<>();
            for (Perm perm : Perm.values()) {
                Map<String, Object> permData = new LinkedHashMap<>();
                permData.put("description", "Erlaubt die Benutzung von " + perm.commands);
                permData.put("default", perm.def.name().toLowerCase());
                data.put(perm.permission, permData);
            }
            YAML.dump(data, new FileWriter(FILE));

            LowdFX.LOG.info("Permission-Konfiguration erstellt.");
            return;
        }

        PluginManager manager = Bukkit.getPluginManager();
        Map<String, Object> data = YAML.load(new FileReader(FILE));
        data.forEach((s, o) -> {
            if (!(o instanceof Map<?, ?> map)) return;
            manager.addPermission(new Permission(
                    (String) map.get("permission"),
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

    @SuppressWarnings("UnstableApiUsage")
    public static boolean check(@NotNull CommandContext<CommandSourceStack> context, @NotNull Perm perm) {
        return check(context.getSource(), perm);
    }
}
