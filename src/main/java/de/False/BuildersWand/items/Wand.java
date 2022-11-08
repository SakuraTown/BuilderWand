package de.False.BuildersWand.items;

import de.False.BuildersWand.Main;
import de.False.BuildersWand.utilities.MessageUtil;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Wand {
    private Component name;
    private List<Component> lore;
    private CustomStack customStack;

    private List<String> blacklist;
    private List<String> whitelist;
    private HashMap<String, Material> ingredient = new HashMap<>();

    private boolean particleEnabled;
    private String particle;
    private int particleCount;

    private boolean consumeItems;
    private int maxSize;

    private boolean durabilityEnabled;
    private int durability;
    private String durabilityText;

    private String permission = "";

    public Wand() {

    }

    public ItemStack getRecipeResult() {
        ItemStack buildersWand = customStack.getItemStack();
        ItemMeta itemMeta = buildersWand.getItemMeta();
        itemMeta.displayName(getName());

        List<Component> copy = new ArrayList<>(lore);
        String content = MessageUtil.colorize(getDurabilityText().replace("{durability}", getDurability() + ""));
        copy.add(Component.text(content));
        itemMeta.lore(copy);

        NamespacedKey key = new NamespacedKey(Main.plugin, "uuid");
        String uuid = UUID.randomUUID().toString();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
        buildersWand.setItemMeta(itemMeta);

        return buildersWand;
    }

    public Component getName() {
        return name;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public CustomStack getCustomStack() {
        return customStack;
    }

    public void setCustomStack(CustomStack customStack) {
        this.customStack = customStack;
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

    public List<Component> getLore() {
        return lore;
    }

    public void setLore(List<Component> lore) {
        this.lore = lore;
    }
}
