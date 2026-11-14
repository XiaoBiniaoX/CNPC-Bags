package bin.cnpcbag.trade;

/** Presentation-only flags. Server payment always validates the actual inventories again. */
public interface TraderAvailability {
    int READY = 128;
    int CAN_BUY = 1;
    int FIRST_COST = 2;
    int SECOND_COST = 4;
    int cnpcbag$getAvailability(int offer);
    void cnpcbag$setAvailability(int offer, int value);
}
