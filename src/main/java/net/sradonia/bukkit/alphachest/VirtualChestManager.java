package net.sradonia.bukkit.alphachest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualChestManager {
    private static final String YAML_CHEST_EXTENSION = ".chest.yml";

    private final File dataFolder;
    private final Logger logger;
    private final Map<String, Inventory> chests;
    private final AtomicBoolean isSaving;

    public VirtualChestManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.chests = new ConcurrentHashMap<>();
        this.isSaving = new AtomicBoolean();

        load();
    }

    /**
     * Loads all existing chests from the data folder.
     */
    private void load() {
        dataFolder.mkdirs();

        FilenameFilter filter = (dir, name) -> name.endsWith(YAML_CHEST_EXTENSION);

        for (File chestFile : dataFolder.listFiles(filter)) {
            String chestFileName = chestFile.getName();

            try {
                String playerName = chestFileName.substring(0, chestFileName.length() - 10);
                chests.put(playerName.toLowerCase(), InventoryIO.loadFromYaml(chestFile));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Couldn't load chest file: " + chestFileName, e);
            }
        }

        logger.info("loaded " + chests.size() + " chests");
    }

    public boolean isSaving() {
        return isSaving.get();
    }

    /**
     * Saves all existing chests to the data folder.
     */
    public void saveAsync(BiConsumer<Integer, Integer> progressCallback, Consumer<Integer> completeCallback) {
        if (isSaving()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(AlphaChestPlugin.class),
                () -> save(progressCallback, completeCallback));
    }

    public void save(BiConsumer<Integer, Integer> progressCallback, Consumer<Integer> completeCallback) {
        if (isSaving()) {
            return;
        }

        try {
            isSaving.set(true);

            int totalChests = chests.size();
            int savedChests = 0;

            dataFolder.mkdirs();

            Iterator<Entry<String, Inventory>> chestIterator = chests.entrySet().iterator();
            while (chestIterator.hasNext()) {
                final Entry<String, Inventory> entry = chestIterator.next();
                final String playerName = entry.getKey();
                final Inventory chest = entry.getValue();

                final File chestFile = new File(dataFolder, playerName + YAML_CHEST_EXTENSION);
                if (chest == null) {
                    // Chest got removed, so we have to delete the file.
                    chestFile.delete();
                    chestIterator.remove();
                } else {
                    try {
                        // Write the chest file in YAML format
                        InventoryIO.saveToYaml(chest, chestFile);

                        savedChests++;

                        if (progressCallback != null) {
                            progressCallback.accept(totalChests, savedChests);
                        }
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Couldn't save chest file: " + chestFile.getName(), e);
                    }
                }
            }

            if (completeCallback != null) {
                completeCallback.accept(savedChests);
            }
        } catch (Exception ex) {
            Bukkit.broadcastMessage(ex.getMessage());
            Arrays.stream(ex.getStackTrace()).map(Object::toString).forEach(Bukkit::broadcastMessage);
            ex.printStackTrace();
            Bukkit.broadcastMessage("§4§l심각: 창고 플러그인에서 알립니다.");
            Bukkit.broadcastMessage("§c창고 자동 저장 중 예기치 못한 에러가 발생하였으므로 모든 유저분들께서는 창고를 열어 스크린샷 저장 해주시고 총관리자(tn9945)님께 연락하여 주시기 바랍니다.");
        }

        isSaving.set(false);
    }

    /**
     * Gets a player's virtual chest.
     *
     * @param playerName the name of the player
     * @return the player's virtual chest.
     */
    public Inventory getChest(String playerName) {
        Inventory chest = chests.get(playerName.toLowerCase());

        if (chest == null) {
            chest = Bukkit.getServer().createInventory(null, 6 * 9);
            chests.put(playerName.toLowerCase(), chest);
        }

        return chest;
    }

    /**
     * Clears a player's virtual chest.
     *
     * @param playerName the name of the player
     */
    public void removeChest(String playerName) {
        // Put a null to the map so we remember to delete the file when saving!
        chests.put(playerName.toLowerCase(), null);
    }

    /**
     * Gets the number of virtual chests.
     *
     * @return the number of virtual chests
     */
    public int getChestCount() {
        return chests.size();
    }
}
