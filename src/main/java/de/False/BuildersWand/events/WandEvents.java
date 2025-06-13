package de.False.BuildersWand.events;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.False.BuildersWand.ConfigurationFiles.Config;
import de.False.BuildersWand.Main;
import de.False.BuildersWand.NMS;
import de.False.BuildersWand.api.canBuildHandler;
import de.False.BuildersWand.enums.ParticleShapeHidden;
import de.False.BuildersWand.helper.WorldGuardAPI;
import de.False.BuildersWand.items.Wand;
import de.False.BuildersWand.manager.InventoryManager;
import de.False.BuildersWand.manager.WandManager;
import de.False.BuildersWand.utilities.MessageUtil;
import de.False.BuildersWand.utilities.ParticleUtil;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class WandEvents implements Listener {
    private Main plugin;
    private Config config;
    private ParticleUtil particleUtil;
    private NMS nms;
    private WandManager wandManager;
    private InventoryManager inventoryManager;
    private HashMap<Block, List<Block>> blockSelection = new HashMap<>();
    private HashMap<Block, List<Block>> replacements = new HashMap<>();
    private HashMap<Block, List<Block>> tmpReplacements = new HashMap<>();
    public static ArrayList<canBuildHandler> canBuildHandlers = new ArrayList<>();
    private List<Material> ignoreList = new ArrayList<>();

    public WandEvents(Main plugin, Config config, ParticleUtil particleUtil, NMS nms, WandManager wandManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.config = config;
        this.particleUtil = particleUtil;
        this.nms = nms;
        this.wandManager = wandManager;
        this.inventoryManager = inventoryManager;

        ignoreList.add(Material.LAVA);
        ignoreList.add(Material.WATER);
        ignoreList.addAll(nms.getAirMaterials());

        startScheduler();
    }

    private void startScheduler() {
        Set<Material> ignoreBlockTypes = new HashSet<>(Arrays.asList(Material.WATER, Material.LAVA));
        ignoreBlockTypes.addAll(nms.getAirMaterials());

        //TODO 用 luck 的 bucket 再次优化
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            blockSelection.clear();
            tmpReplacements.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {

                ItemStack mainHand = nms.getItemInHand(player);
                Wand wand = wandManager.getWand(mainHand);

                Block block;
                try {
                    block = player.getTargetBlock(ignoreBlockTypes, 5);
                } catch (Exception e) {
                    continue;
                }

                Material blockType = block.getType();
                Material blockAbove = player.getLocation().add(0, 1, 0).getBlock().getType();
                if (ignoreList.contains(blockType)
                        || wand == null
                        || (!ignoreList.contains(blockAbove))
                ) {
                    continue;
                }

                List<Block> lastBlocks = player.getLastTwoTargetBlocks(ignoreBlockTypes, 5);
                if (lastBlocks.size() < 2) {
                    continue;
                }

                BlockFace blockFace = lastBlocks.get(1).getFace(lastBlocks.get(0));

                int itemCount = getItemCount(player, block, mainHand);

                blockSelection.put(block, new ArrayList<>());
                tmpReplacements.put(block, new ArrayList<>());

                setBlockSelection(player, blockFace, itemCount, block, block, wand);
                replacements = tmpReplacements;
                List<Block> selection = blockSelection.get(block);

                if (wand.isParticleEnabled()) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        for (Block selectionBlock : selection) {
                            renderBlockOutlines(blockFace, selectionBlock, selection, wand, player);
                        }
                    });
                }
            }
        }, 0L, 10);
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = nms.getItemInHand(player);
        Wand wand = wandManager.getWand(mainHand);
        if (wand == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = nms.getItemInHand(player);
        Wand wand = wandManager.getWand(mainHand);

        if (wand == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || !nms.isMainHand(event)) {
            return;
        }

        Block against = event.getClickedBlock();
        List<Block> selection = replacements.get(against);
        if (selection == null) {
            return;
        }

        if (
                !player.hasPermission("buildersWand.use")
                        || (!player.hasPermission("buildersWand.bypass") && !isAllowedToBuildForExternalPlugins(player, selection))
                        || wand.hasPermission() && !player.hasPermission(wand.getPermission())
                        || !canBuildHandlerCheck(player, selection)
        ) {
            MessageUtil.sendMessage(player, "noPermissions");
            return;
        }

        Material blockType = against.getType();
        byte blockSubId = against.getData();
        ItemStack itemStack = new ItemStack(against.getType());
        MaterialData materialData = itemStack.getData();
        materialData.setData(blockSubId);
        itemStack.setData(materialData);
        event.setCancelled(true);

        ItemStack customItemStack = null;
        if (getExternalPlugin("ItemsAdder") != null) {
            customItemStack = ItemsAdder.getCustomBlock(against);
        }

        ItemStack finalCustomItemStack = customItemStack;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block selectionBlock : selection) {
                if (getExternalPlugin("ItemsAdder") != null && ItemsAdder.isCustomBlock(against)) {
                    ItemsAdder.placeCustomBlock(selectionBlock.getLocation(), finalCustomItemStack);
                } else {
                    selectionBlock.setType(blockType);
                    selectionBlock = nms.setBlockData(against, selectionBlock);
                }


                Plugin coreProtect = getExternalPlugin("CoreProtect");
                if (coreProtect != null) {
                    CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();
                    if (coreProtectAPI.isEnabled()) {
                        coreProtectAPI.logPlacement(player.getName(), selectionBlock.getLocation(), blockType, selectionBlock.getData());
                    }
                }

                try {
                    Method m = Block.class.getMethod("setData", byte.class);
                    m.invoke(selectionBlock, blockSubId);
                } catch (NoSuchMethodException | IllegalAccessException
                         | InvocationTargetException e) {
                }
            }

        }, 1L);

        Integer amount = selection.size();
        if (wand.isConsumeItems()) {
            if (getExternalPlugin("ItemsAdder") != null && ItemsAdder.isCustomBlock(against)) {
                removeCustomItemStack(ItemsAdder.getCustomBlock(against), amount, player, mainHand, customItemStack);
            } else {
                removeItemStack(itemStack, amount, player, mainHand);
            }

        }
        if (wand.isDurabilityEnabled() && amount >= 1) {
            removeDurability(mainHand, player, wand);
        }
    }

    private boolean canBuildHandlerCheck(Player player, List<Block> selection) {
        for (canBuildHandler canBuildHandler : canBuildHandlers) {
            for (Block selectionBlock : selection) {
                if (!canBuildHandler.canBuild(player, selectionBlock.getLocation())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canBuildHandlerCheck(Player player, Location location) {
        for (canBuildHandler canBuildHandler : canBuildHandlers) {
            if (!canBuildHandler.canBuild(player, location)) {
                return false;
            }
        }

        return true;
    }

    @EventHandler
    private void craftItemEvent(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();
        Wand wand = wandManager.getWand(result);
        if (wand == null) {
            return;
        }

        if (!player.hasPermission("buildersWand.craft")) {
            MessageUtil.sendMessage(player, "noPermissions");
            event.setCancelled(true);
        }

        Inventory inventory = event.getInventory();
        ItemStack itemStack = event.getInventory().getResult();
        inventory.setItem(0, itemStack);
        player.updateInventory();
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory instanceof CraftingInventory)) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        Wand wand = wandManager.getWand(itemStack);
        if (wand == null) {
            return;
        }

        ClickType clickType = event.getClick();
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            event.setCancelled(true);
        }
    }

    private int getItemCount(Player player, Block block, ItemStack mainHand) {
        int count = 0;
        Inventory inventory = player.getInventory();
        Material blockMaterial = block.getType();
        ItemStack[] inventoryContents = inventory.getContents();
        ItemStack helmet = inventory.getItem(39);

        if (helmet != null) {
            inventoryContents = (ItemStack[]) ArrayUtils.removeElement(inventoryContents, helmet);
        }

        if (mainHand.getType() == Material.AIR) {
            return 0;
        }

        ItemStack[] itemStacks = (ItemStack[]) ArrayUtils.addAll(inventoryContents, inventoryManager.getInventory());

        if (player.getGameMode() == GameMode.CREATIVE) {
            return Integer.MAX_VALUE;
        }

        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) {
                continue;
            }
            Material itemMaterial = itemStack.getType();

            if (!itemMaterial.equals(blockMaterial) || block.getData() != itemStack.getData().getData() || isSpecialItem(itemStack)) {
                continue;
            }

            count += itemStack.getAmount();
        }

        return count;
    }

    private int getCustomBlockCount(Player player, Block block, ItemStack mainHand, ItemStack customBlockItemStack) {
        int count = 0;
        Inventory inventory = player.getInventory();
        Material blockMaterial = block.getType();
        ItemStack[] inventoryContents = inventory.getContents();
        ItemStack helmet = inventory.getItem(39);

        if (helmet != null) {
            inventoryContents = (ItemStack[]) ArrayUtils.removeElement(inventoryContents, helmet);
        }

        if (mainHand.getType() == Material.AIR) {
            return 0;
        }

        ItemStack[] itemStacks = (ItemStack[]) ArrayUtils.addAll(inventoryContents, inventoryManager.getInventory());

        if (player.getGameMode() == GameMode.CREATIVE) {
            return Integer.MAX_VALUE;
        }

        for (ItemStack itemStack : itemStacks) {
            if (!(CustomStack.byItemStack(itemStack) == null)) {
                continue;
            }

            if (!ItemsAdder.getCustomItemName(itemStack).equalsIgnoreCase(ItemsAdder.getCustomItemName(customBlockItemStack))) {
                continue;
            }

            count += itemStack.getAmount();
        }

        return count;
    }

    private void removeDurability(ItemStack wandItemStack, Player player, Wand wand) {
        Inventory inventory = player.getInventory();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Integer durability = getDurability(wandItemStack, wand);
        Integer newDurability = durability - 1;

        if (newDurability <= 0) {
            inventory.removeItem(wandItemStack);
        }

        ItemMeta itemMeta = wandItemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        String durabilityText = MessageUtil.colorize(wand.getDurabilityText().replace("{durability}", newDurability + ""));
        if (lore == null) {
            lore = new ArrayList<>();
            lore.add(durabilityText);
        } else {
            lore.set(getDurabilityRow(wandItemStack, wand), durabilityText);
        }

        itemMeta.setLore(lore);
        wandItemStack.setItemMeta(itemMeta);
    }

    private void removeItemStack(ItemStack itemStack, int amount, Player player, ItemStack mainHand) {
        Inventory inventory = player.getInventory();
        Material material = itemStack.getType();
        ItemStack[] itemStacks = inventory.getContents();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        for (ItemStack inventoryItemStack : itemStacks) {
            if (inventoryItemStack == null) {
                continue;
            }
            Material itemMaterial = inventoryItemStack.getType();
            if (!itemMaterial.equals(material) || itemStack.getData().getData() != inventoryItemStack.getData().getData() || isSpecialItem(inventoryItemStack)) {
                continue;
            }

            int itemAmount = inventoryItemStack.getAmount();
            if (amount >= itemAmount) {

                HashMap<Integer, ItemStack> didntRemovedItems = inventory.removeItem(inventoryItemStack);

                if (didntRemovedItems.size() == 1) {
                    player.getInventory().setItemInOffHand(null);
                }

                amount -= itemAmount;
                player.updateInventory();
            } else {
                inventoryItemStack.setAmount(itemAmount - amount);
                player.updateInventory();
                return;
            }
        }

        ItemStack[] inventoryItemStacks = inventoryManager.getInventory();
        ArrayList<ItemStack> inventoryItemStacksList = new ArrayList<>(Arrays.asList(inventoryItemStacks));
        for (ItemStack inventoryItemStack : inventoryItemStacks) {
            if (inventoryItemStack == null) {
                continue;
            }
            Material itemMaterial = inventoryItemStack.getType();
            if (!itemMaterial.equals(material) || itemStack.getData().getData() != inventoryItemStack.getData().getData()) {
                continue;
            }
            int itemAmount = inventoryItemStack.getAmount();
            if (amount >= itemAmount) {
                inventoryItemStacksList.remove(inventoryItemStack);
                amount -= itemAmount;
            } else {
                int index = inventoryItemStacksList.indexOf(inventoryItemStack);
                inventoryItemStack.setAmount(itemAmount - amount);
                inventoryItemStacksList.set(index, inventoryItemStack);
                return;
            }
        }
    }

    private void removeCustomItemStack(ItemStack itemStack, int amount, Player player, ItemStack mainHand, ItemStack customBlockItemStack) {

        Inventory inventory = player.getInventory();
        ItemStack[] itemStacks = inventory.getContents();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        for (ItemStack inventoryItemStack : itemStacks) {
            if (inventoryItemStack == null || !ItemsAdder.isCustomItem(inventoryItemStack) || !ItemsAdder.getCustomItemName(inventoryItemStack).equalsIgnoreCase(ItemsAdder.getCustomItemName(customBlockItemStack))) {
                continue;
            }

            int itemAmount = inventoryItemStack.getAmount();
            if (amount >= itemAmount) {
                HashMap<Integer, ItemStack> didntRemovedItems = inventory.removeItem(inventoryItemStack);

                if (didntRemovedItems.size() == 1) {
                    player.getInventory().setItemInOffHand(null);
                }

                amount -= itemAmount;
                player.updateInventory();
            } else {
                inventoryItemStack.setAmount(itemAmount - amount);
                player.updateInventory();
                return;
            }
        }
    }


    private void setBlockSelection(Player player, BlockFace blockFace, int maxLocations, Block startBlock, Block blockToCheck, Wand wand) {
        int blockToCheckData = blockToCheck.getData();
        int startBlockData = startBlock.getData();
        Location startLocation = startBlock.getLocation();
        Location checkLocation = blockToCheck.getLocation();
        Material startMaterial = startBlock.getType();
        Material blockToCheckMaterial = blockToCheck.getType();
        Material relativeBlock = blockToCheck.getRelative(blockFace).getType();
        List<Block> selection = blockSelection.get(startBlock);
        List<Block> replacementsList = tmpReplacements.get(startBlock);
        List<String> blacklist = wand.getBlacklist();
        List<String> whitelist = wand.getWhitelist();

        if (startLocation.distance(checkLocation) >= wand.getMaxSize()
                || !(startMaterial.equals(blockToCheckMaterial))
                || startMaterial.toString().endsWith("SLAB")
                || startMaterial.toString().endsWith("STEP")
                || maxLocations <= selection.size()
                || blockToCheckData != startBlockData
                || selection.contains(blockToCheck)
                || !ignoreList.contains(relativeBlock)
                || whitelist.size() == 0 && blacklist.size() > 0 && blacklist.contains(startMaterial.toString())
                || blacklist.size() == 0 && whitelist.size() > 0 && !whitelist.contains(startMaterial.toString())
                || (!isAllowedToBuildForExternalPlugins(player, checkLocation) && !player.hasPermission("buildersWand.bypass"))
                || !canBuildHandlerCheck(player, checkLocation)
                || !player.hasPermission("buildersWand.use")
                || wand.hasPermission() && !player.hasPermission(wand.getPermission())
        ) {
            return;
        }

        if (startMaterial.toString().contains("SHULKER_BOX")) {
            return;
        }

        if (!startMaterial.isSolid()) {
            return;
        }

        selection.add(blockToCheck);
        replacementsList.add(blockToCheck.getRelative(blockFace));
        Block blockEast = blockToCheck.getRelative(BlockFace.EAST);
        Block blockWest = blockToCheck.getRelative(BlockFace.WEST);
        Block blockNorth = blockToCheck.getRelative(BlockFace.NORTH);
        Block blockSouth = blockToCheck.getRelative(BlockFace.SOUTH);
        Block blockUp = blockToCheck.getRelative(BlockFace.UP);
        Block blockDown = blockToCheck.getRelative(BlockFace.DOWN);
        switch (blockFace) {
            case UP:
            case DOWN:
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockEast, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockWest, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockNorth, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockSouth, wand);
            case EAST:
            case WEST:
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockNorth, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockSouth, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockDown, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockUp, wand);
            case SOUTH:
            case NORTH:
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockWest, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockEast, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockDown, wand);
                setBlockSelection(player, blockFace, maxLocations, startBlock, blockUp, wand);
        }
    }

    private void renderBlockOutlines(BlockFace blockFace, Block selectionBlock, List<Block> selection, Wand wand, Player player) {
        List<ParticleShapeHidden> shapes = new ArrayList<>();

        Block blockEast = selectionBlock.getRelative(BlockFace.EAST);
        Block blockWest = selectionBlock.getRelative(BlockFace.WEST);
        Block blockNorth = selectionBlock.getRelative(BlockFace.NORTH);
        Block blockSouth = selectionBlock.getRelative(BlockFace.SOUTH);
        Block blockUp = selectionBlock.getRelative(BlockFace.UP);
        Block blockDown = selectionBlock.getRelative(BlockFace.DOWN);
        Block blockNorthWest = selectionBlock.getRelative(BlockFace.NORTH_WEST);
        Block blockNorthEast = selectionBlock.getRelative(BlockFace.NORTH_EAST);
        Block blockSouthEast = selectionBlock.getRelative(BlockFace.SOUTH_EAST);
        Block blockSouthWest = selectionBlock.getRelative(BlockFace.SOUTH_WEST);
        Block blockDownEast = selectionBlock.getRelative(1, -1, 0);
        Block blockUpEast = selectionBlock.getRelative(1, 1, 0);
        Block blockDownWest = selectionBlock.getRelative(-1, -1, 0);
        Block blockUpWest = selectionBlock.getRelative(-1, 1, 0);
        Block blockDownSouth = selectionBlock.getRelative(0, -1, 1);
        Block blockUpSouth = selectionBlock.getRelative(0, 1, 1);
        Block blockDownNorth = selectionBlock.getRelative(0, -1, -1);
        Block blockUpNorth = selectionBlock.getRelative(0, 1, -1);

        Boolean blockEastContains = selection.contains(blockEast);
        Boolean blockWestContains = selection.contains(blockWest);
        Boolean blockNorthContains = selection.contains(blockNorth);
        Boolean blockSouthContains = selection.contains(blockSouth);
        Boolean blockUpContains = selection.contains(blockUp);
        Boolean blockDownContains = selection.contains(blockDown);
        Boolean blockNorthWestContains = selection.contains(blockNorthWest);
        Boolean blockNorthEastContains = selection.contains(blockNorthEast);
        Boolean blockSouthEastContains = selection.contains(blockSouthEast);
        Boolean blockSouthWestContains = selection.contains(blockSouthWest);
        Boolean blockDownEastContains = selection.contains(blockDownEast);
        Boolean blockUpEastContains = selection.contains(blockUpEast);
        Boolean blockDownWestContains = selection.contains(blockDownWest);
        Boolean blockUpWestContains = selection.contains(blockUpWest);
        Boolean blockDownSouthContains = selection.contains(blockDownSouth);
        Boolean blockUpSouthContains = selection.contains(blockUpSouth);
        Boolean blockDownNorthContains = selection.contains(blockDownNorth);
        Boolean blockUpNorthContains = selection.contains(blockUpNorth);

        if (blockEastContains) {
            shapes.add(ParticleShapeHidden.EAST);
        }
        if (blockWestContains) {
            shapes.add(ParticleShapeHidden.WEST);
        }
        if (blockNorthContains) {
            shapes.add(ParticleShapeHidden.NORTH);
        }
        if (blockSouthContains) {
            shapes.add(ParticleShapeHidden.SOUTH);
        }
        if (blockUpContains) {
            shapes.add(ParticleShapeHidden.UP);
        }
        if (blockDownContains) {
            shapes.add(ParticleShapeHidden.DOWN);
        }
        if (blockNorthWestContains) {
            shapes.add(ParticleShapeHidden.NORTH_WEST);
        }
        if (blockNorthEastContains) {
            shapes.add(ParticleShapeHidden.NORTH_EAST);
        }
        if (blockSouthEastContains) {
            shapes.add(ParticleShapeHidden.SOUTH_EAST);
        }
        if (blockSouthWestContains) {
            shapes.add(ParticleShapeHidden.SOUTH_WEST);
        }
        if (blockDownEastContains) {
            shapes.add(ParticleShapeHidden.DOWN_EAST);
        }
        if (blockUpEastContains) {
            shapes.add(ParticleShapeHidden.UP_EAST);
        }
        if (blockDownWestContains) {
            shapes.add(ParticleShapeHidden.DOWN_WEST);
        }
        if (blockUpWestContains) {
            shapes.add(ParticleShapeHidden.UP_WEST);
        }
        if (blockDownSouthContains) {
            shapes.add(ParticleShapeHidden.DOWN_SOUTH);
        }
        if (blockUpSouthContains) {
            shapes.add(ParticleShapeHidden.UP_SOUTH);
        }
        if (blockDownNorthContains) {
            shapes.add(ParticleShapeHidden.DOWN_NORTH);
        }
        if (blockUpNorthContains) {
            shapes.add(ParticleShapeHidden.UP_NORTH);
        }

        particleUtil.drawBlockOutlines(blockFace, shapes, selectionBlock.getRelative(blockFace).getLocation(), wand, player);
    }

    private boolean isAllowedToBuildForExternalPlugins(Player player, Location location) {
        Plugin worldGuardPlugin = getExternalPlugin("WorldGuard");
        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            if (!WorldGuardAPI.getWorldGuardAPI().allows(player, location)) {
                return false;
            }
        }

        Plugin bentoBox = getExternalPlugin("BentoBox");
        if (bentoBox != null) {
            BentoBox bentoBoxapi = BentoBox.getInstance();
            User user = User.getInstance(player);
            Optional<Island> island = bentoBoxapi.getIslands().getIslandAt(location);
            return island.isEmpty() || island.get().isAllowed(user, Flags.PLACE_BLOCKS);
        }

        return true;
    }

    private boolean isAllowedToBuildForExternalPlugins(Player player, List<Block> selection) {
        Plugin worldGuardPlugin = getExternalPlugin("WorldGuard");
        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            for (Block selectionBlock : selection) {
                if (!WorldGuardAPI.getWorldGuardAPI().allows(player, selectionBlock.getLocation())) {
                    return false;
                }
            }
        }

        Plugin bentoBox = getExternalPlugin("BentoBox");
        if (bentoBox != null) {
            BentoBox bentoBoxapi = BentoBox.getInstance();
            User user = User.getInstance(player);
            for (Block selectionBlock : selection) {
                Optional<Island> island = bentoBoxapi.getIslands().getIslandAt(selectionBlock.getLocation());
                if (island.isPresent() && !island.get().isAllowed(user, Flags.PLACE_BLOCKS)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Plugin getExternalPlugin(String name) {
        return plugin.getServer().getPluginManager().getPlugin(name);
    }

    private int getDurability(ItemStack wandItemStack, Wand wand) {
        ItemMeta itemMeta = wandItemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            return wand.getDurability();
        }
        String durabilityString = lore.get(getDurabilityRow(wandItemStack, wand));
        durabilityString = ChatColor.stripColor(durabilityString);
        durabilityString = durabilityString.replaceAll("[^0-9]", "");

        return Integer.parseInt(durabilityString);
    }

    private int getDurabilityRow(ItemStack wandItemStack, Wand wand) {
        ItemMeta itemMeta = wandItemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();

        if (lore == null) {
            return 0;
        }
//
//        for (int i = 0; i < lore.size(); i++) {
//            if (lore.get(i).contains(wand.getDurabilityText())) {
//                return i;
//            }
//        }
        // TODO 临时处理
        return lore.size() - 1;
    }

    private boolean isSpecialItem(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null &&
                (meta.hasEnchants() ||
                        meta.hasLore() ||
                        meta.hasDisplayName());
    }
}
