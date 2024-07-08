package de.False.BuildersWand.manager;

import de.False.BuildersWand.Main;
import de.False.BuildersWand.NMS;
import de.False.BuildersWand.items.Wand;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WandManager {
    private File file;
    private FileConfiguration config;
    private List<Wand> wandList = new ArrayList<>();
    private NMS nms;

    public WandManager(Main plugin, NMS nms) {
        plugin.saveResource("wands.yml", false);

        this.file = new File(plugin.getDataFolder(), "wands.yml");
        this.nms = nms;
    }

    private void loadWands() {
        wandList.clear();
        ConfigurationSection configurationSection = config.getConfigurationSection("wands");

        for (String key : configurationSection.getKeys(false)) {
            wandList.add(getWand(key));
        }
    }

    private Wand getWand(String key) {
        String configPrefix = "wands." + key + ".";
        Wand wand = new Wand();
        String name = Objects.requireNonNull(config.getString(configPrefix + "name"));
        wand.setName(name);
        wand.setTier(Integer.parseInt(key));
        String material = config.getString(configPrefix + "material");

        if (material != null) {
            wand.setMaterial(Material.matchMaterial(material));
        }

        wand.setMaxSize(config.getInt(configPrefix + "maxSize"));
        wand.setConsumeItems(config.getBoolean(configPrefix + "consumeItems"));
        wand.setDurability(config.getInt(configPrefix + "durability.amount"));
        wand.setDurabilityEnabled(config.getBoolean(configPrefix + "durability.enabled"));
        wand.setDurabilityText(config.getString(configPrefix + "durability.text"));

        wand.setBlacklist(config.getStringList(configPrefix + "blacklist"));
        wand.setWhitelist(config.getStringList(configPrefix + "whitelist"));
        wand.setParticleEnabled(config.getBoolean(configPrefix + "particles.enabled"));
        wand.setParticle(config.getString(configPrefix + "particles.type"));
        wand.setParticleCount(config.getInt(configPrefix + "particles.count"));

        if (config.isSet(configPrefix + "permission")) {
            wand.setPermission(config.getString(configPrefix + "permission"));
        }

        List<String> lore = config.getStringList(configPrefix + "lore");

        wand.setLore(lore);
        return wand;
    }

    public Wand getWand(ItemStack itemStack) {
        for (Wand wand : wandList) {

            if (itemStack == null) {
                return null;
            }

//            if (wand.getCustomStack() == null) {
//                String configPrefix = "wands." + wand.getTier() + ".";
//                wand.setCustomStack(CustomStack.getInstance(config.getString(configPrefix + "customStack")));
//            }

            Material material = itemStack.getType();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null) {
                return null;
            }

            String name = itemMeta.getDisplayName();
            if (wand.getName().equals(name)) {
                return wand;
            }
        }
        return null;
    }



    public void load() {
        config = YamlConfiguration.loadConfiguration(file);
        loadWands();
    }

    private void registerRecipes() {

    }

    public Wand getWandTier(int tier) {
        if (config.isSet("wands." + tier)) {
            return getWand(tier + "");
        }

        return null;
    }

    private void save() {
        try {
            config.save(file);
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isWand(ItemStack itemStack) {
        return getWand(itemStack) != null;
    }
}
