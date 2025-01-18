package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.commands.WarnCommand;
import at.lowdfx.lowdfx.commands.basic.FeedCommand;
import at.lowdfx.lowdfx.commands.basic.FlyCommand;
import at.lowdfx.lowdfx.commands.basic.GamemodeCommand;
import at.lowdfx.lowdfx.commands.basic.HealCommand;
import at.lowdfx.lowdfx.commands.basic.inventoryCommands.*;
import at.lowdfx.lowdfx.commands.basic.vanishCommand.InvisiblePlayerHandler;
import at.lowdfx.lowdfx.commands.basic.vanishCommand.VanishCommand;
import at.lowdfx.lowdfx.commands.chest.lock.ChestData;
import at.lowdfx.lowdfx.commands.chest.lock.ChestLockCommand;
import at.lowdfx.lowdfx.commands.chest.lock.ChestLockListener;
import at.lowdfx.lowdfx.commands.chest.shop.ChestShopCommand;
import at.lowdfx.lowdfx.commands.chest.shop.ChestShopListener;
import at.lowdfx.lowdfx.commands.chest.shop.ChestShopManager;
import at.lowdfx.lowdfx.commands.subcommands.Info;
import at.lowdfx.lowdfx.commands.subcommands.kits.OPKit;
import at.lowdfx.lowdfx.commands.subcommands.kits.StarterKit;
import at.lowdfx.lowdfx.commands.tab_completion.ChestLockTabCompleter;
import at.lowdfx.lowdfx.commands.tab_completion.LowCommandHandling.LowCommandDispatcher;
import at.lowdfx.lowdfx.commands.tab_completion.LowCommandHandling.LowTabCompleter;
import at.lowdfx.lowdfx.commands.tab_completion.WarnTabCompleter;
import at.lowdfx.lowdfx.commands.tab_completion.basicTabCompleters.*;
import at.lowdfx.lowdfx.commands.tab_completion.basicTabCompleters.inventoryTabCompleters.*;
import at.lowdfx.lowdfx.commands.tab_completion.teleport.HomeTabCompleter;
import at.lowdfx.lowdfx.commands.tab_completion.teleport.SpawnTabCompleter;
import at.lowdfx.lowdfx.commands.tab_completion.teleport.WarpsTabCompleter;
import at.lowdfx.lowdfx.commands.teleport.HomeCommand;
import at.lowdfx.lowdfx.commands.teleport.SpawnCommand;
import at.lowdfx.lowdfx.commands.teleport.WarpCommand;
import at.lowdfx.lowdfx.commands.teleport.managers.HomeManager;
import at.lowdfx.lowdfx.commands.teleport.managers.SpawnManager;
import at.lowdfx.lowdfx.commands.teleport.managers.WarpManager;
import at.lowdfx.lowdfx.items.opkit.ArmorProtectionListener;
import at.lowdfx.lowdfx.items.opkit.OPApple;
import at.lowdfx.lowdfx.quit.QuitEvents;
import at.lowdfx.lowdfx.welcome.WelcomeEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


