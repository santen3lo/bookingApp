package domain;

import enums.InstrumentType;

public final class Instrument {
    private long id;
    private InstrumentType type;

    public void setId(long id) {
        this.id = id;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }

    public Instrument(long id, InstrumentType type) {
        if (id > 0) {
            this.id = id;
        }
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public InstrumentType getType() {
        return type;
    }

}
