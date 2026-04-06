package com.leonardobishop.quests.bukkit.api.event;

import com.leonardobishop.quests.common.player.QPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerQuestDataLoadedEvent extends PlayerQuestEvent {

    private final static HandlerList handlers = new HandlerList();

    public PlayerQuestDataLoadedEvent(@NotNull Player who, @NotNull QPlayer questPlayer) {
        super(who, questPlayer);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}