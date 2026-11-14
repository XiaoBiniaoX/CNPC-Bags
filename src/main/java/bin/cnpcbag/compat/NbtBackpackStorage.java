package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackStorage;
import java.util.function.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.items.ItemStackHandler;

/** Only managed storage slots are merged; tanks, tools, crafting and unknown tags remain intact. */
public final class NbtBackpackStorage extends ItemStackHandler {
    private final ItemStack carrier;
    private final int offset;
    private final Predicate<ItemStack> accepts;
    private final IntFunction<ItemStack> liveRead;
    private final BiConsumer<Integer, ItemStack> liveWrite;

    private NbtBackpackStorage(ItemStack carrier, int offset, int size, Predicate<ItemStack> accepts,
                              IntFunction<ItemStack> liveRead, BiConsumer<Integer, ItemStack> liveWrite) {
        super(size);
        this.carrier = carrier; this.offset = offset; this.accepts = accepts;
        this.liveRead = liveRead; this.liveWrite = liveWrite;
        NBTTagCompound root = carrier.getTagCompound();
        if (root != null) {
            NBTTagList list = root.getTagList("Items", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                int slot = (entry.getByte("Slot") & 255) - offset;
                if (slot >= 0 && slot < size) stacks.set(slot, new ItemStack(entry));
            }
        }
    }

    public static BackpackStorage create(ItemStack stack, int offset, int size, Predicate<ItemStack> accepts,
                                         IntFunction<ItemStack> read, BiConsumer<Integer, ItemStack> write) {
        NbtBackpackStorage handler = new NbtBackpackStorage(stack, offset, size, accepts, read, write);
        return new BackpackStorage(handler, stack, handler::save);
    }

    public static Predicate<ItemStack> slotRules(String slotClass) throws ReflectiveOperationException {
        Slot slot = (Slot) ExternalApi.type(slotClass).getConstructor(IInventory.class,
                int.class, int.class, int.class).newInstance(new InventoryBasic("cnpcbag", false, 1), 0, 0, 0);
        return slot::isItemValid;
    }

    private void refresh(int slot) {
        validateSlotIndex(slot);
        if (liveRead != null) stacks.set(slot, liveRead.apply(slot + offset).copy());
    }
    @Override public ItemStack getStackInSlot(int slot) { refresh(slot); return super.getStackInSlot(slot); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        refresh(slot); return super.insertItem(slot, stack, simulate);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        refresh(slot); return super.extractItem(slot, amount, simulate);
    }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        return accepts.test(stack);
    }
    @Override protected void onContentsChanged(int slot) {
        if (liveWrite != null) liveWrite.accept(slot + offset, stacks.get(slot).copy());
    }

    private void save() {
        NBTTagCompound root = carrier.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); carrier.setTagCompound(root); }
        NBTTagList before = root.getTagList("Items", 10);
        NBTTagList after = new NBTTagList();
        for (int i = 0; i < before.tagCount(); i++) {
            NBTTagCompound entry = before.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < offset || slot >= offset + getSlots()) after.appendTag(entry.copy());
        }
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty()) {
                NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
                entry.setByte("Slot", (byte) (slot + offset));
                after.appendTag(entry);
            }
        }
        root.setTag("Items", after);
    }
}
