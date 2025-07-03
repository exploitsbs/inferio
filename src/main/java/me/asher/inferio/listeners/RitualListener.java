 
package me.asher.inferio.listeners;

import me.asher.inferio.Inferio;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.EnumSet;
import java.util.Set;

public class RitualListener implements Listener {
    private final Inferio plugin;
    private final Set<Material> ritualItems = EnumSet.of(
        Material.CRYING_OBSIDIAN, 
        Material.DIAMOND, 
        Material.WITHER_ROSE
    );
    
    public RitualListener(Inferio plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (ritualItems.contains(item.getItemStack().getType())) {
            plugin.getRitualSystem().processItemDrop(item, event.getPlayer());
        }
    }
    
    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (ritualItems.contains(event.getEntity().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }
}