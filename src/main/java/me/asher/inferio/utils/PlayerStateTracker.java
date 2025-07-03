 
package me.asher.inferio.utils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateTracker {
    private final ConcurrentHashMap<UUID, PlayerTensionData> playerData = new ConcurrentHashMap<>();
    
    public void incrementTension(UUID playerId, int amount) {
        playerData.compute(playerId, (k, v) -> {
            if (v == null) v = new PlayerTensionData();
            v.tension = Math.min(100, v.tension + amount);
            v.lastUpdate = System.currentTimeMillis();
            return v;
        });
    }
    
    public void decrementTension(UUID playerId, int amount) {
        playerData.compute(playerId, (k, v) -> {
            if (v == null) v = new PlayerTensionData();
            v.tension = Math.max(0, v.tension - amount);
            v.lastUpdate = System.currentTimeMillis();
            return v;
        });
    }
    
    public double getPlayerTension(UUID playerId) {
        PlayerTensionData data = playerData.get(playerId);
        if (data == null) return 0;
        
        long timeDiff = System.currentTimeMillis() - data.lastUpdate;
        double decay = Math.min(timeDiff / 300000.0, 1.0) * 20;
        
        return Math.max(0, data.tension - decay);
    }
    
    public void resetAllTension() {
        playerData.clear();
    }
    
    public void amplifyGlobalTension(int amount) {
        playerData.values().forEach(data -> {
            data.tension = Math.min(100, data.tension + amount);
            data.lastUpdate = System.currentTimeMillis();
        });
    }
    
    private static class PlayerTensionData {
        double tension = 0;
        long lastUpdate = System.currentTimeMillis();
    }
}