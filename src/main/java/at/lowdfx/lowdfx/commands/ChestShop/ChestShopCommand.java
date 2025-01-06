package at.lowdfx.lowdfx.commands.ChestShop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ChestShopCommand implements CommandExecutor {

    private final ChestShopManager shopManager;

    public ChestShopCommand(ChestShopManager shopManager) {
        this.shopManager = shopManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /shop create <price> or /shop add <player>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /shop create <price>");
                    return true;
                }

                int price;
                try {
                    price = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Price must be a valid number.");
                    return true;
                }

                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null || !(targetBlock.getType() == Material.CHEST || targetBlock.getType() == Material.SHULKER_BOX)) {
                    player.sendMessage(ChatColor.RED + "You must be looking at a chest or shulker box to create a shop.");
                    return true;
                }

                Location location = targetBlock.getLocation();

                if (shopManager.isShop(location)) {
                    player.sendMessage(ChatColor.RED + "This chest is already a shop.");
                    return true;
                }

                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must hold an item to set as the shop's product.");
                    return true;
                }

                ShopData shopData = new ShopData(player.getUniqueId(), location, heldItem.clone(), price);
                shopManager.registerShop(player.getUniqueId(), location, shopData);
                player.sendMessage(ChatColor.GREEN + "Shop created successfully!");
                break;

            case "add":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /shop add <player>");
                    return true;
                }

                String targetPlayerName = args[1];
                Player targetPlayer = player.getServer().getPlayerExact(targetPlayerName);

                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                Block targetBlockForWhitelist = player.getTargetBlockExact(5);
                if (targetBlockForWhitelist == null || !shopManager.isShop(targetBlockForWhitelist.getLocation())) {
                    player.sendMessage(ChatColor.RED + "You must be looking at your shop to whitelist a player.");
                    return true;
                }

                Location shopLocation = targetBlockForWhitelist.getLocation();
                if (!shopManager.isOwner(player.getUniqueId(), shopLocation)) {
                    player.sendMessage(ChatColor.RED + "You are not the owner of this shop.");
                    return true;
                }

                shopManager.whitelistPlayer(shopLocation, targetPlayer.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Player " + targetPlayerName + " has been whitelisted for this shop.");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /shop create <price> or /shop add <player>");
                break;
        }

        return true;
    }
}
