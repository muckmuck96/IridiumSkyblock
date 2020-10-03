package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            final Player player = event.getPlayer();
            final Location playerLocation = player.getLocation();
            final IslandManager islandManager = IridiumSkyblock.getIslandManager();
            if (!islandManager.isIslandWorld(playerLocation)) return;

            final User user = User.getUser(player);
            final Block block = event.getClickedBlock();

            if (event.getAction().toString().startsWith("RIGHT_CLICK")) {
                if (player.getItemInHand() != null) {
                    int crystals = Utils.getCrystals(player.getItemInHand()) * player.getItemInHand().getAmount();
                    if (crystals != 0) {
                        player.setItemInHand(null);
                        user.getIsland().setCrystals(user.getIsland().getCrystals() + crystals);
                        player.sendMessage(Utils.color(IridiumSkyblock.getMessages().depositedCrystals.replace("%amount%", crystals + "").replace("%prefix%", IridiumSkyblock.getConfiguration().prefix)));
                    }
                }
            }

            if (block != null) {
                final Location location = block.getLocation();
                final Island island = islandManager.getIslandViaLocation(location);
                if (island != null) {
                    if (!island.getPermissions(user).interact) {
                        event.setCancelled(true);
                        return;
                    }
                    final ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand.getType().equals(Material.BUCKET) && island.failedGenerators.remove(location)) {
                        if (itemInHand.getAmount() == 1)
                            itemInHand.setType(Material.LAVA_BUCKET);
                        else {
                            player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
                            player.getItemInHand().setAmount(itemInHand.getAmount() - 1);
                        }
                        block.setType(Material.AIR);
                    }
                } else if (!user.bypassing) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            IridiumSkyblock.getInstance().sendErrorMessage(e);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        try {
            final Player player = event.getPlayer();
            final User user = User.getUser(player);
            final Entity rightClicked = event.getRightClicked();
            final Location location = rightClicked.getLocation();
            final IslandManager islandManager = IridiumSkyblock.getIslandManager();
            final Island island = islandManager.getIslandViaLocation(location);
            if (island == null) return;

            if (island.getPermissions(user).interact) return;

            event.setCancelled(true);
        } catch (Exception e) {
            IridiumSkyblock.getInstance().sendErrorMessage(e);
        }
    }
}
