package bin.cnpcbag.script;

import bin.cnpcbag.backpack.*;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.core.registries.BuiltInRegistries;
import noppes.npcs.api.item.IItemStack;

public final class BackpackScriptService {
    public static BackpackHandle handle(Player player, int index) {
        List<BackpackHandle> packs = BackpackRegistry.discover(player);
        return index >= 0 && index < packs.size() ? packs.get(index) : null;
    }

    public static int getBackpackCount(Player player) { return BackpackRegistry.discover(player).size(); }
    public static boolean hasBackpack(Player player) { return getBackpackCount(player) > 0; }
    public static boolean isBackpackEquipped(Player player, int index) {
        var pack = handle(player, index);
        return pack != null && pack.equipped();
    }
    public static int getEquippedBackpackIndex(Player player) {
        var packs = BackpackRegistry.discover(player);
        for (int i = 0; i < packs.size(); i++) if (packs.get(i).equipped()) return i;
        return -1;
    }
    public static String getBackpackName(Player player, int index) {
        var pack = handle(player, index);
        return pack == null ? "" : pack.carrier().getHoverName().getString();
    }
    public static String getBackpackModId(Player player, int index) {
        var pack = handle(player, index);
        return pack == null ? "" : pack.modId();
    }
    public static int getBackpackSlotCount(Player player, int index) {
        var pack = handle(player, index);
        return pack == null ? -1 : pack.storage().handler().getSlots();
    }
    public static int getBackpackFreeSlots(Player player, int index) {
        var pack = handle(player, index);
        if (pack == null) return -1;
        var handler = pack.storage().handler();
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) if (handler.getStackInSlot(slot).isEmpty()) total++;
        return total;
    }
    public static ScriptBackpack getBackpack(Player player, int index) {
        return handle(player, index) == null ? null : new ScriptBackpack(player, index);
    }
    public static String getAllBackpackContents(Player player) {
        var packs = BackpackRegistry.discover(player);
        if (packs.isEmpty()) return "No backpacks.";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < packs.size(); i++) {
            var pack = packs.get(i);
            var inventory = pack.storage().handler();
            result.append("Backpack ").append(i).append(": ").append(pack.carrier().getHoverName().getString())
                    .append(" [").append(pack.modId()).append("] equipped=").append(pack.equipped())
                    .append(" slots=").append(inventory.getSlots()).append('\n');
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.isEmpty()) result.append("  [").append(slot).append("] ").append(stack.getCount())
                        .append("x ").append(BuiltInRegistries.ITEM.getKey(stack.getItem())).append(" ")
                        .append(stack.getHoverName().getString()).append('\n');
            }
        }
        return result.toString();
    }
    public static boolean hasItemInBackpack(Player player, String id, int count) {
        ItemStack query = ScriptItems.byId(id, count, -1);
        return !query.isEmpty() && countAll(player, query, true, true, false, false) >= count;
    }
    private static int countAll(Player player, ItemStack query, boolean ignoreDamage,
                                boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        long total = 0;
        for (var pack : BackpackRegistry.discover(player)) total += InventoryOps.count(
                pack.storage().handler(), query, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
    public static int countItemInBackpack(Player player, int index, IItemStack stack, boolean ignoreDamage,
                                        boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        var pack = handle(player, index);
        return pack == null ? 0 : InventoryOps.count(pack.storage().handler(), ScriptItems.unwrap(stack),
                ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }
    public static int countItemInAllBackpacks(Player player, IItemStack stack, boolean ignoreDamage,
                                            boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return countAll(player, ScriptItems.unwrap(stack), ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }
    public static boolean hasItemInAllBackpacks(Player player, IItemStack stack, boolean ignoreDamage,
                                              boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        ItemStack query = ScriptItems.unwrap(stack);
        return !query.isEmpty() && countAll(player, query, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) >= query.getCount();
    }
    public static boolean canAddItemToBackpack(Player player, int index, IItemStack stack) {
        var pack = handle(player, index);
        var input = ScriptItems.unwrap(stack);
        return pack != null && !input.isEmpty() && InventoryOps.insert(pack.storage().handler(), input, true).isEmpty();
    }
    public static int removeItemFromBackpack(Player player, int index, IItemStack stack, int count,
                                            boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        var pack = handle(player, index);
        if (pack == null) return 0;
        int removed = InventoryOps.remove(pack.storage().handler(), ScriptItems.unwrap(stack), count,
                ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
        if (removed > 0) pack.changed();
        return removed;
    }
    public static int removeItemFromBackpacks(Player player, IItemStack stack, int count,
                                             boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        if (count <= 0) return 0;
        int remaining = count;
        ItemStack query = ScriptItems.unwrap(stack);
        for (var pack : BackpackRegistry.discover(player)) {
            int removed = InventoryOps.remove(pack.storage().handler(), query, remaining,
                    ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
            if (removed > 0) pack.changed();
            remaining -= removed;
            if (remaining == 0) break;
        }
        return count - remaining;
    }
    public static int addItemToBackpack(Player player, int index, IItemStack stack, int count) {
        if (count <= 0) return 0;
        var pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        if (pack == null || input.isEmpty()) return count;
        input.setCount(count);
        ItemStack rest = InventoryOps.insert(pack.storage().handler(), input, false);
        if (rest.getCount() < count) pack.changed();
        return rest.getCount();
    }
    public static int addItemToBackpacks(Player player, IItemStack stack, int count) {
        if (count <= 0) return 0;
        ItemStack remaining = ScriptItems.unwrap(stack);
        if (remaining.isEmpty()) return count;
        remaining.setCount(count);
        for (var pack : BackpackRegistry.discover(player)) {
            int before = remaining.getCount();
            remaining = InventoryOps.insert(pack.storage().handler(), remaining, false);
            if (remaining.getCount() < before) pack.changed();
            if (remaining.isEmpty()) break;
        }
        return remaining.getCount();
    }
    public static int removeItemFromBackpack(Player player, int index, String id, int count, int damage, boolean ignoreNBT) {
        return removeItemFromBackpack(player, index, ScriptItems.wrap(ScriptItems.byId(id, 1, damage)), count,
                damage < 0, ignoreNBT, false, false);
    }
    /** Legacy string overload returns inserted count; IItemStack overloads return remainder count. */
    public static int addItemToBackpack(Player player, int index, String id, int count, int damage) {
        ItemStack input = ScriptItems.byId(id, count, damage);
        return input.isEmpty() ? 0 : count - addItemToBackpack(player, index, ScriptItems.wrap(input), count);
    }
    public static IItemStack getBackpackItem(Player player, int index, int slot) {
        var pack = handle(player, index);
        return pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot) ? null
                : ScriptItems.wrap(pack.storage().handler().getStackInSlot(slot));
    }
    public static boolean setBackpackItem(Player player, int index, int slot, IItemStack stack) {
        var pack = handle(player, index);
        if (pack == null) return false;
        var handler = pack.storage().handler();
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
        if (!rest.isEmpty() && handler instanceof IItemHandlerModifiable mutable) {
            ItemStack current = handler.getStackInSlot(slot).copy();
            if (current.isEmpty()) current = rest.copy(); else current.grow(rest.getCount());
            mutable.setStackInSlot(slot, current);
        } else if (!rest.isEmpty()) throw new IllegalStateException("Backpack handler rejected rollback");
    }
    public static int[] findItemInBackpacks(Player player, IItemStack stack, boolean ignoreDamage,
                                           boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        var packs = BackpackRegistry.discover(player);
        ItemStack query = ScriptItems.unwrap(stack);
        for (int index = 0; index < packs.size(); index++) {
            var handler = packs.get(index).storage().handler();
            for (int slot = 0; slot < handler.getSlots(); slot++) if (InventoryOps.matches(query,
                    handler.getStackInSlot(slot), ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck)) return new int[]{index, slot};
        }
        return new int[]{-1, -1};
    }
    public static int getSlots(Player player) {
        long count = 0;
        for (var pack : BackpackRegistry.discover(player)) count += pack.storage().handler().getSlots();
        return (int) Math.min(count, Integer.MAX_VALUE);
    }
    public static int[] locate(Player player, int globalSlot) {
        if (globalSlot < 0) return new int[]{-1, -1};
        var packs = BackpackRegistry.discover(player);
        for (int i = 0; i < packs.size(); i++) {
            int slots = packs.get(i).storage().handler().getSlots();
            if (globalSlot < slots) return new int[]{i, globalSlot};
            globalSlot -= slots;
        }
        return new int[]{-1, -1};
    }
    public static IItemStack insertItem(Player player, int index, int slot, IItemStack stack, boolean simulate) {
        var pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        if (pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot)) return ScriptItems.wrap(input);
        ItemStack rest = pack.storage().handler().insertItem(slot, input, simulate);
        if (!simulate && rest.getCount() < input.getCount()) pack.changed();
        return ScriptItems.wrap(rest);
    }
    public static IItemStack extractItem(Player player, int index, int slot, int amount, boolean simulate) {
        var pack = handle(player, index);
        if (pack == null || amount <= 0 || !InventoryOps.validSlot(pack.storage().handler(), slot)) return null;
        ItemStack extracted = pack.storage().handler().extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) pack.changed();
        return ScriptItems.wrap(extracted);
    }
    public static int getSlotLimit(Player player, int index, int slot) {
        var pack = handle(player, index);
        return pack == null || !InventoryOps.validSlot(pack.storage().handler(), slot) ? -1
                : pack.storage().handler().getSlotLimit(slot);
    }
    public static boolean isItemValid(Player player, int index, int slot, IItemStack stack) {
        var pack = handle(player, index);
        ItemStack input = ScriptItems.unwrap(stack);
        return pack != null && !input.isEmpty() && InventoryOps.validSlot(pack.storage().handler(), slot)
                && pack.storage().handler().isItemValid(slot, input);
    }
    private BackpackScriptService() {}
}
