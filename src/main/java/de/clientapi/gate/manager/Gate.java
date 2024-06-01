package de.clientapi.gate.manager;

import org.bukkit.Location;
import org.bukkit.Material;

public class Gate {
    private String name;
    private Material blockType;
    private Location location;

    public Gate(String name, Material blockType, Location location) {
        this.name = name;
        this.blockType = blockType;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Material getBlockType() {
        return blockType;
    }

    public Location getLocation() {
        return location;
    }
}
