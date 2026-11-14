package bin.cnpcbag.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class BackpackHandle {
    private final EntityPlayer player;
    private final ItemStack carrier;
    private final BackpackStorage storage;
    private final boolean equipped;
    public BackpackHandle(EntityPlayer player, ItemStack carrier, BackpackStorage storage, boolean equipped) {
        this.player = player; this.carrier = carrier; this.storage = storage; this.equipped = equipped;
    }
    public EntityPlayer player() { return player; }
    public ItemStack carrier() { return carrier; }
    public BackpackStorage storage() { return storage; }
    public boolean equipped() { return equipped; }
    public void changed() {
        storage.save().run();
        player.inventory.markDirty();
        player.openContainer.detectAndSendChanges();
    }
    public String modId() {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(carrier.getItem());
        return id == null ? "" : id.getNamespace();
    }
}
