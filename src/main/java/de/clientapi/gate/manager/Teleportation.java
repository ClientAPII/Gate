package de.clientapi.gate.manager;

import de.clientapi.gate.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Teleportation implements Listener {

    private GateManager gateManager;
    private Map<UUID, Location> activePortals = new HashMap<>();
    private Map<UUID, Gate> destinationGates = new HashMap<>();

    public Teleportation(GateManager gateManager) {
        this.gateManager = gateManager;
    }

    public void initiateTeleport(Player player, Gate gate) {
        Location portalLocation = player.getLocation().clone().add(player.getLocation().getDirection().multiply(2)); // Zwei Blöcke vor dem Spieler
        createParticlePortal(portalLocation, player.getLocation().getYaw());
        activePortals.put(player.getUniqueId(), portalLocation);
        destinationGates.put(player.getUniqueId(), gate);
        player.sendMessage("Portal erschaffen. Gehe durch das Portal, um dich zu teleportieren.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!activePortals.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                Location playerLocation = player.getLocation();
                if (isPlayerInPortal(playerLocation, portalLocation)) {
                    teleportPlayer(player, player.getUniqueId());
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 10);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (activePortals.containsKey(playerId)) {
            Location portalLocation = activePortals.get(playerId);
            Location playerLocation = player.getLocation();
            if (isPlayerInPortal(playerLocation, portalLocation)) {
                teleportPlayer(player, playerId);
            }
        }
    }

    private void teleportPlayer(Player player, UUID playerId) {
        Gate destinationGate = destinationGates.get(playerId);
        if (destinationGate != null) {
            Location destination = destinationGate.getLocation();
            player.teleport(destination);
            createParticlePortal(destination, player.getLocation().getYaw()); // Portal auch am Ziel erstellen
            activePortals.remove(playerId);
            destinationGates.remove(playerId);
            player.sendMessage("Teleportiert!");
        }
    }

    private boolean isPlayerInPortal(Location playerLocation, Location portalLocation) {
        double dx = Math.abs(playerLocation.getX() - portalLocation.getX());
        double dy = Math.abs(playerLocation.getY() - portalLocation.getY());
        double dz = Math.abs(playerLocation.getZ() - portalLocation.getZ());
        return dx < 1 && dy < 1 && dz < 1;
    }

    public void createParticlePortal(Location location, float yaw) {
        new BukkitRunnable() {
            int timer = 80; // Timer for about 20 seconds (400 ticks / 20 ticks per second)
            double initialRadiusX = 0.0;
            double initialRadiusY = 0.0;
            double maxRadiusX = 1.5; // Maximum radius in X direction
            double maxRadiusY = 2.5; // Maximum radius in Y direction
            double centerX = location.getX();
            double centerY = location.getY() + 2; // Slightly above the ground
            double centerZ = location.getZ();

            @Override
            public void run() {
                if (timer <= 0) {
                    cancel();
                    activePortals.remove(location); // Remove portal after the timer ends
                    return;
                }

                // Calculate current radius based on the timer
                double radiusX, radiusY;
                if (timer > 60) { // Opening phase (20 ticks)
                    double progress = (80 - timer) / 20.0;
                    radiusX = initialRadiusX + progress * (maxRadiusX - initialRadiusX);
                    radiusY = initialRadiusY + progress * (maxRadiusY - initialRadiusY);
                } else if (timer > 20) { // Active phase (40 ticks)
                    radiusX = maxRadiusX;
                    radiusY = maxRadiusY;
                } else { // Closing phase (20 ticks)
                    double progress = (20 - timer) / 20.0;
                    radiusX = maxRadiusX - progress * (maxRadiusX - initialRadiusX);
                    radiusY = maxRadiusY - progress * (maxRadiusY - initialRadiusY);
                }

                // Adjust particle positions based on player's yaw to rotate around the Y-axis
                double angle = Math.toRadians(yaw);
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                for (double t = 0; t < Math.PI * 2; t += Math.PI / 32) { // Increased density
                    double x = centerX + radiusX * Math.cos(t) * cos;
                    double y = centerY + radiusY * Math.sin(t);
                    double z = centerZ + radiusX * Math.cos(t) * sin;
                    Location particleLocation = new Location(location.getWorld(), x, y, z);
                    // Spawn particles with zero velocity to prevent spreading
                    //location.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 3, 0, 0, 0, 0); // More particles
                    //location.getWorld().spawnParticle(Particle.SPELL_INSTANT, particleLocation, 3, 0, 0, 0, 0); // More particles
                    //location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 3, 0, 0, 0, 0); // More particles
                }

                // Create concentric ovals with increasing darkness
                for (int i = 0; i <= 20; i++) {
                    double factor = i / 20.0;
                    double currentRadiusX = radiusX * (1 - factor);
                    double currentRadiusY = radiusY * (1 - factor);
                    int colorValue = (int) (255 * (1 - factor));
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(colorValue, 0, colorValue), 1.0F);

                    for (double t = 0; t < Math.PI * 2; t += Math.PI / 32) { // Increased density
                        double x = centerX + currentRadiusX * Math.cos(t) * cos;
                        double y = centerY + currentRadiusY * Math.sin(t);
                        double z = centerZ + currentRadiusX * Math.cos(t) * sin;
                        Location particleLocation = new Location(location.getWorld(), x, y, z);
                        // Spawn particles with zero velocity to prevent spreading
                        location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 2, 0, 0, 0, 0, dustOptions); // More particles
                    }
                }

                timer--;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 5);
    }
}
