package com.iridium.iridiumskyblock.api;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.User;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IslandKickEvent extends IslandEvent implements Cancellable {
  @Getter @Nullable User userWhoKicked;
  @Getter @NotNull User kickedUser;
  @Getter @Setter boolean cancelled = false;
  
  public IslandKickEvent(@NotNull Island island, @Nullable User userWhoKicked, @NotNull User kickedUser) {
    super(island);
    this.userWhoKicked = userWhoKicked;
    this.kickedUser = kickedUser;
  }
}
