package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import cn.superiormc.ultimateshop.api.ItemFinishTransactionEvent;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UltimateShopBuyTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public UltimateShopBuyTaskType(BukkitQuestsPlugin plugin) {
        super( "zshop_buy", "DotDebian", "Purchase a given item from a ZShop shop", "zshop_buycertain");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler
    public void onTransaction(ItemFinishTransactionEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        int amountBought = event.getAmount();
        final ItemStack itemStack = event.getItem().getDisplayItem(event.getPlayer());
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        String itemId = customStack != null ? customStack.getNamespacedID() : itemStack.getType().name();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player traded item (item = " + itemId + ", action = BUY)", quest.getId(), task.getId(), player.getUniqueId());

            String taskItemId = (String) task.getConfigValue("item-id");
            if (taskItemId != null && !taskItemId.equals(itemId)) {
                super.debug("Item id does not match required id, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = progress + amountBought;
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
