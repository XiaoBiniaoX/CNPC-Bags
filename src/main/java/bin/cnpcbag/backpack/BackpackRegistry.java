package bin.cnpcbag.backpack;

import bin.cnpcbag.CnpcBag;
import bin.cnpcbag.compat.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/** A fresh snapshot per operation avoids retaining detached inventories after a player swaps a backpack. */
public final class BackpackRegistry {
    private static final List<BackpackAdapter> ADAPTERS = List.of(
            new SophisticatedAdapter(), new TravelersAdapter(), new BackpackedAdapter(), new UsefulAdapter());
    private static final Set<String> WARNED = new HashSet<>();

    public static boolean onServer(Player player) {
        return player instanceof ServerPlayer && player.getServer() != null
                && player.getServer().isSameThread();
    }

    public static List<BackpackHandle> discover(Player player) {
        if (!onServer(player)) return List.of();
        List<BackpackHandle> found = new ArrayList<>();
        Set<ItemStack> carriers = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Object> inventories = new HashSet<>();
        // Real equipped inventories take precedence over item-derived copies.
        for (BackpackAdapter adapter : ADAPTERS) {
            if (!ModList.get().isLoaded(adapter.modId())) continue;
            try {
                adapter.equipped(player, (stack, storage) ->
                        add(player, stack, storage, true, found, carriers, inventories));
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                warn(adapter.modId(), e);
            }
        }
        CuriosCompat.equipped(player, stack -> find(player, stack, true, found, carriers, inventories));
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            find(player, inventory.getItem(slot), slot >= 36 && slot < 40, found, carriers, inventories);
        }
        return found;
    }

    private static void find(Player player, ItemStack stack, boolean equipped, List<BackpackHandle> found,
                             Set<ItemStack> carriers, Set<Object> inventories) {
        if (stack.isEmpty() || carriers.contains(stack)) return;
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        // Prefer native storage for known mods whose automation capability changes slot semantics.
        if (id != null) {
            for (BackpackAdapter adapter : ADAPTERS) {
                if (!adapter.modId().equals(id.getNamespace()) || !ModList.get().isLoaded(adapter.modId())) continue;
                try {
                    BackpackStorage storage = adapter.find(player, stack);
                    if (storage != null) {
                        add(player, stack, storage, equipped, found, carriers, inventories);
                        return;
                    }
                } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                    warn(adapter.modId(), e);
                }
                // Travelers' generic capability also constructs a wrapper on the original item.
                if ("travelersbackpack".equals(adapter.modId())) return;
            }
        }
        // Every other mod works without knowing its name if it exposes Forge's item handler.
        try {
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler ->
                    add(player, stack, new BackpackStorage(handler), equipped, found, carriers, inventories));
        } catch (RuntimeException | LinkageError e) {
            warn(id == null ? "unknown" : id.getNamespace(), e);
        }
    }

    private static void add(Player player, ItemStack stack, BackpackStorage storage, boolean equipped,
                            List<BackpackHandle> found, Set<ItemStack> carriers, Set<Object> inventories) {
        if (stack == null || stack.isEmpty() || storage == null || storage.handler().getSlots() <= 0
                || !carriers.add(stack)) return;
        if (inventories.add(storage.identity())) found.add(new BackpackHandle(player, stack, storage, equipped));
    }

    private static void warn(String modId, Throwable cause) {
        if (WARNED.add(modId)) CnpcBag.LOGGER.warn("CNPC-Bag adapter failed for {}; {}", modId,
                "travelersbackpack".equals(modId) ? "skipping unsafe generic capability" : "trying generic capability", cause);
    }

    private BackpackRegistry() {}
}
