package bin.cnpcbag.mixin;

import bin.cnpcbag.backpack.BackpackRegistry;
import bin.cnpcbag.trade.*;
import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = ContainerNPCTrader.class, remap = false)
public abstract class MixinContainerNPCTrader extends ContainerNpcInterface implements TraderAvailability {
    @Shadow public RoleTrader role;
    @Unique private TradePayment cnpcbag$payment;
    @Unique private TradeAvailabilityState cnpcbag$availability;
    @Unique private int[] cnpcbag$sent;
    @Unique private EntityPlayer cnpcbag$player;
    @Unique private static final int CNPCBAG_PROPERTY_BASE = 0x6b00;

    protected MixinContainerNPCTrader(EntityPlayer player) { super(player); }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cnpcbag$init(EntityNPCInterface npc, EntityPlayer player, CallbackInfo ci) {
        cnpcbag$player = player;
        cnpcbag$availability = new TradeAvailabilityState((ContainerNPCTrader) (Object) this, player);
        cnpcbag$sent = new int[18];
        Arrays.fill(cnpcbag$sent, -1);
    }

    // CNPC inherits these Container methods. Add SRG overrides for the production 1.12.2 namespace.
    public void func_75142_b() {
        super.detectAndSendChanges();
        if (cnpcbag$availability == null || !BackpackRegistry.onServer(cnpcbag$player)) return;
        for (int offer = 0; offer < 18; offer++) {
            int value = cnpcbag$getAvailability(offer);
            if (cnpcbag$sent[offer] == value) continue;
            cnpcbag$sent[offer] = value;
            for (IContainerListener listener : listeners)
                listener.sendWindowProperty((Container) (Object) this, CNPCBAG_PROPERTY_BASE + offer, value);
        }
    }

    public void func_75132_a(IContainerListener listener) {
        super.addListener(listener);
        if (cnpcbag$availability == null || !BackpackRegistry.onServer(cnpcbag$player)) return;
        for (int offer = 0; offer < 18; offer++)
            listener.sendWindowProperty((Container) (Object) this, CNPCBAG_PROPERTY_BASE + offer,
                    cnpcbag$getAvailability(offer));
    }

    public void func_75137_b(int id, int value) {
        int offer = id - CNPCBAG_PROPERTY_BASE;
        if (offer >= 0 && offer < 18) cnpcbag$setAvailability(offer, value);
    }

    @Override public int cnpcbag$getAvailability(int offer) {
        return cnpcbag$availability == null ? 0 : cnpcbag$availability.cnpcbag$getAvailability(offer);
    }
    @Override public void cnpcbag$setAvailability(int offer, int value) {
        if (cnpcbag$availability != null) cnpcbag$availability.cnpcbag$setAvailability(offer, value);
    }

    @Inject(method = "canBuy", at = @At("HEAD"), cancellable = true)
    private void cnpcbag$canBuy(ItemStack first, ItemStack second, EntityPlayer player,
                               CallbackInfoReturnable<Boolean> cir) {
        if (!BackpackRegistry.onServer(player)) return;
        cnpcbag$payment = TradePayment.plan(player, first, second, role.ignoreDamage, role.ignoreNBT, false, false);
        cir.setReturnValue(cnpcbag$payment != null);
    }

    @Inject(method = "func_184996_a", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;consumeItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ZZ)V",
            ordinal = 0), cancellable = true)
    private void cnpcbag$payBeforeReward(int slot, int button, ClickType click, EntityPlayer player,
                                        CallbackInfoReturnable<ItemStack> cir) {
        TradePayment payment = cnpcbag$payment;
        cnpcbag$payment = null;
        cnpcbag$availability.invalidate();
        if (payment == null || !payment.belongsTo(player) || !payment.commit()) cir.setReturnValue(ItemStack.EMPTY);
        cnpcbag$availability.invalidate();
    }

    @Redirect(method = "func_184996_a", at = @At(value = "INVOKE",
            target = "Lnoppes/npcs/NoppesUtilPlayer;consumeItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ZZ)V"))
    private void cnpcbag$skipPaidCosts(EntityPlayer player, ItemStack currency, boolean ignoreDamage, boolean ignoreNBT) {
        // Both costs were committed together before this invocation.
    }
}
