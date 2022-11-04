package de.False.BuildersWand;

import de.False.BuildersWand.ConfigurationFiles.Config;
import de.False.BuildersWand.ConfigurationFiles.Locales;
import de.False.BuildersWand.Updater.Update;
import de.False.BuildersWand.Updater.UpdateNotification;
import de.False.BuildersWand.events.WandEvents;
import de.False.BuildersWand.events.WandStorageEvents;
import de.False.BuildersWand.manager.InventoryManager;
import de.False.BuildersWand.manager.WandManager;
import de.False.BuildersWand.utilities.Metrics;
import de.False.BuildersWand.utilities.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Main extends JavaPlugin {

    public static Main plugin;
    private final Locales locales = new Locales(this);
    private Config config;
    private ParticleUtil particleUtil;
    private NMS nms = new NMS(this);
    private WandManager wandManager;
    private InventoryManager inventoryManager;
    private Update update;

    @Override
    public void onEnable() {
        plugin = this;
        wandManager = new WandManager(this, nms);
        inventoryManager = new InventoryManager(this, nms);
        try {
            Class.forName("com.google.gson.JsonParser");
            update = new Update(this, 51577);
            update.sendUpdateMessage();
        } catch (ClassNotFoundException e) {
            getLogger().warning("Skipping version Check, because the Gson Library couldn't be found!");
        }

        loadConfigFiles();
        particleUtil = new ParticleUtil(nms, config);
        registerEvents();
        registerCommands();
        loadMetrics();
    }

    private void loadMetrics() {
        Metrics metrics = new Metrics(this);

        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("servers", 1);
                valueMap.put("players", Bukkit.getOnlinePlayers().size());
                return valueMap;
            }
        }));
    }

    private void registerCommands() {
        getCommand("builderswand").setExecutor(new Commands(config, wandManager, nms));
    }

    private void registerEvents() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new WandEvents(this, config, particleUtil, nms, wandManager, inventoryManager), this);
        pluginManager.registerEvents(new WandStorageEvents(this, nms, wandManager, inventoryManager), this);
        if (update != null)
            pluginManager.registerEvents(new UpdateNotification(this, config, update), this);
    }

    private void loadConfigFiles() {
        config = new Config(this);
        locales.load();
        config.load();
        wandManager.load();
        inventoryManager.load();
    }

}
