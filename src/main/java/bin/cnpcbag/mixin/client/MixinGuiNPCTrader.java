package bin.cnpcbag.mixin.client;

import bin.cnpcbag.trade.TraderAvailability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = GuiNPCTrader.class, remap = false)
public abstract class MixinGuiNPCTrader extends GuiContainerNPCInterface {
    @Shadow private ContainerNPCTrader container;
    protected MixinGuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader menu) { super(npc, menu); }

    @Unique private int cnpcbag$hoveredOffer(int mouseX, int mouseY) {
        int x = mouseX - guiLeft - 10;
        int y = mouseY - guiTop - 6;
        if (x < 0 || y < 0) return -1;
        int column = x / 72;
        int row = y / 21;
        return column < 3 && row < 6 ? row * 3 + column : -1;
    }
    @Unique private int cnpcbag$status(int mouseX, int mouseY) {
        return ((TraderAvailability) container).cnpcbag$getAvailability(cnpcbag$hoveredOffer(mouseX, mouseY));
    }

    @Redirect(method = "func_146979_b", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/containers/ContainerNPCTrader;canBuy(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Z"))
    private boolean cnpcbag$showServerAvailability(ContainerNPCTrader menu, ItemStack first, ItemStack second,
                                                 EntityPlayer player, int mouseX, int mouseY) {
        int status = cnpcbag$status(mouseX, mouseY);
        return (status & TraderAvailability.READY) == 0 ? menu.canBuy(first, second, player)
                : (status & TraderAvailability.CAN_BUY) != 0;
    }

    @Redirect(method = "func_146979_b", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;compareItems(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ZZ)Z"))
    private boolean cnpcbag$highlightMissingCost(EntityPlayer player, ItemStack query, boolean ignoreDamage,
                                                boolean ignoreNBT, int mouseX, int mouseY) {
        int status = cnpcbag$status(mouseX, mouseY);
        if ((status & TraderAvailability.READY) == 0)
            return NoppesUtilPlayer.compareItems(player, query, ignoreDamage, ignoreNBT);
        int offer = cnpcbag$hoveredOffer(mouseX, mouseY);
        ItemStack first = container.role.inventoryCurrency.items.get(offer);
        ItemStack second = container.role.inventoryCurrency.items.get(offer + 18);
        if (query == second || first.isEmpty()) return (status & TraderAvailability.SECOND_COST) != 0;
        if (!second.isEmpty() && NoppesUtilPlayer.compareItems(first, second, ignoreDamage, ignoreNBT))
            return (status & TraderAvailability.CAN_BUY) != 0;
        return (status & TraderAvailability.FIRST_COST) != 0;
    }
}
