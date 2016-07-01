package fr.rsommerard.privacyaware.data;

import fr.rsommerard.privacyaware.dao.Data;

public class DataAvailableEvent {

    public final Data data;

    public DataAvailableEvent(Data data) {
        this.data = data;
    }
}
