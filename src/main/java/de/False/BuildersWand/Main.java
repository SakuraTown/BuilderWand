package de.False.BuildersWand;

import de.False.BuildersWand.ConfigurationFiles.Config;
import de.False.BuildersWand.ConfigurationFiles.Locales;
import de.False.BuildersWand.events.WandEvents;
import de.False.BuildersWand.manager.InventoryManager;
import de.False.BuildersWand.manager.WandManager;
import de.False.BuildersWand.utilities.ParticleUtil;
import de.False.BuildersWand.version.NMS;
import de.False.BuildersWand.version.v_1_12_R1;
import de.False.BuildersWand.version.v_1_18_R1;
import de.False.BuildersWand.version.v_1_8_R1;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static Main plugin;
    private final Locales locales = new Locales(this);
    private Config config;
    private ParticleUtil particleUtil;
    private NMS nms;
    private WandManager wandManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        plugin = this;
        initNMS();
        wandManager = new WandManager(this, nms);
        inventoryManager = new InventoryManager(this, nms);
        try {
            Class.forName("com.google.gson.JsonParser");
        } catch (ClassNotFoundException e) {
            getLogger().warning("Skipping version Check, because the Gson Library couldn't be found!");
        }

        loadConfigFiles();
        particleUtil = new ParticleUtil(nms, config);
        registerEvents();
        registerCommands();
    }

    private void registerCommands() {
        getCommand("builderswand").setExecutor(new Commands(config, wandManager));
    }

    private void registerEvents() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new WandEvents(this, config, particleUtil, nms, wandManager, inventoryManager), this);
    }

    private void loadConfigFiles() {
        config = new Config(this);
        locales.load();
        config.load();
        wandManager.load();
        inventoryManager.load();
    }

    private void initNMS() {
        if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_6)) {
            nms = new v_1_8_R1(plugin);
        }

        if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_12)) {
            nms = new v_1_12_R1(plugin);
        }

        if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_18)) {
            nms = new v_1_18_R1(plugin);
        }
    }

    public static boolean isVersionHigherThan(String version) {
        String currentVersion = Bukkit.getBukkitVersion().split("-")[0]; // 获取当前 Bukkit 版本并去掉后缀
        return compareVersions(currentVersion, version) > 0;
    }

    private static int compareVersions(String v1, String v2) {
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            if (v1Part < v2Part) {
                return -1;
            } else if (v1Part > v2Part) {
                return 1;
            }
        }
        return 0;
    }

}
