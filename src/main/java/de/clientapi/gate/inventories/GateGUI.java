package de.clientapi.gate.inventories;

import de.clientapi.gate.manager.Gate;
import de.clientapi.gate.manager.GateManager;
import de.clientapi.gate.manager.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GateGUI implements Listener {

    private GateManager gateManager;

    public GateGUI(GateManager gateManager) {
        this.gateManager = gateManager;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Select a Gate");

        for (Gate gate : gateManager.getGates(player.getUniqueId())) {
            ItemStack item = new ItemStack(gate.getBlockType() != Material.AIR ? gate.getBlockType() : Material.STONE); // Fallback to STONE if AIR
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(gate.getName());
                meta.setLore(Arrays.asList("Location:", gate.getLocation().toString()));
                item.setItemMeta(meta);
                inv.addItem(item);
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select a Gate")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();
                ItemStack item = event.getCurrentItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String gateName = meta.getDisplayName();
                    Gate gate = gateManager.getGate(player.getUniqueId(), gateName);
                    if (gate != null) {
                        new Teleportation(gateManager).initiateTeleport(player, gate);
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
