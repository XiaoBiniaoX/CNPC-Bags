package bin.cnpcbag.trade;

import net.minecraft.world.inventory.DataSlot;

/** Vanilla menu synchronization sends these small flags to the owning client only. */
public final class AvailabilityDataSlot extends DataSlot {
    private final TraderAvailability availability;
    private final int offer;

    public AvailabilityDataSlot(TraderAvailability availability, int offer) {
        this.availability = availability;
        this.offer = offer;
    }

    @Override
    public int get() { return availability.cnpcbag$getAvailability(offer); }

    @Override
    public void set(int value) { availability.cnpcbag$setAvailability(offer, value); }
}
