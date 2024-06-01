package de.clientapi.gate;

import de.clientapi.gate.commands.GateCommand;
import de.clientapi.gate.inventories.GateGUI;
import de.clientapi.gate.manager.GateManager;
import de.clientapi.gate.manager.Teleportation;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static GateManager gateManager;

    @Override
    public void onEnable() {
        gateManager = new GateManager(this);
        getCommand("setgate").setExecutor(new GateCommand(gateManager));
        getCommand("gate").setExecutor(new GateCommand(gateManager));
        getCommand("delgate").setExecutor(new GateCommand(gateManager));
        getServer().getPluginManager().registerEvents(new GateGUI(gateManager), this);
        getServer().getPluginManager().registerEvents(new Teleportation(gateManager), this);
        gateManager.loadGates();
    }

    @Override
    public void onDisable() {
        if (gateManager != null) {
            gateManager.saveGates();
        }
    }

    public static GateManager getGateManager() {
        return gateManager;
    }
}
