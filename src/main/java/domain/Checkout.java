package domain;

import enums.ReturnCondition;
import exceptions.StartAfterEndException;

import java.time.Instant;

public final class Checkout {
    public void setId(long id) {
        this.id = id;
    }

    private long id;
        private long instrumentId;
        private final long userId;
        private String comment;
        private Instant takenAt;
        private Instant returnedAt;
        private ReturnCondition returnCondition;
        private String ownerUsername;
        private final Instant createdAt;

        public void setInstrumentId(long instrumentId) {
                if(instrumentId>0){
                        this.instrumentId = instrumentId;
                        System.out.println("ID предмета успешно заменен");
                }else System.err.println("Такой id не принадлежит ни одному предмету");
        }

        public void setUsername(String username) {
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                        System.out.println("Имя клиента успешно заменено");
                }else System.err.println("Имя не может быть пустым");
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

        public void setTakenAt(Instant takenAt) {
                if(takenAt.isAfter(Instant.now())){
                        this.takenAt = takenAt;
                        System.out.println("Изменение времени выдачи сохранено");
                }else System.err.println("К сожалению,нельзя поставить такое время");
        }

        public void setReturnedAt(Instant returnedAt) {
                if(returnedAt.isAfter(takenAt)){
                        this.returnedAt = returnedAt;
                        System.out.println("Изменение времени возврата сохранено");
                }else {
                    throw new StartAfterEndException("К сожалению,нельзя поставить такое время");
                }
        }

        public void setReturnCondition(ReturnCondition returnCondition) {
                this.returnCondition = returnCondition;
        }

        public void setOwnerUsername(String ownerUsername) {
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                        System.out.println("Имя сотрудника успешно заменено");
                }else System.err.println("Имя не может быть пустым");
        }


        public Checkout(long id, long instrumentId, long userId, String comment, Instant takenAt, Instant returnedAt, ReturnCondition returnCondition, String ownerUsername, Instant createdAt) {
                this.id = id;
                this.instrumentId = instrumentId;
                this.userId = userId;
                this.comment = comment;
                this.takenAt = takenAt;
                this.returnedAt = returnedAt;
                this.returnCondition = returnCondition;
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                }
                this.createdAt = createdAt;
        }

        public long getId() {
                return id;
        }

        public long getInstrumentId() {
                return instrumentId;
        }

        public long getUserId() {
                return userId;
        }

        public String getComment() {
                return comment;
        }

        public Instant getTakenAt() {
                return takenAt;
        }

        public Instant getReturnedAt() {
                return returnedAt;
        }

        public ReturnCondition getReturnCondition() {
                return returnCondition;
        }

        public String getOwnerUsername() {
                return ownerUsername;
        }

        public Instant getCreatedAt() {
                return createdAt;
        }
}
