package de.False.BuildersWand.enums;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.False.BuildersWand.helper.WorldGuardAPI;

public enum WGFlag {
    BUILD;

    private static byte version = WorldGuardAPI.getWorldGuardAPI().version;
    private StateFlag flag;
    private boolean supportsLegacy;
    WGFlag() { supportsLegacy = true; }
    WGFlag(boolean supportsLegacy) { this.supportsLegacy = supportsLegacy; }

    public  StateFlag getFlag() {
        return flag;
    }
}