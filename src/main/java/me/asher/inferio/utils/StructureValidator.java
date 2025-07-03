 
package me.asher.inferio.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class StructureValidator {
    public boolean validateRitualStructure(Location center) {
        World world = center.getWorld();
        
        if (world.getBlockAt(center).getType() != Material.NETHERRACK) return false;
        
        Location goldBase = center.clone().subtract(0, 1, 0);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (world.getBlockAt(goldBase.clone().add(x, 0, z)).getType() != Material.GOLD_BLOCK) {
                    return false;
                }
            }
        }
        
        return hasRedstoneTorch(world, center, BlockFace.NORTH) &&
               hasRedstoneTorch(world, center, BlockFace.SOUTH) &&
               hasRedstoneTorch(world, center, BlockFace.EAST) &&
               hasRedstoneTorch(world, center, BlockFace.WEST);
    }
    
    private boolean hasRedstoneTorch(World world, Location center, BlockFace direction) {
        Location torchLoc = center.clone();
        switch (direction) {
            case NORTH -> torchLoc.add(0, 0, -1);
            case SOUTH -> torchLoc.add(0, 0, 1);
            case EAST -> torchLoc.add(1, 0, 0);
            case WEST -> torchLoc.add(-1, 0, 0);
            default -> { return false; }
        }
        
        Material type = world.getBlockAt(torchLoc).getType();
        return type == Material.REDSTONE_TORCH || type == Material.REDSTONE_WALL_TORCH;
    }
}
