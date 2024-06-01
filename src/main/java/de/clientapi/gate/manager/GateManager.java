package de.clientapi.gate.manager;

import de.clientapi.gate.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GateManager {
    private Main main;
    private Map<UUID, List<Gate>> gates = new HashMap<>();
    private File gateFile;
    private YamlConfiguration gateConfig;

    public GateManager(Main main) {
        this.main = main;
        gateFile = new File(Bukkit.getPluginManager().getPlugin("GatePlugin").getDataFolder(), "gate.yml");
        if (!gateFile.exists()) {
            try {
                gateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gateConfig = YamlConfiguration.loadConfiguration(gateFile);
        loadGates();
    }

    public void addGate(UUID playerUUID, String name, Material blockType, Location location) {
        Gate gate = new Gate(name, blockType, location);
        if (!gates.containsKey(playerUUID)) {
            gates.put(playerUUID, new ArrayList<>());
        }
        gates.get(playerUUID).add(gate);
        saveGates();
    }

    public boolean removeGate(UUID playerUUID, String name) {
        if (gates.containsKey(playerUUID)) {
            List<Gate> playerGates = gates.get(playerUUID);
            for (Gate gate : playerGates) {
                if (gate.getName().equalsIgnoreCase(name)) {
                    playerGates.remove(gate);
                    saveGates();
                    return true;
                }
            }
        }
        return false;
    }

    public List<Gate> getGates(UUID playerUUID) {
        return gates.getOrDefault(playerUUID, new ArrayList<>());
    }

    public Gate getGate(UUID playerUUID, String name) {
        if (gates.containsKey(playerUUID)) {
            for (Gate gate : gates.get(playerUUID)) {
                if (gate.getName().equalsIgnoreCase(name)) {
                    return gate;
                }
            }
        }
        return null;
    }

    public void saveGates() {
        for (UUID uuid : gates.keySet()) {
            List<Map<String, Object>> gateList = new ArrayList<>();
            for (Gate gate : gates.get(uuid)) {
                Map<String, Object> gateMap = new HashMap<>();
                gateMap.put("name", gate.getName());
                gateMap.put("blockType", gate.getBlockType().toString());
                gateMap.put("location", gate.getLocation().serialize());
                gateList.add(gateMap);
            }
            gateConfig.set(uuid.toString(), gateList);
        }
        try {
            gateConfig.save(gateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGates() {
        for (String uuidStr : gateConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<Map<String, Object>> gateList = (List<Map<String, Object>>) gateConfig.getList(uuidStr);
            List<Gate> loadedGates = new ArrayList<>();
            for (Map<String, Object> gateMap : gateList) {
                String name = (String) gateMap.get("name");
                Material blockType = Material.valueOf((String) gateMap.get("blockType"));
                Location location = Location.deserialize((Map<String, Object>) gateMap.get("location"));
                loadedGates.add(new Gate(name, blockType, location));
            }
            gates.put(uuid, loadedGates);
        }
    }
}
