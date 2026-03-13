
import java.sql.SQLOutput;
import java.time.Instant;
import java.util.ArrayList;

public class Order {
    int BookingID = 1;
    int InstrumentId = 1;
    private Instrument instrument;
    private Booking booking;
    private Checkout checkout;
    ArrayList<Booking> books = new ArrayList<Booking>();
    ArrayList<Checkout> checkouts = new ArrayList<Checkout>();
    ArrayList<Instrument> instruments = new ArrayList<Instrument>();


    public void book_create (long instrument_id, Instant startAt, Instant endAt, String ownerUsername, Instant createdAt, Instant updatedAt){
        books.add(new Booking(BookingID, instrument_id, startAt, endAt, ownerUsername));
        BookingID++;
    }//конец раньше начала; формат даты неверный; instrument не найден

    public void book_cancel (long book_id){
        for (int i = 0; i < books.size(); i++) {
            if(book_id==books.get(i).getId()) {
                books.get(i).setStatus(BookingStatus.CANCELLED);
                //books.get(i).getReturnCondition = ReturnCondition.OK;
                System.out.println("чтото типа статуса");
            }
         }
//        System.out.println();
//        System.out.println();
    }
    //не найден; уже началась бронь → Ошибка: нельзя отменить начавшуюся бронь
    public void book_list(long instrument_id, Instant from){
        System.out.println("ID" + "  "+"START"+"          "+"END"+"\n");
        for (Booking book : books) {
            if (book.getInstrumentId() == instrument_id && book.getStartAt().isAfter(from)){
                System.out.println(book.getId() + " " + book.getStartAt() + " " + book.getEndAt());
            }
        }
    }

    public void checkout_take (long instrument_id){
        for (Instrument instrument: instruments) {
            if(instrument_id == instrument.getId()){
                checkout.setReturnCondition(ReturnCondition.OK);
                System.out.println("Кто берет:"+checkout.getOwnerUsername());
                System.out.println("Комметнтарий");
                System.out.println(checkout.getReturnCondition());
            }
        }

    }//прибор уже выдан

    public void checkout_return (long checkout_id){
        long k = 0;
        for (Checkout checkout: checkouts) {
            if(checkout_id == checkout.getId()){
                k = checkout_id;
                checkout.setReturnCondition(ReturnCondition.OK);
                System.out.println("Кто берет:"+checkout.getOwnerUsername());
                System.out.println("Комметнтарий");
                System.out.println(checkout.getReturnCondition());
            }
        }
    }//уже возвращен

    public void checkout_list(Checkout checkout){
        System.out.println("ID  Instrument   User    TakenAt");
        System.out.println(checkout.getId() + "  "+ checkout.getInstrumentId()+"  "+checkout.getUsername()+"        "+checkout.getTakenAt());
    }//[--open-only]?????

    public void inst_available ( InstrumentType type, Instant start, Instant end){
        System.out.print("Available instruments: ");
        boolean first = true;
        for (Instrument instrument: instruments){
            if(instrument.getType()==type){
                for (Booking book : books) {
                    if(book.getStartAt()==start&&book.getEndAt()==end){
                        if (!first) {
                            System.out.print(", ");
                        }
                        System.out.print(book.getInstrumentId());
                        first = false;
                    }
                }
            }
        }
    }
    //неверный формат времени
    public void book_show (long booking_id){
        for (Booking book : books) {
            if (book.getId()==booking_id){
                System.out.println("#"+booking_id);
                System.out.println("instrument_id:"+book.getInstrumentId());
                System.out.println("start:" + book.getStartAt());
                System.out.println("end:" + book.getEndAt());
                System.out.println("status:" + book.getStatus());
            }
        }
    }

    public void checkout_show (long checkout_id){
        for (Checkout checkout : checkouts) {
            if (checkout.getId()==checkout_id){
                System.out.println("#"+checkout_id);
                System.out.println("instrument_id:"+checkout.getInstrumentId());
                System.out.println("user:" + checkout.getUsername());
                System.out.println("takenAt:" + checkout.getTakenAt());
                System.out.println("returnedAt:" + checkout.getReturnedAt());
            }
        }
    }

    public void book_reschedule (long booking_id,Instant start, Instant end){
        for (Booking book : books) {
            if (book.getId() == booking_id) {
                book.setStartAt(start);
                book.setEndAt(end);
                System.out.println(book.getStatus()+" "+"reschedule");
            }
        }
    }//проверить отноительно других броней чтобы не пересекались


}
