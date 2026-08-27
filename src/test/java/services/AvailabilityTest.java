package services;

import auth.SessionContext;
import domain.Booking;
import domain.Checkout;
import domain.Instrument;
import domain.User;
import enums.BookingStatus;
import enums.InstrumentType;
import enums.ReturnCondition;
import exceptions.SecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AvailabilityTest {

    private InstrumentManager instrumentManager;
    private BookingManager bookingManager;
    private CheckoutManager checkoutManager;

    @BeforeEach
    void setUp() {
        instrumentManager = new InstrumentManager();
        bookingManager = new BookingManager();
        checkoutManager = new CheckoutManager();
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    @Test
    void testInstAvailableWithoutNPEOnActiveCheckout() {
        // Setup: Instrument 1 (MICROSCOPE)
        Instrument microscope = new Instrument(1L, InstrumentType.MICROSCOPE);
        instrumentManager.replaceAll(List.of(microscope));

        // Checkout with returnedAt == null (tool is currently taken out!)
        Instant takenAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Checkout activeCheckout = new Checkout(1L, 1L, 1L, "Field work", takenAt, null, null, "user1", takenAt);
        checkoutManager.replaceAll(List.of(activeCheckout));

        // Request future slot
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(4, ChronoUnit.HOURS);

        // This must NOT throw NullPointerException!
        assertDoesNotThrow(() -> {
            List<Instrument> avail = instrumentManager.instAvailable(checkoutManager, bookingManager, InstrumentType.MICROSCOPE, start, end);
            // Since activeCheckout is not returned, it cannot be available
            assertTrue(avail.isEmpty());
        });
    }

    @Test
    void testInstAvailableWhenFree() {
        Instrument beaker = new Instrument(2L, InstrumentType.BEAKER);
        instrumentManager.replaceAll(List.of(beaker));

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS);

        List<Instrument> avail = instrumentManager.instAvailable(checkoutManager, bookingManager, InstrumentType.BEAKER, start, end);
        assertEquals(1, avail.size());
        assertEquals(2L, avail.get(0).getId());
    }

    @Test
    void testSecurityCheckWhenUnauthenticated() {
        assertThrows(SecurityException.class, () -> {
            bookingManager.createBooking(1L, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(2, ChronoUnit.HOURS));
        });

        assertThrows(SecurityException.class, () -> {
            checkoutManager.takeCheckout(1L, "Test");
        });
    }
}
