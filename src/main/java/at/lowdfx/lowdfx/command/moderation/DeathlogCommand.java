package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.moderation.DeathLogManager;
import at.lowdfx.lowdfx.model.DeathLogEntry;
import at.lowdfx.lowdfx.model.InventoryDTO;
import at.lowdfx.lowdfx.model.SimpleItemDTO;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DeathlogCommand {

    static {
        CommandHelp.register("deathlog",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/deathlog <player> <deathlogNumber>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du den Deathlog eines Spielers auslesen.<newline></gray>" +
                                "<yellow>· /deathlog <player> <deathlogNumber><newline></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.DEATHLOG.getPermission(),
                null); // Keine separate Admin-Permission
    }

    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        RequiredArgumentBuilder<CommandSourceStack, String> playerArg =
                RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            List<String> players = DeathLogManager.getInstance().getAllPlayers();
                            for (String p : players) {
                                builder.suggest(p);
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        });
        RequiredArgumentBuilder<CommandSourceStack, Integer> deathNumberArg =
                RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("deathlognumber", IntegerArgumentType.integer(1, 10))
                        .suggests((context, builder) -> {
                            int max = LowdFX.PLUGIN.getConfig().getInt("deathlog.maxEntriesPerPlayer", 3);
                            for (int i = 1; i <= max; i++) {
                                builder.suggest(i);
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            CommandSender sender = source.getSender();
                            String playerName = context.getArgument("player", String.class);
                            int deathNumber = context.getArgument("deathlognumber", Integer.class);

                            // Prüfe Permission
                            if (!Perms.check(sender, Perms.Perm.DEATHLOG)) {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine Berechtigung, diesen Befehl zu nutzen.")));
                                return 0;
                            }

                            DeathLogEntry entry = DeathLogManager.getInstance().getDeath(playerName, deathNumber);
                            if (entry == null) {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Für Spieler " + playerName + " wurde kein Deathlog " + deathNumber + " gefunden.")));
                                return 0;
                            }

                            String displayText = entry.getKiller().equals("Unknown")
                                    ? entry.getCause() + " (kein Killer)"
                                    : entry.getKiller();

                            LocalDateTime deathTime = LocalDateTime.parse(entry.getDeathTime());
                            String formattedDeathTime = deathTime.format(LowdFX.TIME_FORMAT);

                            Component message = Component.text("Spieler " + entry.getPlayer())
                                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                                    .append(Component.text(" wurde getötet durch: "))
                                    .append(Component.text(displayText).color(net.kyori.adventure.text.format.NamedTextColor.RED))
                                    .append(Component.text(" um "))
                                    .append(Component.text(formattedDeathTime).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
                            sender.sendMessage(LowdFX.serverMessage(message));

                            InventoryDTO dto = DeathLogManager.getInstance().deserializeInventory(entry.getInventory());

                            List<SimpleItemDTO> allMain = dto.getMainItems();
                            List<SimpleItemDTO> hotbar;
                            List<SimpleItemDTO> mainInventory;
                            if (allMain.size() >= 9) {
                                hotbar = new ArrayList<>(allMain.subList(allMain.size() - 9, allMain.size()));
                                mainInventory = new ArrayList<>(allMain.subList(0, allMain.size() - 9));
                            } else {
                                hotbar = new ArrayList<>(allMain);
                                mainInventory = new ArrayList<>();
                            }

                            Inventory gui = Bukkit.createInventory(null, 54, "Deathlog: " + entry.getPlayer());

                            // Zeile 0: Rüstung & Offhand
                            for (int i = 0; i < 9; i++) {
                                gui.setItem(i, createGlassPane());
                            }
                            List<SimpleItemDTO> armor = dto.getArmorItems();
                            for (int i = 0; i < armor.size() && i < 4; i++) {
                                gui.setItem(2 + i, createDisplayItem(armor.get(i)));
                            }
                            SimpleItemDTO offhand = dto.getOffhandItem();
                            if (offhand != null && offhand.getItemData() != null) {
                                gui.setItem(7, createDisplayItem(offhand));
                            }

                            // Zeile 1: Separator
                            for (int i = 9; i < 18; i++) {
                                gui.setItem(i, createGlassPane());
                            }

                            // Zeilen 2-4: Hauptinventar
                            int slot = 18;
                            for (SimpleItemDTO s : mainInventory) {
                                if (slot >= 45) break;
                                gui.setItem(slot, createDisplayItem(s));
                                slot++;
                            }

                            // Zeile 5: Hotbar
                            slot = 45;
                            for (SimpleItemDTO s : hotbar) {
                                if (slot >= 54) break;
                                gui.setItem(slot, createDisplayItem(s));
                                slot++;
                            }

                            if (sender instanceof Player) {
                                ((Player) sender).openInventory(gui);
                            }
                            return 1;
                        });

        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.<CommandSourceStack>literal("deathlog")
                .then(playerArg.then(deathNumberArg));

        return root.build();
    }

    // Erzeugt einen ItemStack aus dem SimpleItemDTO, indem er die gespeicherte Map des ItemStacks deserialisiert.
    private static ItemStack createDisplayItem(SimpleItemDTO dto) {
        if (dto.getItemData() == null) {
            return new ItemStack(Material.AIR);
        }
        return org.bukkit.inventory.ItemStack.deserialize(dto.getItemData());
    }

    private static ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }
}
