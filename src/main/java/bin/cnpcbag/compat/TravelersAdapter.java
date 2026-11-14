package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.*;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class TravelersAdapter implements BackpackAdapter {
    private static final String PREFIX = "com.tiviacz.travelersbackpack.";
    @Override public String modId() { return "travelersbackpack"; }

    @Override public void equipped(EntityPlayer player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        ItemStack stack = (ItemStack) ExternalApi.call(ExternalApi.type(PREFIX + "capability.CapabilityUtils"),
                "getWearingBackpack", new Class<?>[]{EntityPlayer.class}, player);
        if (stack != null && !stack.isEmpty()) consumer.accept(stack, find(player, stack));
    }

    @Override public BackpackStorage find(EntityPlayer player, ItemStack stack) throws ReflectiveOperationException {
        if (!ExternalApi.type(PREFIX + "items.ItemTravelersBackpack").isInstance(stack.getItem())) return null;
        List<ItemStack> live = null;
        if (ExternalApi.type(PREFIX + "gui.container.ContainerTravelersBackpack").isInstance(player.openContainer)) {
            Object inv = ExternalApi.field(player.openContainer, "inv");
            if (ExternalApi.type(PREFIX + "gui.inventory.InventoryTravelersBackpack").isInstance(inv)
                    && ExternalApi.field(inv, "stack") == stack) {
                @SuppressWarnings("unchecked")
                List<ItemStack> items = (List<ItemStack>) ExternalApi.call(inv, "getInventory");
                live = items;
            }
        }
        final List<ItemStack> inventory = live;
        return NbtBackpackStorage.create(stack, 9, 39,
                NbtBackpackStorage.slotRules(PREFIX + "gui.container.slots.SlotBackpack"),
                inventory == null ? null : inventory::get, inventory == null ? null : inventory::set);
    }
}
