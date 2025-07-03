 
package me.asher.inferio.effects;

import me.asher.inferio.Inferio;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class EnvironmentalDistortion {
    private final Inferio plugin;
    
    public EnvironmentalDistortion(Inferio plugin) {
        this.plugin = plugin;
    }
    
    public void applyEnvironmentalEffect(Player player, int intensityLevel) {
        int effectType = ThreadLocalRandom.current().nextInt(5);
        
        switch (effectType) {
            case 0 -> createRedstoneFalter(player, intensityLevel);
            case 1 -> createEnvironmentalSounds(player, intensityLevel);
            case 2 -> createGroundDisturbance(player, intensityLevel);
            case 3 -> createAtmosphericPressure(player, intensityLevel);
            case 4 -> createTemperatureShift(player, intensityLevel);
        }
    }
    
    public void createMinorDistortion(Player player) {
        if (ThreadLocalRandom.current().nextDouble() < 0.6) {
            createSubtleFlicker(player);
        } else {
            createDistantEcho(player);
        }
    }
    
    private void createRedstoneFalter(Player player, int intensityLevel) {
        Location center = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 15,
            ThreadLocalRandom.current().nextDouble() * 3,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 15
        );
        
        player.playSound(center, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.6f, 0.7f);
        
        new BukkitRunnable() {
            int flickers = 0;
            final int maxFlickers = 3 + intensityLevel;
            
            @Override
            public void run() {
                if (flickers >= maxFlickers) {
                    cancel();
                    return;
                }
                
                player.spawnParticle(
                    Particle.DUST,
                    center, 8, 0.5, 0.5, 0.5, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5f)
                );
                
                if (flickers % 2 == 0) {
                    player.playSound(center, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.3f, 0.8f);
                }
                
                flickers++;
            }
        }.runTaskTimer(plugin, 0L, 8L);
    }
    
    private void createEnvironmentalSounds(Player player, int intensityLevel) {
        Sound[] envSounds = {
            Sound.BLOCK_WOODEN_DOOR_OPEN, Sound.BLOCK_WOODEN_DOOR_CLOSE,
            Sound.BLOCK_FENCE_GATE_OPEN, Sound.BLOCK_FENCE_GATE_CLOSE,
            Sound.BLOCK_CHEST_OPEN, Sound.BLOCK_CHEST_CLOSE,
            Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE,
            Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF
        };
        
        int soundCount = 1 + intensityLevel;
        
        for (int i = 0; i < soundCount; i++) {
            Sound sound = envSounds[ThreadLocalRandom.current().nextInt(envSounds.length)];
            Location soundLoc = player.getLocation().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 25,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 8,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 25
            );
            
            final int delay = i * 10 + ThreadLocalRandom.current().nextInt(20);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.playSound(soundLoc, sound, 0.5f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
                }
            }, delay);
        }
    }
    
    private void createGroundDisturbance(Player player, int intensityLevel) {
        for (int i = 0; i < 2 + intensityLevel; i++) {
            Location disturbLoc = player.getLocation().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 20,
                -1,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 20
            );
            
            disturbLoc = findGroundLevel(disturbLoc);
            if (disturbLoc != null) {
                createSingleDisturbance(player, disturbLoc);
            }
        }
    }
    
    private void createSingleDisturbance(Player player, Location center) {
        player.playSound(center, Sound.BLOCK_GRAVEL_BREAK, 0.4f, 0.6f);
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                double intensity = Math.sin((ticks / (double) maxTicks) * Math.PI);
                int particleCount = (int) (intensity * 12);
                
                player.spawnParticle(Particle.CLOUD, center, particleCount, 0.8, 0.3, 0.8, 0.05);
                player.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center.clone().add(0, 0.5, 0), 
                    particleCount / 2, 0.5, 0.2, 0.5, 0.02);
                
                if (ticks % 15 == 0) {
                    player.playSound(center, Sound.BLOCK_GRAVEL_STEP, 0.2f, 0.7f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createAtmosphericPressure(Player player, int intensityLevel) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.3f, 0.4f);
        
        new BukkitRunnable() {
            int pulses = 0;
            final int maxPulses = 4 + intensityLevel;
            
            @Override
            public void run() {
                if (pulses >= maxPulses) {
                    cancel();
                    return;
                }
                
                Location playerLoc = player.getLocation();
                
                for (int i = 0; i < 8; i++) {
                    double angle = (2 * Math.PI * i) / 8;
                    double radius = 3 + (pulses * 0.5);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = playerLoc.clone().add(x, 1, z);
                    player.spawnParticle(Particle.SMOKE, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
                }
                
                if (pulses % 2 == 0) {
                    player.playSound(playerLoc, Sound.ENTITY_PLAYER_BREATH, 0.2f, 0.5f);
                }
                
                pulses++;
            }
        }.runTaskTimer(plugin, 0L, 15L);
    }
    
    private void createTemperatureShift(Player player, int intensityLevel) {
        boolean coldShift = ThreadLocalRandom.current().nextBoolean();
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                Location playerLoc = player.getLocation();
                
                if (coldShift) {
                    createColdEffect(player, playerLoc, ticks);
                } else {
                    createHeatEffect(player, playerLoc, ticks);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createColdEffect(Player player, Location center, int tick) {
        if (tick % 10 == 0) {
            player.playSound(center, Sound.BLOCK_POWDER_SNOW_BREAK, 0.2f, 0.8f);
        }
        
        for (int i = 0; i < 3; i++) {
            Location iceLoc = center.clone().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 6,
                ThreadLocalRandom.current().nextDouble() * 3,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 6
            );
            
            player.spawnParticle(
                Particle.DUST,
                iceLoc, 1, 0.1, 0.1, 0.1, 0,
                new Particle.DustOptions(Color.fromRGB(173, 216, 230), 1.0f)
            );
        }
    }
    
    private void createHeatEffect(Player player, Location center, int tick) {
        if (tick % 12 == 0) {
            player.playSound(center, Sound.BLOCK_FIRE_AMBIENT, 0.15f, 1.2f);
        }
        
        for (int i = 0; i < 4; i++) {
            Location heatLoc = center.clone().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 8,
                ThreadLocalRandom.current().nextDouble() * 2,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * 8
            );
            
            player.spawnParticle(Particle.FLAME, heatLoc, 1, 0.1, 0.1, 0.1, 0.01);
            
            if (ThreadLocalRandom.current().nextDouble() < 0.4) {
                player.spawnParticle(
                    Particle.DUST,
                    heatLoc, 1, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 69, 0), 0.8f)
                );
            }
        }
    }
    
    private void createSubtleFlicker(Player player) {
        Location flickerLoc = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 8,
            ThreadLocalRandom.current().nextDouble() * 2,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 8
        );
        
        player.playSound(flickerLoc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.2f, 0.9f);
        player.spawnParticle(
            Particle.DUST,
            flickerLoc, 3, 0.2, 0.2, 0.2, 0,
            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.7f)
        );
    }
    
    private void createDistantEcho(Player player) {
        Location echoLoc = player.getLocation().add(
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20,
            0,
            (ThreadLocalRandom.current().nextDouble() - 0.5) * 20
        );
        
        Sound[] echoSounds = {
            Sound.BLOCK_WOODEN_DOOR_CLOSE, Sound.BLOCK_CHEST_CLOSE,
            Sound.ENTITY_PLAYER_BREATH, Sound.BLOCK_GRAVEL_STEP
        };
        
        Sound sound = echoSounds[ThreadLocalRandom.current().nextInt(echoSounds.length)];
        player.playSound(echoLoc, sound, 0.3f, 0.6f);
    }
    
    private Location findGroundLevel(Location loc) {
        Location test = loc.clone();
        
        for (int i = 0; i < 15; i++) {
            if (test.getBlock().getType().isSolid()) {
                return test.add(0, 1, 0);
            }
            test.subtract(0, 1, 0);
        }
        
        return null;
    }
    
    public void cleanup() {}
}
