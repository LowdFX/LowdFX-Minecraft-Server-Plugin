package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.command.*;
import at.lowdfx.lowdfx.event.*;
import at.lowdfx.lowdfx.inventory.LockableData;
import at.lowdfx.lowdfx.kit.KitManager;
import at.lowdfx.lowdfx.managers.*;
import at.lowdfx.lowdfx.moderation.VanishingHandler;
import at.lowdfx.lowdfx.util.Perms;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.xenondevs.invui.InvUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class LowdFX extends JavaPlugin {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    public static final List<Component> OP_LORE = List.of(Component.text("OP Kit", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    public static final List<Component> STARTER_LORE = List.of(Component.text("Starter Kit", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));

    public static Logger LOG;
    public static FileConfiguration CONFIG;
    public static LowdFX PLUGIN;
    public static Path DATA_DIR;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();
        PLUGIN = this;
        DATA_DIR = getDataPath();

        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            // Runtime exception, weil das sehr kritisch ist und der rest dann nicht funktioniert.
            throw new RuntimeException(e);
        }

        InvUI.getInstance().setPlugin(this);

        // === Permissions Klasse laden === //
        try {
            Perms.loadPermissions();
        } catch (IOException e) {
            LOG.warn("Konnte permissions nicht sichern/laden.");
        }

        // === Config Datei === //
        saveDefaultConfig();
        CONFIG = getConfig();

        ConnectionEvents.JOIN_MESSAGE = MiniMessage.miniMessage().deserialize(Objects.requireNonNullElse(CONFIG.getString("connection.join"), ""));
        ConnectionEvents.QUIT_MESSAGE = MiniMessage.miniMessage().deserialize(Objects.requireNonNullElse(CONFIG.getString("connection.quit"), ""));

        ChestShopManager.loadAllShops();
        SpawnManager.loadData();
        HomeManager.loadAll();
        WarpManager.loadData();
        LockableData.loadData();
        KitManager.loadAll();

        // === Events === //
        getServer().getPluginManager().registerEvents(new ConnectionEvents(), this);
        getServer().getPluginManager().registerEvents(new KitEvents(), this);
        getServer().getPluginManager().registerEvents(new ChestShopEvents(), this);
        getServer().getPluginManager().registerEvents(new LockEvents(), this);
        getServer().getPluginManager().registerEvents(new VanishEvents(), this);
        getServer().getPluginManager().registerEvents(new MuteEvents(), this);

        // === ChestData Datei === //
        ensureDataFolderExists();

        // === Vanished Spieler Datei === //
        VanishingHandler.loadAll();

        // === Playtime Datei === //
        PlaytimeManager.load();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
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
    }

    @Override
    public void onDisable() {
        CONFIG = getConfig();

        boolean vanish = CONFIG.getBoolean("basic.vanish", false);
        if (CONFIG.getBoolean("basic.vanish")) {
            VanishingHandler.saveAll();}
        LOG.info("Vanish war: {}", vanish ? "An" : "Aus");

        // Speichere von Sachen
        ChestShopManager.saveAllShops();
        PlaytimeManager.save();
        HomeManager.saveAll();
        WarpManager.saveData();
        SpawnManager.saveData();
        LockableData.saveData();
        KitManager.saveAll();

        saveConfig();
    }

    // Diese Methode stellt sicher, dass der Plugin-Ordner und die Datei existieren.
    private void ensureDataFolderExists() {
        if (DATA_DIR.toFile().mkdirs()) {
            LOG.info("Datenordner erstellt: {}", getDataFolder().getAbsolutePath());
        }

        // Überprüfen und sicherstellen, dass die Datei erstellt wird, falls sie nicht existiert.

    }

    public static @NotNull Component serverMessage(Component message) {
        return Component.text(Objects.requireNonNullElse(LowdFX.CONFIG.getString("basic.servername"), "???"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(message);
    }
}
