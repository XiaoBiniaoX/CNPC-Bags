package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public final class TravelersAdapter implements BackpackAdapter {
    private Class<?> utils() throws ClassNotFoundException {
        return ExternalApi.type("com.tiviacz.travelersbackpack.capability.CapabilityUtils");
    }

    @Override
    public String modId() { return "travelersbackpack"; }

    private BackpackStorage storage(Object wrapper, Object identity, Runnable save)
            throws ReflectiveOperationException {
        // Use the real storage: automation wrappers can redirect insertion to another slot or void it.
        IItemHandler handler = (IItemHandler) ExternalApi.call(wrapper, "getStorage");
        return new BackpackStorage(handler, identity == null ? handler : identity, save);
    }

    private Object menuWrapper(Player player, ItemStack stack) throws ReflectiveOperationException {
        if (!ExternalApi.type("com.tiviacz.travelersbackpack.inventory.menu.AbstractBackpackMenu")
                .isInstance(player.containerMenu)) return null;
        Object wrapper = ExternalApi.call(player.containerMenu, "getWrapper");
        return wrapper != null && ExternalApi.call(wrapper, "getBackpackStack") == stack ? wrapper : null;
    }

    private Object activeWrapper(Player player, ItemStack stack) throws ReflectiveOperationException {
        Object wrapper = menuWrapper(player, stack);
        if (wrapper != null) return wrapper;
        LazyOptional<?> capability = (LazyOptional<?>) ExternalApi.call(utils(), "getCapability",
                new Class<?>[]{Player.class}, player);
        Object equipped = capability.resolve().orElse(null);
        if (equipped != null && ExternalApi.call(equipped, "getBackpack") == stack) {
            wrapper = ExternalApi.call(equipped, "getWrapper");
            if (wrapper != null && ExternalApi.call(wrapper, "getBackpackStack") == stack) return wrapper;
        }
        // Another player can have this player's worn backpack open.
        if (ExternalApi.call(utils(), "getWearingBackpack", new Class<?>[]{Player.class}, player) == stack) {
            for (Player viewer : player.level().players()) {
                wrapper = menuWrapper(viewer, stack);
                if (wrapper != null) return wrapper;
            }
        }
        return null;
    }

    static void mergeInventory(CompoundTag source, CompoundTag target) {
        if (source != null && source.contains("Inventory", Tag.TAG_COMPOUND)) {
            target.put("Inventory", source.getCompound("Inventory").copy());
        }
    }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        Object wrapper = activeWrapper(player, stack);
        if (wrapper != null) return storage(wrapper, null, () -> {});
        // Construction installs/migrates upgrades and removes their old NBT even with a null level.
        // Isolate those side effects, then commit only storage; reads and simulations never commit.
        ItemStack copy = stack.copy();
        wrapper = ExternalApi.call(ExternalApi.type("com.tiviacz.travelersbackpack.inventory.BackpackWrapper"),
                "fromStack", new Class<?>[]{ItemStack.class}, copy);
        return wrapper == null ? null : storage(wrapper, stack,
                () -> mergeInventory(copy.getTag(), stack.getOrCreateTag()));
    }

    @Override
    public void equipped(Player player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        ItemStack stack = (ItemStack) ExternalApi.call(utils(), "getWearingBackpack",
                new Class<?>[]{Player.class}, player);
        if (!stack.isEmpty()) consumer.accept(stack, find(player, stack));
    }
}
