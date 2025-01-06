package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.commands.*;
import at.lowdfx.lowdfx.commands.ChestShop.*;
import at.lowdfx.lowdfx.commands.basicCommands.*;
import at.lowdfx.lowdfx.commands.basicCommands.TrashCommand;
import at.lowdfx.lowdfx.commands.basicCommands.inventoryCommands.*;
import at.lowdfx.lowdfx.commands.basicCommands.vanishCommand.*;
import at.lowdfx.lowdfx.commands.chestLock.*;
import at.lowdfx.lowdfx.commands.lowSubcommands.*;
import at.lowdfx.lowdfx.commands.tabCompleters.LowCommandHandling.*;
import at.lowdfx.lowdfx.commands.tabCompleters.basicTabCompleters.*;
import at.lowdfx.lowdfx.commands.tabCompleters.basicTabCompleters.inventoryTabCompleters.*;
import at.lowdfx.lowdfx.commands.tabCompleters.teleport.*;
import at.lowdfx.lowdfx.commands.tabCompleters.*;
import at.lowdfx.lowdfx.commands.teleport.*;
import at.lowdfx.lowdfx.commands.lowSubcommands.kits.*;
import at.lowdfx.lowdfx.commands.teleport.managers.*;
import at.lowdfx.lowdfx.items.opkit.*;
import at.lowdfx.lowdfx.quit.*;
import at.lowdfx.lowdfx.welcome.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;


public final class lowdfx extends JavaPlugin {

    public static FileConfiguration config;
    public static lowdfx plugin;
    private static HomeManager homeManager;
    private static WarpManager warpManager;
    private static SpawnManager spawnManager;
    private ChestData chestData;
    private InvisiblePlayerHandler invisibleHandler;
    private ChestShopManager shopManager;

    @Override
    public void onEnable() {
        plugin = this;
        //Permissions Klasse laden
        Permissions permissions = new Permissions(this);
        permissions.loadPermissions();
        /*Config Datei*/
        saveDefaultConfig();
        config = getConfig();

/*Events*/
        getServer().getPluginManager().registerEvents(new WelcomeEvents(), this);
        getServer().getPluginManager().registerEvents(new QuitEvents(), this);
        // OP Kit Rüstung - Schaden blocker
        getServer().getPluginManager().registerEvents(new ArmorProtectionListener(), this);
        // Home Events - Manager
        homeManager = new HomeManager(this);
        getServer().getPluginManager().registerEvents(homeManager, this);
        // Warp Events - Manager
        warpManager = new WarpManager(this);
        // Spawn Events - Manager
        spawnManager = new SpawnManager(this);
        getServer().getPluginManager().registerEvents(spawnManager, this);

        // ChestShop Manager
        File shopFolder = new File(getDataFolder(), "ChestShops");
        if (!shopFolder.exists() && shopFolder.mkdirs()) {
            getLogger().info("ChestShops-Ordner wurde erstellt.");
        }

        // Initialisiere den ShopManager
        shopManager = new ChestShopManager(shopFolder, plugin);

        // Lade alle Shops aus den Spielerdateien
        shopManager.loadAllShops();

        // Command registrieren
        ChestShopCommand chestShopCommand = new ChestShopCommand(shopManager);
        getCommand("shop").setExecutor(chestShopCommand);
        //getCommand("shop").setTabCompleter(chestShopCommand);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new ChestShopListener(shopManager), this);
        // ------------------------------------------------------------------------------------------------

/*ChestData Datei*/
        // Stelle sicher, dass das Datenverzeichnis existiert
        ensureDataFolderExists();
        chestData = new ChestData(this);
/*Warn Command DataFolder(Plugin Folder)*/

/*Commands*/
        // Command-Executor für /low mit haupt und subcommand
        this.getCommand("low").setExecutor(new Info());
        this.getCommand("low").setExecutor(new StarterKit());
        this.getCommand("low").setExecutor(new OPKit());
        // ------------------------------------------------------------------------------------------------
        // Command-Executor für normale Commands (z.B. /home) ohne Hauptcommand /low
        this.getCommand("home").setExecutor(new HomeCommand(this));
        this.getCommand("warp").setExecutor(new WarpCommand(this));
        this.getCommand("spawn").setExecutor(new SpawnCommand(this));
        this.getCommand("gm").setExecutor(new GamemodeCommand(this));
        this.getCommand("fly").setExecutor(new FlyCommand(this));
        //Vanish
        invisibleHandler = new InvisiblePlayerHandler(this);
        this.getCommand("vanish").setExecutor(new VanishCommand(this, invisibleHandler));
        this.getCommand("vanish").setTabCompleter(new VanishTabCompleter());
        getServer().getPluginManager().registerEvents(invisibleHandler, this);
        invisibleHandler.loadVanishedPlayers();// Registriere den TabCompleter

        this.getCommand("heal").setExecutor(new HealCommand(this));
        this.getCommand("feed").setExecutor(new FeedCommand(this));
        this.getCommand("trash").setExecutor(new TrashCommand(this));
        this.getCommand("invsee").setExecutor(new InvseeCommand(this));
        this.getCommand("endersee").setExecutor(new EnderseeCommand(this));
        this.getCommand("anvil").setExecutor(new AnvilCommand(this));
        this.getCommand("workbench").setExecutor(new WorkbenchCommand(this));
        this.getCommand("lock").setExecutor(new ChestLockCommand(this));
        this.getCommand("warn").setExecutor(new WarnCommand(getDataFolder()));

        // ------------------------------------------------------------------------------------------------
        getServer().getPluginManager().registerEvents(new ChestLockListener(this), this);
        //Command OPKIT ist für mehr goldene herzen und längere Behaltedauer der Herzen
        getServer().getPluginManager().registerEvents(new OPApple(), this);
        //InvseeCommand Listener Events
        getServer().getPluginManager().registerEvents(new InvseeCommand(this), this);

        // -----------------------------------------------------------------------------------------------
/*Tab Completer*/
        // Command Dispatcher - um /low Subcommands zu handlen
        this.getCommand("low").setExecutor(new LowCommandDispatcher());
        this.getCommand("low").setTabCompleter(new LowTabCompleter());
        this.getCommand("gm").setTabCompleter(new GamemodeTabCompleter());
        this.getCommand("fly").setTabCompleter(new FlyTabCompleter());
        this.getCommand("heal").setTabCompleter(new HealTabCompleter());
        this.getCommand("feed").setTabCompleter(new FeedTabCompleter());
        this.getCommand("trash").setTabCompleter(new TrashTabCompleter());
        this.getCommand("invsee").setTabCompleter(new InvseeTabCompleter());
        this.getCommand("endersee").setTabCompleter(new EnderseeTabCompleter());
        this.getCommand("anvil").setTabCompleter(new AnvilTabCompleter());
        this.getCommand("workbench").setTabCompleter(new WorkbenchTabCompleter());
        this.getCommand("home").setTabCompleter(new HomeTabCompleter());
        this.getCommand("warp").setTabCompleter(new WarpsTabCompleter());
        this.getCommand("spawn").setTabCompleter(new SpawnTabCompleter());
        this.getCommand("lock").setTabCompleter(new ChestLockTabCompleter());
        this.getCommand("warn").setTabCompleter(new WarnTabCompleter());






        // Plugin gestartet Logger
        getLogger().info("LowdFX Plugin gestartet!");
    }
    // Methode, um auf die ChestData-Instanz zuzugreifen
    public ChestData getChestData() {
        return chestData;
    }

