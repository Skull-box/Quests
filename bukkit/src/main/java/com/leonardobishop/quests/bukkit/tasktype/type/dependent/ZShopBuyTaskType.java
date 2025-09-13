package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import dev.lone.itemsadder.api.CustomStack;
import fr.maxlego08.shop.api.event.events.ZShopBuyEvent;
import net.brcdev.shopgui.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class ZShopBuyTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public ZShopBuyTaskType(BukkitQuestsPlugin plugin) {
        super( "zshop_buy", "DotDebian", "Purchase a given item from a ZShop shop", "zshop_buycertain");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler
    public void onZShopBuy(ZShopBuyEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        int amountBought = event.getAmount();
        CustomStack customStack = CustomStack.byItemStack(event.getItemStack());
        String itemId = customStack != null ? customStack.getNamespacedID() : event.getItemStack().getType().name();

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
