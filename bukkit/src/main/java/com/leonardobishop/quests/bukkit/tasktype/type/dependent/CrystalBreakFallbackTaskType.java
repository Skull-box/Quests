package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;

public final class CrystalBreakFallbackTaskType extends BukkitTaskType {

    public CrystalBreakFallbackTaskType(BukkitQuestsPlugin plugin) {
        super("crystal_break", "DotDebian", "Break a certain amount of crystals.");

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
    }
}
