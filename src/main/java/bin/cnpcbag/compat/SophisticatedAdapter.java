package bin.cnpcbag.compat;

import bin.cnpcbag.backpack.BackpackAdapter;
import bin.cnpcbag.backpack.BackpackStorage;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

public final class SophisticatedAdapter implements BackpackAdapter {
    @Override
    public String modId() { return "sophisticatedbackpacks"; }

    @Override
    public BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException {
        Object value = ExternalApi.call(ExternalApi.type(
                "net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper"), "getCapabilityInstance");
        @SuppressWarnings("unchecked")
        Capability<Object> capability = (Capability<Object>) value;
        Object wrapper = stack.getCapability(capability).orElse(null);
        if (wrapper == null) return null;
        IItemHandler inventory = (IItemHandler) ExternalApi.call(wrapper, "getInventoryHandler");
        Optional<?> uuid = (Optional<?>) ExternalApi.call(wrapper, "getContentsUuid");
        // UUID aliases and linked carriers must not count the same storage twice.
        Object identity = uuid.isPresent() ? modId() + ":" + uuid.get() : inventory;
        return new BackpackStorage(inventory, identity, () -> {});
    }
}
