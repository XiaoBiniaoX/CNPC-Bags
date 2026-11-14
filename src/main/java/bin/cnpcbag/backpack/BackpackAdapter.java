package bin.cnpcbag.backpack;

import java.util.function.BiConsumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BackpackAdapter {
    String modId();
    BackpackStorage find(Player player, ItemStack stack) throws ReflectiveOperationException;
    default void equipped(Player player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {}
}
