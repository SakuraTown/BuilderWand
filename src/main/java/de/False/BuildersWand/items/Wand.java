package de.False.BuildersWand.items;

import de.False.BuildersWand.utilities.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Wand {
    private String name;
    private List<String> lore;
    private Material material;

    private List<String> blacklist;
    private List<String> whitelist;
    private HashMap<String, Material> ingredient = new HashMap<>();

    private boolean particleEnabled;
    private String particle;
    private int particleCount;

    private boolean consumeItems;
    private int maxSize;

    private int tier;
    private boolean durabilityEnabled;
    private int durability;
    private String durabilityText;

    private String permission = "";

    public Wand() {

    }

    public ItemStack getRecipeResult() {
        ItemStack buildersWand;

        buildersWand = new ItemStack(material);

        ItemMeta itemMeta = buildersWand.getItemMeta();
        itemMeta.setDisplayName(getName());

        List<String> copy = new ArrayList<>(lore);
        String content = MessageUtil.colorize(getDurabilityText().replace("{durability}", getDurability() + ""));
        copy.add(content);
        itemMeta.setLore(copy);

        //TODO NBT API 写UUID 防止堆叠
        buildersWand.setItemMeta(itemMeta);

        return buildersWand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public HashMap<String, Material> getIngredient() {
        return ingredient;
    }

    public void setIngredient(HashMap<String, Material> ingredient) {
        this.ingredient = ingredient;
    }

    public boolean isParticleEnabled() {
        return particleEnabled;
    }

    public void setParticleEnabled(boolean particleEnabled) {
        this.particleEnabled = particleEnabled;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }

    public boolean isConsumeItems() {
        return consumeItems;
    }

    public void setConsumeItems(boolean consumeItems) {
        this.consumeItems = consumeItems;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isDurabilityEnabled() {
        return durabilityEnabled;
    }

    public void setDurabilityEnabled(boolean durabilityEnabled) {
        this.durabilityEnabled = durabilityEnabled;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public String getDurabilityText() {
        return durabilityText;
    }

    public void setDurabilityText(String durabilityText) {
        this.durabilityText = durabilityText;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission() {
        return getPermission().length() > 0;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore.stream().map( it ->
                ChatColor.translateAlternateColorCodes('&', it)
        ).collect(Collectors.toList());
    }


    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
