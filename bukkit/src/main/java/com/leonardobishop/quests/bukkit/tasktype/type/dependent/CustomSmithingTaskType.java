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
import fr.skullbox.skullboxEssentials.module.runes.CustomSmithItemEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public final class CustomSmithingTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public CustomSmithingTaskType(BukkitQuestsPlugin plugin) {
        super("custom_smithing", TaskUtils.TASK_ATTRIBUTION_STRING, "Smith an item with specific input items (supports ItemsAdder and vanilla).");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSmithItem(CustomSmithItemEvent event) {
        Player player = event.getPlayer();

        ItemStack leftItem = event.getTemplate();
        ItemStack middleItem = event.getBase();
        ItemStack rightItem = event.getAddition();
        ItemStack result = event.getResult();

        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        String leftDesc = describeItem(leftItem);
        String middleDesc = describeItem(middleItem);
        String rightDesc = describeItem(rightItem);

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Checking smithing event (left = " + leftDesc + ", middle = " + middleDesc + ", right = " + rightDesc + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (task.hasConfigKey("left")) {
                List<?> allowedLeft = (List<?>) task.getConfigValue("left");
                super.debug("Left config: " + allowedLeft + ", actual: " + leftDesc, quest.getId(), task.getId(), player.getUniqueId());
                if (allowedLeft != null && !allowedLeft.isEmpty() && !matchesAny(allowedLeft, leftItem)) {
                    super.debug("Left slot item does not match any required item, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }

            if (task.hasConfigKey("middle")) {
                List<?> allowedMiddle = (List<?>) task.getConfigValue("middle");
                super.debug("Middle config: " + allowedMiddle + ", actual: " + middleDesc, quest.getId(), task.getId(), player.getUniqueId());
                if (allowedMiddle != null && !allowedMiddle.isEmpty() && !matchesAny(allowedMiddle, middleItem)) {
                    super.debug("Middle slot item does not match any required item, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }

            if (task.hasConfigKey("right")) {
                List<?> allowedRight = (List<?>) task.getConfigValue("right");
                super.debug("Right config: " + allowedRight + ", actual: " + rightDesc, quest.getId(), task.getId(), player.getUniqueId());
                if (allowedRight != null && !allowedRight.isEmpty() && !matchesAny(allowedRight, rightItem)) {
                    super.debug("Right slot item does not match any required item, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }

            super.debug("All slot checks passed, incrementing progress", quest.getId(), task.getId(), player.getUniqueId());

            int amountNeeded = task.getConfigValue("amount") instanceof Number n ? n.intValue() : 1;

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
            super.debug("Updating task progress (now " + progress + "/" + amountNeeded + ")", quest.getId(), task.getId(), player.getUniqueId());

            if (progress >= amountNeeded) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amountNeeded);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amountNeeded);
        }
    }

    private boolean matchesAny(List<?> allowedIds, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        String vanillaId = "minecraft:" + item.getType().name().toLowerCase(Locale.ROOT);

        CustomStack customStack = CustomStack.byItemStack(item);
        String customId = customStack != null ? customStack.getNamespacedID() : null;

        for (Object obj : allowedIds) {
            String id = obj.toString();
            if (id.equalsIgnoreCase(vanillaId)) {
                return true;
            }
            if (customId != null && customId.equalsIgnoreCase(id)) {
                return true;
            }
        }

        return false;
    }

    private String describeItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "empty";
        }
        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack != null) {
            return customStack.getNamespacedID();
        }
        return "minecraft:" + item.getType().name().toLowerCase(Locale.ROOT);
    }
}
