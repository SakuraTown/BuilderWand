package de.False.BuildersWand;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NMS {
    private JavaPlugin plugin;
    private Random random;

    public NMS(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void spawnParticle(String particle, Location location) {
        location.getWorld().playEffect(location, Effect.valueOf(particle), 0);
    }

    public void spawnParticle(String particle, Location location, Player player) {
        player.playEffect(location, Effect.valueOf(particle),null);
    }

    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInHand();
    }

    public boolean isMainHand(PlayerInteractEvent event) {
        return true;
    }

    public String getDefaultParticle() {
        return Effect.FLAME.toString();
    }

    public void addShapedRecipe(List<String> recipeStrings, HashMap<String, Material> ingredients, ItemStack resultItemStack) {
    }


    public Block setBlockData(Block against, Block SelectionBlock) {
        return SelectionBlock;
    }

    public List<Material> getAirMaterials() {
        List<Material> airBlocks = new ArrayList<>();
        airBlocks.add(Material.AIR);

        return airBlocks;
    }
}