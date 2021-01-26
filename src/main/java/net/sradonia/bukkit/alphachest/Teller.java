package net.sradonia.bukkit.alphachest;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Teller {

	public enum Type {
		INFO, SUCCESS, WARNING, ERROR, MISC;
	}

	public static void tell(CommandSender sender, Type type, String message) {
		ChatColor color = ChatColor.WHITE;
		switch (type) {
		case INFO:
			color = ChatColor.WHITE;
			break;
		case SUCCESS:
			color = ChatColor.DARK_GREEN;
			break;
		case WARNING:
			color = ChatColor.GOLD;
			break;
		case ERROR:
			color = ChatColor.DARK_RED;
			break;
		case MISC:
			color = ChatColor.DARK_BLUE;
		}

		sender.sendMessage(ChatColor.BLACK + "[" + ChatColor.GRAY + "AlphaChest" + ChatColor.BLACK + "] " + color + message);
	}

}
