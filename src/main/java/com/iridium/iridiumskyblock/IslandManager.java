package com.iridium.iridiumskyblock;

import com.iridium.iridiumskyblock.configs.Config;
import com.iridium.iridiumskyblock.configs.Schematics;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IslandManager {

    public Map<Integer, Island> islands = new HashMap<>();
    public Map<String, User> users = new HashMap<>();
    public Map<List<Integer>, Set<Integer>> islandCache = new HashMap<>();

    int length = 1;
    int current = 0;

    public Direction direction = Direction.NORTH;
    public Location nextLocation;

    public int nextID = 1;

    public IslandManager() {
        makeWorld();
        nextLocation = new Location(getWorld(), 0, 0, 0);
    }

    public World getWorld() {
        return Bukkit.getWorld(IridiumSkyblock.getConfiguration().worldName);
    }

    public World getNetherWorld() {
        return Bukkit.getWorld(IridiumSkyblock.getConfiguration().netherWorldName);
    }

    public void createIsland(Player player) {
        User user = User.getUser(player);
        if (user.lastCreate != null && new Date().before(user.lastCreate) && IridiumSkyblock.getConfiguration().createCooldown && !user.bypassing) {
            //The user cannot create an island
            long time = (user.lastCreate.getTime() - System.currentTimeMillis()) / 1000;
            int day = (int) TimeUnit.SECONDS.toDays(time);
            int hours = (int) Math.floor(TimeUnit.SECONDS.toHours(time - day * 86400));
            int minute = (int) Math.floor((time - day * 86400 - hours * 3600) / 60.00);
            int second = (int) Math.floor((time - day * 86400 - hours * 3600) % 60.00);
            player.sendMessage(Utils.color(IridiumSkyblock.getMessages().createCooldown.replace("%days%", day + "").replace("%hours%", hours + "").replace("%minutes%", minute + "").replace("%seconds%", second + "").replace("%prefix%", IridiumSkyblock.getConfiguration().prefix)));
            return;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, IridiumSkyblock.getConfiguration().regenCooldown);
        user.lastCreate = c.getTime();

        Location pos1 = nextLocation.clone().subtract(IridiumSkyblock.getUpgrades().sizeUpgrade.upgrades.get(1).size / 2.00, 0, IridiumSkyblock.getUpgrades().sizeUpgrade.upgrades.get(1).size / 2.00);
        Location pos2 = nextLocation.clone().add(IridiumSkyblock.getUpgrades().sizeUpgrade.upgrades.get(1).size / 2.00, 0, IridiumSkyblock.getUpgrades().sizeUpgrade.upgrades.get(1).size / 2.00);
        Location center = nextLocation.clone().add(0, 100, 0);
        Location home = nextLocation.clone();

        Location netherhome = home.clone();

        if (IridiumSkyblock.getConfiguration().netherIslands) {
            netherhome.setWorld(IridiumSkyblock.getIslandManager().getNetherWorld());
        }
        Island island = new Island(player, pos1, pos2, center, home, netherhome, nextID);
        islands.put(nextID, island);

        user.islandID = nextID;
        user.role = Role.Owner;

        if (IridiumSkyblock.getSchematics().schematics.size() == 1) {
            for (Schematics.FakeSchematic schematic : IridiumSkyblock.getSchematics().schematics) {
                island.setSchematic(schematic.name);
                island.setNetherschematic(schematic.netherisland);
                island.setHome(island.getHome().add(schematic.x, schematic.y, schematic.z));
                island.setNetherhome(island.getNetherhome().add(schematic.x, schematic.y, schematic.z));
            }
            island.pasteSchematic(player, false);
        } else {
            player.openInventory(island.getSchematicSelectGUI().getInventory());
        }

        switch (direction) {
            case NORTH:
                nextLocation.add(IridiumSkyblock.getConfiguration().distance, 0, 0);
                break;
            case EAST:
                nextLocation.add(0, 0, IridiumSkyblock.getConfiguration().distance);
                break;
            case SOUTH:
                nextLocation.subtract(IridiumSkyblock.getConfiguration().distance, 0, 0);
                break;
            case WEST:
                nextLocation.subtract(0, 0, IridiumSkyblock.getConfiguration().distance);
                break;
        }

        current++;

        if (current == length) {
            current = 0;
            direction = direction.next();
            if (direction == Direction.SOUTH || direction == Direction.NORTH) {
                length++;
            }
        }

        IridiumSkyblock.getInstance().saveData();

        nextID++;
    }

    private void makeWorld() {
        makeWorld(Environment.NORMAL, IridiumSkyblock.getConfiguration().worldName);
        if (IridiumSkyblock.getConfiguration().netherIslands)
            makeWorld(Environment.NETHER, IridiumSkyblock.getConfiguration().netherWorldName);
    }

    private void makeWorld(Environment env, String name) {
        WorldCreator wc = new WorldCreator(name);
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);
        wc.generator(new SkyblockGenerator());
        wc.environment(env);
        wc.createWorld();
    }

    public Island getIslandViaLocation(Location location) {
        if (location == null) return null;
        if (!isIslandWorld(location)) return null;

        final Chunk chunk = location.getChunk();
        final List<Integer> chunkKey = Collections.unmodifiableList(Arrays.asList(chunk.getX(), chunk.getZ()));

        final double x = location.getX();
        final double z = location.getZ();

        final Set<Integer> islandIds = islandCache.computeIfAbsent(chunkKey, (hash) -> islands
                .values()
                .stream()
                .filter(island -> island.isInIsland(x, z))
                .map(Island::getId)
                .collect(Collectors.toSet()));

        for (int id : islandIds) {
            final Island island = islands.get(id);
            if (island == null) continue;
            if (island.isInIsland(x, z)) return island;
        }

        for (Island island : islands.values()) {
            if (!island.isInIsland(x, z)) continue;
            islandIds.add(island.getId());
            return island;
        }

        return null;
    }

    public Island getIslandViaId(int i) {
        return islands.get(i);
    }

    public boolean isIslandWorld(Location location) {
        if (location == null) return false;
        return isIslandWorld(location.getWorld());
    }

    public boolean isIslandWorld(World world) {
        if (world == null) return false;
        final String name = world.getName();
        return isIslandWorld(name);
    }

    public boolean isIslandWorld(String name) {
        final Config config = IridiumSkyblock.getConfiguration();
        return (name.equals(config.worldName) || name.equals(config.netherWorldName));
    }

    public void removeIsland(Island island) {
        final int id = island.getId();
        islands.remove(id);
        islandCache
                .forEach((key, value) -> value.remove(id));
    }
}