    @Override
    public void onDisable() {
        config = getConfig();
        if (this.config != null) {
            boolean vanish = config.getBoolean("basic.vanish", false);
            if (config.getBoolean("basic.vanish")) {
                invisibleHandler.saveVanishedPlayers();}
            getLogger().info("Vanish war: " + vanish);
        } else {
            getLogger().warning("Config ist null während onDisable().");
        }

        // Speichere alle Shops in die Spielerdateien
        shopManager.saveAllShops();



        //------------------------------------------
        if (!this.getServer().isPrimaryThread()) {
            // Hier asynchrone Aufgaben starten oder Operationen durchführen, die nicht während dem Shutdown laufen
            if (homeManager != null) {
                homeManager.onDisable();
            }
            if (warpManager != null) {
                warpManager.onDisable();
            }
            if (spawnManager != null) {
                spawnManager.onDisable();
            }
            if (chestData != null) {
                chestData.save();
            }
        }
        saveConfig();
    }




    // Diese Methode stellt sicher, dass der Plugin-Ordner und die Datei existieren.
    private void ensureDataFolderExists() {
        if (!getDataFolder().exists()) {
            boolean created = getDataFolder().mkdirs();
            if (created) {
                getLogger().info("Datenordner erstellt: " + getDataFolder().getAbsolutePath());
            } else {
                getLogger().warning("Konnte den Datenordner nicht erstellen!");
            }
        }

        // Überprüfen und sicherstellen, dass die Datei erstellt wird, falls sie nicht existiert
        File dataFile = new File(getDataFolder(), "chestdata.yml");
        if (!dataFile.exists()) {
            try {
                boolean created = dataFile.createNewFile();
                if (created) {
                    getLogger().info("Daten-Datei erstellt: " + dataFile.getAbsolutePath());
                } else {
                    getLogger().warning("Die Datei 'chestdata.yml' konnte nicht erstellt werden.");
                }
            } catch (IOException e) {
                getLogger().severe("Fehler beim Erstellen der Datei 'chestdata.yml': " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public static File getDataFolde(){
        return plugin.getDataFolder();
    }

    public static FileConfiguration getConfiguration(){
        return config;
    }


    public InvisiblePlayerHandler getInvisibleHandler() {
        return invisibleHandler;
    }

    public ChestShopManager getShopManager() {
        return shopManager;
    }
}
