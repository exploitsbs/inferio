 
package me.asher.inferio.core;

import me.asher.inferio.Inferio;
import me.asher.inferio.entities.InferioEntity;
import me.asher.inferio.utils.LocationCalculator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class EntityManager {
    private final Inferio plugin;
    private final List<InferioEntity> activeEntities = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Player, Long> lastEncounter = new ConcurrentHashMap<>();
    private final LocationCalculator locationCalc;
    private final int maxEntities;
    private final long minEncounterInterval;

    public EntityManager(Inferio plugin) {
        this.plugin = plugin;
        this.locationCalc = new LocationCalculator();
        this.maxEntities = 4;
        this.minEncounterInterval = 120000L;
    }

    public void scheduleEncounter(Player target) {
        if (!canScheduleEncounter(target)) return;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline() && plugin.isSystemActive()) {
                    createEncounter(target);
                }
            }
        }.runTaskLater(plugin, calculateDelay());
    }

    private boolean canScheduleEncounter(Player target) {
        if (activeEntities.size() >= maxEntities) return false;
        
        Long lastTime = lastEncounter.get(target);
        if (lastTime != null && (System.currentTimeMillis() - lastTime) < minEncounterInterval) {
            return false;
        }
        
        return true;
    }

    private void createEncounter(Player target) {
        Location spawnLoc = locationCalc.findOptimalSpawnLocation(target);
        if (spawnLoc == null) return;

        InferioEntity entity = new InferioEntity(plugin, spawnLoc, target);
        if (entity.spawn()) {
            activeEntities.add(entity);
            lastEncounter.put(target, System.currentTimeMillis());
            
            scheduleDespawn(entity);
            
            int tension = plugin.getAtmosphereEngine().getGlobalTension();
            plugin.getAtmosphereEngine().getStateTracker().incrementTension(
                target.getUniqueId(), 
                5 + (tension / 20)
            );
        }
    }

    private void scheduleDespawn(InferioEntity entity) {
        int lifetime = calculateEntityLifetime();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeEntities.contains(entity)) {
                    entity.despawn();
                    activeEntities.remove(entity);
                }
            }
        }.runTaskLater(plugin, lifetime);
    }

    private long calculateDelay() {
        int tension = plugin.getAtmosphereEngine().getGlobalTension();
        long baseDelay = 600L;
        long variableDelay = ThreadLocalRandom.current().nextLong(200L, 800L);
        double tensionMultiplier = 1.0 - (tension / 150.0);
        
        return (long) ((baseDelay + variableDelay) * Math.max(0.2, tensionMultiplier));
    }

    private int calculateEntityLifetime() {
        int baseTicks = 400;
        int variableTicks = ThreadLocalRandom.current().nextInt(200, 600);
        int tension = plugin.getAtmosphereEngine().getGlobalTension();
        
        return (int) ((baseTicks + variableTicks) * (1.0 + tension / 200.0));
    }

    public void onEntityInteraction(InferioEntity entity, Player player) {
        if (activeEntities.contains(entity)) {
            plugin.getAtmosphereEngine().getStateTracker().incrementTension(
                player.getUniqueId(), 
                15 + ThreadLocalRandom.current().nextInt(10)
            );
            
            entity.triggerInteractionEffect(player);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (activeEntities.contains(entity)) {
                        entity.despawn();
                        activeEntities.remove(entity);
                    }
                }
            }.runTaskLater(plugin, 40L);
        }
    }

    public void cleanup() {
        activeEntities.forEach(InferioEntity::despawn);
        activeEntities.clear();
        lastEncounter.clear();
    }

    public List<InferioEntity> getActiveEntities() {
        return List.copyOf(activeEntities);
    }

    public boolean hasActiveEntity(Player player) {
        return activeEntities.stream()
                .anyMatch(e -> e.getTarget().equals(player));
    }

    public void forceCleanupPlayer(Player player) {
        activeEntities.removeIf(entity -> {
            if (entity.getTarget().equals(player)) {
                entity.despawn();
                return true;
            }
            return false;
        });
        lastEncounter.remove(player);
    }
}