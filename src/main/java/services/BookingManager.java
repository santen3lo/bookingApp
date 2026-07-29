package services;

import auth.SessionContext;
import enums.BookingStatus;
import exceptions.*;
import exceptions.SecurityException;
import storage.DbErrorHandler;
import storage.JdbcStorage;
import validators.FlagValidator;
import validators.IdValidator;
import validators.NameValidator;
import validators.TimeValidator;
import domain.Booking;
import utils.IdGen;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookingManager {
    private final ArrayList<Booking> books = new ArrayList<>();
    private final IdGen id = new IdGen();
    private final JdbcStorage jdbc = new JdbcStorage();



    public void createNewBooking(String instrumentId, Scanner sc) {
        try{
            if (!SessionContext.isAuthenticated()) {
                throw new SecurityException("Для создания брони необходимо войти в систему (login)");
            }
            boolean flag = true;
            int instId = Integer.parseInt(instrumentId);
            IdValidator.checkId(instId);

            System.out.println("Начало (YYYY-MM-DD HH:MM): ");
            Instant startAt = Instant.parse(TimeValidator.timeFormat(sc.nextLine()));

            System.out.println("Конец (YYYY-MM-DD HH:MM):");
            Instant endAt = Instant.parse(TimeValidator.timeFormat(sc.nextLine()));

            TimeValidator.StartEndCheck(startAt, endAt);

            long userId = SessionContext.getCurrentUserId();

            for (Booking booking : books) {
                if (booking.getInstrumentId() == instId && booking.getStatus() == BookingStatus.ACTIVE){
                    if ((!booking.getStartAt().isAfter(endAt) && !booking.getEndAt().isBefore(startAt)) ||
                            (booking.getStartAt().isBefore(startAt) && booking.getEndAt().isAfter(endAt))) {
                        flag = false;
                    }
                }
            }

            if (flag) {
                books.add(new Booking(id.createId(), instId, startAt, endAt, userId));
                System.out.println("OK booking_id = "+id.getId());
            } else {
                System.err.println("В это время инструмент занят");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id интсрумента");
        } catch (NumberFormatException ex){
            System.err.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | NotFromException | PastTimeException | StartAfterEndException | SecurityException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Вы неправильно ввели дату");
        }
    }


    public ArrayList<Booking> getBooks() {
        return books;
    }public void bookCancel(String[] com) {
        try{
            if (!SessionContext.isAuthenticated()) {
                throw new SecurityException("Для создания брони необходимо войти в систему (login)");
            }
            long bookingId;
            bookingId = Integer.parseInt(com[1]);
            IdValidator.checkId(bookingId);
            if (books.stream().noneMatch(b -> b.getId() == bookingId)){
                throw new NotFoundException("Booking "+bookingId+" не найден");
            }
            for (Booking book : books){
                if (bookingId == book.getId()) {
                    if (SessionContext.getCurrentUserId() == book.getOwnerUserId()){
                        book.setStatus(BookingStatus.CANCELLED);
                        System.out.println("Бронь успешно отменена");
                    } else {
                        System.err.println("Вы не можете менять не свои бронирования");
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id букинга");
        } catch (NumberFormatException ex){
            System.err.println("Вы ввели недействительный booking id");
        } catch (UnderOneException | SecurityException | NotFoundException e) {
            System.err.println(e.getMessage());
        }
    }


    public void bookList(String[] com) {
        try{
            long instId = Integer.parseInt(com[1]);
            IdValidator.checkId(instId);
            String flag = com[2];
            FlagValidator.checkFromFlag(flag);
            Instant from = Instant.parse(TimeValidator.timeFormat(com[3]+" 00:00"));
            if (books.stream().noneMatch(b -> b.getInstrumentId() == instId)){
                throw new NotFoundException("Booking с instrument_id "+instId+" не найден");
            }
            System.out.println("ID" + "  " + "START" + "          " + "END" + "          " + "\n");

            for (Booking book : books) {
                if (book.getInstrumentId() == instId && book.getStartAt().isAfter(from) && book.getStatus() != BookingStatus.CANCELLED) {
                    System.out.println(book.getId() + " " + TimeValidator.parseBack(book.getStartAt())
                            + " " + TimeValidator.parseBack(book.getEndAt()));
                }
            }
        } catch (NumberFormatException ex){
            System.err.println("Вы ввели недействительный instrument id\n");
        } catch (UnderOneException | NotFromException | PastTimeException | NotFoundException e) {
            System.err.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("Вы ввели неправильное количество аргументов");
        }
        catch (Exception e) {
            System.err.println("Вы неправильно ввели дату\n");
        }
    }public void bookShow(String[] com) {
        try {
            long bookingId = Integer.parseInt(com[1]);
            IdValidator.checkId(bookingId);
            if (books.stream().noneMatch(b -> b.getId() == bookingId)) {
                throw new NotFoundException("Booking " + bookingId + " не найден");
            }
            for (Booking book : books) {
                if (book.getId() == bookingId) {
                    System.out.println("#" + bookingId);
                    System.out.println("instrument_id:" + book.getInstrumentId());
                    System.out.println("start:" + TimeValidator.parseBack(book.getStartAt()));
                    System.out.println("end:" + TimeValidator.parseBack(book.getEndAt()));
                    System.out.println("status:" + book.getStatus());
                    System.out.println("User:" + book.getOwnerUserId());
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id букинга");
        } catch (UnderOneException | NotFoundException e){
            System.err.println(e.getMessage());
        } catch (NumberFormatException e){
            System.err.println("Вы ввели недействительный instrument id");
        }
    }

    public void bookReschedule(String[] com) {
        //booking_id, Instant start, Instant end
        try{
            if (!SessionContext.isAuthenticated()) {
                throw new SecurityException("Для создания брони необходимо войти в систему (login)");
            }
            long bookId = Integer.parseInt(com[1]);// создавать в отдельном сервисе
            IdValidator.checkId(bookId);
            Instant start = Instant.parse(TimeValidator.timeFormat(com[2]+" "+com[3]));
            Instant end = Instant.parse(TimeValidator.timeFormat(com[4]+" "+com[5]));
            TimeValidator.StartEndCheck(start, end);
            if (books.stream().noneMatch(b -> b.getId() == bookId)){
                throw new NotFoundException("Booking "+bookId+" не найден");
            }
            for (Booking book : books) {
                boolean flag = true;
                if (book.getId() == bookId) {
                    for (Booking booking : books) {
                        if (booking.getInstrumentId() == book.getInstrumentId() && booking.getId() != bookId
                                && booking.getStatus() == BookingStatus.ACTIVE){
                            if (SessionContext.getCurrentUserId() == book.getOwnerUserId()){
                                if ((!booking.getStartAt().isAfter(end) && !booking.getEndAt().isBefore(start)) ||
                                        (booking.getStartAt().isBefore(start) && booking.getEndAt().isAfter(end))) {
                                    flag = false;
                                }
                            } else {
                                System.err.println("У вас нет прав на изменение чужой брони");
                            }
                        }
                    }
                    if (flag){
                        book.setStartAt(start);
                        book.setEndAt(end);
                        System.out.println(book.getStatus() + " " + "reschedule");
                        break;
                    } else {
                        System.err.println("Нельзя перенести на это время");
                    }

                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id букинга");
        } catch (NumberFormatException ex){
            System.err.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | PastTimeException | StartAfterEndException | SecurityException | NotFoundException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Вы неправильно ввели дату");
        }
    }

    public void replaceAll(List<Booking> newBookings) {
        books.clear();
        books.addAll(newBookings);long maxId = newBookings.stream()
                .mapToLong(Booking::getId)
                .max()
                .orElse(0L);

        id.setId(maxId + 1);
    }

    public void loadFromDb() {
        try {
            books.clear();
            books.addAll(jdbc.loadAllBookings());
            System.out.println("Загружено броней из БД: " + books.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки броней: " + e.getMessage());
        }
    }

    public void createNewBooking(long instId, Instant startAt, Instant endAt)
            throws UnderOneException, RuntimeException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для создания брони необходимо войти в систему (login)");
        }
        boolean flag = true;
        IdValidator.checkId(instId);
        TimeValidator.StartEndCheck(startAt, endAt);

        for (Booking booking : books) {
            if (booking.getInstrumentId() == instId && booking.getStatus() == BookingStatus.ACTIVE){
                if ((!booking.getStartAt().isAfter(endAt) && !booking.getEndAt().isBefore(startAt)) ||
                        (booking.getStartAt().isBefore(startAt) && booking.getEndAt().isAfter(endAt))) {
                    flag = false;
                }
            }
        }

        if (flag) {
            try {
                Booking newB = new Booking(0, instId, startAt, endAt, SessionContext.getCurrentUserId(),
                        BookingStatus.ACTIVE, Instant.now(), Instant.now());
                long generatedId = jdbc.insertBooking(newB);
                newB.setId(generatedId);
                books.add(newB);
            } catch (SQLException e){
                throw dbError(e);
            }
        } else {
            throw new NotAvailableException();
        }
    }

    public void bookCancel(long bookingId) throws UnderOneException, RuntimeException{
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для создания брони необходимо войти в систему (login)");
        }
        IdValidator.checkId(bookingId);
        for (Booking book : books){
            if (bookingId == book.getId()) {
                if (SessionContext.getCurrentUserId() == book.getOwnerUserId()){
                    BookingStatus oldStatus = book.getStatus();
                    book.setStatus(BookingStatus.CANCELLED);
                    try {
                        jdbc.updateBooking(book);
                    } catch (SQLException e) {
                        book.setStatus(oldStatus);
                        throw dbError(e);
                    }
                } else {
                    throw new RuntimeException("Вы не можете отменить не свою бронь");
                }

            }
        }
    }

    public void bookReschedule(long bookId, Instant start, Instant end)
            throws Exception{
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для создания брони необходимо войти в систему (login)");
        }
        IdValidator.checkId(bookId);
        TimeValidator.StartEndCheck(start, end);

        for (Booking book : books) {
            boolean flag = true;
            if (book.getId() == bookId) {
                for (Booking booking : books) {
                    if (booking.getInstrumentId() == book.getInstrumentId() && booking.getId() != bookId
                            && booking.getStatus() == BookingStatus.ACTIVE){
                        if ((!booking.getStartAt().isAfter(end) && !booking.getEndAt().isBefore(start)) ||
                                (booking.getStartAt().isBefore(start) && booking.getEndAt().isAfter(end))) {
                            flag = false;
                        }
                    }
                }
                if (flag){
                    if (SessionContext.getCurrentUserId() == book.getOwnerUserId()){
                        Instant oldStart = book.getStartAt();
                        Instant oldEnd = book.getEndAt();book.setStartAt(start);
                        book.setEndAt(end);
                        try {
                            jdbc.updateBooking(book);
                            return;
                        } catch (SQLException e) {
                            book.setStartAt(oldStart);
                            book.setEndAt(oldEnd);
                            throw dbError(e);
                        }
                    } else {
                        throw new Exception("Нельзя перенести не свою бронь");
                    }
                } else {
                    throw new Exception("Нельзя перенести на это время");
                }
            }
        }
        throw new Exception("Такой брони нет");
    }

    private RuntimeException dbError(SQLException e) {
        return new RuntimeException(DbErrorHandler.translate(e), e);
    }
}