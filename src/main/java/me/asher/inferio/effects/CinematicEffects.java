 
package me.asher.inferio.effects;

import me.asher.inferio.Inferio;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class CinematicEffects {
    private final Inferio plugin;

    public CinematicEffects(Inferio plugin) {
        this.plugin = plugin;
    }

    public void playSpawnEffect(Location location, Player target) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }
                
                Location effectLoc = location.clone().add(0, 1, 0);
                double progress = ticks / 60.0;
                
                if (ticks < 30) {
                    double radius = 2.0 - (progress * 1.5);
                    createSpiralEffect(effectLoc, radius, ticks * 0.3);
                } else {
                    createMaterializationEffect(effectLoc, progress - 0.5);
                }
                
                if (ticks == 20) {
                    target.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.3f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void playDespawnEffect(Location location, Player target) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                Location effectLoc = location.clone().add(0, 1, 0);
                double progress = ticks / 40.0;
                
                createDisintegrationEffect(effectLoc, progress);
                
                if (ticks == 10) {
                    target.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 0.8f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void playGazeEffect(Location location, Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.7f, 0.4f);
        target.playSound(location, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.4f, 0.1f);
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                Location effectLoc = location.clone().add(0, 1.8, 0);
                createGazeParticles(effectLoc, target.getEyeLocation());
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void playProximityEffect(Location location, Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 0.2f);
        target.playSound(location, Sound.ENTITY_GHAST_SCREAM, 0.3f, 0.1f);
        
        Location effectLoc = location.clone().add(0, 1, 0);
        location.getWorld().spawnParticle(
            Particle.SOUL_FIRE_FLAME,
            effectLoc, 30, 0.5, 1.0, 0.5, 0.1
        );
        
        createShockwave(effectLoc, 3.0);
    }

    public void playInteractionEffect(Location location, Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.5f);
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                Location effectLoc = location.clone().add(0, 1, 0);
                createInteractionBurst(effectLoc, ticks);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void createStalkingEffect(Location location, Player target) {
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            Location effectLoc = location.clone().add(0, 1, 0);
            location.getWorld().spawnParticle(
                Particle.SOUL,
                effectLoc, 3, 0.2, 0.3, 0.2, 0.01
            );
        }
    }

    public void createManifestationEffect(Location location, Player target) {
        Location effectLoc = location.clone().add(0, 1, 0);
        location.getWorld().spawnParticle(
            Particle.SMOKE,
            effectLoc, 5, 0.3, 0.5, 0.3, 0.02
        );
        
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            location.getWorld().spawnParticle(
                Particle.PORTAL,
                effectLoc, 10, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    public void createWithdrawalEffect(Location location, Player target) {
        Location effectLoc = location.clone().add(0, 1, 0);
        location.getWorld().spawnParticle(
            Particle.ASH,
            effectLoc, 8, 0.4, 0.4, 0.4, 0.01
        );
    }

    private void createSpiralEffect(Location center, double radius, double angle) {
        for (int i = 0; i < 8; i++) {
            double currentAngle = angle + (i * Math.PI / 4);
            double x = Math.cos(currentAngle) * radius;
            double z = Math.sin(currentAngle) * radius;
            
            Location particleLoc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(
                Particle.SOUL,
                particleLoc, 1, 0.1, 0.1, 0.1, 0.01
            );
        }
    }

    private void createMaterializationEffect(Location center, double progress) {
        int particleCount = (int) (progress * 20);
        center.getWorld().spawnParticle(
            Particle.PORTAL,
            center, particleCount, 0.3, 0.5, 0.3, 0.1
        );
        
        center.getWorld().spawnParticle(
            Particle.SMOKE,
            center, particleCount / 2, 0.2, 0.3, 0.2, 0.05
        );
    }

    private void createDisintegrationEffect(Location center, double progress) {
        int particleCount = (int) ((1.0 - progress) * 15);
        center.getWorld().spawnParticle(
            Particle.ASH,
            center, particleCount, 0.5, 0.5, 0.5, 0.1
        );
        
        center.getWorld().spawnParticle(
            Particle.SOUL,
            center, particleCount / 2, 0.3, 0.3, 0.3, 0.05
        );
    }

    private void createGazeParticles(Location entityLoc, Location playerLoc) {
        double distance = entityLoc.distance(playerLoc);
        int steps = (int) (distance * 3);
        
        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            Location particleLoc = entityLoc.clone().add(
                (playerLoc.getX() - entityLoc.getX()) * progress,
                (playerLoc.getY() - entityLoc.getY()) * progress,
                (playerLoc.getZ() - entityLoc.getZ()) * progress
            );
            
            entityLoc.getWorld().spawnParticle(
                Particle.DUST,
                particleLoc, 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 0.5f)
            );
        }
    }

    private void createShockwave(Location center, double maxRadius) {
        new BukkitRunnable() {
            double radius = 0;
            
            @Override
            public void run() {
                if (radius >= maxRadius) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < 16; i++) {
                    double angle = (2 * Math.PI * i) / 16;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = center.clone().add(x, 0, z);
                    center.getWorld().spawnParticle(
                        Particle.DUST,
                        particleLoc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 0, 0), 1.0f)
                    );
                }
                
                radius += 0.3;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void createInteractionBurst(Location center, int tick) {
        double intensity = 1.0 - (tick / 20.0);
        int particleCount = (int) (intensity * 25);
        
        center.getWorld().spawnParticle(
            Particle.SOUL_FIRE_FLAME,
            center, particleCount, 0.8, 0.8, 0.8, 0.2
        );
        
        center.getWorld().spawnParticle(
            Particle.LARGE_SMOKE,
            center, particleCount / 2, 0.5, 0.5, 0.5, 0.1
        );
    }
}