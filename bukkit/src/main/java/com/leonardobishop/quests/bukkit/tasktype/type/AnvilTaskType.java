package com.leonardobishop.quests.bukkit.tasktype.type;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.item.QuestItem;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.bukkit.util.constraint.TaskConstraintSet;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AnvilTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;
    private final Table<String, String, List<QuestItem>> fixedInputCache = HashBasedTable.create();
    private final Table<String, String, List<QuestItem>> fixedOutputCache = HashBasedTable.create();

    public AnvilTaskType(BukkitQuestsPlugin plugin) {
        super("anvil", TaskUtils.TASK_ATTRIBUTION_STRING, "Use an anvil with specific input and/or output items.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useBooleanConfigValidator(this, "exact-match"));
    }

    @Override
    public void onReady() {
        fixedInputCache.clear();
        fixedOutputCache.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL
                || event.getSlotType() != InventoryType.SlotType.RESULT
                || event.getRawSlot() != 2
                || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
                || event.getAction() == InventoryAction.NOTHING
                || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD && event.getClick() == ClickType.NUMBER_KEY && !plugin.getVersionSpecificHandler().isHotbarMoveAndReaddSupported()
                || event.getAction() == InventoryAction.DROP_ONE_SLOT && event.getClick() == ClickType.DROP && (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
                || event.getAction() == InventoryAction.DROP_ALL_SLOT && event.getClick() == ClickType.CONTROL_DROP && (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
                || event.getAction() == InventoryAction.UNKNOWN && event.getClick() == ClickType.UNKNOWN
                || !(event.getWhoClicked() instanceof Player player)
                || plugin.getVersionSpecificHandler().isOffHandSwap(event.getClick()) && !plugin.getVersionSpecificHandler().isOffHandEmpty(player)) {
            return;
        }

        QPlayer qPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);
        ItemStack resultItem = event.getCurrentItem();
        int eventAmount = resultItem.getAmount();

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this, TaskConstraintSet.ALL)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            boolean exactMatch = TaskUtils.getConfigBoolean(task, "exact-match", true);

            if (task.hasConfigKey("input")) {
                List<QuestItem> inputItems = getQuestItemList(fixedInputCache, quest, task, "input");

                boolean firstMatches = firstItem != null && matchesAny(inputItems, firstItem, exactMatch);
                boolean secondMatches = secondItem != null && matchesAny(inputItems, secondItem, exactMatch);
                if (!firstMatches && !secondMatches) {
                    super.debug("Neither input slot matches any required input, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }

            if (task.hasConfigKey("output")) {
                List<QuestItem> outputItems = getQuestItemList(fixedOutputCache, quest, task, "output");

                if (!matchesAny(outputItems, resultItem, exactMatch)) {
                    super.debug("Output item does not match any required output, continuing...", quest.getId(), task.getId(), player.getUniqueId());
                    continue;
                }
            }

            super.debug("Player used anvil (first = " + (firstItem != null ? firstItem.getType() : "null") + ", second = " + (secondItem != null ? secondItem.getType() : "null") + ", output = " + resultItem.getType() + ")", quest.getId(), task.getId(), player.getUniqueId());

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress, eventAmount);
            super.debug("Updating task progress (now " + (progress + eventAmount) + ")", quest.getId(), task.getId(), player.getUniqueId());

            int amount = (int) task.getConfigValue("amount");

            if ((int) taskProgress.getProgress() >= amount) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                taskProgress.setProgress(amount);
                taskProgress.setCompleted(true);
            }

            TaskUtils.sendTrackAdvancement(player, quest, task, pendingTask, amount);
        }
    }

    private List<QuestItem> getQuestItemList(Table<String, String, List<QuestItem>> cache, Quest quest, Task task, String key) {
        List<QuestItem> cached = cache.get(quest.getId(), task.getId());
        if (cached != null) {
            return cached;
        }

        List<QuestItem> items = new ArrayList<>();
        Object configValue = task.getConfigValue(key);

        if (configValue instanceof ConfigurationSection section) {
            for (String subKey : section.getKeys(false)) {
                ConfigurationSection itemSection = section.getConfigurationSection(subKey);
                if (itemSection != null) {
                    items.add(plugin.getConfiguredQuestItem("", itemSection));
                }
            }
        }

        if (items.isEmpty()) {
            items.add(TaskUtils.getConfigQuestItem(task, key, "data"));
        }

        cache.put(quest.getId(), task.getId(), items);
        return items;
    }

    private boolean matchesAny(List<QuestItem> questItems, ItemStack item, boolean exactMatch) {
        for (QuestItem qi : questItems) {
            if (qi.compareItemStack(item, exactMatch)) {
                return true;
            }
        }
        return false;
    }
}
