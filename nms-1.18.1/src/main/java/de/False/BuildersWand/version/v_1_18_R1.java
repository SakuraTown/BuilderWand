package de.False.BuildersWand.version;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class v_1_18_R1 implements NMS {
    private final JavaPlugin plugin;
    private final Random random;

    public v_1_18_R1(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @Override
    public void spawnParticle(String particle, Location location) {
        location.getWorld().spawnParticle(Particle.valueOf(particle), location.getX(), location.getY(), location.getZ(), 0, 128, 0, 0, 10);
    }

    @Override
    public void spawnParticle(String particle, Location location, Player player) {
        Optional<Particle> particleOptional = Enums.getIfPresent(Particle.class, particle);

        if (particleOptional.isPresent()) {
            player.spawnParticle(particleOptional.get(), location.getX(), location.getY(), location.getZ(), 0, 128, 0, 0);
        } else {
            player.spawnParticle(Particle.valueOf(getDefaultParticle()), location.getX(), location.getY(), location.getZ(), 0, 128, 0, 0);
        }
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
        return event.getHand() == EquipmentSlot.HAND;
    }

    @Override
    public String getDefaultParticle() {
        return Particle.REDSTONE.toString();
    }

    public Block setBlockData(Block against, Block SelectionBlock) {
        return SelectionBlock;
    }

    @Override
    public List<Material> getAirMaterials() {
        List<Material> airBlocks = new ArrayList<>();
        airBlocks.add(Material.AIR);

        return airBlocks;
    }
}