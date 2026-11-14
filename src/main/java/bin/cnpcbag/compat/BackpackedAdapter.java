package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import java.util.function.BiConsumer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class BackpackedAdapter implements BackpackAdapter {
    @Override
    public String modId() { return "backpacked"; }

    private BackpackStorage storage(Object inventory) {
        return new BackpackStorage(new InvWrapper((Container) inventory), inventory, () -> {
            try {
                ExternalApi.call(inventory, "saveItemsToStack");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Backpacked inventory save failed", e);
            }
        });
    }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        int columns = (Integer) ExternalApi.call(stack.getItem(), "getColumnCount");
        int rows = (Integer) ExternalApi.call(stack.getItem(), "getRowCount");
        Object inventory = ExternalApi.type("com.mrcrayfish.backpacked.inventory.BackpackInventory")
                .getConstructor(int.class, int.class, int.class, Player.class, ItemStack.class)
                .newInstance(-1, columns, rows, player, stack);
        return storage(inventory);
    }

    @Override
    public void equipped(Player player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        Class<?> access = ExternalApi.type("com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess");
        if (!access.isInstance(player)) return;
        int count = (Integer) access.getMethod("backpacked$GetBackpackInventoryCount").invoke(player);
        for (int i = 0; i < count; i++) {
            Object inventory = access.getMethod("backpacked$GetBackpackInventory", int.class).invoke(player, i);
            if (inventory != null) {
                ItemStack stack = (ItemStack) ExternalApi.call(inventory, "getBackpackStack");
                consumer.accept(stack, storage(inventory));
            }
        }
    }
}
