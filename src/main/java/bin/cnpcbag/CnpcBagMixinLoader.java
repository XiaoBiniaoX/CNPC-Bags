package bin.cnpcbag;

import java.util.Collections;
import java.util.List;
import zone.rong.mixinbooter.ILateMixinLoader;

public final class CnpcBagMixinLoader implements ILateMixinLoader {
    @Override public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.cnpcbag.json");
    }
}
