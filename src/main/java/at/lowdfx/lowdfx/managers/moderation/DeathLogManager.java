package at.lowdfx.lowdfx.managers.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.dto.DeathLogEntry;
import at.lowdfx.lowdfx.dto.InventoryDTO;
import at.lowdfx.lowdfx.dto.SimpleItemDTO;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.ItemStackSerializer;
import at.lowdfx.lowdfx.util.OptionalAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DeathLogManager {
    private static DeathLogManager instance;
    private final Connection connection;
    private final Gson gson;

    private DeathLogManager() {
        try {
            File dbFile = new File(LowdFX.PLUGIN.getDataFolder(), "deathlog.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS deathlog (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "player TEXT, " +
                            "killer TEXT, " +
                            "cause TEXT, " +
                            "weapon TEXT, " +
                            "deathTime TEXT, " +
                            "world TEXT, " +
                            "inventory TEXT" +
                            ")"
            );
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Konnte die Datenbank nicht initialisieren!");
        }
        gson = new GsonBuilder()
                .registerTypeAdapter(Optional.class, new OptionalAdapter())
                .create();
    }

    public static DeathLogManager getInstance() {
        if (instance == null) {
            instance = new DeathLogManager();
        }
        return instance;
    }

    public void saveDeath(PlayerDeathEvent event) {
        // Prüfe anhand der Config, ob global oder nur aus einer bestimmten Welt gespeichert werden soll.
        boolean global = Configuration.get().getBoolean("deathlog.global", false);
        String targetWorld = Configuration.get().getString("deathlog.world", "world");
        String eventWorld = event.getEntity().getWorld().getName();
        if (!global && !eventWorld.equalsIgnoreCase(targetWorld)) {
            return;
        }
        try {
            String playerName = event.getEntity().getName();
            String killerName = (event.getEntity().getKiller() != null)
                    ? event.getEntity().getKiller().getName()
                    : "Unbekannt";
            Component deathMsg = event.deathMessage();
            String cause = (deathMsg != null)
                    ? PlainTextComponentSerializer.plainText().serialize(deathMsg)
                    : "Unbekannt";

            String weapon = "";
            if (event.getEntity().getKiller() != null) {
                ItemStack item = event.getEntity().getKiller().getInventory().getItemInMainHand();
                weapon = item.getType().toString();
            }
            String deathTime = LocalDateTime.now().toString();
            String inventorySerialized = serializeInventory(event.getEntity().getInventory());
            String world = event.getEntity().getWorld().getName();

            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO deathlog (player, killer, cause, weapon, deathTime, world, inventory) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, playerName);
            ps.setString(2, killerName);
            ps.setString(3, cause);
            ps.setString(4, weapon);
            ps.setString(5, deathTime);
            ps.setString(6, world);
            ps.setString(7, inventorySerialized);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Serialisiert das Inventar, indem für jeden relevanten ItemStack dessen komplette Map (mit Meta-Daten)
     * erzeugt und über fixItemData angepasst wird.
     */
    private String serializeInventory(PlayerInventory inv) {
        List<SimpleItemDTO> mainItems = new ArrayList<>();
        ItemStack[] contents = inv.getContents();
        // Hauptinventar: Slots 9-35
        for (int slot = 9; slot <= 35; slot++) {
            if (slot < contents.length && contents[slot] != null) {
                String serialized = ItemStackSerializer.itemStackToString(contents[slot]);
                mainItems.add(new SimpleItemDTO(java.util.Map.of("data", serialized)));
            }
        }
        // Hotbar: Slots 0-8
        for (int slot = 0; slot <= 8; slot++) {
            if (slot < contents.length && contents[slot] != null) {
                String serialized = ItemStackSerializer.itemStackToString(contents[slot]);
                mainItems.add(new SimpleItemDTO(java.util.Map.of("data", serialized)));
            }
        }
        List<SimpleItemDTO> armorItems = new ArrayList<>();
        Arrays.stream(inv.getArmorContents())
                .filter(Objects::nonNull)
                .forEach(item -> armorItems.add(new SimpleItemDTO(java.util.Map.of("data", ItemStackSerializer.itemStackToString(item)))));
        SimpleItemDTO offhandDTO;
        if (inv.getItemInOffHand() != null) {
            offhandDTO = new SimpleItemDTO(java.util.Map.of("data", ItemStackSerializer.itemStackToString(inv.getItemInOffHand())));
        } else {
            offhandDTO = new SimpleItemDTO(null);
        }
        InventoryDTO dto = new InventoryDTO(mainItems, armorItems, offhandDTO);
        return gson.toJson(dto);
    }

    public InventoryDTO deserializeInventory(String json) {
        return gson.fromJson(json, InventoryDTO.class);
    }

    // Liefert alle Spielernamen aus der Datenbank (für Tabcompletion)
    public List<String> getAllPlayers() {
        List<String> players = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT player FROM deathlog");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                players.add(rs.getString("player"));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return players;
    }

    // Liefert den n-ten Tod (1 = aktuellster, 2 = vorletzter, ...) für einen Spieler.
    public DeathLogEntry getDeath(String playerName, int deathNumber) {
        int offset = deathNumber - 1;
        try {
            boolean global = Configuration.get().getBoolean("deathlog.global", false);
            String targetWorld = Configuration.get().getString("deathlog.world", "world");
            String query;
            if (global) {
                query = "SELECT * FROM deathlog WHERE player = ? ORDER BY id DESC LIMIT 1 OFFSET ?";
            } else {
                query = "SELECT * FROM deathlog WHERE player = ? AND world = ? ORDER BY id DESC LIMIT 1 OFFSET ?";
            }
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, playerName);
            if (!global) {
                ps.setString(2, targetWorld);
                ps.setInt(3, offset);
            } else {
                ps.setInt(2, offset);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DeathLogEntry entry = new DeathLogEntry(
                        rs.getString("player"),
                        rs.getString("killer"),
                        rs.getString("cause"),
                        rs.getString("weapon"),
                        rs.getString("deathTime"),
                        rs.getString("inventory")
                );
                rs.close();
                ps.close();
                return entry;
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
