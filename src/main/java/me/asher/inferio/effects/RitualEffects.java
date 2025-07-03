 
package me.asher.inferio.effects;

import me.asher.inferio.Inferio;
import me.asher.inferio.core.RitualSystem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class RitualEffects {
    private final Inferio plugin;
    
    public RitualEffects(Inferio plugin) {
        this.plugin = plugin;
    }
    
    public void playRitualStart(Location center, Object ritualType) {
        String type = ritualType.toString();
        
        switch (type) {
            case "AWAKENING" -> playAwakeningStart(center);
            case "BANISHMENT" -> playBanishmentStart(center);
            case "AMPLIFICATION" -> playAmplificationStart(center);
        }
    }
    
    public void playRitualProgress(Location center, Object ritualType, int currentTick, int maxTicks) {
        String type = ritualType.toString();
        double progress = (double) currentTick / maxTicks;
        
        switch (type) {
            case "AWAKENING" -> playAwakeningProgress(center, progress);
            case "BANISHMENT" -> playBanishmentProgress(center, progress);
            case "AMPLIFICATION" -> playAmplificationProgress(center, progress);
        }
    }
    
    public void playRitualComplete(Location center, Object ritualType) {
        String type = ritualType.toString();
        
        switch (type) {
            case "AWAKENING" -> playAwakeningComplete(center);
            case "BANISHMENT" -> playBanishmentComplete(center);
            case "AMPLIFICATION" -> playAmplificationComplete(center);
        }
    }
    
    private void playAwakeningStart(Location center) {
        center.getWorld().strikeLightningEffect(center);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 2.0f, 0.3f);
        center.getWorld().playSound(center, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.5f);
        center.getWorld().playSound(center, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 2.0f, 0.4f);
        
        createDarkVortex(center, 80);
    }
    
    private void playAwakeningProgress(Location center, double progress) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        
        double angle = progress * 6 * Math.PI;
        double radius = 2.5 - (progress * 1.5);
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        double y = progress * 0.08;
        
        Location particleLoc = effectLoc.clone().add(x, y, z);
        center.getWorld().spawnParticle(Particle.SOUL, particleLoc, 4, 0.2, 0.2, 0.2, 0.02);
        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
        
        if (ThreadLocalRandom.current().nextDouble() < progress * 0.3) {
            center.getWorld().spawnParticle(Particle.PORTAL, effectLoc, 15, 1.0, 1.0, 1.0, 0.3);
        }
    }
    
    private void playAwakeningComplete(Location center) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        
        center.getWorld().playSound(effectLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        center.getWorld().playSound(effectLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        
        createExplosiveEffect(effectLoc, Color.fromRGB(139, 0, 0), 100);
        createShockwaveEffect(effectLoc, 8.0);
    }
    
    private void playBanishmentStart(Location center) {
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.5f);
        center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        
        createLightPillar(center, 60);
    }
    
    private void playBanishmentProgress(Location center, double progress) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        double radius = progress * 4.0;
        
        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI * i) / 12;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = effectLoc.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.4, 0.2, 0.03);
            center.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 8, 0.3, 0.5, 0.3, 0.15);
        }
        
        if (progress > 0.5) {
            center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, effectLoc, 5, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    private void playBanishmentComplete(Location center) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        
        center.getWorld().playSound(effectLoc, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.0f);
        center.getWorld().playSound(effectLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.8f);
        
        createPurificationEffect(effectLoc, 80);
    }
    
    private void playAmplificationStart(Location center) {
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.6f);
        center.getWorld().playSound(center, Sound.ENTITY_VEX_CHARGE, 1.0f, 0.4f);
        
        createCorruptionField(center, 60);
    }
    
    private void playAmplificationProgress(Location center, double progress) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        
        double spiralRadius = 1.5 + (progress * 2.0);
        double spiralHeight = progress * 4.0;
        
        for (int i = 0; i < 8; i++) {
            double angle = (progress * 8 * Math.PI) + (i * Math.PI / 4);
            double x = Math.cos(angle) * spiralRadius;
            double z = Math.sin(angle) * spiralRadius;
            double y = spiralHeight + Math.sin(angle * 3) * 0.5;
            
            Location particleLoc = effectLoc.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.WITCH, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
            center.getWorld().spawnParticle(
                Particle.DUST,
                particleLoc, 1, 0.1, 0.1, 0.1, 0,
                new Particle.DustOptions(Color.fromRGB(75, 0, 130), 1.2f)
            );
        }
    }
    
    private void playAmplificationComplete(Location center) {
        Location effectLoc = center.clone().add(0.5, 1.5, 0.5);
        
        center.getWorld().playSound(effectLoc, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);
        center.getWorld().playSound(effectLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.5f);
        
        createCorruptionBurst(effectLoc, 120);
    }
    
    private void createDarkVortex(Location center, int duration) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                double progress = (double) ticks / duration;
                double radius = 3.0 - (progress * 2.0);
                
                for (int i = 0; i < 16; i++) {
                    double angle = (ticks * 0.4) + (i * Math.PI / 8);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(ticks * 0.2) * 1.5;
                    
                    Location particleLoc = center.clone().add(x, y + 1.5, z);
                    center.getWorld().spawnParticle(Particle.SOUL, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                    
                    if (i % 4 == 0) {
                        center.getWorld().spawnParticle(Particle.LARGE_SMOKE, particleLoc, 1, 0.2, 0.2, 0.2, 0.02);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createLightPillar(Location center, int duration) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                for (int y = 0; y < 8; y++) {
                    Location particleLoc = center.clone().add(0.5, y + 1, 0.5);
                    center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 3, 0.2, 0.1, 0.2, 0.02);
                    
                    if (y % 2 == 0) {
                        center.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 5, 0.3, 0.1, 0.3, 0.1);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createCorruptionField(Location center, int duration) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                double radius = 2.0 + (ticks * 0.05);
                
                for (int i = 0; i < 12; i++) {
                    double angle = (2 * Math.PI * i) / 12;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = center.clone().add(x, 0.5, z);
                    center.getWorld().spawnParticle(Particle.WITCH, particleLoc, 2, 0.2, 0.2, 0.2, 0.02);
                    
                    if (ticks % 10 == 0) {
                        center.getWorld().spawnParticle(
                            Particle.DUST,
                            particleLoc, 3, 0.2, 0.2, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(139, 0, 139), 1.0f)
                        );
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createExplosiveEffect(Location center, Color color, int particles) {
        center.getWorld().spawnParticle(
            Particle.DUST,
            center, particles, 2.0, 2.0, 2.0, 0,
            new Particle.DustOptions(color, 2.0f)
        );
        
        center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center, particles / 2, 1.5, 1.5, 1.5, 0.1);
        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, particles / 3, 1.0, 1.0, 1.0, 0.15);
    }
    
    private void createShockwaveEffect(Location center, double maxRadius) {
        new BukkitRunnable() {
            double radius = 0;
            
            @Override
            public void run() {
                if (radius >= maxRadius) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < 24; i++) {
                    double angle = (2 * Math.PI * i) / 24;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = center.clone().add(x, 0, z);
                    center.getWorld().spawnParticle(
                        Particle.DUST,
                        particleLoc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f)
                    );
                }
                
                radius += 0.4;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createPurificationEffect(Location center, int duration) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                double height = (ticks / 10.0) % 6;
                Location particleLoc = center.clone().add(0, height, 0);
                
                center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, particleLoc, 8, 0.5, 0.2, 0.5, 0.1);
                center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.3, 0.1, 0.3, 0.05);
                
                if (ticks % 20 == 0) {
                    center.getWorld().spawnParticle(Particle.FIREWORK, center, 10, 1.0, 1.0, 1.0, 0.2);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createCorruptionBurst(Location center, int duration) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                double expansion = (ticks / 20.0) % 4;
                
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i) / 20;
                    double x = Math.cos(angle) * expansion;
                    double z = Math.sin(angle) * expansion;
                    
                    Location particleLoc = center.clone().add(x, 0.5, z);
                    center.getWorld().spawnParticle(Particle.WITCH, particleLoc, 3, 0.2, 0.2, 0.2, 0.02);
                    
                    if (ticks % 5 == 0) {
                        center.getWorld().spawnParticle(
                            Particle.DUST,
                            particleLoc, 2, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(75, 0, 130), 1.3f)
                        );
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    public void cleanup() {}
}