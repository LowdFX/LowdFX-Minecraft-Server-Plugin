package at.lowdfx.lowdfx.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemStackSerializer {

    /**
     * Serialisiert einen ItemStack in einen String, der alle wichtigen Daten enthält:
     * Material, Durability, Amount, Enchantments (normale und gespeicherte), DisplayName und Lore.
     * Hier wird das Pipe-Zeichen ("|") als Trenner verwendet.
     */
    @SuppressWarnings("deprecation")
    public static String itemStackToString(ItemStack itemStack) {
        if (itemStack == null) return "null";
        StringBuilder sb = new StringBuilder();

        // Material: Verwende den Materialnamen
        sb.append("t@").append(itemStack.getType().name());

        // Damage (falls vorhanden, bei Damageable-Items)
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable) {
            int damage = ((Damageable) itemStack.getItemMeta()).getDamage();
            if (damage != 0) {
                sb.append("|d@").append(damage);
            }
        }

        // Amount (falls > 1)
        if (itemStack.getAmount() != 1) {
            sb.append("|a@").append(itemStack.getAmount());
        }

        // Normale Enchantments
        Map<Enchantment, Integer> enchants = itemStack.getEnchantments();
        if (enchants.isEmpty() && itemStack.hasItemMeta()) {
            enchants = itemStack.getItemMeta().getEnchants();
        }
        if (enchants != null && !enchants.isEmpty()){
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
                String keyStr = entry.getKey().getKey().toString(); // z. B. "minecraft:sharpness"
                sb.append("|e@").append(keyStr).append("@").append(entry.getValue());
            }
        }

        // Gespeicherte Enchantments für verzauberte Bücher
        if (itemStack.getType() == Material.ENCHANTED_BOOK
                && itemStack.hasItemMeta()
                && itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            Map<Enchantment, Integer> stored = bookMeta.getStoredEnchants();
            if (!stored.isEmpty()){
                for (Map.Entry<Enchantment, Integer> entry : stored.entrySet()){
                    String keyStr = entry.getKey().getKey().toString();
                    sb.append("|m@").append(keyStr).append("@").append(entry.getValue());
                }
            }
        }

        // DisplayName und Lore
        if (itemStack.hasItemMeta()){
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()){
                sb.append("|n@").append(meta.getDisplayName());
            }
            if (meta.hasLore()){
                String lore = meta.getLore().stream().reduce((l1, l2) -> l1 + ";" + l2).orElse("");
                sb.append("|l@").append(lore);
            }
        }
        return sb.toString();
    }

    /**
     * Deserialisiert einen String in einen ItemStack.
     * Hierbei werden zuerst alle Enchantments in temporären Maps gesammelt und
     * anschließend nach dem Setzen von Material, Meta, DisplayName und Lore explizit
     * auf den ItemStack angewendet.
     * Auch hier wird als Trenner das Pipe-Zeichen ("|") verwendet.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack stringToItemStack(String serializedItem) {
        if (serializedItem == null || serializedItem.equals("null")) return null;
        String[] parts = serializedItem.split("\\|");
        ItemStack itemStack = null;
        ItemMeta meta = null;
        // Temporäre Maps zum Sammeln der normalen und gespeicherten Enchantments
        Map<Enchantment, Integer> normalEnchants = new HashMap<>();
        Map<Enchantment, Integer> storedEnchants = new HashMap<>();

        for (String part : parts) {
            String[] attribute = part.split("@", 3);
            switch(attribute[0]) {
                case "t":
                    Material mat = Material.matchMaterial(attribute[1]);
                    if (mat == null) {
                        mat = Material.AIR;
                    }
                    itemStack = new ItemStack(mat, 1);
                    meta = itemStack.getItemMeta();
                    break;
                case "d":
                    if (itemStack != null && meta instanceof Damageable) {
                        ((Damageable) meta).setDamage(Integer.parseInt(attribute[1]));
                    }
                    break;
                case "a":
                    if (itemStack != null) {
                        itemStack.setAmount(Integer.parseInt(attribute[1]));
                    }
                    break;
                case "e":
                    if (itemStack != null) {
                        Enchantment ench = parseEnchantment(attribute[1]);
                        if (ench != null) {
                            normalEnchants.put(ench, Integer.parseInt(attribute[2]));
                        }
                    }
                    break;
                case "m":
                    if (itemStack != null && itemStack.getType() == Material.ENCHANTED_BOOK) {
                        Enchantment ench = parseEnchantment(attribute[1]);
                        if (ench != null) {
                            storedEnchants.put(ench, Integer.parseInt(attribute[2]));
                        }
                    }
                    break;
                case "n":
                    if (meta != null) {
                        meta.setDisplayName(attribute[1]);
                    }
                    break;
                case "l":
                    if (meta != null) {
                        String[] loreLines = attribute[1].split(";");
                        meta.setLore(Arrays.asList(loreLines));
                    }
                    break;
            }
        }
        if (itemStack != null && meta != null) {
            itemStack.setItemMeta(meta);
            // Wende normale Enchantments direkt auf den ItemStack an
            for (Map.Entry<Enchantment, Integer> entry : normalEnchants.entrySet()) {
                itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
            // Falls verzaubertes Buch: Wende gespeicherte Enchantments an
            if (itemStack.getType() == Material.ENCHANTED_BOOK && !storedEnchants.isEmpty()) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) (itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
                for (Map.Entry<Enchantment, Integer> entry : storedEnchants.entrySet()) {
                    bookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                itemStack.setItemMeta(bookMeta);
            }
        }
        return itemStack;
    }

    /**
     * Versucht, anhand eines Enchantment-Schlüssel-Strings den Enchantment zu ermitteln.
     * Nutzt NamespacedKey.fromString() und als Fallback Enchantment.getByName().
     */
    private static Enchantment parseEnchantment(String keyStr) {
        NamespacedKey key = NamespacedKey.fromString(keyStr);
        if (key == null) {
            // Fallback: entferne "minecraft:" falls vorhanden
            keyStr = keyStr.replace("minecraft:", "");
            key = NamespacedKey.minecraft(keyStr);
        }
        Enchantment ench = Enchantment.getByKey(key);
        if (ench == null) {
            ench = Enchantment.getByName(keyStr.toUpperCase());
        }
        return ench;
    }
}
