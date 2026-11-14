package bin.cnpcbag.compat;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {
    public static void equipped(Player player, Consumer<ItemStack> consumer) {
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
            inventory.getCurios().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> {
                    var stacks = entry.getValue().getStacks();
                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        consumer.accept(stacks.getStackInSlot(slot));
                    }
                }));
    }

    private CuriosCompat() {}
}
