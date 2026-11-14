package bin.cnpcbag.script;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.item.IItemStack;

/** Holds an index, never a stale handler. Empty slots and empty remainders are null. */
public final class ScriptBackpack {
    private final EntityPlayer player;
    private final int index;
    public ScriptBackpack(EntityPlayer player, int index) { this.player = player; this.index = index; }
    public int getSlots() { return BackpackScriptService.getBackpackSlotCount(player, index); }
    public IItemStack getStackInSlot(int slot) { return BackpackScriptService.getBackpackItem(player, index, slot); }
    public IItemStack insertItem(int slot, IItemStack stack, boolean simulate) {
        return BackpackScriptService.insertItem(player, index, slot, stack, simulate);
    }
    public IItemStack extractItem(int slot, int amount, boolean simulate) {
        return BackpackScriptService.extractItem(player, index, slot, amount, simulate);
    }
    public int getSlotLimit(int slot) { return BackpackScriptService.getSlotLimit(player, index, slot); }
    public boolean isItemValid(int slot, IItemStack stack) {
        return BackpackScriptService.isItemValid(player, index, slot, stack);
    }
}
