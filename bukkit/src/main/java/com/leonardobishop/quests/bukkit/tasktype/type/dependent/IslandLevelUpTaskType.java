package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.example.boxed.core.api.TeamAPI;
import com.example.boxed.core.boxupgrade.IslandLevelUpEvent;
import com.example.boxed.core.team.Team;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.common.player.questprogressfile.QuestProgressFile;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import com.leonardobishop.quests.bukkit.api.event.PlayerQuestDataLoadedEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.UUID;

public final class IslandLevelUpTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public IslandLevelUpTaskType(BukkitQuestsPlugin plugin) {
        super("island_levelup", "DotDebian", "Reach a certain island level.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "min-level"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "min-level"));
    }

    private TeamAPI getTeamAPI() {
        for (String name : new String[]{"BoxedIsland", "BoxedSpawn"}) {
            Plugin boxed = Bukkit.getPluginManager().getPlugin(name);
            if (boxed != null) {
                try {
                    Method method = boxed.getClass().getMethod("getTeamAPI");
                    return (TeamAPI) method.invoke(boxed);
                } catch (ReflectiveOperationException e) {
                    // plugin found but no getTeamAPI method, try next
                }
            }
        }
        return null;
    }

    @Override
    public void onStart(final @NotNull Quest quest, final @NotNull Task task, final @NotNull UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }

        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        QuestProgressFile questProgressFile = qPlayer.getQuestProgressFile();
        QuestProgress questProgress = questProgressFile.getQuestProgress(quest);
        TaskProgress taskProgress = questProgress.getTaskProgress(task.getId());

        checkIslandLevel(player, quest, task, taskProgress);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestDataLoaded(PlayerQuestDataLoadedEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = event.getQuestPlayer();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            checkIslandLevel(player, pendingTask.quest(), pendingTask.task(), pendingTask.taskProgress());
        }
    }

    private void checkIslandLevel(Player player, Quest quest, Task task, TaskProgress taskProgress) {
        TeamAPI teamAPI = getTeamAPI();
        if (teamAPI == null) {
            return;
        }

        Team team = teamAPI.getTeamByPlayer(player.getUniqueId());
        if (team == null) {
            return;
        }

        int currentLevel = team.getIslandLevel();
        int minLevel = (int) task.getConfigValue("min-level");

        super.debug("Checking current island level (level = " + currentLevel + ")", quest.getId(), task.getId(), player.getUniqueId());

        taskProgress.setProgress(currentLevel);

        if (currentLevel >= minLevel) {
            super.debug("Already at required level, marking complete", quest.getId(), task.getId(), player.getUniqueId());

            player.sendMessage(Component.text("Votre île est déjà au niveau requis ! Félicitations matelot, je vois que tu as déjà un bon équipage ! Passons à la suite.").color(NamedTextColor.GRAY));

            taskProgress.setCompleted(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandLevelUp(IslandLevelUpEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        int newLevel = event.getNewLevel();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Island level up (new level = " + newLevel + ")", quest.getId(), task.getId(), player.getUniqueId());

            int minLevel = (int) task.getConfigValue("min-level");

            taskProgress.setProgress(newLevel);

            if (newLevel >= minLevel) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, minLevel);
        }
    }
}
