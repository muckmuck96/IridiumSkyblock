package com.iridium.iridiumskyblock.api;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.MissionType;
import com.iridium.iridiumskyblock.User;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MissionCompleteEvent extends IslandEvent {
    @NotNull @Getter String missionName;
    @NotNull @Getter MissionType missionType;
    @Getter int missionLevel;

    public MissionCompleteEvent(@NotNull Island island, @NotNull String missionName, @NotNull MissionType missionType, int missionLevel) {
        super(island);
        this.missionName = missionName;
        this.missionType = missionType;
        this.missionLevel = missionLevel;
    }
}
