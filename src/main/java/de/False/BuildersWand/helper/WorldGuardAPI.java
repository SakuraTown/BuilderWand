package de.False.BuildersWand.helper;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardAPI {
    private static WorldGuardAPI instance;
    public byte version;

    public static WorldGuardAPI getWorldGuardAPI() {
        if (instance == null) {
            instance = new WorldGuardAPI();
            final Plugin p = Bukkit.getPluginManager().getPlugin("WorldGuard");
            final String version = p != null && p.isEnabled() ? p.getDescription().getVersion() : null;

            instance.version = (byte) (version != null ? version.startsWith("6") ? 6 : 7 : -1);
        }
        return instance;
    }

    public boolean canBuild(Player player, Location location) {

        if (version == 6) {
            return WorldGuardPlugin.inst().canBuild(player, location);
        }

        if (version == 7) {
            final com.sk89q.worldguard.protection.regions.RegionContainer c = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            final com.sk89q.worldguard.protection.regions.RegionQuery q = c.createQuery();
            final ApplicableRegionSet s = q.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));

            try {
                return s.testState(getLocalPlayer(player), (StateFlag) Flags.class.getDeclaredField("BUILD").get(null));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private LocalPlayer getLocalPlayer(Player player) {
        return player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
    }
}