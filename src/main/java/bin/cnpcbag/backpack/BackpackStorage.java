package bin.cnpcbag.backpack;

import net.neoforged.neoforge.items.IItemHandler;

public record BackpackStorage(IItemHandler handler, Object identity, Runnable save) {
    public BackpackStorage(IItemHandler handler) {
        this(handler, handler, () -> {});
    }
}
