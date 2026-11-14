package bin.cnpcbag.backpack;

import java.util.function.BiConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface BackpackAdapter {
    String modId();
    BackpackStorage find(EntityPlayer player, ItemStack stack) throws ReflectiveOperationException;
    default void equipped(EntityPlayer player, BiConsumer<ItemStack, BackpackStorage> consumer)
            throws ReflectiveOperationException {}
}
