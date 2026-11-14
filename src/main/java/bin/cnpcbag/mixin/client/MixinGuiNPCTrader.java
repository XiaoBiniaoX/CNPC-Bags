package bin.cnpcbag.mixin.client;

import bin.cnpcbag.trade.TraderAvailability;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiNPCTrader.class, remap = false)
public abstract class MixinGuiNPCTrader extends GuiContainerNPCInterface<ContainerNPCTrader> {
    @Shadow private ContainerNPCTrader container;

    protected MixinGuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader menu, Inventory inventory, Component title) {
        super(npc, menu, inventory, title);
    }

    @Unique
    private int cnpcbag$hoveredOffer(int mouseX, int mouseY) {
        int x = mouseX - guiLeft - 10;
        int y = mouseY - guiTop - 6;
        if (x < 0 || y < 0) return -1;
        int column = x / 72;
        int row = y / 21;
        return column < 3 && row < 6 ? row * 3 + column : -1;
    }

    @Unique
    private int cnpcbag$status(int mouseX, int mouseY) {
        return ((TraderAvailability) container).cnpcbag$getAvailability(cnpcbag$hoveredOffer(mouseX, mouseY));
    }

    @Redirect(method = "renderLabels", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/containers/ContainerNPCTrader;canBuy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean cnpcbag$showServerAvailability(ContainerNPCTrader menu, ItemStack first, ItemStack second,
                                                 Player player, GuiGraphics graphics, int mouseX, int mouseY) {
        int status = cnpcbag$status(mouseX, mouseY);
        return (status & TraderAvailability.READY) == 0 ? menu.canBuy(first, second, player)
                : (status & TraderAvailability.CAN_BUY) != 0;
    }

    @Redirect(method = "renderLabels", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;compareItems(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZZ)Z"))
    private boolean cnpcbag$highlightMissingCost(Player player, ItemStack query, boolean ignoreDamage,
                                                boolean ignoreNBT, GuiGraphics graphics, int mouseX, int mouseY) {
        int status = cnpcbag$status(mouseX, mouseY);
        if ((status & TraderAvailability.READY) == 0) {
            return NoppesUtilPlayer.compareItems(player, query, ignoreDamage, ignoreNBT);
        }
        int offer = cnpcbag$hoveredOffer(mouseX, mouseY);
        ItemStack first = container.role.inventoryCurrency.items.get(offer);
        ItemStack second = container.role.inventoryCurrency.items.get(offer + 18);
        if (query == second || first.isEmpty()) return (status & TraderAvailability.SECOND_COST) != 0;
        if (!second.isEmpty() && NoppesUtilPlayer.compareItems(first, second, ignoreDamage, ignoreNBT)) {
            return (status & TraderAvailability.CAN_BUY) != 0;
        }
        return (status & TraderAvailability.FIRST_COST) != 0;
    }
}
