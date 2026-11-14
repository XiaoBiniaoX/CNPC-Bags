package bin.cnpcbag;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = CnpcBag.MOD_ID, name = "CNPC-Bag", version = "1.0.0", acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:customnpcs;required-after:cnpcplus@[4.0.5,);required-after:mixinbooter;"
                + "after:retro_sophisticated_backpacks;after:travelersbackpack;after:backpacked;"
                + "after:simplybackpacks;after:usefulbackpacks")
public final class CnpcBag {
    public static final String MOD_ID = "cnpcbag";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
}
