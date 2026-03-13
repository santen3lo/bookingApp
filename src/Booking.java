import java.time.Instant;

public final class Booking {

    private long id;
    private long instrumentId;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
    private String ownerUsername;
    private Instant createdAt;

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }


    public void setOwnerUsername(String ownerUsername) {
        if(!ownerUsername.isEmpty()){
            this.ownerUsername = ownerUsername;
            System.out.println("Имя сотрудника успешно заменено");
        }else System.out.println("Имя не может быть пустым");
    }


    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setEndAt(Instant endAt) {
        if(endAt.isAfter(Instant.now())){
            this.endAt = endAt;
            setUpdatedAt(endAt);
            System.out.println("Время окончания брони успешо заменено");
        }else System.out.println("Нельзя поставить такое время");
    }

    public void setStartAt(Instant startAt) {
        if(startAt.isAfter(Instant.now())){
            this.startAt = startAt;
            setUpdatedAt(startAt);
            System.out.println("Время начала брони успешо заменено");
        }else System.out.println("Нельзя поставить такое время");
    }

    public void setInstrumentId(long instrumentId) {
        if(instrumentId>0){
            this.instrumentId = instrumentId;
            System.out.println("ID предмета успешно заменен");
        }else System.out.println("Такой id не принадлежит ни одному предмету");
    }


    private Instant updatedAt;

    public Booking(long id, long instrumentId, Instant startAt, Instant endAt, String ownerUsername) {
        this.id = id;
        this.instrumentId = instrumentId;
        if(startAt.isAfter(Instant.now())){
            this.startAt = startAt;
        }
        if(endAt.isAfter(Instant.now())){
            this.endAt = endAt;
        }
        this.status = BookingStatus.ACTIVE;
        if(!ownerUsername.isEmpty()){
            this.ownerUsername = ownerUsername;
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
