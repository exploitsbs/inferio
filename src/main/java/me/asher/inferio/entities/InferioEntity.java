 
package me.asher.inferio.entities;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import me.asher.inferio.Inferio;
import me.asher.inferio.effects.CinematicEffects;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class InferioEntity {
    private final Inferio plugin;
    private final Location location;
    private final Player target;
    private Npc npcEntity;
    private boolean spawned = false;
    private BukkitTask behaviorTask;
    private BukkitTask watcherTask;
    private final CinematicEffects fx;
    private int behaviorState = 0;
    private long stateChangeTime = System.currentTimeMillis();

    public InferioEntity(Inferio plugin, Location location, Player target) {
        this.plugin = plugin;
        this.location = location.clone();
        this.target = target;
        this.fx = new CinematicEffects(plugin);
    }

    public boolean spawn() {
        try {
            NpcData data = createNpcData();
            npcEntity = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
            
            if (npcEntity == null) return false;
            
            npcEntity.setSaveToFile(false);
            npcEntity.create();
            npcEntity.spawnForAll();
            
            spawned = true;
            startBehaviorCycles();
            fx.playSpawnEffect(location, target);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn entity: " + e.getMessage());
            return false;
        }
    }

    private NpcData createNpcData() {
        NpcData data = new NpcData(
            "inferio_" + System.nanoTime(),
            UUID.randomUUID(),
            location
        );
        
        data.setSkin("https://i.imgur.com/QdRh0LS.png");
        data.setDisplayName("");
        data.setShowInTab(false);
        data.setCollidable(false);
        data.setTurnToPlayer(false);
        data.setInteractionCooldown(0f);
        
        return data;
    }

    private void startBehaviorCycles() {
        behaviorTask = new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!spawned || npcEntity == null) {
                    cancel();
                    return;
                }
                
                tick++;
                updateBehaviorState();
                executeBehavior(tick);
                updateEntityLook();
            }
        }.runTaskTimer(plugin, 0L, 2L);

        watcherTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!spawned || !target.isOnline()) {
                    cancel();
                    return;
                }
                
                checkPlayerProximity();
                checkPlayerGaze();
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }

    private void updateBehaviorState() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - stateChangeTime > getBehaviorDuration()) {
            behaviorState = (behaviorState + 1) % 4;
            stateChangeTime = currentTime;
        }
    }

    private long getBehaviorDuration() {
        return switch (behaviorState) {
            case 0 -> 3000L + ThreadLocalRandom.current().nextLong(2000L);
            case 1 -> 2000L + ThreadLocalRandom.current().nextLong(1500L);
            case 2 -> 4000L + ThreadLocalRandom.current().nextLong(3000L);
            case 3 -> 1500L + ThreadLocalRandom.current().nextLong(1000L);
            default -> 2000L;
        };
    }

    private void executeBehavior(int tick) {
        switch (behaviorState) {
            case 0 -> executeStalkingBehavior(tick);
            case 1 -> executeObservingBehavior(tick);
            case 2 -> executeManifestingBehavior(tick);
            case 3 -> executeWithdrawingBehavior(tick);
        }
    }

    private void executeStalkingBehavior(int tick) {
        if (tick % 40 == 0) {
            fx.createStalkingEffect(location, target);
        }
        
        if (tick % 100 == 0 && ThreadLocalRandom.current().nextDouble() < 0.3) {
            target.playSound(target.getLocation(), Sound.AMBIENT_CAVE, 0.2f, 0.7f);
        }
    }

    private void executeObservingBehavior(int tick) {
        if (tick % 20 == 0) {
            location.getWorld().spawnParticle(
                Particle.SOUL,
                location.clone().add(0, 1.8, 0),
                2, 0.1, 0.1, 0.1, 0.01
            );
        }
    }

    private void executeManifestingBehavior(int tick) {
        if (tick % 30 == 0) {
            fx.createManifestationEffect(location, target);
        }
        
        if (tick % 80 == 0 && ThreadLocalRandom.current().nextDouble() < 0.4) {
            target.playSound(location, Sound.ENTITY_ENDERMAN_STARE, 0.3f, 0.5f);
        }
    }

    private void executeWithdrawingBehavior(int tick) {
        if (tick % 60 == 0) {
            fx.createWithdrawalEffect(location, target);
        }
    }

    private void updateEntityLook() {
        if (target.getGameMode() == GameMode.SPECTATOR) return;
        
        double distance = target.getLocation().distance(location);
        if (distance > 60) return;
        
        lookAtTarget();
    }

    private void lookAtTarget() {
        Vector direction = target.getEyeLocation().toVector()
                .subtract(location.toVector()).normalize();
        
        double dx = direction.getX();
        double dy = direction.getY();
        double dz = direction.getZ();
        
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, distanceXZ));
        
        pitch = Math.max(-90f, Math.min(90f, pitch));
        
        Location newLoc = location.clone();
        newLoc.setYaw(yaw);
        newLoc.setPitch(pitch);
        
        npcEntity.getData().setLocation(newLoc);
        npcEntity.updateForAll();
    }

    private void checkPlayerProximity() {
        double distance = target.getLocation().distance(location);
        
        if (distance < 3.0) {
            triggerProximityEvent();
        } else if (distance > 50.0) {
            despawn();
        }
    }

    private void checkPlayerGaze() {
        if (isPlayerLookingAt()) {
            triggerGazeEvent();
        }
    }

    private boolean isPlayerLookingAt() {
        Location eyeLoc = target.getEyeLocation();
        Vector eyeDir = eyeLoc.getDirection().normalize();
        Vector toEntity = location.clone().add(0, 1, 0).toVector()
                .subtract(eyeLoc.toVector()).normalize();
        
        double distance = eyeLoc.distance(location);
        if (distance > 40 || distance < 1) return false;
        
        return eyeDir.dot(toEntity) > 0.95;
    }

    private void triggerProximityEvent() {
        fx.playProximityEffect(location, target);
        plugin.getEntityManager().onEntityInteraction(this, target);
    }

    private void triggerGazeEvent() {
        fx.playGazeEffect(location, target);
        plugin.getEntityManager().onEntityInteraction(this, target);
    }

    public void triggerInteractionEffect(Player player) {
        fx.playInteractionEffect(location, player);
    }

    public void despawn() {
        if (spawned) {
            if (behaviorTask != null) behaviorTask.cancel();
            if (watcherTask != null) watcherTask.cancel();
            
            if (npcEntity != null) {
                fx.playDespawnEffect(location, target);
                npcEntity.removeForAll();
                npcEntity = null;
            }
            
            spawned = false;
        }
    }

    public boolean isSpawned() { return spawned && npcEntity != null; }
    public Location getLocation() { return location.clone(); }
    public Player getTarget() { return target; }
    public Npc getNpc() { return npcEntity; }
}