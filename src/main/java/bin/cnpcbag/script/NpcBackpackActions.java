package bin.cnpcbag.script;

import bin.cnpcbag.backpack.BackpackRegistry;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

public final class NpcBackpackActions {
    public static void openTraderMarket(EntityNPCInterface npc, EntityPlayer player, String tag) {
        if (npc == null || !BackpackRegistry.onServer(player) || tag == null || tag.isEmpty()
                || npc.world != player.world || !(npc.roleInterface instanceof RoleTrader)) return;
        RoleTrader role = (RoleTrader) npc.roleInterface;
        role.marketName = tag;
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
    }

    public static void printPlayerBackpackContents(EntityNPCInterface npc, EntityPlayer player) {
        if (npc == null || !BackpackRegistry.onServer(player) || npc.world != player.world) return;
        for (String line : BackpackScriptService.getAllBackpackContents(player).split("\\n")) {
            npc.say(player, new Line(line));
        }
    }

    private NpcBackpackActions() {}
}
