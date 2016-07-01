package fr.rsommerard.privacyaware.wifidirect.device;

import fr.rsommerard.privacyaware.dao.Device;

public class DeviceEvent {

    public final Device device;

    public DeviceEvent(final Device device) {
        this.device = device;
    }
}
