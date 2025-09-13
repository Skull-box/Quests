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
import fr.maxlego08.shop.api.event.ShopAction;
import fr.maxlego08.shop.api.event.events.ZShopSellAllEvent;
import fr.maxlego08.shop.api.event.events.ZShopSellEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ZShopSellTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public ZShopSellTaskType(BukkitQuestsPlugin plugin) {
        super( "zshop_sell", "DotDebian", "Sell a given item from a ZShop shop", "zshop_sellcertain");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler
    public void onZShopSell(ZShopSellEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        int amountSold = event.getAmount();
        CustomStack customStack = CustomStack.byItemStack(event.getItemStack());
        String itemId = customStack != null ? customStack.getNamespacedID() : event.getItemStack().getType().name();

        handle(player, qPlayer, itemId, amountSold);
    }

    @EventHandler
    public void onZShopSellAll(ZShopSellAllEvent event) {
        Player player = event.getPlayer();
        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (ShopAction shopAction : event.getShopActions()) {
            CustomStack customStack = CustomStack.byItemStack(shopAction.getItemStack());
            String itemId = customStack != null ? customStack.getNamespacedID() : shopAction.getItemStack().getType().name();
            handle(player, qPlayer, itemId, shopAction.getItemStack().getAmount());
        }
    }

    public void handle(Player player, QPlayer qPlayer, String itemId, int amountSold) {
        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player traded item (item = " + itemId + ", action = SELL)", quest.getId(), task.getId(), player.getUniqueId());

            String taskItemId = (String) task.getConfigValue("item-id");
            if (taskItemId != null && !taskItemId.equals(itemId)) {
                super.debug("Item id does not match required id, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                continue;
            }

            int amountNeeded = (int) task.getConfigValue("amount");

            int progress = TaskUtils.getIntegerTaskProgress(taskProgress);
            int newProgress = progress + amountSold;
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
