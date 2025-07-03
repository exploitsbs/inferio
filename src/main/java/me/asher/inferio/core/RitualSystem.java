package me.asher.inferio.core;

import me.asher.inferio.Inferio;
import me.asher.inferio.effects.RitualEffects;
import me.asher.inferio.utils.StructureValidator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RitualSystem {
    private final Inferio plugin;
    private final RitualEffects effects;
    private final StructureValidator validator;
    private final Map<RitualType, RitualData> ritualConfigs;
    private final ConcurrentHashMap<Location, Long> activeRituals;
    private final ConcurrentHashMap<Location, RitualProgress> ongoingRituals;

    public RitualSystem(Inferio plugin) {
        this.plugin = plugin;
        this.effects = new RitualEffects(plugin);
        this.validator = new StructureValidator();
        this.ritualConfigs = new EnumMap<>(RitualType.class);
        this.activeRituals = new ConcurrentHashMap<>();
        this.ongoingRituals = new ConcurrentHashMap<>();
        
        initializeRitualConfigs();
    }

    private void initializeRitualConfigs() {
        ritualConfigs.put(RitualType.AWAKENING, new RitualData(
            Material.CRYING_OBSIDIAN,
            60,
            true,
            "The shadows stir...",
            "Ancient power awakens..."
        ));
        
        ritualConfigs.put(RitualType.BANISHMENT, new RitualData(
            Material.DIAMOND,
            40,
            false,
            "Light pierces the veil...",
            "The darkness retreats..."
        ));
        
        ritualConfigs.put(RitualType.AMPLIFICATION, new RitualData(
            Material.WITHER_ROSE,
            80,
            true,
            "Terror magnifies...",
            "Fear becomes reality..."
        ));
    }

    public void processItemDrop(Item item, Player player) {
        Material itemType = item.getItemStack().getType();
        Location itemLocation = item.getLocation();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid()) return;
                
                Location baseLocation = findRitualBase(itemLocation);
                if (baseLocation == null) return;
                
                RitualType ritualType = determineRitualType(itemType);
                if (ritualType == null) return;
                
                if (canExecuteRitual(ritualType, baseLocation)) {
                    executeRitual(ritualType, baseLocation, item, player);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    private Location findRitualBase(Location itemLoc) {
        Location below = itemLoc.clone().subtract(0, 1, 0);
        return validator.validateRitualStructure(below) ? below : null;
    }

    private RitualType determineRitualType(Material material) {
        return ritualConfigs.entrySet().stream()
                .filter(entry -> entry.getValue().catalyst == material)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private boolean canExecuteRitual(RitualType type, Location location) {
        if (activeRituals.containsKey(location)) return false;
        
        boolean currentState = plugin.isSystemActive();
        boolean desiredState = ritualConfigs.get(type).activateSystem;
        
        if (type == RitualType.AWAKENING && currentState) return false;
        if (type == RitualType.BANISHMENT && !currentState) return false;
        
        return true;
    }

    private void executeRitual(RitualType type, Location location, Item catalyst, Player caster) {
        RitualData config = ritualConfigs.get(type);
        RitualProgress progress = new RitualProgress(type, caster, System.currentTimeMillis());
        
        ongoingRituals.put(location, progress);
        activeRituals.put(location, System.currentTimeMillis());
        
        catalyst.remove();
        caster.sendMessage("§8§o" + config.startMessage);
        
        effects.playRitualStart(location, type);
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = config.duration;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    completeRitual(type, location, caster, config);
                    cancel();
                    return;
                }
                
                effects.playRitualProgress(location, type, ticks, maxTicks);
                
                if (ticks % 20 == 0) {
                    location.getWorld().playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.8f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void completeRitual(RitualType type, Location location, Player caster, RitualData config) {
        ongoingRituals.remove(location);
        activeRituals.remove(location);
        
        switch (type) {
            case AWAKENING -> {
                plugin.setSystemActive(true);
                plugin.getAtmosphereEngine().getStateTracker().resetAllTension();
                broadcastMessage("§4§l" + config.completeMessage);
            }
            case BANISHMENT -> {
                plugin.setSystemActive(false);
                plugin.getEntityManager().cleanup();
                broadcastMessage("§e§l" + config.completeMessage);
            }
            case AMPLIFICATION -> {
                plugin.getAtmosphereEngine().getStateTracker().amplifyGlobalTension(50);
                caster.sendMessage("§c§l" + config.completeMessage);
            }
        }
        
        effects.playRitualComplete(location, type);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                activeRituals.remove(location);
            }
        }.runTaskLater(plugin, 1200L);
    }

    private void broadcastMessage(String message) {
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public void shutdown() {
        ongoingRituals.clear();
        activeRituals.clear();
        effects.cleanup();
    }

    public boolean isRitualActive(Location location) {
        return activeRituals.containsKey(location);
    }

    private enum RitualType {
        AWAKENING, BANISHMENT, AMPLIFICATION
    }

    private static class RitualData {
        final Material catalyst;
        final int duration;
        final boolean activateSystem;
        final String startMessage;
        final String completeMessage;

        RitualData(Material catalyst, int duration, boolean activateSystem, 
                  String startMessage, String completeMessage) {
            this.catalyst = catalyst;
            this.duration = duration;
            this.activateSystem = activateSystem;
            this.startMessage = startMessage;
            this.completeMessage = completeMessage;
        }
    }

    private static class RitualProgress {
        final RitualType type;
        final Player caster;
        final long startTime;

        RitualProgress(RitualType type, Player caster, long startTime) {
            this.type = type;
            this.caster = caster;
            this.startTime = startTime;
        }
    }
}