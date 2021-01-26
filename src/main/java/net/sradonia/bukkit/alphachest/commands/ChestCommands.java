package net.sradonia.bukkit.alphachest.commands;

import net.sradonia.bukkit.alphachest.Teller;
import net.sradonia.bukkit.alphachest.Teller.Type;
import net.sradonia.bukkit.alphachest.VirtualChestManager;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ChestCommands implements CommandExecutor {

	private final VirtualChestManager chestManager;

	public ChestCommands(VirtualChestManager chestManager) {
		this.chestManager = chestManager;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String name = command.getName();
		if (name.equals("chest"))
			return performChestCommand(sender, args);
		else if (name.equalsIgnoreCase("clearchest"))
			return performClearChestCommand(sender, args);
		else if (name.equalsIgnoreCase("savechests"))
			return performSaveChestsCommand(sender, args);
		else
			return false;
	}

	private boolean performChestCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			// Prevent opening of the chest in Creative Mode
			if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("alphachest.chest.creativeMode")) {
				Teller.tell(sender, Type.ERROR, "You are not allowed to open your chest in Creative Mode!");
				return true;
			}

			if (args.length == 0) {
				// Open own chest
				if (sender.hasPermission("alphachest.chest")) {
					Inventory chest = chestManager.getChest(sender.getName());
					player.openInventory(chest);
				} else {
					Teller.tell(sender, Type.ERROR, "You are not allowed to use this command.");
				}
				return true;
			} else if (args.length == 1) {
				// Open someone else's chest
				if (sender.hasPermission("alphachest.admin")) {
					Inventory chest = chestManager.getChest(args[0]);
					player.openInventory(chest);
				} else {
					Teller.tell(sender, Type.ERROR, "You are not allowed to open other user's chests.");
				}
				return true;
			}

			return false;
		} else {
			Teller.tell(sender, Type.ERROR, "Only players are able to open chests.");
			return true;
		}
	}

	private boolean performClearChestCommand(CommandSender sender, String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			if (sender.hasPermission("alphachest.chest")) {
				chestManager.removeChest(sender.getName());
				Teller.tell(sender, Type.SUCCESS, "Successfully cleared your chest.");
			} else {
				Teller.tell(sender, Type.ERROR, "You are not allowed to use this command.");
			}
			return true;
		} else if (args.length == 1) {
			if (sender.hasPermission("alphachest.admin")) {
				chestManager.removeChest(args[0]);
				Teller.tell(sender, Type.SUCCESS, "Successfully cleared " + args[0] + "\'s chest.");
			} else {
				Teller.tell(sender, Type.ERROR, "You are not allowed to clear other user's chests.");
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean performSaveChestsCommand(CommandSender sender, String[] args) {
		if (sender.hasPermission("alphachest.save")) {
			chestManager.saveAsync((totalCount, currentCount) -> {
				int quarter = totalCount / 4;

				if (quarter == 0 || currentCount % quarter == 0) {
					Teller.tell(sender, Type.INFO, String.format("%.1f", (double) currentCount / totalCount * 100) + "% [" + currentCount + "/" + totalCount + "]");
				}
			}, savedChests -> {
				Teller.tell(sender, Type.SUCCESS, "Saved " + savedChests + " chests.");
			});
		} else {
			Teller.tell(sender, Type.ERROR, "You are not allowed to use this command.");
		}
		return true;
	}

}
