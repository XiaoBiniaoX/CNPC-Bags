package bin.cnpcbag.backpack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record BackpackHandle(Player player, ItemStack carrier, BackpackStorage storage, boolean equipped) {
    public void changed() {
        storage.save().run();
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public String modId() {
        var id = ForgeRegistries.ITEMS.getKey(carrier.getItem());
        return id == null ? "" : id.getNamespace();
    }
}
