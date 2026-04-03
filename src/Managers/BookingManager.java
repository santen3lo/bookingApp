package Managers;

import Enums.BookingStatus;
import Exceptions.NotFromException;
import Exceptions.PastTimeException;
import Exceptions.StartAfterEndException;
import Exceptions.UnderOneException;
import Validators.FlagValidator;
import Validators.IdValidator;
import Validators.NameValidator;
import Validators.TimeValidator;
import domain.Booking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;

public class BookingManager {
    private ArrayList<Booking> books;
    private IdGen id;
    public void createNewBooking(Scanner sc) {
        //long bookingId, long instrument_id, Instant startAt, Instant endAt, String ownerUsername
        try{
            long instId = Integer.parseInt(sc.next());
            IdValidator.checkId(instId);

            System.out.println("Начало (YYYY-MM-DD HH:MM): ");
            Instant startAt = Instant.parse(TimeValidator.timeFormat(sc.next()));

            System.out.println("Конец (YYYY-MM-DD HH:MM):");
            Instant endAt = Instant.parse(TimeValidator.timeFormat(sc.next()));

            TimeValidator.StartEndCheck(startAt, endAt);

            System.out.println("Введите имя");
            String ownerUsername = sc.next();
            NameValidator.nameCheck(ownerUsername);

            books.add(new Booking(id.createId(), instId, startAt, endAt, ownerUsername));
            System.out.println("OK booking_id = "+id.getId());
        } catch (NumberFormatException ex){
            System.out.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | NotFromException | PastTimeException | StartAfterEndException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Вы неправильно ввели дату");
        }
    }

    public ArrayList<Booking> getBooks() {
        return books;
    }

    public void bookCancel(String[] com) {
        try{
            long bookingId;
            bookingId = Integer.parseInt(com[1]);
            IdValidator.checkId(bookingId);
            for (Booking book : books){
                if (bookingId == book.getId()) {
                    book.setStatus(BookingStatus.CANCELLED);
                    System.out.println("чтото типа статуса");
                }
            }
        } catch (NumberFormatException ex){
            System.out.println("Вы ввели недействительный booking id");
        } catch (UnderOneException e) {
            System.out.println(e.getMessage());
        }

    }
    public void bookList(String[] com) {
        try{
            long instId = Integer.parseInt(com[1]);
            IdValidator.checkId(instId);
            String flag = com[2];
            FlagValidator.checkFromFlag(flag);
            Instant from = Instant.parse(TimeValidator.timeFormat(com[3]));
            System.out.println("ID" + "  " + "START" + "          " + "END" + "\n");

            for (Booking book : books) {
                if (book.getInstrumentId() == instId && book.getStartAt().isAfter(from)) {
                    System.out.println(book.getId() + " " + book.getStartAt() + " " + book.getEndAt());
                }
            }
        } catch (NumberFormatException ex){
            System.out.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | NotFromException | PastTimeException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Вы неправильно ввели дату");
        }


    }
    public void bookShow(String[] com) {
        try {
            long bookingId = Integer.parseInt(com[1]);
            IdValidator.checkId(bookingId);

            for (Booking book : books) {
                if (book.getId() == bookingId) {
                    System.out.println("#" + bookingId);
                    System.out.println("instrument_id:" + book.getInstrumentId());
                    System.out.println("start:" + book.getStartAt());
                    System.out.println("end:" + book.getEndAt());
                    System.out.println("status:" + book.getStatus());
                }
            }
        } catch (UnderOneException e){
            System.out.println(e.getMessage());
        } catch (NumberFormatException e){
            System.out.println("Вы ввели недействительный instrument id");
        }
    }
    public void bookReschedule(String[] com) {
        //Managers.IdGen id, Instant start, Instant end
        try{
            long instId = Integer.parseInt(com[1]);
            IdValidator.checkId(instId);
            Instant start = Instant.parse(TimeValidator.timeFormat(com[2]));
            Instant end = Instant.parse(TimeValidator.timeFormat(com[3]));
            TimeValidator.StartEndCheck(start, end);

            for (Booking book : books) {
                if (book.getId() == id.getId()) {
                    book.setStartAt(start);
                    book.setEndAt(end);
                    System.out.println(book.getStatus() + " " + "reschedule");
                }
            }
        } catch (NumberFormatException ex){
            System.out.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | PastTimeException | StartAfterEndException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Вы неправильно ввели дату");
        }
    }
}

