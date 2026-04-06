package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;

public final class TreasureOpenFallbackTaskType extends BukkitTaskType {

    public TreasureOpenFallbackTaskType(BukkitQuestsPlugin plugin) {
        super("treasure_open", "DotDebian", "Open a certain amount of treasure chests.");

        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }
}
