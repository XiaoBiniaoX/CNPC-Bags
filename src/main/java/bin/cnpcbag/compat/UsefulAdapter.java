package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class UsefulAdapter implements BackpackAdapter {
    private static final String PREFIX = "info.u_team.useful_backpacks.";
    @Override public String modId() { return "usefulbackpacks"; }
    @Override public BackpackStorage find(EntityPlayer player, ItemStack stack) throws ReflectiveOperationException {
        if (!ExternalApi.type(PREFIX + "item.ItemBackPack").isInstance(stack.getItem())) return null;
        Object type = ExternalApi.call(ExternalApi.type(PREFIX + "enums.EnumBackPacks"), "byMetadata",
                new Class<?>[]{int.class}, stack.getMetadata());
        int slots = ((Number) ExternalApi.call(type, "getCount")).intValue();
        IInventory live = null;
        for (Slot slot : player.openContainer.inventorySlots) {
            if (ExternalApi.type(PREFIX + "inventory.InventoryBackPack").isInstance(slot.inventory)
                    && ExternalApi.call(slot.inventory, "getStack") == stack) { live = slot.inventory; break; }
        }
        final IInventory inventory = live;
        return NbtBackpackStorage.create(stack, 0, slots,
                NbtBackpackStorage.slotRules(PREFIX + "container.SlotBackpack"),
                inventory == null ? null : inventory::getStackInSlot,
                inventory == null ? null : inventory::setInventorySlotContents);
    }
}
