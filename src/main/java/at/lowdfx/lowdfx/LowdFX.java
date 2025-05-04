package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.command.*;
import at.lowdfx.lowdfx.command.block.ChestShopCommand;
import at.lowdfx.lowdfx.command.block.LockCommand;
import at.lowdfx.lowdfx.command.inventory.InventoryCommands;
import at.lowdfx.lowdfx.command.inventory.KitCommand;
import at.lowdfx.lowdfx.command.moderation.*;
import at.lowdfx.lowdfx.command.teleport.*;
import at.lowdfx.lowdfx.command.util.LowCommand;
import at.lowdfx.lowdfx.command.util.TimeCommands;
import at.lowdfx.lowdfx.command.util.UtilityCommands;
import at.lowdfx.lowdfx.event.*;
import at.lowdfx.lowdfx.managers.DeathMessageManager;
import at.lowdfx.lowdfx.managers.EmojiManager;
import at.lowdfx.lowdfx.managers.HologramManager;
import at.lowdfx.lowdfx.managers.ManagerManager;
import at.lowdfx.lowdfx.listeners.TeleportCancelOnDamageListener;
import at.lowdfx.lowdfx.managers.teleport.CooldownManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.FileUpdater;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.UpdaterJoinListener;
import com.marcpg.libpg.MinecraftLibPG;
import com.marcpg.libpg.util.ServerUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import at.lowdfx.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.xenondevs.invui.InvUI;
import at.lowdfx.lowdfx.listeners.*;

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
        // Standardkonfigurationen und Dateien werden gemerged
        FileUpdater.updateYaml(this, "config.yml");
        FileUpdater.updateJson(this, "permissions.json");
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
        CooldownManager.init();
        EmojiManager.init(this);
        DeathMessageManager deathMessageManager = new DeathMessageManager(this);

       // Plugin Updater
       String updateUrl = "https://raw.githubusercontent.com/LowdFX/LowdFX-Minecraft-Server-Plugin/refs/heads/master/update.txt";
       String downloadLink = "https://www.spigotmc.org/resources/lowdfx.123832/";
       getServer().getPluginManager().registerEvents(new UpdaterJoinListener(this, updateUrl, downloadLink), this);


        // bStats starten
        int pluginId = 25282;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> getConfig().getString("language")));

        getServer().getPluginManager().registerEvents(new TeleportCancelOnDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new ChatEmojiTabCompleteListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(deathMessageManager), this);
        getServer().getPluginManager().registerEvents(new BindItemGiveListener(), this);
        getServer().getPluginManager().registerEvents(new BindItemListener(), this);
        getServer().getPluginManager().registerEvents(new CommandSignListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandSignCreationListener(), this);


        ServerUtils.registerEvents(new ConnectionEvents(), new KitEvents(), new ChestShopEvents(), new LockEvents(), new VanishEvents(), new MuteEvents());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(ChestShopCommand.command(), "Erstelle oder verwalte einen Kisten-Shop.");
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
            registrar.register(MuteCommands.muteAllCommand(), "Schaltet alle Spieler stumm.");
            registrar.register(MuteCommands.unmuteAllCommand(), "Entfernt den Mute aller Spieler vom muteall Command.");
            registrar.register(PlaytimeCommand.command(), "Zeigt deine Spielzeit auf dem Server.");
            registrar.register(SpawnCommand.command(), "Teleportiert dich zum Spawn.", List.of("hub"));
            registrar.register(StatCommands.feedCommand(), "Füttert einen Spieler.", List.of("saturate"));
            registrar.register(StatCommands.healCommand(), "Heilt einen Spieler und löscht alle negativen effekte.", List.of("regen"));
            registrar.register(TimeCommands.dayCommand(), "Stellt die Zeit zu Tag.");
            registrar.register(TimeCommands.nightCommand(), "Stellt die Zeit zu Mitternacht.", List.of("midnight"));
            registrar.register(TpCommands.backCommand(), "Teleportiert dich zurück an deinen letzten Ort.");
            registrar.register(TpCommands.tpallCommand(), "Teleportiere alle Spieler zu dir.");
            registrar.register(TpCommands.tphereCommand(), "Teleportiere einen oder mehrere Spieler zu dir.");
            registrar.register(TpCommands.rtpCommand(), "Teleportiere dich an eine zufällige Stelle.");
            registrar.register(TpaCommand.command(), "Versendet eine TPA an einen Spieler.");
            registrar.register(UtilityCommands.flyCommand(), "Erlaubt einen Spieler zu fliegen.");
            registrar.register(UtilityCommands.gmCommand(), "Setzt den Spielmodus eines Spielers.");
            registrar.register(UtilityCommands.godCommand(), "Versetzt dich in den Godmode.");
            registrar.register(UtilityCommands.chatClearCommand(), "Lösche den Chat.");
            registrar.register(VanishCommand.command(), "Macht dich unsichtbar oder wieder sichtbar.", List.of("v"));
            registrar.register(WarnCommand.command(), "Ermahnt einen Spieler.");
            registrar.register(WarpCommand.command(), "Teleportiert dich zu einem Warp.");
            registrar.register(EmojiCommands.command(), "Teleportiert dich zu einem Warp.");
            registrar.register(TempbanCommand.command(), "Temporärer Bancommand.");
            registrar.register(AdminHelpCommand.command(), "Admin Help.");
            registrar.register(ScaleCommand.command(), "Macht dich klein.");
            registrar.register(HelpCommand.command(), "Custom Help Befehl.");
            registrar.register(DeathlogCommand.command(), "Deathlog Befehl.");
            registrar.register(ClearEntitysCommand.command(), "Lösche Entitys. (Monster/Items)");
            registrar.register(BindCommand.command(), "Binde einen Befehl auf ein Item.");
            registrar.register(SudoCommand.command(), "Führe einen Befehl als anderer Spieler aus.");

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
