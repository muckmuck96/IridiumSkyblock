package com.iridium.iridiumskyblock.api;

import com.iridium.iridiumskyblock.Island;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class IslandWorthCalculatedEvent extends IslandEvent {
    @Getter
    @Setter
    double islandWorth;

    public IslandWorthCalculatedEvent(@NotNull Island island, double islandWorth) {
        super(island);
        this.islandWorth = islandWorth;
    }
}
