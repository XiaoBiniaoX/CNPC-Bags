package bin.cnpcbag.script;

import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;

public final class ScriptItems {
    public static ItemStack unwrap(IItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.getMCItemStack().copy();
    }

    public static IItemStack wrap(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : NpcAPI.Instance().getIItemStack(stack.copy());
    }

    public static ItemStack byId(String itemId, int count, int damage) {
        if (itemId == null || count <= 0) return ItemStack.EMPTY;
        ResourceLocation id;
        try { id = new ResourceLocation(itemId); } catch (IllegalArgumentException e) { return ItemStack.EMPTY; }
        if (!ForgeRegistries.ITEMS.containsKey(id)) return ItemStack.EMPTY;
        net.minecraft.item.Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item, count);
        if (damage >= 0) stack.setItemDamage(damage);
        return stack;
    }

    private ScriptItems() {}
}
