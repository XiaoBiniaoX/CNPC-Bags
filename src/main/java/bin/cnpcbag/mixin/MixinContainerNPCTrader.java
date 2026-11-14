package bin.cnpcbag.mixin;

import bin.cnpcbag.backpack.BackpackRegistry;
import bin.cnpcbag.trade.TradePayment;
import bin.cnpcbag.trade.TradeAvailabilityState;
import bin.cnpcbag.trade.TraderAvailability;
import bin.cnpcbag.trade.AvailabilityDataSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.roles.RoleTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = ContainerNPCTrader.class, remap = false)
public abstract class MixinContainerNPCTrader extends ContainerNpcInterface implements TraderAvailability {
    @Shadow public RoleTrader role;
    @Unique private TradePayment cnpcbag$payment;
    @Unique private TradeAvailabilityState cnpcbag$availability;

    protected MixinContainerNPCTrader(MenuType<?> type, int id, Inventory inventory) {
        super(type, id, inventory);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cnpcbag$addAvailabilitySlots(int id, Inventory inventory, int entityId, CallbackInfo ci) {
        cnpcbag$availability = new TradeAvailabilityState((ContainerNPCTrader) (Object) this, inventory.player);
        for (int offer = 0; offer < 18; offer++) addDataSlot(new AvailabilityDataSlot(cnpcbag$availability, offer));
    }

    @Override
    public int cnpcbag$getAvailability(int offer) {
        return cnpcbag$availability == null ? 0 : cnpcbag$availability.cnpcbag$getAvailability(offer);
    }

    @Override
    public void cnpcbag$setAvailability(int offer, int value) {
        if (cnpcbag$availability != null) cnpcbag$availability.cnpcbag$setAvailability(offer, value);
    }

    @Inject(method = "canBuy", at = @At("HEAD"), cancellable = true)
    private void cnpcbag$canBuy(ItemStack first, ItemStack second, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!BackpackRegistry.onServer(player)) return;
        cnpcbag$payment = TradePayment.plan(player, first, second,
                role.ignoreDamage, role.ignoreNBT, role.fuzzyMatch, role.nameCheck);
        cir.setReturnValue(cnpcbag$payment != null);
    }

    @Inject(method = "m_150399_", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;consumeItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZZZZ)V",
            ordinal = 0), cancellable = true)
    private void cnpcbag$payBeforeReward(int slot, int button, ClickType click, Player player, CallbackInfo ci) {
        TradePayment payment = cnpcbag$payment;
        cnpcbag$payment = null;
        cnpcbag$availability.invalidate();
        if (payment == null || !payment.belongsTo(player) || !payment.commit()) ci.cancel();
        cnpcbag$availability.invalidate();
    }

    @Redirect(method = "m_150399_", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;consumeItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZZZZ)V"))
    private void cnpcbag$skipPaidCosts(Player player, ItemStack currency, boolean ignoreDamage,
                                      boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        // Both costs were paid together before this invocation. Only the CNPC RoleTrader menu is patched.
    }
}
