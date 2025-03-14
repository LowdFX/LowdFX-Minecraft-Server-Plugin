package at.lowdfx.lowdfx.command.block;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.HologramManager;
import at.lowdfx.lowdfx.managers.block.ChestShopManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class ChestShopCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("chest-shop")
                .requires(source -> Perms.check(source, Perms.Perm.CHEST_SHOP) && source.getExecutor() instanceof Player)
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("price", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                    int price = context.getArgument("price", Integer.class);

                                    Block chest = player.getTargetBlockExact(5);
                                    if (chest == null || !(chest.getState() instanceof Lidded)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (ChestShopManager.isShop(chest.getLocation())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das ist schon ein Shop.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                                    if (heldItem.isEmpty()) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst ein Item für den Shop in der Hand haben.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    ChestShopManager.registerShop(player.getUniqueId(), chest, heldItem.clone(), price);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Shop wurde erstellt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block chest = player.getTargetBlockExact(5);
                            if (chest == null || !(chest.getState() instanceof Lidded)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                            if (shop == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            if (!shop.isOwner(player)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um einen Shop zu entfernen.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            ChestShopManager.removeShop(chest);
                            player.sendMessage(LowdFX.serverMessage(Component.text("Shop wurde entfernt!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("price")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("price", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                    int price = context.getArgument("price", Integer.class);

                                    Block chest = player.getTargetBlockExact(5);
                                    if (chest == null || !(chest.getState() instanceof Lidded)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                                    if (shop == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (shop.notAllowed(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst in der Whitelist des Shops sein, um ihn zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    shop.price().set(price);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Preis vom Shop wurde zu " + price + " Diamanten gesetzt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("item")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block chest = player.getTargetBlockExact(5);
                            if (chest == null || !(chest.getState() instanceof Lidded)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                            if (shop == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            ItemStack heldItem = player.getInventory().getItemInMainHand();
                            if (heldItem.isEmpty()) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst ein Item für den Shop in der Hand haben.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            if (shop.notAllowed(player)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst in der Whitelist des Shops sein, um ihn zu modifizieren.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            // noinspection deprecation
                            shop.item().setType(heldItem.getType());
                            shop.item().setItemMeta(heldItem.getItemMeta());
                            shop.item().setAmount(heldItem.getAmount());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Das Item vom Shop wurde zu ", NamedTextColor.GREEN).append(Component.translatable(heldItem.translationKey())).append(Component.text(" gesetzt!"))));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("whitelist")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                            Collection<PlayerProfile> players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());

                                            Block chest = player.getTargetBlockExact(5);
                                            if (chest == null || !(chest.getState() instanceof Lidded)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                                            if (shop == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            if (!shop.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um einen Shop zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            players.forEach(p -> shop.addWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) zur whitelist vom Shop hinzugefügt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(player);
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                            Collection<PlayerProfile> players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());

                                            Block chest = player.getTargetBlockExact(5);
                                            if (chest == null || !(chest.getState() instanceof Lidded)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                                            if (shop == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            if (!shop.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um einen Shop zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            players.forEach(p -> shop.removeWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) von der whitelist vom Shop entfernt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(player);
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("list")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;

                                    Block chest = player.getTargetBlockExact(5);
                                    if (chest == null || !(chest.getState() instanceof Lidded)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine Kiste oder Shulker-Kiste anschauen, um sie zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    ChestShopManager.Shop shop = ChestShopManager.getShop(chest.getLocation());
                                    if (shop == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das ist kein Shop.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (shop.notAllowed(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst berechtigt sein, um einen Shop zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (shop.whitelist().isEmpty()) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Es sind keine Spieler in der Whitelist von dem Shop.", NamedTextColor.YELLOW)));
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Spieler in der Whitelist von dem Shop:", NamedTextColor.GREEN)));
                                        shop.whitelist().forEach(p -> player.sendMessage(Component.text("- " + Bukkit.getOfflinePlayer(p).getName(), NamedTextColor.GREEN)));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("fix-holograms")
                        .requires(source -> Perms.check(source, Perms.Perm.CHEST_SHOP_ADMIN))
                        .executes(context -> {
                            HologramManager.fixAll();
                            context.getSource().getSender().sendMessage(Component.text("Alle Holograms wurden repariert!", NamedTextColor.GREEN));
                            return 1;
                        })
                )
                .build();
    }
}
