package de.False.BuildersWand.manager;

import de.False.BuildersWand.Commands;
import de.False.BuildersWand.Main;
import de.False.BuildersWand.version.NMS;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class InventoryManager {

    private HashMap<String, ItemStack[]> inventories = new HashMap<>();
    private Main plugin;

    public InventoryManager(Main plugin, NMS nms) {
        this.plugin = plugin;
    }

    public void load() {
        File[] files = new File(plugin.getDataFolder() + "/storage").listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".yml")) {
                FileConfiguration inventoryFile = YamlConfiguration.loadConfiguration(file);
                String uuid = file.getName().replaceFirst("[.][^.]+$", "");
                ArrayList<ItemStack> content = (ArrayList<ItemStack>) inventoryFile.getList("inventory");
                ItemStack[] contentArray = new ItemStack[content.size()];
                for (int i = 0; i < content.size(); i++) {
                    ItemStack item = content.get(i);
                    if (item != null) {
                        contentArray[i] = item;
                    } else {
                        contentArray[i] = null;
                    }
                }

                inventories.put(uuid, contentArray);
            }
        }
    }

    public ItemStack[] getInventory() {
            return new ItemStack[0];
    }

    public void setInventory() {
//        inventories.put(uuid, itemStack);
//
//        String filePath = "storage/" + uuid + ".yml";
//        File storageFile = new File(plugin.getDataFolder(), filePath);
//        FileConfiguration storageConfigurationFile = YamlConfiguration.loadConfiguration(storageFile);
//        storageConfigurationFile.set("inventory", itemStack);
//        try {
//            storageConfigurationFile.save(storageFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
