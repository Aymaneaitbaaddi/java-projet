package com.hotel.loader;

import com.hotel.exception.DataLoadingException;
import com.hotel.model.DoubleRoom;
import com.hotel.model.Room;
import com.hotel.model.SingleRoom;
import com.hotel.model.Suite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomCsvLoaderTest {

    private static final String HEADER = "number,type,pricePerNight,capacity\n";
    private final RoomCsvLoader loader = new RoomCsvLoader();

    @Test
    void validFileCreatesTheRightRoomTypes() throws DataLoadingException {
        String csv = HEADER
                + "101,SINGLE,60.0,1\n"
                + "103,DOUBLE,90.0,2\n"
                + "202,SUITE,180.0,4\n";

        List<Room> rooms = loader.load(new StringReader(csv));

        assertEquals(3, rooms.size());
        assertInstanceOf(SingleRoom.class, rooms.get(0));
        assertInstanceOf(DoubleRoom.class, rooms.get(1));
        assertInstanceOf(Suite.class, rooms.get(2));
        assertEquals(180.0, rooms.get(2).getPricePerNight(), 0.001);
    }

    @Test
    void blankLinesAndLowerCaseTypesAreAccepted() throws DataLoadingException {
        String csv = HEADER + "\n101, single , 60.0, 1\n\n";

        List<Room> rooms = loader.load(new StringReader(csv));

        assertEquals(1, rooms.size());
    }

    @Test
    void unknownRoomTypeIsReportedWithItsLineNumber() {
        String csv = HEADER + "101,SINGLE,60.0,1\n102,PENTHOUSE,500.0,2\n";

        DataLoadingException exception = assertThrows(DataLoadingException.class,
                () -> loader.load(new StringReader(csv)));

        assertTrue(exception.getMessage().startsWith("Line 3"));
    }

    @Test
    void invalidNumberIsRefused() {
        String csv = HEADER + "101,SINGLE,cheap,1\n";

        DataLoadingException exception = assertThrows(DataLoadingException.class,
                () -> loader.load(new StringReader(csv)));

        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void missingColumnIsRefused() {
        String csv = HEADER + "101,SINGLE,60.0\n";

        assertThrows(DataLoadingException.class, () -> loader.load(new StringReader(csv)));
    }

    @Test
    void duplicateRoomNumberIsRefused() {
        String csv = HEADER + "101,SINGLE,60.0,1\n101,DOUBLE,90.0,2\n";

        DataLoadingException exception = assertThrows(DataLoadingException.class,
                () -> loader.load(new StringReader(csv)));

        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void fileWithOnlyAHeaderIsRefused() {
        assertThrows(DataLoadingException.class, () -> loader.load(new StringReader(HEADER)));
    }

    @Test
    void missingFileIsRefused(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.csv");

        assertThrows(DataLoadingException.class, () -> loader.loadFromFile(missing));
    }

    @Test
    void roomsAreLoadedFromAFileOnDisk(@TempDir Path tempDir) throws IOException, DataLoadingException {
        Path file = tempDir.resolve("rooms.csv");
        Files.writeString(file, HEADER + "101,SINGLE,60.0,1\n");

        List<Room> rooms = loader.loadFromFile(file);

        assertEquals(101, rooms.get(0).getNumber());
    }

    @Test
    void defaultResourceFileIsValid() throws DataLoadingException {
        List<Room> rooms = loader.loadFromClasspath("rooms.csv");

        assertFalse(rooms.isEmpty());
    }
}
