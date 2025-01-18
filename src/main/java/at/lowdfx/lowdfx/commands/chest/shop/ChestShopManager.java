package at.lowdfx.lowdfx.commands.chest.shop;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestShopManager {
    private final File shopFolder;
    private final Map<UUID, Map<Location, ShopData>> playerShops = new HashMap<>();
    private final Map<Location, List<ArmorStand>> holograms = new HashMap<>();
    private final Map<Location, Set<UUID>> whitelistedPlayers = new HashMap<>();

    public ChestShopManager(File shopFolder) {
        this.shopFolder = shopFolder;
    }

    public void registerShop(UUID owner, Location location, ShopData shopData) {
        playerShops.computeIfAbsent(owner, k -> new HashMap<>()).put(location, shopData);
        whitelistedPlayers.put(location, new HashSet<>());// Initialize empty whitelist
        // Check for connected chests and register them as part of the shop
        Map<Location, ShopData> ownerShops = playerShops.computeIfAbsent(owner, k -> new HashMap<>());
        ownerShops.put(location, shopData);

        Set<Location> connectedChests = getConnectedChests(location);
        for (Location adjacentLocation : connectedChests) {
            if (!ownerShops.containsKey(adjacentLocation)) {
                ownerShops.put(adjacentLocation, shopData);
            }
        }

        saveShop(owner);
        //spawnHologram(location, shopData);
        updateHologramForDoubleChest(owner, location);
    }

    public void removeShop(Location location) {
        location = normalizeLocation(location); // Normierte Location verwenden
        for (UUID owner : playerShops.keySet()) {
            Map<Location, ShopData> shops = playerShops.get(owner);

            if (shops.remove(location) != null) {
                Set<Location> connectedChests = getConnectedChests(location);
                connectedChests.add(location); // Aktuelle Kiste hinzufügen

                // Entferne Hologramme aller verbundenen Kisten
                connectedChests.forEach(this::removeHologram);

                // Falls eine Kiste übrig bleibt, aktualisiere deren Hologramm
                connectedChests.remove(location);
                if (!connectedChests.isEmpty()) {
                    Location remainingChest = connectedChests.iterator().next();
                    ShopData remainingShop = shops.get(remainingChest);
                    if (remainingShop != null) {
                        updateHologramForSingleChest(remainingChest, remainingShop);
                    }
                }

                saveShop(owner);
                break;
            }
        }
    }

    private void updateHologramForSingleChest(Location location, ShopData shopData) {
        removeHologram(location); // Altes Hologramm entfernen
        spawnHologram(location.clone().add(0.5, 1.3, 0.5), shopData); // Neues Hologramm leicht angepasst
    }

    public boolean isShop(Location location) {
        return playerShops.values().stream().anyMatch(shops -> shops.containsKey(location));
    }

    public Optional<ShopData> getShop(Location location) {
        return playerShops.values().stream()
                .map(shops -> shops.get(location))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public void updateHologramForDoubleChest(UUID owner, Location location) {
        location = normalizeLocation(location);
        Set<Location> connectedChests = getConnectedChests(location);
        connectedChests.add(location);

        double centerX = 0, centerY = 0, centerZ = 0;
        for (Location loc : connectedChests) {
            loc = normalizeLocation(loc); // Normiere jede Location
            centerX += loc.getX();
            centerY += loc.getY();
            centerZ += loc.getZ();
        }

        centerX /= connectedChests.size();
        centerY /= connectedChests.size();
        centerZ /= connectedChests.size();

        Location centerLocation = new Location(location.getWorld(), centerX, centerY, centerZ);
        connectedChests.forEach(this::removeHologram);

        ShopData shopData = playerShops.getOrDefault(owner, Collections.emptyMap()).get(location);
        if (shopData != null) {
            spawnHologram(centerLocation.add(0.5, 1.5, 0.5), shopData);
        }
    }




    public boolean isOwner(UUID playerUUID, Location location) {
        ShopData shop = getShop(location).orElse(null);
        return shop != null && shop.owner().equals(playerUUID);
    }

    public void whitelistPlayer(Location location, UUID playerUUID) {
        whitelistedPlayers.computeIfAbsent(location, k -> new HashSet<>()).add(playerUUID);
    }

    public boolean isWhitelisted(UUID playerUUID, Location location) {
        return whitelistedPlayers.getOrDefault(location, Collections.emptySet()).contains(playerUUID);
    }

    public void saveShop(@NotNull UUID owner) {
        File playerFile = new File(shopFolder, owner + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        Map<Location, ShopData> shops = playerShops.getOrDefault(owner, Collections.emptyMap());
        for (Map.Entry<Location, ShopData> entry : shops.entrySet()) {
            String locKey = locationToString(entry.getKey());
            ShopData shop = entry.getValue();

            config.set("shops." + locKey + ".item", shop.item());
            config.set("shops." + locKey + ".price", shop.price());

            Set<UUID> whitelist = whitelistedPlayers.getOrDefault(entry.getKey(), new HashSet<>());
            config.set("shops." + locKey + ".whitelist", new ArrayList<>(whitelist));
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            Lowdfx.LOG.error("Konnte chest shop nicht speichern.", e);
        }
    }

    public void loadAllShops() {
        File[] playerFiles = shopFolder.listFiles();
        if (playerFiles == null) return;

        for (File playerFile : playerFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            UUID owner = UUID.fromString(playerFile.getName().replace(".yml", ""));
            Map<Location, ShopData> shops = new HashMap<>();

            if (config.contains("shops")) {
                for (String locKey : Objects.requireNonNull(config.getConfigurationSection("shops")).getKeys(false)) {
                    Location location = parseLocation(locKey);
                    if (location == null) continue;

                    ItemStack item = config.getItemStack("shops." + locKey + ".item");
                    int price = config.getInt("shops." + locKey + ".price");

                    ShopData shopData = new ShopData(owner, location, item, price);
                    shops.put(location, shopData);

                    List<String> whitelist = config.getStringList("shops." + locKey + ".whitelist");
                    whitelistedPlayers.put(location, new HashSet<>());
                    whitelist.forEach(uuid -> whitelistedPlayers.get(location).add(UUID.fromString(uuid)));

                    spawnHologram(location, shopData);
                }
            }

            playerShops.put(owner, shops);
        }
    }

    public void saveAllShops() {
        for (UUID owner : playerShops.keySet()) {
            saveShop(owner);
        }
    }

    private void spawnHologram(Location location, @NotNull ShopData shopData) {
        location = normalizeLocation(location); // Normierte Location verwenden
        removeHologram(location); // Alte Hologramme entfernen

        List<ArmorStand> hologramLines = new ArrayList<>();
        Location holoLocation = location.clone().add(0.5, 1.3, 0.5); // Zentrierung sicherstellen

        hologramLines.add(spawnArmorStand(holoLocation, "§e" + shopData.item().getType() + " x " + shopData.item().getAmount()));
        hologramLines.add(spawnArmorStand(holoLocation.subtract(0, 0.25, 0), "§aStock: " + getStock(location, shopData)));
        hologramLines.add(spawnArmorStand(holoLocation.subtract(0, 0.25, 0), "§bPrice: " + shopData.price() + " Diamonds"));

        holograms.put(location, hologramLines);
        Lowdfx.LOG.info("[Debug] Hologram created at: {} -> {} ArmorStands added.", location, hologramLines.size());
    }

    private @NotNull Location normalizeLocation(@NotNull Location location) {
        return new Location(
                location.getWorld(),
                location.getBlockX(), // Nur Block-Koordinaten verwenden
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private void removeHologram(Location location) {
        location = normalizeLocation(location); // Normierte Location verwenden

        List<ArmorStand> stands = holograms.remove(location);
        if (stands == null || stands.isEmpty()) {
            Lowdfx.LOG.info("[Debug] No holograms found at: {}", location);
            return;
        }

        for (ArmorStand stand : stands) {
            if (stand != null && !stand.isDead() && stand.isValid()) {
                stand.remove();
                Lowdfx.LOG.info("[Debug] Removed hologram at: {}", location);
            }
        }
    }

    private @NotNull ArmorStand spawnArmorStand(@NotNull Location location, String text) {
        return location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setCustomNameVisible(true);
            stand.customName(LegacyComponentSerializer.legacySection().deserialize(text));
        });
    }

    void startHologramUpdater(Location location, ShopData shopData) {
        Bukkit.getScheduler().runTaskTimer(Lowdfx.PLUGIN, r -> {
            if (!holograms.containsKey(location)) {
                r.cancel();
                return;
            }

            List<ArmorStand> stands = holograms.get(location);
            if (stands.size() >= 2) {
                stands.get(1).customName(Component.text("Bestand: " + getStock(location, shopData)));
            }
        }, 20, 100);
    }

    private int getStock(@NotNull Location location, ShopData shopData) {
        Block block = location.getBlock();
        Inventory inventory = null;

        if (block.getState() instanceof Chest chest) {
            inventory = chest.getInventory();
        } else if (block.getState() instanceof ShulkerBox shulkerBox) {
            inventory = shulkerBox.getInventory();
        }

        if (inventory == null) return 0;

        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.isSimilar(shopData.item()))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private @NotNull String locationToString(@NotNull Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private @Nullable Location parseLocation(@NotNull String locString) {
        String[] parts = locString.split(",");
        if (parts.length != 4) return null;

        String worldName = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private @NotNull Set<Location> getConnectedChests(@NotNull Location location) {
        Set<Location> connectedChests = new HashSet<>();
        Block block = location.getBlock();

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getType() == Material.CHEST) {
                connectedChests.add(normalizeLocation(relativeBlock.getLocation()));
            }
        }

        Lowdfx.LOG.info("[Debug] Connected chests for {}: {}", normalizeLocation(location), connectedChests.size());
        return connectedChests;
    }
}
