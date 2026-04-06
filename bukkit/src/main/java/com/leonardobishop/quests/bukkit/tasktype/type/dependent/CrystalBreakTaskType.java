package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import fr.skullbox.skullboxCrystals.event.CrystalBreakEvent;
import fr.skullbox.skullboxCrystals.event.SpecialBlockBreakEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

public final class CrystalBreakTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public CrystalBreakTaskType(BukkitQuestsPlugin plugin) {
        super("crystal_break", "DotDebian", "Break a certain amount of crystals.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrystalBreak(CrystalBreakEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        String crystalId = event.getNamespacedId();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player broke crystal (id = " + crystalId + ")", quest.getId(), task.getId(), player.getUniqueId());

            List<?> allowedBlocks = (List<?>) task.getConfigValue("blocks");
            if (allowedBlocks != null && !allowedBlocks.isEmpty() && !allowedBlocks.contains(crystalId)) {
                super.debug("Crystal id " + crystalId + " is not in allowed blocks list, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
            super.debug("Updating task progress (now " + progress + ")", quest.getId(), task.getId(), player.getUniqueId());

            player.sendMessage(Component.text("Ta progression de quête pour le minage de cristal a progressée.").color(NamedTextColor.GRAY));

            if (progress >= amountNeeded) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amountNeeded);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amountNeeded);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpecialBlockBreak(SpecialBlockBreakEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        String crystalId = event.getNamespacedId();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player broke crystal (id = " + crystalId + ")", quest.getId(), task.getId(), player.getUniqueId());

            List<?> allowedBlocks = (List<?>) task.getConfigValue("blocks");
            if (allowedBlocks != null && !allowedBlocks.isEmpty() && !allowedBlocks.contains(crystalId)) {
                super.debug("Crystal id " + crystalId + " is not in allowed blocks list, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
            super.debug("Updating task progress (now " + progress + ")", quest.getId(), task.getId(), player.getUniqueId());

            player.sendMessage(Component.text("Ta progression de quête pour les fragments de roche noire a progressée.").color(NamedTextColor.GRAY));

            if (progress >= amountNeeded) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amountNeeded);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amountNeeded);
        }
    }
}
