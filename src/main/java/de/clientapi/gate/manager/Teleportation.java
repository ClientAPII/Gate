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
        createParticlePortal(portalLocation);
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
            createParticlePortal(destination); // Portal auch am Ziel erstellen
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

    public void createParticlePortal(Location location) {
        new BukkitRunnable() {
            int timer = 50; // Reduced active time
            double radiusX = 2.0; // Radius in X-Richtung
            double radiusY = 4.0; // Radius in Y-Richtung
            double centerX = location.getX();
            double centerY = location.getY() + 1; // Leicht über dem Boden
            double centerZ = location.getZ();

            @Override
            public void run() {
                if (timer <= 0) {
                    cancel();
                    return;
                }
                for (double t = 0; t < Math.PI * 2; t += Math.PI / 16) {
                    double x = centerX + radiusX * Math.cos(t);
                    double y = centerY + radiusY * Math.sin(t);
                    double z = centerZ;
                    Location particleLocation = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 0);
                    location.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 0);
                    location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 0);
                }

                // Create concentric ovals with increasing darkness
                for (int i = 0; i <= 40; i++) {
                    double factor = i / 20.0;
                    double currentRadiusX = radiusX * (1 - factor);
                    double currentRadiusY = radiusY * (1 - factor);
                    int colorValue = (int) (255 * (1 - factor));
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(colorValue, 0, colorValue), 1.0F);

                    for (double t = 0; t < Math.PI * 2; t += Math.PI / 16) {
                        double x = centerX + currentRadiusX * Math.cos(t);
                        double y = centerY + currentRadiusY * Math.sin(t);
                        double z = centerZ;
                        Location particleLocation = new Location(location.getWorld(), x, y, z);
                        location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
                    }
                }

                timer--;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 5);
    }
}
