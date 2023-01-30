package me.vinaco.placeableplants;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PlaceablePlants extends JavaPlugin implements Listener {

    ArrayList<String> plantsList = new ArrayList<>();
    ArrayList<String> crops = new ArrayList<>();
    ArrayList<String> dirts = new ArrayList<>();
    ArrayList<String> tall = new ArrayList<>();
    ArrayList<String> sugarCaneBlocks;
    ArrayList<String> bambooBlocks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Plants();
        System.out.println("Placeable Plants Plugin successfully loaded");
    }

    @EventHandler
    public void onPlant(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() == null) {
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        //p.sendMessage(e.getItem().getType().toString());
        if (plantsList.contains(e.getItem().getType().toString())) {
            BlockFace face = getBlockFace(p);
            if (face == null){
                return;
            }
            Location blockLocation = p.getTargetBlockExact(5).getLocation();
            Material toPlace = e.getItem().getType();
            Location blockBelow = blockLocation.getBlock().getRelative(face).getLocation();
            blockBelow.setY(blockBelow.getY() - 1);
            if (blockLocation.getBlock().getRelative(face).getLocation().getBlock().getType() != Material.AIR) {
                return;
            }
            if (blockBelow.getBlock().getType() == Material.AIR && !getConfig().getBoolean("options.allowAirPlacement")) {
                return;
            }
            //Checks if plant is one of the crops with different plant block name
            if (crops.contains(e.getItem().getType().toString())) {
                if (e.getItem().getType().toString().equalsIgnoreCase("WHEAT_SEEDS")) {
                    toPlace = Material.WHEAT;
                } else if (e.getItem().getType().toString().equalsIgnoreCase("BEETROOT_SEEDS")) {
                    toPlace = Material.BEETROOTS;
                } else if (e.getItem().getType().toString().equalsIgnoreCase("POTATO")) {
                    toPlace = Material.POTATOES;
                } else if (e.getItem().getType().toString().equalsIgnoreCase("CARROT")) {
                    toPlace = Material.CARROTS;
                } else if (e.getItem().getType().toString().equalsIgnoreCase("SWEET_BERRY")) {
                    toPlace = Material.SWEET_BERRY_BUSH;
                } else if (e.getItem().getType().toString().equalsIgnoreCase("BAMBOO")) {
                    toPlace = Material.BAMBOO_SAPLING;
                }
            }
            if (toPlace == Material.BAMBOO_SAPLING) {
                if (blockBelow.getBlock().getType() == Material.BAMBOO || blockBelow.getBlock().getType() == Material.BAMBOO_SAPLING) {
                    toPlace = Material.BAMBOO;
                    blockBelow.getBlock().setType(Material.BAMBOO, false);
                }
            }

            if (tall.contains(e.getItem().getType().toString())) {
                Block flowerBlockLower = blockLocation.getBlock().getRelative(face);
                Block flowerBlockUpper = flowerBlockLower.getRelative(BlockFace.UP);
                flowerBlockLower.setType(toPlace, false);
                flowerBlockUpper.setType(toPlace, false);

                Bisected dataLower = (Bisected) flowerBlockLower.getBlockData();
                dataLower.setHalf(Bisected.Half.BOTTOM);
                Bisected dataHigher = (Bisected) flowerBlockUpper.getBlockData();
                dataHigher.setHalf(Bisected.Half.TOP);

                flowerBlockLower.setBlockData(dataLower, false);
                flowerBlockUpper.setBlockData(dataHigher, false);
            } else {
                blockLocation.getBlock().getRelative(face).setType(toPlace, false);
            }

            if (toPlace == Material.BIG_DRIPLEAF) {
                Directional direction = (Directional) blockLocation.getBlock().getRelative(face).getBlockData();
                if (blockBelow.getBlock().getType() == Material.BIG_DRIPLEAF || blockBelow.getBlock().getType() == Material.BIG_DRIPLEAF_STEM) {
                    BlockFace bellowFacing = ((Directional) blockBelow.getBlock().getBlockData()).getFacing();
                    direction.setFacing(((Directional) blockBelow.getBlock().getBlockData()).getFacing());
                    blockBelow.getBlock().setType(Material.BIG_DRIPLEAF_STEM, false);

                    Directional belowDirection = (Directional) blockBelow.getBlock().getBlockData();
                    System.out.println(bellowFacing);
                    belowDirection.setFacing(bellowFacing);

                    blockBelow.getBlock().setBlockData(belowDirection);
                } else {
                    direction.setFacing(p.getFacing().getOppositeFace());
                }
                blockLocation.getBlock().getRelative(face).setBlockData(direction);


            }

            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GRASS_PLACE, 3.0F, 1F);
            if (p.getGameMode() != GameMode.CREATIVE) {
                if (e.getHand() == EquipmentSlot.OFF_HAND) {
                    p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                } else if (e.getHand() == EquipmentSlot.HAND) {
                    p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlantGrow(BlockGrowEvent e){
        if (e.getBlock().getRelative(0,-1,0).getType() == Material.SUGAR_CANE){
            if (!getConfig().getBoolean("options.suppressSugarcaneGrowth")){
                return;
            }
            Block blockBelow = e.getBlock().getRelative(0,-2,0);
            while (blockBelow.getType() == Material.SUGAR_CANE){
                blockBelow = blockBelow.getRelative(0,-1,0);
            }
            //System.out.println(blockBelow.getType());
            if (
                     blockBelow.getRelative(1,0,0).getType() == Material.WATER ||
                     blockBelow.getRelative(-1,0,0).getType() == Material.WATER ||
                     blockBelow.getRelative(0,0,1).getType() == Material.WATER ||
                     blockBelow.getRelative(0,0,-1).getType() == Material.WATER ||
                     blockBelow.getRelative(1,0,0).getType() == Material.FROSTED_ICE ||
                     blockBelow.getRelative(-1,0,0).getType() == Material.FROSTED_ICE ||
                     blockBelow.getRelative(0,0,1).getType() == Material.FROSTED_ICE ||
                     blockBelow.getRelative(0,0,-1).getType() == Material.FROSTED_ICE ||
                     blockBelow.getRelative(1,0,0).getBlockData() instanceof Waterlogged ||
                     blockBelow.getRelative(-1,0,0).getBlockData() instanceof Waterlogged ||
                     blockBelow.getRelative(0,0,1).getBlockData() instanceof Waterlogged ||
                     blockBelow.getRelative(0,0,-1).getBlockData() instanceof Waterlogged){
                if (sugarCaneBlocks.contains(blockBelow.getType().toString())){
                    return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBambooGrow(BlockSpreadEvent e){
        if (e.getBlock().getRelative(0, -1, 0).getType() == Material.BAMBOO || e.getBlock().getRelative(0,-1,0).getType() == Material.BAMBOO_SAPLING){
            if (!getConfig().getBoolean("options.suppressBambooGrowth")){
                return;
            }
            Block blockBelow = e.getBlock().getRelative(0,-2,0);
            while (blockBelow.getType() == Material.BAMBOO || blockBelow.getType() == Material.BAMBOO_SAPLING){
                blockBelow = blockBelow.getRelative(0,-1,0);
            }
            if (bambooBlocks.contains(blockBelow.getType().toString())){
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent e) {
        //System.out.println(e.getLocation().getBlock().getRelative(0,-1,0));
        if (!getConfig().getBoolean("options.suppressTreeGrowth")) {
            return;
        }
        if (!dirts.contains(e.getLocation().getBlock().getRelative(0, -1, 0).getType().toString())) {
            e.setCancelled(true);
        }
    }

    /**
     * Initializes the Array Lists obtained from the config folder and defined in the method
     */
    public void Plants() {
        System.out.println("Initialising plants config");
        getConfig().getConfigurationSection("plants").getKeys(false).forEach(plant -> {
            //adds plant to array list if true in config.yml
            if (getConfig().getBoolean("plants." + plant)) {
                plantsList.add(plant);
                System.out.println("added plant: " + plant);
            }
        });


        crops.add("WHEAT_SEEDS");
        crops.add("BEETROOT_SEEDS");
        crops.add("POTATO");
        crops.add("CARROT");
        crops.add("SWEET_BERRIES");
        crops.add("BAMBOO");

        dirts.add("DIRT");
        dirts.add("GRASS_BLOCK");
        dirts.add("COARSE_DIRT");
        dirts.add("PODZOL");
        dirts.add("ROOTED_DIRT");
        dirts.add("MYCELIUM");
        dirts.add("FARMLAND");
        dirts.add("MUD");

        tall.add("LARGE_FERN");
        tall.add("TALL_GRASS");
        tall.add("SUNFLOWER");
        tall.add("LILAC");
        tall.add("ROSE_BUSH");
        tall.add("PEONY");

        sugarCaneBlocks = (ArrayList<String>)dirts.clone();
        sugarCaneBlocks.add("SAND");
        sugarCaneBlocks.add("RED_SAND");
        sugarCaneBlocks.add("MOSS_BLOCK");
        sugarCaneBlocks.remove("FARMLAND");

        System.out.println("dirts: " + dirts);
        System.out.println("sugarcaneblocks " + sugarCaneBlocks);

        bambooBlocks = (ArrayList<String>) sugarCaneBlocks.clone();
        bambooBlocks.add("GRAVEL");

    }

    /**
     * Gets the BlockFace of the block the player is currently targeting.
     *
     * @param player the player's whos targeted blocks BlockFace is to be checked.
     * @return the BlockFace of the targeted block, or null if the targeted block is non-occluding.
     */
    public BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (getConfig().getBoolean("options.fullBlocksOnly")) {
            if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) return null;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }
}
