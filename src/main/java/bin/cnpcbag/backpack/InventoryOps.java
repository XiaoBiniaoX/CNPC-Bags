package bin.cnpcbag.backpack;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import noppes.npcs.NoppesUtilPlayer;

public final class InventoryOps {
    public static boolean matches(ItemStack expected, ItemStack actual, boolean ignoreDamage,
                                  boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return !expected.isEmpty() && !actual.isEmpty()
                && NoppesUtilPlayer.compareItems(expected, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    public static int count(IItemHandler handler, ItemStack query, boolean ignoreDamage,
                            boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        long total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack actual = handler.getStackInSlot(slot);
            if (matches(query, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck)) total += actual.getCount();
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    public static int remove(IItemHandler handler, ItemStack query, int amount, boolean ignoreDamage,
                             boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        if (amount <= 0 || query.isEmpty()) return 0;
        int remaining = amount;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            // Some handlers cap one extraction to one normal stack even with larger slot limits.
            while (remaining > 0 && matches(query, handler.getStackInSlot(slot),
                    ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck)) {
                ItemStack extracted = handler.extractItem(slot, remaining, false);
                if (extracted.isEmpty()) break;
                remaining -= Math.min(remaining, extracted.getCount());
            }
        }
        return amount - remaining;
    }

    public static ItemStack insert(IItemHandler handler, ItemStack input, boolean simulate) {
        ItemStack remainder = input.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    public static boolean validSlot(IItemHandler handler, int slot) {
        return slot >= 0 && slot < handler.getSlots();
    }

    private InventoryOps() {}
}
