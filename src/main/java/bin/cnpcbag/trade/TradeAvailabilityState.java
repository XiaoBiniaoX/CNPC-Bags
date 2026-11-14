package bin.cnpcbag.trade;

import bin.cnpcbag.backpack.BackpackRegistry;
import java.util.Arrays;
import java.util.List;
import bin.cnpcbag.backpack.BackpackHandle;
import bin.cnpcbag.backpack.InventoryOps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.containers.ContainerNPCTrader;

public final class TradeAvailabilityState implements TraderAvailability {
    private final ContainerNPCTrader menu;
    private final EntityPlayer player;
    private final int[] values = new int[18];
    private final ItemStack[] previousCosts = new ItemStack[36];
    private int lastCheck = Integer.MIN_VALUE;
    private int lastUpdate = Integer.MIN_VALUE;
    private int previousRules = -1;

    public TradeAvailabilityState(ContainerNPCTrader menu, EntityPlayer player) {
        this.menu = menu;
        this.player = player;
        Arrays.fill(previousCosts, ItemStack.EMPTY);
    }

    @Override
    public int cnpcbag$getAvailability(int offer) {
        if (offer < 0 || offer >= values.length) return 0;
        if (BackpackRegistry.onServer(player)) refresh();
        return values[offer];
    }

    @Override
    public void cnpcbag$setAvailability(int offer, int value) {
        // Clientbound menu data is only a display cache; a server never consumes client-written flags.
        if (player.world.isRemote && offer >= 0 && offer < values.length) values[offer] = value & 135;
    }

    public void invalidate() {
        lastCheck = Integer.MIN_VALUE;
        lastUpdate = Integer.MIN_VALUE;
    }

    private void refresh() {
        int tick = player.ticksExisted;
        if (tick == lastCheck) return;
        lastCheck = tick;
        noppes.npcs.roles.RoleTrader role = menu.role;
        int rules = (role.ignoreDamage ? 1 : 0) | (role.ignoreNBT ? 2 : 0)
               ;
        boolean costsChanged = rules != previousRules;
        for (int slot = 0; slot < previousCosts.length && !costsChanged; slot++) {
            ItemStack cost = role.inventoryCurrency.items.get(slot);
            ItemStack before = previousCosts[slot];
            costsChanged = cost.getCount() != before.getCount() || !InventoryOps.sameStack(cost, before);
        }
        // Share one server snapshot across all offers, and poll backpack changes at most every four ticks.
        if (lastUpdate != Integer.MIN_VALUE && tick - lastUpdate < 4 && !costsChanged) return;
        lastUpdate = tick;
        previousRules = rules;
        List<BackpackHandle> backpacks = BackpackRegistry.discover(player);
        for (int slot = 0; slot < previousCosts.length; slot++) {
            previousCosts[slot] = role.inventoryCurrency.items.get(slot).copy();
        }
        for (int offer = 0; offer < values.length; offer++) {
            ItemStack first = previousCosts[offer];
            ItemStack second = previousCosts[offer + 18];
            boolean canBuy = TradePayment.plan(player, first, second, role.ignoreDamage, role.ignoreNBT,
                    false, false, backpacks) != null;
            boolean firstCovered = canBuy || TradePayment.plan(player, first, ItemStack.EMPTY,
                    role.ignoreDamage, role.ignoreNBT, false, false, backpacks) != null;
            boolean secondCovered = canBuy || TradePayment.plan(player, ItemStack.EMPTY, second,
                    role.ignoreDamage, role.ignoreNBT, false, false, backpacks) != null;
            values[offer] = READY | (canBuy ? CAN_BUY : 0)
                    | (firstCovered ? FIRST_COST : 0) | (secondCovered ? SECOND_COST : 0);
        }
    }
}
