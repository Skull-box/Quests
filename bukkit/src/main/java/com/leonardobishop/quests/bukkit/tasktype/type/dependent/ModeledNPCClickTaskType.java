package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import fr.elias.npcs.api.events.NPCInteractEvent;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ModeledNPCClickTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public ModeledNPCClickTaskType(BukkitQuestsPlugin plugin) {
        super( "modelednpc_interact", "DotDebian", "Task completed when a player interacts with a modeled NPC.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "npc-id"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onModeledNPCClick(NPCInteractEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            final int taskNpcId = (int) task.getConfigValue("npc-id");
            if (taskNpcId != event.getNpcId()) {
                continue;
            }

            taskProgress.setProgress(1);
            taskProgress.setCompleted(true);

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, 1);
        }
    }

}
