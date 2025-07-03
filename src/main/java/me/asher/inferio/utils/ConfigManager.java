 
package me.asher.inferio.utils;

import me.asher.inferio.Inferio;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Inferio plugin;
    private FileConfiguration config;
    
    public ConfigManager(Inferio plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public double getEntitySpawnChance() {
        return config.getDouble("entity.spawn_chance", 0.02);
    }
    
    public int getMaxEntities() {
        return config.getInt("entity.max_concurrent", 3);
    }
    
    public long getMinEncounterInterval() {
        return config.getLong("entity.min_interval", 120000);
    }
    
    public boolean isWorldEnabled(String worldName) {
        return config.getStringList("enabled_worlds").contains(worldName);
    }
    
    public int getGlobalTensionDecay() {
        return config.getInt("tension.global_decay", 5);
    }
    
    public int getPlayerTensionDecay() {
        return config.getInt("tension.player_decay", 20);
    }
}