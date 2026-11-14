package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import java.util.function.BiConsumer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class BackpackedAdapter implements BackpackAdapter {
    @Override
    public String modId() { return "backpacked"; }

    private BackpackStorage storage(Object inventory) {
        return new BackpackStorage(new InvWrapper((Container) inventory), inventory, () -> {
            try {
                ExternalApi.call(inventory, "saveItemsToStack");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Backpacked inventory save failed", e);
            }
        });
    }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        int columns = (Integer) ExternalApi.call(stack.getItem(), "getColumnCount");
        int rows = (Integer) ExternalApi.call(stack.getItem(), "getRowCount");
        int size = Math.multiplyExact(columns, rows);
        if (size <= 0 || size > 256) return null;
        Object slots = ExternalApi.call(stack.getItem(), "getUnlockableSlots", new Class<?>[]{ItemStack.class}, stack);
        boolean[] unlocked = new boolean[size];
        for (int i = 0; i < size; i++) unlocked[i] = slots == null || (Boolean) ExternalApi.call(slots,
                "isUnlocked", new Class<?>[]{int.class}, i);
        // The native constructor drops locked/trailing items. Reads and simulations must not spawn items.
        // Keep those entries untouched while exposing only the same unlocked slots as the native inventory.
        NonNullList<ItemStack> stored = NonNullList.withSize(256, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(stored);
        ItemStackHandler handler = new ItemStackHandler(stored) {
            private boolean accessible(int slot) { return slot >= 0 && slot < size && unlocked[slot]; }
            @Override public int getSlots() { return size; }
            @Override public ItemStack getStackInSlot(int slot) {
                return accessible(slot) ? super.getStackInSlot(slot) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return accessible(slot) ? 64 : 0; }
            @Override public boolean isItemValid(int slot, ItemStack input) {
                if (!accessible(slot)) return false;
                try {
                    return (Boolean) ExternalApi.call(ExternalApi.type("com.mrcrayfish.backpacked.inventory.BackpackInventory"),
                            "isAllowedItem", new Class<?>[]{ItemStack.class}, input);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Backpacked item validation failed", e);
                }
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return accessible(slot) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public void setStackInSlot(int slot, ItemStack input) {
                if (accessible(slot)) super.setStackInSlot(slot, input);
            }
        };
        return new BackpackStorage(handler, stack,
                () -> stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stored)));
    }

    @Override
    public void equipped(Player player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        Class<?> access = ExternalApi.type("com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess");
        if (!access.isInstance(player)) return;
        int count = (Integer) access.getMethod("backpacked$GetBackpackInventoryCount").invoke(player);
        for (int i = 0; i < count; i++) {
            Object inventory = access.getMethod("backpacked$GetBackpackInventory", int.class).invoke(player, i);
            if (inventory != null) {
                ItemStack stack = (ItemStack) ExternalApi.call(inventory, "getBackpackStack");
                consumer.accept(stack, storage(inventory));
            }
        }
    }
}
