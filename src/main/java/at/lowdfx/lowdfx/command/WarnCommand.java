package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class WarnCommand {
    public static final String WARN_PERMISSION = "lowdfx.warn";
    public static final String WARN_ADMIN_PERMISSION = "lowdfx.warn.admin";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("warn")
                .requires(source -> source.getSender().hasPermission(WARN_PERMISSION))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                        .requires(source -> source.getSender().hasPermission(WARN_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                    String reason = context.getArgument("reason", String.class);

                                    for (PlayerProfile t : targets) {
                                        try {
                                            File file = LowdFX.DATA_DIR.resolve("WarnSystem").resolve(Objects.requireNonNullElse(t.getName(), "unknown")).toFile();
                                            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                                            // Warns erhöhen und neuen Grund speichern
                                            int warns = config.getInt("warns", 0) + 1;
                                            config.set("warns", warns);
                                            config.set("reasons." + warns, reason);
                                            config.set("warned_by." + warns, sender.getName());
                                            config.set("warn_date." + warns, LocalDateTime.now().format(LowdFX.TIME_FORMAT));
                                            config.set("uuid", Objects.requireNonNull(t.getId()).toString());

                                            config.save(file);

                                            sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + t.getName() + "</b> wurde verwarnt.")));
                                            sender.sendMessage(LowdFX.serverMessage(Component.text("Aktuelle Punkte: ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));

                                            if (warns == 2) {
                                                long banDuration = LowdFX.CONFIG.getLong("warnsystem.tempban-duration", 86400);
                                                ban(t, banMessage(config), banDuration);
                                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + t.getName() + "</b> wurde für <red>" + (banDuration / 3600) + " <green>Stunden temporär gebannt.")));
                                            }
                                            else if (warns >= 3) {
                                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + t.getName() + "</b> wurde <red>permanent <green> gebannt.")));
                                                ban(t, banMessage(config), -1);
                                            }
                                        } catch (Exception e) {
                                            sender.sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Modifizieren der Verwarnungsdaten.", NamedTextColor.RED)));
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                                return 1;
                            }

                            try {
                                File file = LowdFX.DATA_DIR.resolve("WarnSystem").resolve(player.getName()).toFile();
                                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                                int warns = config.getInt("warns", 0);
                                player.sendMessage(LowdFX.serverMessage(Component.text("Deine Verwarnungen: ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));
                                sendWarnInfo(player, config, warns);
                            } catch (Exception e) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Laden der Verwarnungsdaten.", NamedTextColor.RED)));
                            }

                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .executes(context -> {
                                    Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                    try {
                                        File file = LowdFX.DATA_DIR.resolve("WarnSystem").resolve(player.getName()).toFile();
                                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                                        int warns = config.getInt("warns", 0);
                                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Verwarnungen von " + player.getName() + ": ", NamedTextColor.GREEN).append(Component.text(warns, NamedTextColor.RED, TextDecoration.BOLD))));
                                        sendWarnInfo(context.getSource().getSender(), config, warns);
                                    } catch (Exception e) {
                                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Laden der Verwarnungsdaten.", NamedTextColor.RED)));
                                    }

                                    return 1;
                                })
                        )
                )
                .build();
    }

    private static void sendWarnInfo(CommandSender sender, YamlConfiguration config, int warns) {
        for (int i = 1; i <= warns; i++) {
            String warnDate = config.getString("warn_date." + i);  // Datum und Uhrzeit anzeigen
            sender.sendMessage(Component.text("➽ " + i + ". Grund: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("reasons." + i)), NamedTextColor.RED))
                    .append(Component.text(", von: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("warned_by." + i)), NamedTextColor.GOLD)))
                    .append(Component.text(", am: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(warnDate), NamedTextColor.WHITE))));
        }
    }

    public static void ban(PlayerProfile target, Component reason, long duration) {
        Bukkit.getBanList(BanListType.PROFILE).addBan(target, LegacyComponentSerializer.legacySection().serialize(reason), duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");

        Player t = Bukkit.getPlayer(Objects.requireNonNull(target.getId()));
        if (t != null) {
            Bukkit.getBanList(BanListType.IP).addBan(Objects.requireNonNull(t.getAddress()).getAddress(), LegacyComponentSerializer.legacySection().serialize(reason), duration > 0 ? new java.util.Date(System.currentTimeMillis() + duration * 1000) : null, "WarnSystem");
            t.kick(reason);
        }
    }

    public static @NotNull Component banMessage(@NotNull YamlConfiguration config) {
        long banDuration = LowdFX.CONFIG.getLong("warnsystem.tempban-duration", 86400);
        TextComponent.Builder banMessage = Component.text().append(MiniMessage.miniMessage().deserialize("""
                    <red>Bei <b>2</b> Verwarnungen hast du einen temporären Ban für <hours> Stunden!
                    Bei <b>3</b> Verwarnungen hast du einen permanenten Ban!
                    ------------------------------------------------------------------""",
                Placeholder.unparsed("hours", String.valueOf(banDuration / 3600))));

        int warns = config.getInt("warns", 0);

        for (int i = 1; i <= warns; i++) {
            String warnDate = config.getString("warn_date." + i);
            banMessage.append(Component.text("➽ " + i + ". Grund: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("reasons." + i)), NamedTextColor.RED))
                    .append(Component.text(", von: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(config.getString("warned_by." + i)), NamedTextColor.GOLD)))
                    .append(Component.text(", am: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNull(warnDate), NamedTextColor.WHITE)))
                    .appendNewline().appendNewline());
        }
        return banMessage.build();
    }
}
