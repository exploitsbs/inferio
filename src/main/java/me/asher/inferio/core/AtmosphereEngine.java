 
package me.asher.inferio.core;

import me.asher.inferio.Inferio;
import me.asher.inferio.effects.AudioscapeManager;
import me.asher.inferio.effects.ParticleWeaver;
import me.asher.inferio.effects.EnvironmentalDistortion;
import me.asher.inferio.utils.PlayerStateTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AtmosphereEngine {
    private final Inferio plugin;
    private final AudioscapeManager audioscape;
    private final ParticleWeaver particles;
    private final EnvironmentalDistortion distortion;
    private final PlayerStateTracker stateTracker;
    private BukkitTask mainLoop;
    private BukkitTask ambientLoop;
    private int globalTension = 0;
    private final int[] tensionThresholds = {0, 25, 50, 75, 100};

    public AtmosphereEngine(Inferio plugin) {
        this.plugin = plugin;
        this.audioscape = new AudioscapeManager(plugin);
        this.particles = new ParticleWeaver(plugin);
        this.distortion = new EnvironmentalDistortion(plugin);
        this.stateTracker = new PlayerStateTracker();
    }

    public void initialize() {
        startMainLoop();
        startAmbientLoop();
    }

    private void startMainLoop() {
        mainLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isSystemActive()) return;
                
                List<Player> validTargets = getValidTargets();
                if (validTargets.isEmpty()) return;

                updateGlobalTension(validTargets);
                processAtmosphere(validTargets);
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private void startAmbientLoop() {
        ambientLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isSystemActive()) return;
                
                for (Player player : getValidTargets()) {
                    if (ThreadLocalRandom.current().nextDouble() < calculateAmbientChance(player)) {
                        executeAmbientEffect(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 80L);
    }

    private List<Player> getValidTargets() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.hasPermission("inferio.immune"))
                .filter(p -> p.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL)
                .collect(Collectors.toList());
    }

    private void updateGlobalTension(List<Player> players) {
        int targetTension = calculateTargetTension(players);
        globalTension += (targetTension - globalTension) * 0.1;
        globalTension = Math.max(0, Math.min(100, globalTension));
    }

    private int calculateTargetTension(List<Player> players) {
        if (players.isEmpty()) return 0;
        
        double avgTension = players.stream()
                .mapToDouble(p -> {
                    Location loc = p.getLocation();
                    double baseTension = isNightTime(p) ? 30 : 10;
                    baseTension += Math.max(0, 50 - loc.getY()) * 0.5;
                    baseTension += stateTracker.getPlayerTension(p.getUniqueId()) * 0.3;
                    return Math.min(100, baseTension);
                })
                .average().orElse(0);
        
        return (int) avgTension;
    }

    private void processAtmosphere(List<Player> players) {
        int intensityLevel = getTensionLevel();
        
        for (Player player : players) {
            double playerTension = stateTracker.getPlayerTension(player.getUniqueId());
            
            if (shouldTriggerEntityEvent(playerTension, intensityLevel)) {
                plugin.getEntityManager().scheduleEncounter(player);
            }
            
            if (shouldTriggerParticleEffect(playerTension)) {
                particles.createCinematicEffect(player, intensityLevel);
            }
            
            if (shouldTriggerAudioEffect(playerTension)) {
                audioscape.playAtmosphericSound(player, intensityLevel);
            }
            
            if (shouldTriggerDistortion(playerTension)) {
                distortion.applyEnvironmentalEffect(player, intensityLevel);
            }
        }
    }

    private void executeAmbientEffect(Player player) {
        int effectType = ThreadLocalRandom.current().nextInt(4);
        int intensity = getTensionLevel();
        
        switch (effectType) {
            case 0 -> particles.createSubtleAmbient(player, intensity);
            case 1 -> audioscape.playDistantSound(player, intensity);
            case 2 -> distortion.createMinorDistortion(player);
            case 3 -> stateTracker.incrementTension(player.getUniqueId(), 2);
        }
    }

    private boolean shouldTriggerEntityEvent(double tension, int level) {
        double baseChance = 0.02 + (level * 0.01);
        return ThreadLocalRandom.current().nextDouble() < baseChance * (1 + tension / 100);
    }

    private boolean shouldTriggerParticleEffect(double tension) {
        return ThreadLocalRandom.current().nextDouble() < 0.15 + (tension / 200);
    }

    private boolean shouldTriggerAudioEffect(double tension) {
        return ThreadLocalRandom.current().nextDouble() < 0.12 + (tension / 150);
    }

    private boolean shouldTriggerDistortion(double tension) {
        return ThreadLocalRandom.current().nextDouble() < 0.08 + (tension / 250);
    }

    private double calculateAmbientChance(Player player) {
        double base = 0.05;
        if (isNightTime(player)) base += 0.03;
        if (player.getLocation().getY() < 40) base += 0.02;
        return base + (stateTracker.getPlayerTension(player.getUniqueId()) / 500);
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time >= 13000 && time <= 23000;
    }

    private int getTensionLevel() {
        for (int i = tensionThresholds.length - 1; i >= 0; i--) {
            if (globalTension >= tensionThresholds[i]) return i;
        }
        return 0;
    }

    public void shutdown() {
        if (mainLoop != null) mainLoop.cancel();
        if (ambientLoop != null) ambientLoop.cancel();
        audioscape.cleanup();
        particles.cleanup();
        distortion.cleanup();
    }

    public PlayerStateTracker getStateTracker() { return stateTracker; }
    public int getGlobalTension() { return globalTension; }
}