package de.False.BuildersWand;

import io.sentry.util.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class NMS {
    private JavaPlugin plugin;
    private Random random;

    public NMS(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void spawnParticle(String particle, Location location) {
        location.getWorld().spawnParticle(Particle.valueOf(particle), location.getX(), location.getY(), location.getZ(), 0, 128, 0, 0, 10);
    }

    public void spawnParticle(String particle, Location location, Player player) {
        player.spawnParticle(Particle.valueOf(particle), location.getX(), location.getY(), location.getZ(), 0, 0, 0, 0);
    }

    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    public boolean isMainHand(PlayerInteractEvent event) {
        return event.getHand() == EquipmentSlot.HAND;
    }

    public String getDefaultParticle() {
        return Particle.FLAME.toString();
    }

    public void addShapedRecipe(List<String> recipeStrings, HashMap<String, Material> ingredients, ItemStack resultItemStack) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "buildersWand" + random.nextInt());
        ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, resultItemStack);
        shapedRecipe.shape(recipeStrings.toArray(new String[recipeStrings.size()]));
        for (Map.Entry<String, Material> entry : ingredients.entrySet()) {
            String materialShortcut = entry.getKey();
            Material material = entry.getValue();
            shapedRecipe.setIngredient(materialShortcut.charAt(0), material);
        }

        Bukkit.getServer().addRecipe(shapedRecipe);
    }


    public Block setBlockData(Block against, Block SelectionBlock) {
        SelectionBlock.setBlockData(against.getBlockData());
        return SelectionBlock;
    }

    public List<Material> getAirMaterials() {
        List<Material> airBlocks = new ArrayList<Material>();
        airBlocks.add(Material.AIR);
        airBlocks.add(Material.CAVE_AIR);

        return airBlocks;
    }
}