package domain;

import Enums.ReturnCondition;

import java.time.Instant;

public final class Checkout {
        private long id;
        private long instrumentId;
        private String username;
        private String comment;
        private Instant takenAt;
        private Instant returnedAt;
        private ReturnCondition returnCondition;
        private String ownerUsername;
        private Instant createdAt;

        public void setInstrumentId(long instrumentId) {
                if(instrumentId>0){
                        this.instrumentId = instrumentId;
                        System.out.println("ID предмета успешно заменен");
                }else System.out.println("Такой id не принадлежит ни одному предмету");
        }

        public void setUsername(String username) {
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                        System.out.println("Имя клиента успешно заменено");
                }else System.out.println("Имя не может быть пустым");
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

        public void setTakenAt(Instant takenAt) {
                if(takenAt.isAfter(Instant.now())){
                        this.takenAt = takenAt;
                        System.out.println("Изменение времени выдачи сохранено");
                }else System.out.println("К сожалению,нельзя поставить такое время");
        }

        public void setReturnedAt(Instant returnedAt) {
                if(returnedAt.isAfter(Instant.now())){
                        this.returnedAt = returnedAt;
                        System.out.println("Изменение времени возврата сохранено");
                }else System.out.println("К сожалению,нельзя поставить такое время");
        }

        public void setReturnCondition(ReturnCondition returnCondition) {
                this.returnCondition = returnCondition;
        }

        public void setOwnerUsername(String ownerUsername) {
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                        System.out.println("Имя сотрудника успешно заменено");
                }else System.out.println("Имя не может быть пустым");
        }


        public Checkout(long id, long instrumentId, String username, String comment, Instant takenAt, Instant returnedAt, ReturnCondition returnCondition, String ownerUsername, Instant createdAt) {
                this.id = id;
                this.instrumentId = instrumentId;
                if(!username.isEmpty()){
                        this.username = username;
                }
                this.comment = comment;
                if(takenAt.isAfter(Instant.now())){
                        this.takenAt = takenAt;
                }
                if(returnedAt.isAfter(Instant.now())){
                        this.returnedAt = returnedAt;
                }
                this.returnCondition = returnCondition;
                if(!ownerUsername.isEmpty()){
                        this.ownerUsername = ownerUsername;
                }
                this.createdAt = Instant.now();
        }

        public long getId() {
                return id;
        }

        public long getInstrumentId() {
                return instrumentId;
        }

        public String getUsername() {
                return username;
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
