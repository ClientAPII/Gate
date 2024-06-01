package de.clientapi.gate.commands;

import de.clientapi.gate.manager.GateManager;
import de.clientapi.gate.inventories.GateGUI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GateCommand implements CommandExecutor {

    private GateManager gateManager;

    public GateCommand(GateManager gateManager) {
        this.gateManager = gateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("setgate")) {
                if (args.length != 1) {
                    player.sendMessage("Bitte gib einen Namen für das Gate an.");
                    return false;
                }
                String gateName = args[0];
                Material blockType = player.getLocation().getBlock().getType();
                Location location = player.getLocation();
                gateManager.addGate(player.getUniqueId(), gateName, blockType, location);
                player.sendMessage("Gate '" + gateName + "' wurde gesetzt!");
                return true;
            } else if (command.getName().equalsIgnoreCase("gate")) {
                new GateGUI(gateManager).openGUI(player);
                return true;
            } else if (command.getName().equalsIgnoreCase("delgate")) {
                if (args.length != 1) {
                    player.sendMessage("Bitte gib einen Namen für das Gate an, das du löschen möchtest.");
                    return false;
                }
                String gateName = args[0];
                boolean result = gateManager.removeGate(player.getUniqueId(), gateName);
                if (result) {
                    player.sendMessage("Gate '" + gateName + "' wurde gelöscht!");
                } else {
                    player.sendMessage("Kein Gate mit dem Namen '" + gateName + "' gefunden!");
                }
                return true;
            }
        }
        return false;
    }
}
