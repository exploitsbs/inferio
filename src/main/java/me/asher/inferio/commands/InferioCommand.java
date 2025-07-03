 
package me.asher.inferio.commands;

import me.asher.inferio.Inferio;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InferioCommand implements CommandExecutor {
    private final Inferio plugin;
    
    public InferioCommand(Inferio plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("inferio.admin")) {
            sender.sendMessage("§cInsufficient permissions.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage("§aInferio configuration reloaded.");
            }
            case "status" -> showStatus(sender);
            case "toggle" -> toggleSystem(sender);
            case "tension" -> {
                if (args.length > 1 && sender instanceof Player player) {
                    try {
                        int amount = Integer.parseInt(args[1]);
                        plugin.getAtmosphereEngine().getStateTracker().incrementTension(
                            player.getUniqueId(), amount
                        );
                        sender.sendMessage("§aTension adjusted by " + amount);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid number format.");
                    }
                }
            }
            case "cleanup" -> {
                plugin.getEntityManager().cleanup();
                sender.sendMessage("§aAll entities removed.");
            }
            default -> showHelp(sender);
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== Inferio Commands ===");
        sender.sendMessage("§4Still in BETA.");
        sender.sendMessage("§e/inferio reload §7- Reload configuration");
        sender.sendMessage("§e/inferio status §7- Show system status");
        sender.sendMessage("§e/inferio toggle §7- Toggle system on/off");
        sender.sendMessage("§e/inferio tension <amount> §7- Adjust personal tension");
        sender.sendMessage("§e/inferio cleanup §7- Remove all entities");
    }
    
    private void showStatus(CommandSender sender) {
        boolean active = plugin.isSystemActive();
        int globalTension = plugin.getAtmosphereEngine().getGlobalTension();
        int activeEntities = plugin.getEntityManager().getActiveEntities().size();
        
        sender.sendMessage("§6=== Inferio Status ===");
        sender.sendMessage("§eSystem Active: " + (active ? "§aYes" : "§cNo"));
        sender.sendMessage("§eGlobal Tension: §f" + globalTension + "%");
        sender.sendMessage("§eActive Entities: §f" + activeEntities);
        
        if (sender instanceof Player player) {
            double playerTension = plugin.getAtmosphereEngine().getStateTracker()
                .getPlayerTension(player.getUniqueId());
            sender.sendMessage("§eYour Tension: §f" + String.format("%.1f", playerTension) + "%");
        }
    }
    
    private void toggleSystem(CommandSender sender) {
        boolean newState = !plugin.isSystemActive();
        plugin.setSystemActive(newState);
        
        if (!newState) {
            plugin.getEntityManager().cleanup();
        }
        
        sender.sendMessage("§eInferio system " + (newState ? "§aenabled" : "§cdisabled"));
    }
}