 
package me.asher.inferio.listeners;

import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import me.asher.inferio.Inferio;
import me.asher.inferio.entities.InferioEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Inferio plugin;
    
    public PlayerListener(Inferio plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.isSystemActive()) {
            plugin.getAtmosphereEngine().getStateTracker().incrementTension(
                event.getPlayer().getUniqueId(), 5
            );
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEntityManager().forceCleanupPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onNpcInteract(NpcInteractEvent event) {
        plugin.getEntityManager().getActiveEntities().stream()
            .filter(entity -> entity.getNpc() != null)
            .filter(entity -> entity.getNpc().getData().getId().equals(event.getNpc().getData().getId()))
            .findFirst()
            .ifPresent(entity -> {
                event.setCancelled(true);
                plugin.getEntityManager().onEntityInteraction(entity, event.getPlayer());
            });
    }
}