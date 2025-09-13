package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class RegionEnterTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public RegionEnterTaskType(BukkitQuestsPlugin plugin) {
        super( "region_enter", "DotDebian", "Purchase a given item from a ZShop shop");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "name"));
    }

    @EventHandler
    public void onRegionEnter(RegionEnteredEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player entered region (region = " + event.getRegion() + ")", quest.getId(), task.getId(), player.getUniqueId());

            String taskRegionName = (String) task.getConfigValue("name");
            if (taskRegionName != null && !taskRegionName.equals(event.getRegion())) {
                super.debug("Region name does not match required name, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int progress = TaskUtils.getIntegerTaskProgress(taskProgress);
            taskProgress.setProgress(1);
            taskProgress.setCompleted(true);

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, 1);
        }
    }

}
