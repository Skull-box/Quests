package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.quest.Task;
import fr.elias.npcs.events.NPCInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

public final class ModeledNPCDeliverTaskType extends DeliverTaskType<Integer> {

    private final BukkitQuestsPlugin plugin;

    public ModeledNPCDeliverTaskType(BukkitQuestsPlugin plugin) {
        super("modelednpc_deliver", "DotDebian", "Deliver a set of items to a Modeled NPC.");
        this.plugin = plugin;

        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "npc-id"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onModeledNPCClick(NPCInteractEvent event) {
        checkInventory(event.getPlayer(), event.getNPCData().getId(), event.getNPCData().getName(), 1L, plugin);
    }

    @Override
    public List<Integer> getNPCId(Task task) {
        return TaskUtils.getConfigIntegerList(task, "npc-id");
    }
}