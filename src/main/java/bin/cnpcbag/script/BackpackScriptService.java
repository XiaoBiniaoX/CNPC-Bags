package bin.cnpcbag.script;

import bin.cnpcbag.backpack.*;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.api.item.IItemStack;

public final class BackpackScriptService {
    public static BackpackHandle handle(EntityPlayer player, int index) {
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        return index >= 0 && index < packs.size() ? packs.get(index) : null;
    }

    public static int getBackpackCount(EntityPlayer player) { return BackpackRegistry.discover(player).size(); }
    public static boolean hasBackpack(EntityPlayer player) { return getBackpackCount(player) > 0; }
    public static boolean isBackpackEquipped(EntityPlayer player, int index) {
        BackpackHandle pack = handle(player, index);
        return pack != null && pack.equipped();
    }
    public static int getEquippedBackpackIndex(EntityPlayer player) {
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        for (int i = 0; i < packs.size(); i++) if (packs.get(i).equipped()) return i;
        return -1;
    }
    public static String getBackpackName(EntityPlayer player, int index) {
        BackpackHandle pack = handle(player, index);
        return pack == null ? "" : pack.carrier().getDisplayName();
    }
    public static String getBackpackModId(EntityPlayer player, int index) {
        BackpackHandle pack = handle(player, index);
        return pack == null ? "" : pack.modId();
    }
    public static int getBackpackSlotCount(EntityPlayer player, int index) {
        BackpackHandle pack = handle(player, index);
        return pack == null ? -1 : pack.storage().handler().getSlots();
    }
    public static int getBackpackFreeSlots(EntityPlayer player, int index) {
        BackpackHandle pack = handle(player, index);
        if (pack == null) return -1;
        IItemHandler handler = pack.storage().handler();
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) if (handler.getStackInSlot(slot).isEmpty()) total++;
        return total;
    }
    public static ScriptBackpack getBackpack(EntityPlayer player, int index) {
        return handle(player, index) == null ? null : new ScriptBackpack(player, index);
    }
    public static String getAllBackpackContents(EntityPlayer player) {
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        if (packs.isEmpty()) return "No backpacks.";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < packs.size(); i++) {
            BackpackHandle pack = packs.get(i);
            IItemHandler inventory = pack.storage().handler();
            result.append("Backpack ").append(i).append(": ").append(pack.carrier().getDisplayName())
                    .append(" [").append(pack.modId()).append("] equipped=").append(pack.equipped())
                    .append(" slots=").append(inventory.getSlots()).append('\n');
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.isEmpty()) result.append("  [").append(slot).append("] ").append(stack.getCount())
                        .append("x ").append(ForgeRegistries.ITEMS.getKey(stack.getItem())).append(" ")
                        .append(stack.getDisplayName()).append('\n');
            }
        }
        return result.toString();
    }
    public static boolean hasItemInBackpack(EntityPlayer player, String id, int count) {
        ItemStack query = ScriptItems.byId(id, count, -1);
        return !query.isEmpty() && countAll(player, query, true, true, false, false) >= count;
    }
    private static int countAll(EntityPlayer player, ItemStack query, boolean ignoreDamage,
                                boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        long total = 0;
        for (BackpackHandle pack : BackpackRegistry.discover(player)) total += InventoryOps.count(
                pack.storage().handler(), query, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
    public static int countItemInBackpack(EntityPlayer player, int index, IItemStack stack, boolean ignoreDamage,
                                        boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        BackpackHandle pack = handle(player, index);
        return pack == null ? 0 : InventoryOps.count(pack.storage().handler(), ScriptItems.unwrap(stack),
                ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }
    public static int countItemInAllBackpacks(EntityPlayer player, IItemStack stack, boolean ignoreDamage,
                                            boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return countAll(player, ScriptItems.unwrap(stack), ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }
    public static boolean hasItemInAllBackpacks(EntityPlayer player, IItemStack stack, boolean ignoreDamage,
                                              boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        ItemStack query = ScriptItems.unwrap(stack);
        return !query.isEmpty() && countAll(player, query, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) >= query.getCount();
    }
    public static boolean canAddItemToBackpack(EntityPlayer player, int index, IItemStack stack) {
        BackpackHandle pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        return pack != null && !input.isEmpty() && InventoryOps.insert(pack.storage().handler(), input, true).isEmpty();
    }
    public static int removeItemFromBackpack(EntityPlayer player, int index, IItemStack stack, int count,
                                            boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        BackpackHandle pack = handle(player, index);
        if (pack == null) return 0;
        int removed = InventoryOps.remove(pack.storage().handler(), ScriptItems.unwrap(stack), count,
                ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
        if (removed > 0) pack.changed();
        return removed;
    }
    public static int removeItemFromBackpacks(EntityPlayer player, IItemStack stack, int count,
                                             boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        if (count <= 0) return 0;
        int remaining = count;
        ItemStack query = ScriptItems.unwrap(stack);
        for (BackpackHandle pack : BackpackRegistry.discover(player)) {
            int removed = InventoryOps.remove(pack.storage().handler(), query, remaining,
                    ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
            if (removed > 0) pack.changed();
            remaining -= removed;
            if (remaining == 0) break;
        }
        return count - remaining;
    }
    public static int addItemToBackpack(EntityPlayer player, int index, IItemStack stack, int count) {
        if (count <= 0) return 0;
        BackpackHandle pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        if (pack == null || input.isEmpty()) return count;
        input.setCount(count);
        ItemStack rest = InventoryOps.insert(pack.storage().handler(), input, false);
        if (rest.getCount() < count) pack.changed();
        return rest.getCount();
    }
    public static int addItemToBackpacks(EntityPlayer player, IItemStack stack, int count) {
        if (count <= 0) return 0;
        ItemStack remaining = ScriptItems.unwrap(stack);
        if (remaining.isEmpty()) return count;
        remaining.setCount(count);
        for (BackpackHandle pack : BackpackRegistry.discover(player)) {
            int before = remaining.getCount();
            remaining = InventoryOps.insert(pack.storage().handler(), remaining, false);
            if (remaining.getCount() < before) pack.changed();
            if (remaining.isEmpty()) break;
        }
        return remaining.getCount();
    }
    public static int removeItemFromBackpack(EntityPlayer player, int index, String id, int count, int damage, boolean ignoreNBT) {
        return removeItemFromBackpack(player, index, ScriptItems.wrap(ScriptItems.byId(id, 1, damage)), count,
                damage < 0, ignoreNBT, false, false);
    }
    /** Legacy string overload returns inserted count; IItemStack overloads return remainder count. */
    public static int addItemToBackpack(EntityPlayer player, int index, String id, int count, int damage) {
        ItemStack input = ScriptItems.byId(id, count, damage);
        return input.isEmpty() ? 0 : count - addItemToBackpack(player, index, ScriptItems.wrap(input), count);
    }
    public static IItemStack getBackpackItem(EntityPlayer player, int index, int slot) {
        BackpackHandle pack = handle(player, index);
        return pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot) ? null
                : ScriptItems.wrap(pack.storage().handler().getStackInSlot(slot));
    }
    public static boolean setBackpackItem(EntityPlayer player, int index, int slot, IItemStack stack) {
        BackpackHandle pack = handle(player, index);
        if (pack == null) return false;
        IItemHandler handler = pack.storage().handler();
        ItemStack input = ScriptItems.unwrap(stack);
        if (!InventoryOps.validSlot(handler, slot) || (!input.isEmpty()
                && (!handler.isItemValid(slot, input) || input.getCount() > handler.getSlotLimit(slot)))) return false;
        ItemStack before = handler.getStackInSlot(slot).copy();
        // Replace via the public extraction/insertion contract, including per-item limits.
        ItemStack removed = ItemStack.EMPTY;
        while (!handler.getStackInSlot(slot).isEmpty()) {
            ItemStack part = handler.extractItem(slot, Integer.MAX_VALUE, false);
            if (part.isEmpty()) {
                restore(handler, slot, removed);
                pack.changed();
                return false;
            }
            if (removed.isEmpty()) removed = part.copy(); else removed.grow(part.getCount());
        }
        if (!input.isEmpty() && !handler.insertItem(slot, input.copy(), true).isEmpty()) {
            restore(handler, slot, before);
            pack.changed();
            return false;
        }
        ItemStack rest = input.isEmpty() ? ItemStack.EMPTY : handler.insertItem(slot, input.copy(), false);
        if (!rest.isEmpty()) {
            ItemStack inserted = handler.extractItem(slot, input.getCount() - rest.getCount(), false);
            if (inserted.getCount() != input.getCount() - rest.getCount()) {
                throw new IllegalStateException("Backpack handler violated insertion simulation");
            }
            restore(handler, slot, before);
            pack.changed();
            return false;
        }
        pack.changed();
        return true;
    }
    private static void restore(IItemHandler handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack rest = handler.insertItem(slot, stack.copy(), false);
        if (!rest.isEmpty() && handler instanceof IItemHandlerModifiable) {
            ItemStack current = handler.getStackInSlot(slot).copy();
            if (current.isEmpty()) current = rest.copy(); else current.grow(rest.getCount());
            ((IItemHandlerModifiable) handler).setStackInSlot(slot, current);
        } else if (!rest.isEmpty()) throw new IllegalStateException("Backpack handler rejected rollback");
    }
    public static int[] findItemInBackpacks(EntityPlayer player, IItemStack stack, boolean ignoreDamage,
                                           boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        ItemStack query = ScriptItems.unwrap(stack);
        for (int index = 0; index < packs.size(); index++) {
            IItemHandler handler = packs.get(index).storage().handler();
            for (int slot = 0; slot < handler.getSlots(); slot++) if (InventoryOps.matches(query,
                    handler.getStackInSlot(slot), ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck)) return new int[]{index, slot};
        }
        return new int[]{-1, -1};
    }
    public static int getSlots(EntityPlayer player) {
        long count = 0;
        for (BackpackHandle pack : BackpackRegistry.discover(player)) count += pack.storage().handler().getSlots();
        return (int) Math.min(count, Integer.MAX_VALUE);
    }
    public static int[] locate(EntityPlayer player, int globalSlot) {
        if (globalSlot < 0) return new int[]{-1, -1};
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        for (int i = 0; i < packs.size(); i++) {
            int slots = packs.get(i).storage().handler().getSlots();
            if (globalSlot < slots) return new int[]{i, globalSlot};
            globalSlot -= slots;
        }
        return new int[]{-1, -1};
    }
    public static IItemStack insertItem(EntityPlayer player, int index, int slot, IItemStack stack, boolean simulate) {
        BackpackHandle pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        if (pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot)) return ScriptItems.wrap(input);
        ItemStack rest = pack.storage().handler().insertItem(slot, input, simulate);
        if (!simulate && rest.getCount() < input.getCount()) pack.changed();
        return ScriptItems.wrap(rest);
    }
    public static IItemStack extractItem(EntityPlayer player, int index, int slot, int amount, boolean simulate) {
        BackpackHandle pack = handle(player, index);
        if (pack == null || amount <= 0 || !InventoryOps.validSlot(pack.storage().handler(), slot)) return null;
        ItemStack extracted = pack.storage().handler().extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) pack.changed();
        return ScriptItems.wrap(extracted);
    }
    public static int getSlotLimit(EntityPlayer player, int index, int slot) {
        BackpackHandle pack = handle(player, index);
        return pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot) ? -1
                : pack.storage().handler().getSlotLimit(slot);
    }
    public static boolean isItemValid(EntityPlayer player, int index, int slot, IItemStack stack) {
        BackpackHandle pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        return pack != null && !input.isEmpty() && InventoryOps.validSlot(pack.storage().handler(), slot)
                && pack.storage().handler().isItemValid(slot, input);
    }
    private BackpackScriptService() {}
}
