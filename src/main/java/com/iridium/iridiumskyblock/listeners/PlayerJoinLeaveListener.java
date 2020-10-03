package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinLeaveListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            final Player player = event.getPlayer();
            final IridiumSkyblock plugin = IridiumSkyblock.getInstance();
            if (player.isOp()) {
                final String latest = plugin.getLatest();
                if (plugin.getLatest() != null
                        && IridiumSkyblock.getConfiguration().notifyAvailableUpdate
                        && !latest.equals(plugin.getDescription().getVersion())) {
                    final String prefix = IridiumSkyblock.getConfiguration().prefix;
                    player.sendMessage(Utils.color(prefix + " &7This message is only seen by opped players."));
                    player.sendMessage(Utils.color(prefix + " &7Newer version available: " + latest));
                }
            }

            final Location location = player.getLocation();
            final IslandManager islandManager = IridiumSkyblock.getIslandManager();
            if (!islandManager.isIslandWorld(location)) return;

            final User user = User.getUser(player);
            user.name = player.getName();

            if (user.flying && (user.getIsland() == null || user.getIsland().getFlightBooster() == 0)) {
                player.setAllowFlight(false);
                player.setFlying(false);
                user.flying = false;
            }
            if (IridiumSkyblock.getConfiguration().disableBypassOnJoin || !player.hasPermission(IridiumSkyblock.getCommands().bypassCommand.getPermission()))
                user.bypassing = false;

            final Island island = islandManager.getIslandViaLocation(location);
            if (island == null) return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> island.sendBorder(player), 1);
        } catch (Exception e) {
            IridiumSkyblock.getInstance().sendErrorMessage(e);
        }
    }
}
