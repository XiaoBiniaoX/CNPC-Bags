package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;

public final class SophisticatedAdapter implements BackpackAdapter {
    @Override public String modId() { return "retro_sophisticated_backpacks"; }
    @Override public BackpackStorage find(EntityPlayer player, ItemStack stack) throws ReflectiveOperationException {
        Object token = ExternalApi.field(ExternalApi.type(
                "com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities"), "BACKPACK_CAPABILITY");
        if (!(token instanceof Capability)) return null;
        Object wrapper = stack.getCapability((Capability<?>) token, null);
        if (wrapper == null) return null;
        IItemHandler handler = (IItemHandler) ExternalApi.call(wrapper, "getBackpackItemStackHandler");
        Object uuid = ExternalApi.call(wrapper, "getUuid");
        int multiplier = Math.max(1, ((Number) ExternalApi.call(wrapper, "getTotalStackMultiplier")).intValue());
        // This legacy handler's insert override omits validation. Enforce its native slot rules here.
        IItemHandlerModifiable guarded = new IItemHandlerModifiable() {
            @Override public int getSlots() { return handler.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return handler.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack input, boolean simulate) {
                return handler.isItemValid(slot, input) ? handler.insertItem(slot, input, simulate) : input;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return handler.extractItem(slot, amount, simulate);
            }
            @Override public int getSlotLimit(int slot) {
                return (int) Math.min(Integer.MAX_VALUE, (long) handler.getSlotLimit(slot) * multiplier);
            }
            @Override public boolean isItemValid(int slot, ItemStack input) { return handler.isItemValid(slot, input); }
            @Override public void setStackInSlot(int slot, ItemStack input) {
                ((IItemHandlerModifiable) handler).setStackInSlot(slot, input);
            }
        };
        return new BackpackStorage(guarded, uuid == null ? handler : uuid, () -> {});
    }
}
