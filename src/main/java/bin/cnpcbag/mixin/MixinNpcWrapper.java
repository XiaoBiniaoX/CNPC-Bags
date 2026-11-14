package bin.cnpcbag.mixin;

import bin.cnpcbag.script.*;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = NPCWrapper.class, remap = false)
public abstract class MixinNpcWrapper {
    @Unique
    public String getAllBackpackContents(IPlayer player) {
        return BackpackScriptService.getAllBackpackContents(cnpcbag$player(player));
    }

    @Unique
    public int getBackpackCount(IPlayer player) {
        return BackpackScriptService.getBackpackCount(cnpcbag$player(player));
    }

    @Unique
    public boolean hasBackpack(IPlayer player) {
        return BackpackScriptService.hasBackpack(cnpcbag$player(player));
    }

    @Unique
    public boolean hasItemInBackpack(IPlayer player, String itemId, int count) {
        return BackpackScriptService.hasItemInBackpack(cnpcbag$player(player), itemId, count);
    }

    @Unique
    public int getBackpackSlotCount(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpackSlotCount(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public boolean isBackpackEquipped(IPlayer player, int backpackIndex) {
        return BackpackScriptService.isBackpackEquipped(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public int getEquippedBackpackIndex(IPlayer player) {
        return BackpackScriptService.getEquippedBackpackIndex(cnpcbag$player(player));
    }

    @Unique
    public String getBackpackName(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpackName(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public String getBackpackModId(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpackModId(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public int getBackpackFreeSlots(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpackFreeSlots(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public ScriptBackpack getBackpack(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpack(cnpcbag$player(player), backpackIndex);
    }

    @Unique
    public boolean canAddItemToBackpack(IPlayer player, int backpackIndex, IItemStack stack) {
        return BackpackScriptService.canAddItemToBackpack(cnpcbag$player(player), backpackIndex, stack);
    }

    @Unique
    public int countItemInBackpack(IPlayer player, int backpackIndex, IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.countItemInBackpack(cnpcbag$player(player), backpackIndex, stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int countItemInAllBackpacks(IPlayer player, IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.countItemInAllBackpacks(cnpcbag$player(player), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public boolean hasItemInAllBackpacks(IPlayer player, IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.hasItemInAllBackpacks(cnpcbag$player(player), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int removeItemFromBackpacks(IPlayer player, IItemStack stack, int count, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.removeItemFromBackpacks(cnpcbag$player(player), stack, count, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int addItemToBackpacks(IPlayer player, IItemStack stack, int count) {
        return BackpackScriptService.addItemToBackpacks(cnpcbag$player(player), stack, count);
    }

    @Unique
    public int removeItemFromBackpack(IPlayer player, int backpackIndex, IItemStack stack, int count, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.removeItemFromBackpack(cnpcbag$player(player), backpackIndex, stack, count, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int addItemToBackpack(IPlayer player, int backpackIndex, IItemStack stack, int count) {
        return BackpackScriptService.addItemToBackpack(cnpcbag$player(player), backpackIndex, stack, count);
    }

    @Unique
    public int removeItemFromBackpack(IPlayer player, int backpackIndex, String itemId, int count, int damage, boolean ignoreNBT) {
        return BackpackScriptService.removeItemFromBackpack(cnpcbag$player(player), backpackIndex, itemId, count, damage, ignoreNBT);
    }

    @Unique
    public int addItemToBackpack(IPlayer player, int backpackIndex, String itemId, int count, int damage) {
        return BackpackScriptService.addItemToBackpack(cnpcbag$player(player), backpackIndex, itemId, count, damage);
    }

    @Unique
    public IItemStack getBackpackItem(IPlayer player, int backpackIndex, int slot) {
        return BackpackScriptService.getBackpackItem(cnpcbag$player(player), backpackIndex, slot);
    }

    @Unique
    public boolean setBackpackItem(IPlayer player, int backpackIndex, int slot, IItemStack stack) {
        return BackpackScriptService.setBackpackItem(cnpcbag$player(player), backpackIndex, slot, stack);
    }

    @Unique
    public int[] findItemInBackpacks(IPlayer player, IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.findItemInBackpacks(cnpcbag$player(player), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int getSlots(IPlayer player) {
        return BackpackScriptService.getSlots(cnpcbag$player(player));
    }

    @Unique
    public IItemStack insertItem(IPlayer player, int backpackIndex, int slot, IItemStack stack, boolean simulate) {
        return BackpackScriptService.insertItem(cnpcbag$player(player), backpackIndex, slot, stack, simulate);
    }

    @Unique
    public IItemStack extractItem(IPlayer player, int backpackIndex, int slot, int amount, boolean simulate) {
        return BackpackScriptService.extractItem(cnpcbag$player(player), backpackIndex, slot, amount, simulate);
    }

    @Unique
    public int getSlotLimit(IPlayer player, int backpackIndex, int slot) {
        return BackpackScriptService.getSlotLimit(cnpcbag$player(player), backpackIndex, slot);
    }

    @Unique
    public boolean isItemValid(IPlayer player, int backpackIndex, int slot, IItemStack stack) {
        return BackpackScriptService.isItemValid(cnpcbag$player(player), backpackIndex, slot, stack);
    }

    @Unique
    public IItemStack getStackInSlot(IPlayer player, int slot) {
        int[] location = BackpackScriptService.locate(cnpcbag$player(player), slot);
        return BackpackScriptService.getBackpackItem(cnpcbag$player(player), location[0], location[1]);
    }

    @Unique
    public IItemStack insertItem(IPlayer player, int slot, IItemStack stack, boolean simulate) {
        int[] location = BackpackScriptService.locate(cnpcbag$player(player), slot);
        return BackpackScriptService.insertItem(cnpcbag$player(player), location[0], location[1], stack, simulate);
    }

    @Unique
    public IItemStack extractItem(IPlayer player, int slot, int amount, boolean simulate) {
        int[] location = BackpackScriptService.locate(cnpcbag$player(player), slot);
        return BackpackScriptService.extractItem(cnpcbag$player(player), location[0], location[1], amount, simulate);
    }

    @Unique
    public int getSlotLimit(IPlayer player, int slot) {
        int[] location = BackpackScriptService.locate(cnpcbag$player(player), slot);
        return BackpackScriptService.getSlotLimit(cnpcbag$player(player), location[0], location[1]);
    }

    @Unique
    public boolean isItemValid(IPlayer player, int slot, IItemStack stack) {
        int[] location = BackpackScriptService.locate(cnpcbag$player(player), slot);
        return BackpackScriptService.isItemValid(cnpcbag$player(player), location[0], location[1], stack);
    }

    @Unique
    public int getSlots(IPlayer player, int backpackIndex) {
        return BackpackScriptService.getBackpackSlotCount(cnpcbag$player(player), backpackIndex);
    }


    @Unique
    public IItemStack getStackInSlot(IPlayer player, int backpackIndex, int slot) {
        return BackpackScriptService.getBackpackItem(cnpcbag$player(player), backpackIndex, slot);
    }

    @Unique
    private EntityPlayer cnpcbag$player(IPlayer player) { return player == null ? null : player.getMCEntity(); }

    @Unique
    public void openTraderMarket(IPlayer player, String marketTag) {
        NpcBackpackActions.openTraderMarket((EntityNPCInterface) ((NPCWrapper) (Object) this).getMCEntity(),
                cnpcbag$player(player), marketTag);
    }

    @Unique
    public void printPlayerBackpackContents(IPlayer player) {
        NpcBackpackActions.printPlayerBackpackContents((EntityNPCInterface) ((NPCWrapper) (Object) this).getMCEntity(),
                cnpcbag$player(player));
    }
}
