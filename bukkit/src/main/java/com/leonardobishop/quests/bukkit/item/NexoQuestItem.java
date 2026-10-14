package com.leonardobishop.quests.bukkit.item;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

public class NexoQuestItem extends QuestItem {

    private final String nexoId;

    public NexoQuestItem(String id, String nexoId) {
        super("nexo", id);
        this.nexoId = nexoId;
    }

    @Override
    public ItemStack getItemStack() {
        ItemBuilder builder = NexoItems.itemFromId(this.nexoId);
        return builder != null ? builder.build() : null;
    }

    @Override
    public boolean compareItemStack(ItemStack other, boolean exactMatch) {
        final String otherId = NexoItems.idFromItem(other);
        return this.nexoId.equals(otherId);
    }
}
