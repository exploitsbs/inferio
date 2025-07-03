 
package me.asher.inferio.effects;

import me.asher.inferio.Inferio;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class ParticleWeaver {
    private final Inferio plugin;
    
    public ParticleWeaver(Inferio plugin) {
        this.plugin = plugin;
    }
    
    public void createCinematicEffect(Player player, int intensityLevel) {
        int effectType = ThreadLocalRandom.current().nextInt(6);
        
        switch (effectType) {
            case 0 -> createSoulVortex(player, intensityLevel);
            case 1 -> createShadowTrail(player, intensityLevel);
            case 2 -> createPortalRifts(player, intensityLevel);
            case 3 -> createAshenRain(player, intensityLevel);
            case 4 -> createSculkPulse(player, intensityLevel);
            case 5 -> createBloodMist(player, intensityLevel);
        }
    }
    
    public void createSubtleAmbient(Player player, int intensityLevel) {
        Location playerLoc = player.getLocation();
        
        if (ThreadLocalRandom.current().nextDouble() < 0.6) {
            createFloatingSouls(playerLoc);
        } else {
            createDistantSmoke(playerLoc);
        }
    }
    
    private void createSoulVortex(Player player, int intensityLevel) {
        Location center = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20,
            ThreadLocalRandom.current().nextDouble() * 5,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20
        );
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 80 + (intensityLevel * 20);
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                double radius = 3.0 + (ticks * 0.05);
                for (int i = 0; i < 6; i++) {
                    double angle = (ticks * 0.3) + (i * Math.PI / 3);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(ticks * 0.1) * 2;
                    
                    Location particleLoc = center.clone().add(x, y, z);
                    player.spawnParticle(Particle.SOUL, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createShadowTrail(Player player, int intensityLevel) {
        Location start = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 25,
            5 + ThreadLocalRandom.current().nextDouble() * 10,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 25
        );
        
        Location end = start.clone().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 30,
            -ThreadLocalRandom.current().nextDouble() * 8,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 30
        );
        
        new BukkitRunnable() {
            double progress = 0;
            
            @Override
            public void run() {
                if (progress >= 1.0) {
                    cancel();
                    return;
                }
                
                Location current = start.clone().add(
                    (end.getX() - start.getX()) * progress,
                    (end.getY() - start.getY()) * progress,
                    (end.getZ() - start.getZ()) * progress
                );
                
                player.spawnParticle(Particle.LARGE_SMOKE, current, 3, 0.2, 0.2, 0.2, 0.02);
                player.spawnParticle(Particle.ASH, current, 2, 0.1, 0.1, 0.1, 0.01);
                
                progress += 0.05;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createPortalRifts(Player player, int intensityLevel) {
        for (int i = 0; i < 2 + intensityLevel; i++) {
            Location riftLoc = player.getLocation().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 30,
                ThreadLocalRandom.current().nextDouble() * 8,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 30
            );
            
            createSingleRift(player, riftLoc, intensityLevel);
        }
    }
    
    private void createSingleRift(Player player, Location center, int intensityLevel) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                double intensity = Math.sin((ticks / (double) maxTicks) * Math.PI);
                int particleCount = (int) (intensity * (10 + intensityLevel * 5));
                
                player.spawnParticle(Particle.PORTAL, center, particleCount, 1.5, 2.0, 1.5, 0.5);
                
                if (ticks % 10 == 0) {
                    player.spawnParticle(Particle.END_ROD, center, 3, 0.5, 0.5, 0.5, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createAshenRain(Player player, int intensityLevel) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < 15 + (intensityLevel * 5); i++) {
                    Location ashLoc = player.getLocation().add(
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 40,
                        15 + ThreadLocalRandom.current().nextDouble() * 10,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 40
                    );
                    
                    player.spawnParticle(Particle.ASH, ashLoc, 1, 0, 0, 0, 0);
                    
                    if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                        player.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, ashLoc, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    private void createSculkPulse(Player player, int intensityLevel) {
        Location center = player.getLocation().add(0, -2, 0);
        
        new BukkitRunnable() {
            double radius = 0;
            final double maxRadius = 15 + (intensityLevel * 5);
            
            @Override
            public void run() {
                if (radius >= maxRadius) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i) / 20;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = center.clone().add(x, 0, z);
                    player.spawnParticle(Particle.SCULK_SOUL, particleLoc, 2, 0.2, 0.2, 0.2, 0.01);
                    
                    if (radius > 5 && ThreadLocalRandom.current().nextDouble() < 0.4) {
                        player.spawnParticle(Particle.SCULK_CHARGE_POP, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
                
                radius += 0.8;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createBloodMist(Player player, int intensityLevel) {
        Location center = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 15,
            1 + ThreadLocalRandom.current().nextDouble() * 3,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 15
        );
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 80;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                double expansion = (ticks / (double) maxTicks) * 4;
                
                for (int i = 0; i < 8 + intensityLevel * 2; i++) {
                    Location mistLoc = center.clone().add(
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * expansion,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * 2,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * expansion
                    );
                    
                    player.spawnParticle(
                        Particle.DUST,
                        mistLoc, 1, 0.1, 0.1, 0.1, 0,
                        new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2f)
                    );
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createFloatingSouls(Location center) {
        for (int i = 0; i < 3; i++) {
            Location soulLoc = center.clone().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 12,
                ThreadLocalRandom.current().nextDouble() * 4,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 12
            );
            
            center.getWorld().spawnParticle(Particle.SOUL, soulLoc, 2, 0.2, 0.2, 0.2, 0.01);
        }
    }
    
    private void createDistantSmoke(Location center) {
        Location smokeLoc = center.clone().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20,
            0,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20
        );
        
        center.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, smokeLoc, 5, 0.5, 1.0, 0.5, 0.02);
    }
    
    public void cleanup() {}
}