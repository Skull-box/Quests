package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CustomBlockBreakTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public CustomBlockBreakTaskType(BukkitQuestsPlugin plugin) {
        super("custom_blockbreak", "DotDebian", "Break a certain amount of custom blocks.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        ItemStack tool = plugin.getVersionSpecificHandler().getItemInMainHand(player);
        if (tool != null && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
            return;
        }

        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(event.getBlock());
        if (customBlock == null) {
            return;
        }

        String blockId = customBlock.getNamespacedID();

        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player broke custom block (id = " + blockId + ")", quest.getId(), task.getId(), player.getUniqueId());

            List<?> allowedBlocks = (List<?>) task.getConfigValue("blocks");
            if (allowedBlocks != null && !allowedBlocks.isEmpty() && !allowedBlocks.contains(blockId)) {
                super.debug("Block id " + blockId + " is not in allowed blocks list, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
            super.debug("Updating task progress (now " + progress + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (progress >= amountNeeded) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amountNeeded);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amountNeeded);
        }
    }
}
