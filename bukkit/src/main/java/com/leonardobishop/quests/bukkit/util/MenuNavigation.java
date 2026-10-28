package com.leonardobishop.quests.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects navigation between menus, for the opening sound ({@link SoundUtils#playOpeningSoundForPlayer}).
 * <p>
 * Local copy of SkullboxCore's detector (this plugin does not depend on it). A click in the top part of any menu
 * (this plugin, zMenu, any other) is noted at the very start of the event ({@link EventPriority#LOWEST}), before the
 * clicked plugin opens the next menu. For {@link #WINDOW_MS} ms an opening is a navigation: the button played its
 * click sound, the menu it leads to stays silent.
 */
public final class MenuNavigation implements Listener {

    /** Window after a menu click during which an opening counts as a navigation. */
    public static final long WINDOW_MS = 1000L;

    private static final Map<UUID, Long> LAST_MENU_CLICK = new ConcurrentHashMap<>();

    public static boolean isNavigating(Player player) {
        Long at = LAST_MENU_CLICK.get(player.getUniqueId());
        return at != null && System.currentTimeMillis() - at < WINDOW_MS;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryType type = top.getType();
        if (type == InventoryType.CRAFTING || type == InventoryType.CREATIVE || type == InventoryType.PLAYER) {
            return; // player's own inventory only: not a menu
        }
        if (event.getClickedInventory() != top) {
            return; // click in the bottom part (player inventory)
        }
        LAST_MENU_CLICK.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        LAST_MENU_CLICK.remove(event.getPlayer().getUniqueId());
    }
}
