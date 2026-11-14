package bin.cnpcbag.trade;

import bin.cnpcbag.CnpcBag;
import bin.cnpcbag.backpack.*;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** Both costs reserve shared slots once. Items are granted only after all actual extractions succeed. */
public final class TradePayment {
    private record Source(IItemHandler handler, Runnable changed) {}
    private record Candidate(Source source, int slot, ItemStack snapshot, int match) {}
    private record Take(Source source, int slot, ItemStack snapshot, int count) {}
    private record Removed(Take take, ItemStack stack) {}
    private final Player player;
    private final List<Take> takes;

    private TradePayment(Player player, List<Take> takes) {
        this.player = player;
        this.takes = takes;
    }

    public static TradePayment plan(Player player, ItemStack first, ItemStack second, boolean ignoreDamage,
                                   boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return plan(player, first, second, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck,
                BackpackRegistry.discover(player));
    }

    static TradePayment plan(Player player, ItemStack first, ItemStack second, boolean ignoreDamage,
                             boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck,
                             List<BackpackHandle> backpacks) {
        if (!BackpackRegistry.onServer(player)) return null;
        first = first == null ? ItemStack.EMPTY : first;
        second = second == null ? ItemStack.EMPTY : second;
        long[] needed = {first.isEmpty() ? 0 : first.getCount(), second.isEmpty() ? 0 : second.getCount()};
        List<Source> sources = new ArrayList<>();
        sources.add(new Source(new InvWrapper(player.getInventory()), () -> player.getInventory().setChanged()));
        for (var pack : backpacks) sources.add(new Source(pack.storage().handler(), pack::changed));
        List<Candidate> candidates = new ArrayList<>();
        for (Source source : sources) {
            var handler = source.handler();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack actual = handler.getStackInSlot(slot);
                int match = (InventoryOps.matches(first, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) ? 1 : 0)
                        | (InventoryOps.matches(second, actual, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck) ? 2 : 0);
                if (match == 0) continue;
                ItemStack probe = handler.extractItem(slot, Integer.MAX_VALUE, true);
                if (!probe.isEmpty() && ItemStack.isSameItemSameComponents(actual, probe)) {
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
            ItemStack carrier = player.getInventory().getItem(take.slot());
            for (var pack : backpacks) {
                if (pack.carrier() == carrier && takes.stream().anyMatch(
                        t -> t.source().handler() == pack.storage().handler())) return null;
            }
        }
        return new TradePayment(player, takes);
    }

    public boolean belongsTo(Player other) { return player == other; }

    public boolean commit() {
        if (!BackpackRegistry.onServer(player)) return false;
        List<Removed> removed = new ArrayList<>();
        try {
            for (Take take : takes) {
                ItemStack current = take.source().handler().getStackInSlot(take.slot());
                if (current.getCount() != take.snapshot().getCount()
                        || !ItemStack.isSameItemSameComponents(current, take.snapshot())) return false;
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
                    if (!ItemStack.isSameItemSameComponents(actual, take.snapshot()) || actual.getCount() > remaining) {
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
            player.containerMenu.broadcastChanges();
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
            if (!rest.isEmpty()) player.getInventory().placeItemBackInInventory(rest);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
