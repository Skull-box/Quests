package com.leonardobishop.quests.bukkit.util;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SoundUtils {

    private static final Map<String, Sound> soundCache = new HashMap<>();
    private static final Method soundValueOfMethod;
    /** Number of sounds actually sent so far (see {@link #getPlayCount()}). */
    private static final AtomicLong playCount = new AtomicLong();

    static {
        try {
            soundValueOfMethod = Sound.class.getDeclaredMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Sound#valueOf method could not be found", e);
        }
    }

    /**
     * Number of sounds sent by {@link #playSoundForPlayer(Player, String)} so far. Compare it before and after an
     * action to know whether the action already played a sound ("one sound per gesture").
     */
    public static long getPlayCount() {
        return playCount.get();
    }

    /**
     * Play a menu-opening sound ({@code options.sounds.gui.open}), unless the player is navigating: if they just
     * clicked in a menu (any plugin), the button played its own click sound and the menu it opens stays silent.
     * The opening sound is only for a menu opened directly (command, item, NPC).
     */
    public static void playOpeningSoundForPlayer(Player player, String soundString) {
        if (!MenuNavigation.isNavigating(player)) {
            playSoundForPlayer(player, soundString);
        }
    }

    /**
     * Play a sound to a player
     *
     * @param soundString the sound string as formatted by a user in the config, e.g. ENTITY_PLAYER_LEVELUP:2:3
     *                    or namespaced, e.g (minecraft:block.decorated_pot.step):2:3.
     *                    An optional fourth field gives the sound category (a {@link SoundCategory} name, which is
     *                    the volume slider the player sees), e.g. (skullbox:ui.book):1:0.5:VOICE or
     *                    ENTITY_PLAYER_LEVELUP:2:3:VOICE. Without it, or if it is not a valid category, the sound
     *                    plays in MASTER as before.
     */
    public static void playSoundForPlayer(Player player, String soundString) {
        if (soundString == null || soundString.isEmpty()) {
            return;
        }

        String[] parts = soundString.split(":");
        boolean namespaced = parts.length >= 2 && parts[0].startsWith("(") && parts[1].endsWith(")");

        // (namespace:key):pitch:volume:category
        // 0          1    2     3      4
        // ENTITY_PLAYER_LEVELUP:2:3:category
        // 0                     1 2 3
        int pitchIndex = namespaced ? 2 : 1;
        int volumeIndex = pitchIndex + 1;
        int categoryIndex = volumeIndex + 1;

        float pitch = 1.0f;
        if (parts.length >= pitchIndex + 1) {
            try {
                pitch = Float.parseFloat(parts[pitchIndex]);
            } catch (NumberFormatException ignored) {
            }
        }

        float volume = 3.0f;
        if (parts.length >= volumeIndex + 1) {
            try {
                volume = Float.parseFloat(parts[volumeIndex]);
            } catch (NumberFormatException ignored) {
            }
        }

        // MASTER unless the config says otherwise (existing configs keep their behaviour)
        SoundCategory category = SoundCategory.MASTER;
        if (parts.length >= categoryIndex + 1) {
            try {
                category = SoundCategory.valueOf(parts[categoryIndex].trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (namespaced) {
            // 0123456789
            // (space:id)
            // (space - length 6
            // id) - length 3
            String sound = soundString.substring(1, parts[0].length() + parts[1].length());
            player.playSound(player.getLocation(), sound, category, volume, pitch);
            playCount.incrementAndGet();
            return;
        }

        Sound sound = soundCache.computeIfAbsent(parts[0], name -> {
            try {
                return (Sound) soundValueOfMethod.invoke(null, parts[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                final Throwable cause = e.getCause();

                if (cause instanceof IllegalArgumentException) {
                    return null;
                } else {
                    throw new IllegalStateException("Sound#valueOf invocation failed", e);
                }
            }
        });

        if (sound == null) {
            return;
        }

        player.playSound(player.getLocation(), sound, category, volume, pitch);
        playCount.incrementAndGet();
    }
}
