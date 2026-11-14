package bin.cnpcbag.backpack;

import net.minecraftforge.items.IItemHandler;

public final class BackpackStorage {
    private final IItemHandler handler;
    private final Object identity;
    private final Runnable save;
    public BackpackStorage(IItemHandler handler) { this(handler, handler, () -> {}); }
    public BackpackStorage(IItemHandler handler, Object identity, Runnable save) {
        this.handler = handler; this.identity = identity; this.save = save;
    }
    public IItemHandler handler() { return handler; }
    public Object identity() { return identity; }
    public Runnable save() { return save; }
}
