package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.kit.KitManager;
import at.lowdfx.lowdfx.kit.op.*;
import at.lowdfx.lowdfx.kit.starter.*;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public final class KitCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("kit")
                .requires(source -> source.getExecutor() instanceof Player)
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("starter")
                        .requires(source -> Perms.check(source, Perms.Perm.STARTER_KIT))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            if (player.getPersistentDataContainer().has(KitManager.STARTERKIT_KEY)) {
                                player.sendMessage(Component.text("Du hast bereits das starter kit bekommen!", NamedTextColor.RED));
                                return 1;
                            }

                            player.getInventory().addItem( // Gibt alle items.
                                    StarterStoneSword.get(), StarterStonePickaxe.get(), StarterStoneShovel.get(), StarterStoneAxe.get(),
                                    StarterLeatherHelmet.get(), StarterLeatherChestplate.get(), StarterLeatherLeggings.get(), StarterLeatherBoots.get(),
                                    StarterFood.get());
                            player.getPersistentDataContainer().set(KitManager.STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);

                            player.sendMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.RED));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("op")
                        .requires(source -> Perms.check(source, Perms.Perm.OP_KIT))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            player.getInventory().addItem( // Gibt alle items.
                                    OPNetheriteSword.get(), OPNetheritePickaxe.get(), OPNetheriteShovel.get(), OPNetheriteAxe.get(),
                                    OPNetheriteHelmet.get(), OPNetheriteChestplate.get(), OPNetheriteLeggings.get(), OPNetheriteBoots.get(),
                                    OPFood.get(), OPApple.get(), OPStick.get());

                            player.sendMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.RED));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("edit")
                        .executes(context -> {
                            if (context.getSource().getExecutor() instanceof Player player)
                                KitManager.showConfig(player);
                            return 1;
                        })
                )
                .build();
    }
}
