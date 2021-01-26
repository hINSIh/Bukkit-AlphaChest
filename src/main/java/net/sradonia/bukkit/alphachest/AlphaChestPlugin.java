package net.sradonia.bukkit.alphachest;

import net.sradonia.bukkit.alphachest.commands.ChestCommands;
import net.sradonia.bukkit.alphachest.commands.WorkbenchCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class AlphaChestPlugin extends JavaPlugin implements Listener {
    private Logger logger;

    private VirtualChestManager chestManager;
    private boolean clearOnDeath;
    private boolean dropOnDeath;

    @Override
    public void onEnable() {
        logger = getLogger();

        // Save default config.yml
        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        // Initialize
        File chestFolder = new File(getDataFolder(), "chests");
        chestManager = new VirtualChestManager(chestFolder, getLogger());

        // Load settings
        clearOnDeath = getConfig().getBoolean("clearOnDeath");
        dropOnDeath = getConfig().getBoolean("dropOnDeath");

        // Set command executors
        final ChestCommands chestCommands = new ChestCommands(chestManager);
        getCommand("chest").setExecutor(chestCommands);
        getCommand("clearchest").setExecutor(chestCommands);
        getCommand("savechests").setExecutor(chestCommands);
        getCommand("workbench").setExecutor(new WorkbenchCommand());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Schedule auto-saving
        int autosaveInterval = getConfig().getInt("autosave") * 1200;
        if (autosaveInterval > 0) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                    boolean isSilent = getConfig().getBoolean("silentAutosave");

                    chestManager.saveAsync((totalCount, currentCount) -> {
                        if (isSilent) {
                            return;
                        }

                        int quarter = totalCount / 4;

                        if (quarter == 0 || currentCount % quarter == 0) {
                            logger.info(String.format("%.1f", (double) currentCount / totalCount * 100) + "% [" + currentCount + "/" + totalCount + "]");
                        }
                    }, savedChests -> {
                        if (isSilent) {
                            return;
                        }

                        if (savedChests > 0) {
                            logger.info("auto-saved " + savedChests + " chests");
                        }
                    });
                }
            }, autosaveInterval, autosaveInterval);
        }
    }

    @Override
    public void onDisable() {
        if (chestManager.isSaving()) {
            while (chestManager.isSaving()) {
                try {
                    Thread.sleep(1000);
                    logger.info("waiting saving chests");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        chestManager.save((totalCount, currentCount) -> {
            int quarter = totalCount / 4;

            if (quarter == 0 || currentCount % quarter == 0) {
                logger.info(String.format("%.1f", (double) currentCount / totalCount * 100) + "% [" + currentCount + "/" + totalCount + "]");
            }
        }, savedChests -> {
            logger.info("saved " + savedChests + " chests");
        });
    }

    /**
     * Handles a player's death and clears the chest or drops its contents depending
     * on configuration and permissions.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();

        boolean drop = dropOnDeath;
        boolean clear = dropOnDeath || clearOnDeath;

        if (player.hasPermission("alphachest.keepOnDeath")) {
            drop = false;
            clear = false;
        } else if (player.hasPermission("alphachest.dropOnDeath")) {
            drop = true;
            clear = true;
        } else if (player.hasPermission("alphachest.clearOnDeath")) {
            drop = false;
            clear = true;
        }

        if (drop) {
            List<ItemStack> drops = event.getDrops();
            Inventory chest = chestManager.getChest(player.getName());
            for (int i = 0; i < chest.getSize(); i++) {
                drops.add(chest.getItem(i));
            }
        }
        if (clear) {
            chestManager.removeChest(player.getName());
        }
    }
}
