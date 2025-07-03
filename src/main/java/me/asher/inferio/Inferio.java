 
package me.asher.inferio;

import me.asher.inferio.commands.InferioCommand;
import me.asher.inferio.core.AtmosphereEngine;
import me.asher.inferio.core.EntityManager;
import me.asher.inferio.core.RitualSystem;
import me.asher.inferio.listeners.PlayerListener;
import me.asher.inferio.listeners.RitualListener;
import me.asher.inferio.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Inferio extends JavaPlugin {
    private static Inferio instance;
    private EntityManager entityManager;
    private AtmosphereEngine atmosphereEngine;
    private RitualSystem ritualSystem;
    private ConfigManager configManager;
    private boolean systemActive = false;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.entityManager = new EntityManager(this);
        this.atmosphereEngine = new AtmosphereEngine(this);
        this.ritualSystem = new RitualSystem(this);
        
        registerEvents();
        registerCommands();
        
        this.atmosphereEngine.initialize();
        getLogger().info("Inferio awakened. Reality bends...");
    }

    @Override
    public void onDisable() {
        if (atmosphereEngine != null) atmosphereEngine.shutdown();
        if (entityManager != null) entityManager.cleanup();
        if (ritualSystem != null) ritualSystem.shutdown();
        getLogger().info("Inferio sleeps. For now...");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new RitualListener(this), this);
    }

    private void registerCommands() {
        getCommand("inferio").setExecutor(new InferioCommand(this));
    }

    public static Inferio getInstance() { return instance; }
    public EntityManager getEntityManager() { return entityManager; }
    public AtmosphereEngine getAtmosphereEngine() { return atmosphereEngine; }
    public RitualSystem getRitualSystem() { return ritualSystem; }
    public ConfigManager getConfigManager() { return configManager; }
    public boolean isSystemActive() { return systemActive; }
    public void setSystemActive(boolean active) { this.systemActive = active; }
}