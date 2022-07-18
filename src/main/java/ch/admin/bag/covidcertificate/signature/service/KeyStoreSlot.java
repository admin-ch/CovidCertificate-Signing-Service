package ch.admin.bag.covidcertificate.signature.service;

import java.util.Objects;

/**
 * The HSM can be configured to use multiple slots.
 * Each slot can be viewed like a separate HSM and has to be accessed separately.
 */
public enum KeyStoreSlot {
    SLOT_NUMBER_0(0),
    SLOT_NUMBER_1(1);

    private final Integer slotNumber;

    KeyStoreSlot(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Integer getSlotNumber() {
        return this.slotNumber;
    }

    public static KeyStoreSlot fromSlotNumber(Integer slotNumber) {
        for (KeyStoreSlot slot : KeyStoreSlot.values()) {
            if (Objects.equals(slot.getSlotNumber(), slotNumber)) {
                return slot;
            }
        }
        throw new IllegalArgumentException(String.format("SlotNumber %s is not a valid value.", slotNumber));
    }
}
