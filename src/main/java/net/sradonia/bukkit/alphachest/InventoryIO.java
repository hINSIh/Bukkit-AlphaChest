package net.sradonia.bukkit.alphachest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to store inventories to file and read them back again.
 */
public class InventoryIO {

	/**
	 * Loads an inventory from a YAML configuration file.
	 * 
	 * @param file
	 *        the YAML file to load
	 * @return the loaded inventory
	 * @throws IOException
	 *         if the file could not be read
	 * @throws InvalidConfigurationException
	 *         if the file could not be parsed
	 */
	public static Inventory loadFromYaml(File file) throws IOException, InvalidConfigurationException {
		YamlConfiguration yaml = new Utf8YamlConfiguration();
		yaml.load(file);

		int inventorySize = yaml.getInt("size", 6 * 9);
		Inventory inventory = Bukkit.getServer().createInventory(null, inventorySize);

		ConfigurationSection items = yaml.getConfigurationSection("items");
		for (int slot = 0; slot < inventorySize; slot++) {
			String slotString = String.valueOf(slot);
			if (items.isItemStack(slotString)) {
				ItemStack itemStack = items.getItemStack(slotString);
				inventory.setItem(slot, itemStack);
			}
		}

		return inventory;
	}

	/**
	 * Save an inventory to a YAML configuration file.
	 * 
	 * @param inventory
	 *        the inventory to save
	 * @param file
	 *        the YAML file to write
	 * @throws IOException
	 *         if the file could not be written
	 */
	public static void saveToYaml(Inventory inventory, File file) throws IOException {
		YamlConfiguration yaml = new Utf8YamlConfiguration();

		int inventorySize = inventory.getSize();
		yaml.set("size", inventorySize);

		ConfigurationSection items = yaml.createSection("items");
		for (int slot = 0; slot < inventorySize; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack != null) {
				items.set(String.valueOf(slot), stack);
			}
		}

		yaml.save(file);
	}
}
