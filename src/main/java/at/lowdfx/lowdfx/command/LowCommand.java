package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public final class LowCommand {
    public static final String STARTERKIT_PERMISSION = "lowdfx.low.starterkit";
    public static final String OPKIT_PERMISSION = "lowdfx.low.opkit";
    public static final String INFO_PERMISSION = "lowdfx.low.info";

    public static final NamespacedKey STARTERKIT_KEY = new NamespacedKey("lowdfx", "got-starterkit");

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("low")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("starterkit")
                        .requires(source -> source.getSender().hasPermission(STARTERKIT_PERMISSION) && source.getExecutor() instanceof Player)
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            if (player.getPersistentDataContainer().has(STARTERKIT_KEY)) {
                                player.sendMessage(Component.text("You already got the starter kit!", NamedTextColor.RED));
                                return 1;
                            }

                            player.getInventory().addItem( // Gibt alle items.
                                    StarterStoneSword.get(), StarterStonePickaxe.get(), StarterStoneShovel.get(), StarterStoneAxe.get(),
                                    StarterLeatherHelmet.get(), StarterLeatherChestplate.get(), StarterLeatherLeggings.get(), StarterLeatherBoots.get(),
                                    StarterFood.get());
                            player.getPersistentDataContainer().set(STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);

                            player.sendMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.RED));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("opkit")
                        .requires(source -> source.getSender().hasPermission(OPKIT_PERMISSION) && source.getExecutor() instanceof Player)
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            player.getInventory().addItem( // Gibt alle items.
                                    OPNetheriteSword.get(), OPNetheritePickaxe.get(), OPNetheriteShovel.get(), OPNetheriteAxe.get(),
                                    OPNetheriteHelmet.get(), OPNetheriteChestplate.get(), OPNetheriteLeggings.get(), OPNetheriteBoots.get(),
                                    OPFood.get());

                            player.sendMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.RED));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .requires(source -> Perms.check(source, Perms.Perm.INFO))
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize("""
                                    <yellow><b>MC-Version:</b> <gold>1.21.3+
                                    <yellow><b>Plugin-Version:</b> <gold>1.0
                                    <yellow><b>Author:</b> <gold>LowdFX"""));
                            return 1;
                        })
                )
                .build();
    }
}
