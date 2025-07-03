 
package me.asher.inferio.effects;

import me.asher.inferio.Inferio;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class AudioscapeManager {
    private final Inferio plugin;
    private final Sound[] atmosphericSounds = {
        Sound.AMBIENT_CAVE, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD,
        Sound.AMBIENT_BASALT_DELTAS_MOOD, Sound.AMBIENT_CRIMSON_FOREST_MOOD,
        Sound.AMBIENT_NETHER_WASTES_MOOD, Sound.AMBIENT_WARPED_FOREST_MOOD,
        Sound.ENTITY_ENDERMAN_SCREAM, Sound.ENTITY_GHAST_SCREAM,
        Sound.ENTITY_VEX_AMBIENT, Sound.ENTITY_WITCH_AMBIENT,
        Sound.ENTITY_PHANTOM_AMBIENT, Sound.BLOCK_SCULK_SENSOR_CLICKING,
        Sound.ENTITY_WARDEN_HEARTBEAT, Sound.BLOCK_PORTAL_AMBIENT
    };
    
    private final Sound[] distantSounds = {
        Sound.BLOCK_WOODEN_DOOR_CLOSE, Sound.BLOCK_CHEST_CLOSE,
        Sound.BLOCK_FENCE_GATE_CLOSE, Sound.BLOCK_GRAVEL_BREAK,
        Sound.ENTITY_PLAYER_BREATH, Sound.BLOCK_REDSTONE_TORCH_BURNOUT
    };
    
    public AudioscapeManager(Inferio plugin) {
        this.plugin = plugin;
    }
    
    public void playAtmosphericSound(Player player, int intensityLevel) {
        Sound sound = atmosphericSounds[ThreadLocalRandom.current().nextInt(atmosphericSounds.length)];
        Location soundLoc = calculateSoundLocation(player);
        
        float volume = 0.2f + (intensityLevel * 0.15f) + ThreadLocalRandom.current().nextFloat() * 0.3f;
        float pitch = 0.4f + ThreadLocalRandom.current().nextFloat() * 0.8f;
        
        player.playSound(soundLoc, sound, volume, pitch);
        
        if (ThreadLocalRandom.current().nextDouble() < 0.25 + (intensityLevel * 0.1)) {
            scheduleEchoEffect(player, sound, soundLoc, volume * 0.6f, pitch * 0.9f);
        }
    }
    
    public void playDistantSound(Player player, int intensityLevel) {
        Sound sound = distantSounds[ThreadLocalRandom.current().nextInt(distantSounds.length)];
        Location soundLoc = calculateDistantLocation(player);
        
        float volume = 0.3f + (intensityLevel * 0.1f);
        float pitch = 0.7f + ThreadLocalRandom.current().nextFloat() * 0.6f;
        
        player.playSound(soundLoc, sound, volume, pitch);
    }
    
    private Location calculateSoundLocation(Player player) {
        Location playerLoc = player.getLocation();
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double distance = 8 + ThreadLocalRandom.current().nextDouble() * 15;
        
        double x = Math.cos(angle) * distance;
        double y = (ThreadLocalRandom.current().nextDouble() - 0.5) * 8;
        double z = Math.sin(angle) * distance;
        
        return playerLoc.clone().add(x, y, z);
    }
    
    private Location calculateDistantLocation(Player player) {
        Location playerLoc = player.getLocation();
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double distance = 15 + ThreadLocalRandom.current().nextDouble() * 25;
        
        double x = Math.cos(angle) * distance;
        double z = Math.sin(angle) * distance;
        
        return playerLoc.clone().add(x, 0, z);
    }
    
    private void scheduleEchoEffect(Player player, Sound sound, Location loc, float volume, float pitch) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.playSound(loc, sound, volume, pitch);
            }
        }, 20L + ThreadLocalRandom.current().nextLong(40L));
    }
    
    public void cleanup() {}
}