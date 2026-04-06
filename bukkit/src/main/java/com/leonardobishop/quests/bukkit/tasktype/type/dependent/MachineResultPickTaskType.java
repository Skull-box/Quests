package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import fr.skullbox.skullboxEssentials.module.machines.MachineResultPickupEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

public final class MachineResultPickTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public MachineResultPickTaskType(BukkitQuestsPlugin plugin) {
        super("machine_result_pick", "DotDebian", "Pick up a certain amount of items from a machine.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "machine-type"));
        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMachineResultPickup(MachineResultPickupEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        String machineType = event.getMachineType().name();
        String itemId = event.getItemId();
        int pickedAmount = event.getAmount();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player picked up machine result (machine = " + machineType + ", item = " + itemId + ", amount = " + pickedAmount + ")", quest.getId(), task.getId(), player.getUniqueId());

            String requiredMachineType = (String) task.getConfigValue("machine-type");
            if (requiredMachineType != null && !requiredMachineType.equalsIgnoreCase(machineType)) {
                super.debug("Machine type does not match required type, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            List<?> allowedItems = (List<?>) task.getConfigValue("items");
            if (allowedItems != null && !allowedItems.isEmpty() && !allowedItems.contains(itemId)) {
                super.debug("Item id " + itemId + " is not in allowed items list, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = progress + pickedAmount;
            taskProgress.setProgress(newProgress);

            super.debug("Updating task progress (now " + newProgress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (newProgress >= amountNeeded) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amountNeeded);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amountNeeded);
        }
    }
}
