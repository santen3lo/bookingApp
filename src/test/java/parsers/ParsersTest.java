package parsers;

import enums.InstrumentType;
import enums.ReturnCondition;
import exceptions.NotFromException;
import exceptions.NotOpenException;
import exceptions.UnderOneException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ParsersTest {

    @Test
    void testCliInputParserValidValues() throws Exception {
        assertEquals(5L, CliInputParser.parseId("5", "instrument"));
        assertEquals(InstrumentType.MICROSCOPE, CliInputParser.parseInstrumentType("microscope"));
        assertEquals(ReturnCondition.DAMAGED, CliInputParser.parseReturnCondition("damaged"));

        assertDoesNotThrow(() -> CliInputParser.validateFromFlag("--from"));
        assertThrows(NotFromException.class, () -> CliInputParser.validateFromFlag("--other"));

        assertDoesNotThrow(() -> CliInputParser.validateOpenOnlyFlag("--open-only"));
        assertThrows(NotOpenException.class, () -> CliInputParser.validateOpenOnlyFlag("--all"));

        Instant fromDate = CliInputParser.parseFromDate("2026-06-01");
        assertNotNull(fromDate);
    }

    @Test
    void testCliInputParserInvalidId() {
        assertThrows(UnderOneException.class, () -> CliInputParser.parseId("0", "booking"));
        assertThrows(IllegalArgumentException.class, () -> CliInputParser.parseId("abc", "booking"));
        assertThrows(IllegalArgumentException.class, () -> CliInputParser.parseId("", "booking"));
    }

    @Test
    void testGuiInputParserBookingCreate() throws Exception {
        String input = "1, 2030-01-01 10:00, 2030-01-01 12:00";
        GuiInputParser.BookingCreateParams params = GuiInputParser.parseBookingCreate(input);
        assertEquals(1L, params.instrumentId());
        assertNotNull(params.startAt());
        assertNotNull(params.endAt());
        assertTrue(params.endAt().isAfter(params.startAt()));
    }

    @Test
    void testGuiInputParserCheckoutTake() throws Exception {
        String input = "2, Field experiment";
        GuiInputParser.CheckoutTakeParams params = GuiInputParser.parseCheckoutTake(input);
        assertEquals(2L, params.instrumentId());
        assertEquals("Field experiment", params.comment());
    }

    @Test
    void testGuiInputParserAvailability() throws Exception {
        String input = "BEAKER, 2030-01-01 09:00, 2030-01-01 11:00";
        GuiInputParser.AvailabilityParams params = GuiInputParser.parseAvailability(input);
        assertEquals(InstrumentType.BEAKER, params.type());
        assertNotNull(params.startAt());
        assertNotNull(params.endAt());
    }
}
