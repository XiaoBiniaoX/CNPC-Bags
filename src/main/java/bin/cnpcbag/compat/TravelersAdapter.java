package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import java.util.function.BiConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class TravelersAdapter implements BackpackAdapter {
    private Class<?> utils() throws ClassNotFoundException {
        return ExternalApi.type("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
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
        Optional<?> attachment = (Optional<?>) ExternalApi.call(utils(), "getAttachment",
                new Class<?>[]{Player.class}, player);
        Object equipped = attachment.orElse(null);
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

    static void mergeInventory(ItemStack source, ItemStack target, DataComponentType<Object> type) {
        Object contents = source.get(type);
        if (contents == null) return;
        try {
            List<?> items = (List<?>) ExternalApi.call(contents, "getItems");
            Object copy = ExternalApi.call(contents.getClass(), "fromItems",
                    new Class<?>[]{int.class, List.class}, items.size(), items);
            target.set(type, copy);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Travelers Backpack storage save failed", e);
        }
    }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        Object wrapper = activeWrapper(player, stack);
        if (wrapper != null) return storage(wrapper, null, () -> {});
        // Construction installs/migrates upgrades and removes components even with a null level.
        // Isolate those side effects, then commit only storage; reads and simulations never commit.
        ItemStack copy = stack.copy();
        @SuppressWarnings("unchecked")
        DataComponentType<Object> type = (DataComponentType<Object>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(
                ResourceLocation.fromNamespaceAndPath(modId(), "backpack_container"));
        if (type == null) return null;
        wrapper = ExternalApi.call(ExternalApi.type("com.tiviacz.travelersbackpack.inventory.BackpackWrapper"),
                "fromStack", new Class<?>[]{ItemStack.class}, copy);
        return wrapper == null ? null : storage(wrapper, stack,
                () -> mergeInventory(copy, stack, type));
    }

    @Override
    public void equipped(Player player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {
        ItemStack stack = (ItemStack) ExternalApi.call(utils(), "getWearingBackpack",
                new Class<?>[]{Player.class}, player);
        if (!stack.isEmpty()) consumer.accept(stack, find(player, stack));
    }
}
