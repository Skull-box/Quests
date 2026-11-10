package com.leonardobishop.quests.bukkit.api.event;

import com.leonardobishop.quests.common.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Une tâche d'une quête commencée vient de progresser (ou de reculer). Déclenché là où Quests envoie sa progression
 * à l'action bar et à la bossbar ({@code TaskUtils.sendTrackAdvancement}), avant de lire leurs options : l'événement
 * part même si {@code options.actionbar} et {@code options.bossbar} sont coupés. Seules les tâches à compteur
 * l'envoient (pas les tâches « en un geste » : interaction PNJ, position, commande…).
 *
 * <p>Informatif, non annulable. Asynchrone si la tâche progresse hors du thread principal.
 */
public class PlayerQuestProgressEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Quest quest;
    private final Task task;
    private final QuestProgress questProgress;
    private final TaskProgress taskProgress;
    private final Number goal;

    public PlayerQuestProgressEvent(@NotNull Player who, @NotNull Quest quest, @NotNull Task task,
                                    @NotNull QuestProgress questProgress, @NotNull TaskProgress taskProgress,
                                    @NotNull Number goal) {
        super(who, !Bukkit.isPrimaryThread());
        this.quest = quest;
        this.task = task;
        this.questProgress = questProgress;
        this.taskProgress = taskProgress;
        this.goal = goal;
    }

    public @NotNull Quest getQuest() {
        return quest;
    }

    public @NotNull Task getTask() {
        return task;
    }

    public @NotNull QuestProgress getQuestProgress() {
        return questProgress;
    }

    public @NotNull TaskProgress getTaskProgress() {
        return taskProgress;
    }

    /** Progression actuelle de la tâche, 0 si elle n'est pas numérique. */
    public double getProgress() {
        return taskProgress.getProgress() instanceof Number number ? number.doubleValue() : 0;
    }

    /** Objectif de la tâche, tel que la tâche le passe à l'action bar (sa clé {@code amount} ou équivalent). */
    public @NotNull Number getGoal() {
        return goal;
    }

    public boolean isTaskCompleted() {
        return taskProgress.isCompleted();
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
