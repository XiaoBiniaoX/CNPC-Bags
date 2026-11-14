package bin.cnpcbag.trade;

import bin.cnpcbag.CnpcBag;
import bin.cnpcbag.backpack.*;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/** Both costs reserve shared slots once. Items are granted only after all actual extractions succeed. */
public final class TradePayment {
    private static final class Source {
        private final IItemHandler handler;
        private final Runnable changed;
        Source(IItemHandler handler, Runnable changed) { this.handler = handler; this.changed = changed; }
        IItemHandler handler() { return handler; }
        Runnable changed() { return changed; }
    }
    private static final class Candidate {
        private final Source source;
        private final int slot;
        private final ItemStack snapshot;
        private final int match;
        Candidate(Source source, int slot, ItemStack snapshot, int match) { this.source = source; this.slot = slot; this.snapshot = snapshot; this.match = match; }
        Source source() { return source; }
        int slot() { return slot; }
        ItemStack snapshot() { return snapshot; }
        int match() { return match; }
    }
    private static final class Take {
        private final Source source;
        private final int slot;
        private final ItemStack snapshot;
        private final int count;
        Take(Source source, int slot, ItemStack snapshot, int count) { this.source = source; this.slot = slot; this.snapshot = snapshot; this.count = count; }
        Source source() { return source; }
        int slot() { return slot; }
        ItemStack snapshot() { return snapshot; }
        int count() { return count; }
    }
    private static final class Removed {
        private final Take take;
        private final ItemStack stack;
        Removed(Take take, ItemStack stack) { this.take = take; this.stack = stack; }
        Take take() { return take; }
        ItemStack stack() { return stack; }
    }
    private final EntityPlayer player;
    private final List<Take> takes;

    private TradePayment(EntityPlayer player, List<Take> takes) {
        this.player = player;
        this.takes = takes;
    }

    public static TradePayment plan(EntityPlayer player, ItemStack first, ItemStack second, boolean ignoreDamage,
                                   boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return plan(player, first, second, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck,
                BackpackRegistry.discover(player));
    }

    static TradePayment plan(EntityPlayer player, ItemStack first, ItemStack second, boolean ignoreDamage,
                             boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck,
                             List<BackpackHandle> backpacks) {
        if (!BackpackRegistry.onServer(player)) return null;
        first = first == null ? ItemStack.EMPTY : first;
        second = second == null ? ItemStack.EMPTY : second;
        long[] needed = {first.isEmpty() ? 0 : first.getCount(), second.isEmpty() ? 0 : second.getCount()};
        List<Source> sources = new ArrayList<>();
        sources.add(new Source(new InvWrapper(player.inventory), () -> player.inventory.markDirty()));
        for (BackpackHandle pack : backpacks) sources.add(new Source(pack.storage().handler(), pack::changed));
        List<Candidate> candidates = new ArrayList<>();
        for (Source source : sources) {
            IItemHandler handler = source.handler();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack actual = handler.getStackInSlot(slot);
                int match = (InventoryOps.matches(first, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) ? 1 : 0)
                        | (InventoryOps.matches(second, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) ? 2 : 0);
                if (match == 0) continue;
                ItemStack probe = handler.extractItem(slot, Integer.MAX_VALUE, true);
                if (!probe.isEmpty() && InventoryOps.sameStack(actual, probe)) {
                    candidates.add(new Candidate(source, slot, actual.copy(), match));
                }
            }
        }
        // Consume exclusive matches before shared matches, avoiding a broad first cost stealing a strict second cost.
        candidates.sort(Comparator.comparingInt(c -> c.match() == 3 ? 1 : 0));
        List<Take> takes = new ArrayList<>();
        for (Candidate candidate : candidates) {
            int available = candidate.snapshot().getCount();
            int count = 0;
            for (int cost = 0; cost < 2; cost++) {
                if ((candidate.match() & (1 << cost)) == 0) continue;
                int amount = (int) Math.min(needed[cost], available);
                needed[cost] -= amount;
                available -= amount;
                count += amount;
            }
            if (count > 0) takes.add(new Take(candidate.source(), candidate.slot(), candidate.snapshot(), count));
        }
        if (needed[0] != 0 || needed[1] != 0) return null;
        // A carrier and its contents cannot both be payment: extracting a carrier can detach its inventory.
        for (Take take : takes) {
            if (take.source() != sources.get(0)) continue;
            ItemStack carrier = player.inventory.getStackInSlot(take.slot());
            for (BackpackHandle pack : backpacks) {
                if (pack.carrier() == carrier && takes.stream().anyMatch(
                        t -> t.source().handler() == pack.storage().handler())) return null;
            }
        }
        return new TradePayment(player, takes);
    }

    public boolean belongsTo(EntityPlayer other) { return player == other; }

    public boolean commit() {
        if (!BackpackRegistry.onServer(player)) return false;
        List<Removed> removed = new ArrayList<>();
        try {
            for (Take take : takes) {
                ItemStack current = take.source().handler().getStackInSlot(take.slot());
                if (current.getCount() != take.snapshot().getCount()
                        || !InventoryOps.sameStack(current, take.snapshot())) return false;
            }
            for (Take take : takes) {
                ItemStack collected = ItemStack.EMPTY;
                int remaining = take.count();
                while (remaining > 0) {
                    ItemStack actual = take.source().handler().extractItem(take.slot(), remaining, false);
                    if (actual.isEmpty()) {
                        rollback(removed);
                        return false;
                    }
                    if (!InventoryOps.sameStack(actual, take.snapshot()) || actual.getCount() > remaining) {
                        removed.add(new Removed(take, actual.copy()));
                        rollback(removed);
                        return false;
                    }
                    if (collected.isEmpty()) {
                        collected = actual.copy();
                        removed.add(new Removed(take, collected));
                    } else {
                        collected.grow(actual.getCount());
                    }
                    remaining -= actual.getCount();
                }
            }
            for (Take take : takes) take.source().changed().run();
            player.openContainer.detectAndSendChanges();
            return true;
        } catch (RuntimeException e) {
            CnpcBag.LOGGER.error("CNPC-Bag payment failed; cancelling trade and refunding extracted items", e);
            rollback(removed);
            return false;
        }
    }

    private void rollback(List<Removed> removed) {
        for (int i = removed.size() - 1; i >= 0; i--) {
            Removed refund = removed.get(i);
            ItemStack rest = refund.stack().copy();
            try {
                rest = refund.take().source().handler().insertItem(refund.take().slot(), rest, false);
                refund.take().source().changed().run();
            } catch (RuntimeException e) {
                CnpcBag.LOGGER.error("Backpack refund failed; returning remaining payment to player inventory", e);
            }
            if (!rest.isEmpty()) player.inventory.placeItemBackInInventory(player.world, rest);
        }
        player.inventory.markDirty();
        player.openContainer.detectAndSendChanges();
    }
}
