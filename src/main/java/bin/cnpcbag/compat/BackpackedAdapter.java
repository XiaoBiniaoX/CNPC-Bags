package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.*;
import java.util.function.BiConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class BackpackedAdapter implements BackpackAdapter {
    private static final String PREFIX = "com.mrcrayfish.backpacked.";
    @Override public String modId() { return "backpacked"; }
    private ItemStack wearing(EntityPlayer player) throws ReflectiveOperationException {
        return (ItemStack) ExternalApi.call(ExternalApi.type(PREFIX + "Backpacked"), "getBackpackStack",
                new Class<?>[]{EntityPlayer.class}, player);
    }
    @Override public void equipped(EntityPlayer player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        ItemStack stack = wearing(player);
        if (stack != null && !stack.isEmpty()) consumer.accept(stack, find(player, stack));
    }
    @Override public BackpackStorage find(EntityPlayer player, ItemStack stack) throws ReflectiveOperationException {
        if (ExternalApi.field(ExternalApi.type(PREFIX + "core.ModItems"), "backpack") != stack.getItem()) return null;
        int rows = ((Number) ExternalApi.field(ExternalApi.type(PREFIX + "Backpacked"), "backpackInventorySize")).intValue();
        int slots = Math.max(1, Math.min(6, rows)) * 9;
        IInventory live = null;
        if (wearing(player) == stack && ExternalApi.type(PREFIX + "inventory.container.BackpackContainer")
                .isInstance(player.openContainer)) {
            for (Slot slot : player.openContainer.inventorySlots) {
                if (ExternalApi.type(PREFIX + "inventory.BackpackInventory").isInstance(slot.inventory)) {
                    live = slot.inventory; break;
                }
            }
        }
        final IInventory inventory = live;
        return NbtBackpackStorage.create(stack, 0, slots,
                NbtBackpackStorage.slotRules(PREFIX + "inventory.container.slot.BackpackSlot"),
                inventory == null ? null : inventory::getStackInSlot,
                inventory == null ? null : inventory::setInventorySlotContents);
    }
}
