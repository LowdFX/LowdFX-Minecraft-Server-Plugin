package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.command.PlaytimeCommand;
import at.lowdfx.lowdfx.command.StatCommands;
import at.lowdfx.lowdfx.command.block.ChestShopCommand;
import at.lowdfx.lowdfx.command.block.LockCommand;
import at.lowdfx.lowdfx.command.inventory.InventoryCommands;
import at.lowdfx.lowdfx.command.inventory.KitCommand;
import at.lowdfx.lowdfx.command.moderation.MuteCommands;
import at.lowdfx.lowdfx.command.moderation.VanishCommand;
import at.lowdfx.lowdfx.command.moderation.WarnCommand;
import at.lowdfx.lowdfx.command.teleport.*;
import at.lowdfx.lowdfx.command.util.LowCommand;
import at.lowdfx.lowdfx.command.util.TimeCommands;
import at.lowdfx.lowdfx.command.util.UtilityCommands;
import at.lowdfx.lowdfx.event.*;
import at.lowdfx.lowdfx.managers.HologramManager;
import at.lowdfx.lowdfx.managers.ManagerManager;
import at.lowdfx.lowdfx.managers.teleport.TeleportCancelOnDamageListener;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import com.marcpg.libpg.MinecraftLibPG;
import com.marcpg.libpg.util.ServerUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.xenondevs.invui.InvUI;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings({ "UnstableApiUsage", "ResultOfMethodCallIgnored" })
public final class LowdFX extends JavaPlugin {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

    public static Logger LOG;
    public static LowdFX PLUGIN;
    public static Path PLUGIN_DIR;
    public static Path DATA_DIR;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();
        PLUGIN = this;
        PLUGIN_DIR = getDataPath();

        DATA_DIR = PLUGIN_DIR.resolve("data");
        DATA_DIR.toFile().mkdirs();

        InvUI.getInstance().setPlugin(this);
        MinecraftLibPG.init(this);
        Configuration.init(this);

        Perms.loadPermissions();
        ManagerManager.load();
        HologramManager.load();

        getServer().getPluginManager().registerEvents(new TeleportCancelOnDamageListener(), this);

        ServerUtils.registerEvents(new ConnectionEvents(), new KitEvents(), new ChestShopEvents(), new LockEvents(), new VanishEvents(), new MuteEvents());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(ChestShopCommand.command(), "Erstelle oder verwalte einen Kisten-Shop.", List.of("shop"));
            registrar.register(HomeCommand.command(), "Teleportiert dich zu deinem Home.");
            registrar.register(InventoryCommands.anvilCommand(), "Öffnet einen Amboss.", List.of("amboss"));
            registrar.register(InventoryCommands.enderseeCommand(), "Öffnet die Enderchest von einem Spieler.");
            registrar.register(InventoryCommands.invseeCommand(), "Öffnet das Inventar von einem Spieler.");
            registrar.register(InventoryCommands.trashCommand(), "Öffnet einen Mülleimer.", List.of("rubbish", "mülleimer"));
            registrar.register(InventoryCommands.workbenchCommand(), "Öffnet eine Werkbank.", List.of("crafting", "crafting-table"));
            registrar.register(KitCommand.command(), "Gibt dir eines deiner Kits.");
            registrar.register(LockCommand.command(), "Sperrt einen Block.");
            registrar.register(LowCommand.command(), "Generelle features vom Plugin.");
            registrar.register(MuteCommands.muteCommand(), "Schaltet einen Spieler stumm.");
            registrar.register(MuteCommands.unmuteCommand(), "Entfernt den Mute eines Spielers.");
            registrar.register(PlaytimeCommand.command(), "Zeigt deine Spielzeit auf dem Server.");
            registrar.register(SpawnCommand.command(), "Teleportiert dich zum Spawn.");
            registrar.register(StatCommands.feedCommand(), "Füttert einen Spieler.", List.of("saturate"));
            registrar.register(StatCommands.healCommand(), "Heilt einen Spieler und löscht alle negativen effekte.", List.of("regen"));
            registrar.register(TimeCommands.dayCommand(), "Stellt die Zeit zu Tag.");
            registrar.register(TimeCommands.nightCommand(), "Stellt die Zeit zu Mitternacht.", List.of("midnight"));
            registrar.register(TpCommands.backCommand(), "Teleportiert dich zurück an deinen letzten Ort.");
            registrar.register(TpCommands.tpallCommand(), "Teleportiere alle Spieler zu dir.");
            registrar.register(TpCommands.tphereCommand(), "Teleportiere einen oder mehrere Spieler zu dir.");
            registrar.register(TpaCommand.command(), "Versendet eine TPA an einen Spieler.");
            registrar.register(UtilityCommands.flyCommand(), "Erlaubt einen Spieler zu fliegen.");
            registrar.register(UtilityCommands.gmCommand(), "Setzt den Spielmodus eines Spielers.");
            registrar.register(VanishCommand.command(), "Macht dich unsichtbar oder wieder sichtbar.");
            registrar.register(WarnCommand.command(), "Ermahnt einen Spieler.");
            registrar.register(WarpCommand.command(), "Teleportiert dich zu einem Warp.");
        });

        LOG.info("LowdFX Plugin gestartet!");

        // Save all data every 3 minutes.
        Bukkit.getScheduler().runTaskTimer(this, ManagerManager::save, 3600, 3600);
    }

    @Override
    public void onDisable() {
        ManagerManager.save();
    }

    public static @NotNull Component serverMessage(@NotNull Component message) {
        return Component.text(Configuration.BASIC_SERVER_NAME, NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(message.decoration(TextDecoration.BOLD, false));
    }
}
