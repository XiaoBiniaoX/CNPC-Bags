package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class UsefulAdapter implements BackpackAdapter {
    @Override
    public String modId() { return "usefulbackpacks"; }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        Object item = stack.getItem();
        // Exclude non-backpack items from the same namespace.
        Class<?> api = ExternalApi.type("info.u_team.useful_backpacks.item.AutoPickupBackpack");
        if (!api.isInstance(item)) return null;
        SimpleContainer inventory = (SimpleContainer) ExternalApi.call(item, "getInventory",
                new Class<?>[]{ServerPlayer.class, ItemStack.class}, player, stack);
        return new BackpackStorage(new InvWrapper(inventory), inventory, () -> {
            try {
                ExternalApi.call(item, "saveInventory", new Class<?>[]{SimpleContainer.class, ItemStack.class},
                        inventory, stack);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Useful Backpacks inventory save failed", e);
            }
        });
    }
}
