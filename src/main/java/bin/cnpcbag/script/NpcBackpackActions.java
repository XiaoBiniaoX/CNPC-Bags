package bin.cnpcbag.script;

import bin.cnpcbag.backpack.BackpackRegistry;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

public final class NpcBackpackActions {
    public static void openTraderMarket(EntityNPCInterface npc, Player player, String tag) {
        if (npc == null || !BackpackRegistry.onServer(player) || tag == null || tag.isEmpty()
                || npc.level() != player.level() || !(npc.role instanceof RoleTrader role)) return;
        role.marketName = tag;
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
    }

    public static void printPlayerBackpackContents(EntityNPCInterface npc, Player player) {
        if (npc == null || !BackpackRegistry.onServer(player) || npc.level() != player.level()) return;
        for (String line : BackpackScriptService.getAllBackpackContents(player).split("\\n")) {
            npc.say(player, new Line(line));
        }
    }

    private NpcBackpackActions() {}
}
