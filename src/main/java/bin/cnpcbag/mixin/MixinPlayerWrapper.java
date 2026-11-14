package bin.cnpcbag.mixin;

import bin.cnpcbag.script.*;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PlayerWrapper.class, remap = false)
public abstract class MixinPlayerWrapper {
    @Unique
    public String getAllBackpackContents() {
        return BackpackScriptService.getAllBackpackContents(cnpcbag$self());
    }

    @Unique
    public int getBackpackCount() {
        return BackpackScriptService.getBackpackCount(cnpcbag$self());
    }

    @Unique
    public boolean hasBackpack() {
        return BackpackScriptService.hasBackpack(cnpcbag$self());
    }

    @Unique
    public boolean hasItemInBackpack(String itemId, int count) {
        return BackpackScriptService.hasItemInBackpack(cnpcbag$self(), itemId, count);
    }

    @Unique
    public int getBackpackSlotCount(int backpackIndex) {
        return BackpackScriptService.getBackpackSlotCount(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public boolean isBackpackEquipped(int backpackIndex) {
        return BackpackScriptService.isBackpackEquipped(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public int getEquippedBackpackIndex() {
        return BackpackScriptService.getEquippedBackpackIndex(cnpcbag$self());
    }

    @Unique
    public String getBackpackName(int backpackIndex) {
        return BackpackScriptService.getBackpackName(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public String getBackpackModId(int backpackIndex) {
        return BackpackScriptService.getBackpackModId(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public int getBackpackFreeSlots(int backpackIndex) {
        return BackpackScriptService.getBackpackFreeSlots(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public ScriptBackpack getBackpack(int backpackIndex) {
        return BackpackScriptService.getBackpack(cnpcbag$self(), backpackIndex);
    }

    @Unique
    public boolean canAddItemToBackpack(int backpackIndex, IItemStack stack) {
        return BackpackScriptService.canAddItemToBackpack(cnpcbag$self(), backpackIndex, stack);
    }

    @Unique
    public int countItemInBackpack(int backpackIndex, IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.countItemInBackpack(cnpcbag$self(), backpackIndex, stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int countItemInAllBackpacks(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.countItemInAllBackpacks(cnpcbag$self(), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public boolean hasItemInAllBackpacks(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.hasItemInAllBackpacks(cnpcbag$self(), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int removeItemFromBackpacks(IItemStack stack, int count, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.removeItemFromBackpacks(cnpcbag$self(), stack, count, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int addItemToBackpacks(IItemStack stack, int count) {
        return BackpackScriptService.addItemToBackpacks(cnpcbag$self(), stack, count);
    }

    @Unique
    public int removeItemFromBackpack(int backpackIndex, IItemStack stack, int count, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.removeItemFromBackpack(cnpcbag$self(), backpackIndex, stack, count, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int addItemToBackpack(int backpackIndex, IItemStack stack, int count) {
        return BackpackScriptService.addItemToBackpack(cnpcbag$self(), backpackIndex, stack, count);
    }

    @Unique
    public int removeItemFromBackpack(int backpackIndex, String itemId, int count, int damage, boolean ignoreNBT) {
        return BackpackScriptService.removeItemFromBackpack(cnpcbag$self(), backpackIndex, itemId, count, damage, ignoreNBT);
    }

    @Unique
    public int addItemToBackpack(int backpackIndex, String itemId, int count, int damage) {
        return BackpackScriptService.addItemToBackpack(cnpcbag$self(), backpackIndex, itemId, count, damage);
    }

    @Unique
    public IItemStack getBackpackItem(int backpackIndex, int slot) {
        return BackpackScriptService.getBackpackItem(cnpcbag$self(), backpackIndex, slot);
    }

    @Unique
    public boolean setBackpackItem(int backpackIndex, int slot, IItemStack stack) {
        return BackpackScriptService.setBackpackItem(cnpcbag$self(), backpackIndex, slot, stack);
    }

    @Unique
    public int[] findItemInBackpacks(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT, boolean fuzzyMatch, boolean nameCheck) {
        return BackpackScriptService.findItemInBackpacks(cnpcbag$self(), stack, ignoreDamage, ignoreNBT, fuzzyMatch, nameCheck);
    }

    @Unique
    public int getSlots() {
        return BackpackScriptService.getSlots(cnpcbag$self());
    }

    @Unique
    public IItemStack insertItem(int backpackIndex, int slot, IItemStack stack, boolean simulate) {
        return BackpackScriptService.insertItem(cnpcbag$self(), backpackIndex, slot, stack, simulate);
    }

    @Unique
    public IItemStack extractItem(int backpackIndex, int slot, int amount, boolean simulate) {
        return BackpackScriptService.extractItem(cnpcbag$self(), backpackIndex, slot, amount, simulate);
    }

    @Unique
    public int getSlotLimit(int backpackIndex, int slot) {
        return BackpackScriptService.getSlotLimit(cnpcbag$self(), backpackIndex, slot);
    }

    @Unique
    public boolean isItemValid(int backpackIndex, int slot, IItemStack stack) {
        return BackpackScriptService.isItemValid(cnpcbag$self(), backpackIndex, slot, stack);
    }

    @Unique
    public IItemStack getStackInSlot(int slot) {
        int[] location = BackpackScriptService.locate(cnpcbag$self(), slot);
        return BackpackScriptService.getBackpackItem(cnpcbag$self(), location[0], location[1]);
    }

    @Unique
    public IItemStack insertItem(int slot, IItemStack stack, boolean simulate) {
        int[] location = BackpackScriptService.locate(cnpcbag$self(), slot);
        return BackpackScriptService.insertItem(cnpcbag$self(), location[0], location[1], stack, simulate);
    }

    @Unique
    public IItemStack extractItem(int slot, int amount, boolean simulate) {
        int[] location = BackpackScriptService.locate(cnpcbag$self(), slot);
        return BackpackScriptService.extractItem(cnpcbag$self(), location[0], location[1], amount, simulate);
    }

    @Unique
    public int getSlotLimit(int slot) {
        int[] location = BackpackScriptService.locate(cnpcbag$self(), slot);
        return BackpackScriptService.getSlotLimit(cnpcbag$self(), location[0], location[1]);
    }

    @Unique
    public boolean isItemValid(int slot, IItemStack stack) {
        int[] location = BackpackScriptService.locate(cnpcbag$self(), slot);
        return BackpackScriptService.isItemValid(cnpcbag$self(), location[0], location[1], stack);
    }

    @Unique
    public int getSlots(int backpackIndex) {
        return BackpackScriptService.getBackpackSlotCount(cnpcbag$self(), backpackIndex);
    }


    @Unique
    public IItemStack getStackInSlot(int backpackIndex, int slot) {
        return BackpackScriptService.getBackpackItem(cnpcbag$self(), backpackIndex, slot);
    }

    @Unique
    private EntityPlayer cnpcbag$self() { return (EntityPlayer) ((PlayerWrapper) (Object) this).getMCEntity(); }

    @Unique
    public void openTraderMarket(ICustomNpc npc, String marketTag) {
        if (npc != null) NpcBackpackActions.openTraderMarket((EntityNPCInterface) npc.getMCEntity(), cnpcbag$self(), marketTag);
    }

    @Unique
    public void printPlayerBackpackContents(ICustomNpc npc) {
        if (npc != null) NpcBackpackActions.printPlayerBackpackContents((EntityNPCInterface) npc.getMCEntity(), cnpcbag$self());
    }

    @Unique
    public void printPlayerBackpackContents() {
        EntityPlayer player = cnpcbag$self();
        if (!bin.cnpcbag.backpack.BackpackRegistry.onServer(player)) return;
        for (String line : BackpackScriptService.getAllBackpackContents(player).split("\\n")) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(line));
        }
    }
}
