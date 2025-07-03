 
package me.asher.inferio.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class LocationCalculator {
    public Location findOptimalSpawnLocation(Player target) {
        Location playerLoc = target.getLocation();
        
        for (int attempt = 0; attempt < 15; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = 20 + ThreadLocalRandom.current().nextDouble() * 30;
            
            double x = playerLoc.getX() + Math.cos(angle) * distance;
            double z = playerLoc.getZ() + Math.sin(angle) * distance;
            
            Location testLoc = new Location(target.getWorld(), x, playerLoc.getY(), z);
            testLoc = findSuitableGround(testLoc);
            
            if (testLoc != null && validateSpawnSafety(testLoc, playerLoc)) {
                return testLoc;
            }
        }
        
        return null;
    }
    
    private Location findSuitableGround(Location loc) {
        Location test = loc.clone();
        
        for (int i = 0; i < 25; i++) {
            if (test.getBlock().getType().isSolid()) {
                return test.add(0, 1, 0);
            }
            test.subtract(0, 1, 0);
        }
        
        test = loc.clone();
        for (int i = 0; i < 25; i++) {
            if (!test.getBlock().getType().isSolid() && 
                !test.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
                test.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                return test;
            }
            test.add(0, 1, 0);
        }
        
        return null;
    }
    
    private boolean validateSpawnSafety(Location loc, Location playerLoc) {
        return !loc.getBlock().getType().isSolid() &&
               !loc.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
               loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid() &&
               loc.distance(playerLoc) >= 15;
    }
}