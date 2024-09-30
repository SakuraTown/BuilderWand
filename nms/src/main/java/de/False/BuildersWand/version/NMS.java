package de.False.BuildersWand.version;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMS {
    void spawnParticle(String particle, Location location);

    void spawnParticle(String particle, Location location, Player player);

    ItemStack getItemInHand(Player player);

    boolean isMainHand(PlayerInteractEvent event);

    String getDefaultParticle();

    Block setBlockData(Block against, Block SelectionBlock);

    List<Material> getAirMaterials();

    boolean hasOffhandItem(Player player);
}