public final class Lowdfx extends JavaPlugin {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    public static final List<Component> OP_LORE = List.of(Component.text("OP Kit", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    public static final List<Component> STARTER_LORE = List.of(Component.text("Starter Kit", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));

    public static Logger LOG;
    public static FileConfiguration CONFIG;
    public static Lowdfx PLUGIN;
    public static Path DATA_DIR;

    private static HomeManager homeManager;
    private static WarpManager warpManager;
    private static SpawnManager spawnManager;

    private ChestData chestData;
    private InvisiblePlayerHandler invisibleHandler;
    private ChestShopManager shopManager;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();
        PLUGIN = this;
        DATA_DIR = getDataPath();

        //Permissions Klasse laden
        Permissions permissions = new Permissions(this);
        permissions.loadPermissions();

        /*Config Datei*/
        saveDefaultConfig();
        CONFIG = getConfig();

        /*Events*/
        getServer().getPluginManager().registerEvents(new WelcomeEvents(), this);
        getServer().getPluginManager().registerEvents(new QuitEvents(), this);
        // OP Kit Rüstung - Schaden blocker
        getServer().getPluginManager().registerEvents(new ArmorProtectionListener(), this);
        // Home Events - Manager
        homeManager = new HomeManager();
        getServer().getPluginManager().registerEvents(homeManager, this);
        // Warp Events - Manager
        warpManager = new WarpManager();
        // Spawn Events - Manager
        spawnManager = new SpawnManager(this);
        getServer().getPluginManager().registerEvents(spawnManager, this);

        // ChestShop Manager
        File shopFolder = new File(getDataFolder(), "ChestShops");
        if (!shopFolder.exists() && shopFolder.mkdirs()) {
            LOG.info("ChestShops-Ordner wurde erstellt.");
        }

        // Initialisiere den ShopManager
        shopManager = new ChestShopManager(shopFolder);

        // Lade alle Shops aus den Spielerdateien
        shopManager.loadAllShops();

        // Command registrieren
        ChestShopCommand chestShopCommand = new ChestShopCommand(shopManager);
        Objects.requireNonNull(getCommand("shop")).setExecutor(chestShopCommand);
        //getCommand("shop").setTabCompleter(chestShopCommand);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new ChestShopListener(shopManager), this);
        // ------------------------------------------------------------------------------------------------

        /*ChestData Datei*/
        // Stelle sicher, dass das Datenverzeichnis existiert
        ensureDataFolderExists();
        chestData = new ChestData();
        /*Warn Command DataFolder(Plugin Folder)*/

        /*Commands*/
        // Command-Executor für /low mit haupt und subcommand
        Objects.requireNonNull(this.getCommand("low")).setExecutor(new Info());
        Objects.requireNonNull(this.getCommand("low")).setExecutor(new StarterKit());
        Objects.requireNonNull(this.getCommand("low")).setExecutor(new OPKit());
        // ------------------------------------------------------------------------------------------------
        // Command-Executor für normale Commands (z.B. /home) ohne Haupt-Command /low
        Objects.requireNonNull(this.getCommand("home")).setExecutor(new HomeCommand());
        Objects.requireNonNull(this.getCommand("warp")).setExecutor(new WarpCommand());
        Objects.requireNonNull(this.getCommand("spawn")).setExecutor(new SpawnCommand());
        Objects.requireNonNull(this.getCommand("gm")).setExecutor(new GamemodeCommand());
        Objects.requireNonNull(this.getCommand("fly")).setExecutor(new FlyCommand());
        //Vanish
        invisibleHandler = new InvisiblePlayerHandler(this);
        Objects.requireNonNull(this.getCommand("vanish")).setExecutor(new VanishCommand(invisibleHandler));
        Objects.requireNonNull(this.getCommand("vanish")).setTabCompleter(new VanishTabCompleter());
        getServer().getPluginManager().registerEvents(invisibleHandler, this);
        invisibleHandler.loadVanishedPlayers();// Registriere den TabCompleter

        Objects.requireNonNull(this.getCommand("heal")).setExecutor(new HealCommand());
        Objects.requireNonNull(this.getCommand("feed")).setExecutor(new FeedCommand());
        Objects.requireNonNull(this.getCommand("trash")).setExecutor(new TrashCommand());
        Objects.requireNonNull(this.getCommand("invsee")).setExecutor(new InvseeCommand());
        Objects.requireNonNull(this.getCommand("endersee")).setExecutor(new EnderseeCommand());
        Objects.requireNonNull(this.getCommand("anvil")).setExecutor(new AnvilCommand());
        Objects.requireNonNull(this.getCommand("workbench")).setExecutor(new WorkbenchCommand());
        Objects.requireNonNull(this.getCommand("lock")).setExecutor(new ChestLockCommand());
        Objects.requireNonNull(this.getCommand("warn")).setExecutor(new WarnCommand());

        // ------------------------------------------------------------------------------------------------
        getServer().getPluginManager().registerEvents(new ChestLockListener(), this);
        //Command OPKIT ist für mehr goldene herzen und längere Behalte-Dauer der Herzen
        getServer().getPluginManager().registerEvents(new OPApple(), this);
        //InvseeCommand Listener Events
        getServer().getPluginManager().registerEvents(new InvseeCommand(), this);

        // -----------------------------------------------------------------------------------------------
        // /*Tab Completer*/
        // Command Dispatcher - um /low Subcommands zu handlen
        Objects.requireNonNull(this.getCommand("low")).setExecutor(new LowCommandDispatcher());
        Objects.requireNonNull(this.getCommand("low")).setTabCompleter(new LowTabCompleter());
        Objects.requireNonNull(this.getCommand("gm")).setTabCompleter(new GamemodeTabCompleter());
        Objects.requireNonNull(this.getCommand("fly")).setTabCompleter(new FlyTabCompleter());
        Objects.requireNonNull(this.getCommand("heal")).setTabCompleter(new HealTabCompleter());
        Objects.requireNonNull(this.getCommand("feed")).setTabCompleter(new FeedTabCompleter());
        Objects.requireNonNull(this.getCommand("trash")).setTabCompleter(new TrashTabCompleter());
        Objects.requireNonNull(this.getCommand("invsee")).setTabCompleter(new InvseeTabCompleter());
        Objects.requireNonNull(this.getCommand("endersee")).setTabCompleter(new EnderseeTabCompleter());
        Objects.requireNonNull(this.getCommand("anvil")).setTabCompleter(new AnvilTabCompleter());
        Objects.requireNonNull(this.getCommand("workbench")).setTabCompleter(new WorkbenchTabCompleter());
        Objects.requireNonNull(this.getCommand("home")).setTabCompleter(new HomeTabCompleter());
        Objects.requireNonNull(this.getCommand("warp")).setTabCompleter(new WarpsTabCompleter());
        Objects.requireNonNull(this.getCommand("spawn")).setTabCompleter(new SpawnTabCompleter());
        Objects.requireNonNull(this.getCommand("lock")).setTabCompleter(new ChestLockTabCompleter());
        Objects.requireNonNull(this.getCommand("warn")).setTabCompleter(new WarnTabCompleter());



        // Plugin gestartet Logger
        LOG.info("LowdFX Plugin gestartet!");
    }

    // Methode, um auf die ChestData-Instanz zuzugreifen
    public ChestData getChestData() {
        return chestData;
    }

    @Override
    public void onDisable() {
        CONFIG = getConfig();

        boolean vanish = CONFIG.getBoolean("basic.vanish", false);
        if (CONFIG.getBoolean("basic.vanish")) {
            invisibleHandler.saveVanishedPlayers();}
        LOG.info("Vanish war: {}", vanish);

        // Speichere alle Shops in die Spielerdateien
        shopManager.saveAllShops();


        //------------------------------------------
        if (!this.getServer().isPrimaryThread()) {
            // Hier asynchrone Aufgaben starten oder Operationen durchführen, die nicht während dem Shutdown laufen
            if (homeManager != null) homeManager.onDisable();
            if (warpManager != null) warpManager.onDisable();
            if (spawnManager != null) spawnManager.onDisable();
            if (chestData != null) chestData.save();
        }
        saveConfig();
    }

    // Diese Methode stellt sicher, dass der Plugin-Ordner und die Datei existieren.
    private void ensureDataFolderExists() {
        if (!getDataFolder().exists()) {
            boolean created = getDataFolder().mkdirs();
            if (created) {
                LOG.info("Datenordner erstellt: {}", getDataFolder().getAbsolutePath());
            } else {
                LOG.warn("Konnte den Datenordner nicht erstellen!");
            }
        }

        // Überprüfen und sicherstellen, dass die Datei erstellt wird, falls sie nicht existiert
        File dataFile = new File(getDataFolder(), "chestdata.yml");
        if (!dataFile.exists()) {
            try {
                boolean created = dataFile.createNewFile();
                if (created) {
                    LOG.info("Daten-Datei erstellt: {}", dataFile.getAbsolutePath());
                } else {
                    LOG.warn("Die Datei 'chestdata.yml' konnte nicht erstellt werden.");
                }
            } catch (IOException e) {
                LOG.error("Fehler beim Erstellen der Datei 'chestdata.yml'.", e);
            }
        }

    }

    public InvisiblePlayerHandler getInvisibleHandler() {
        return invisibleHandler;
    }

    public ChestShopManager getShopManager() {
        return shopManager;
    }

    public static @NotNull Component serverMessage(Component message) {
        return Component.text(Objects.requireNonNullElse(Lowdfx.CONFIG.getString("basic.servername"), "???"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(message);
    }
}
