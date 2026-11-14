package com.hotel.loader;

import com.hotel.exception.DataLoadingException;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the hotel rooms from a CSV file with the format:
 * number,type,pricePerNight,capacity
 *
 * Only responsible for reading and converting the file (Single Responsibility):
 * storing the rooms is the job of the repository.
 */
public class RoomCsvLoader {

    private static final int EXPECTED_COLUMNS = 4;

    /** Loads the rooms from a file on disk. */
    public List<Room> loadFromFile(Path path) throws DataLoadingException {
        if (!Files.exists(path)) {
            throw new DataLoadingException("Data file not found: " + path.toAbsolutePath());
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(reader);
        } catch (IOException exception) {
            throw new DataLoadingException("Unable to read " + path, exception);
        }
    }

    /** Loads the rooms from a file packaged in src/main/resources. */
    public List<Room> loadFromClasspath(String resourceName) throws DataLoadingException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new DataLoadingException("Resource not found: " + resourceName);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return load(reader);
        } catch (IOException exception) {
            throw new DataLoadingException("Unable to read resource " + resourceName, exception);
        }
    }

    /**
     * Parses CSV content. Kept separate from the file access so it can be
     * unit-tested with a simple StringReader, without creating files.
     */
    public List<Room> load(Reader source) throws DataLoadingException {
        List<Room> rooms = new ArrayList<>();
        Set<Integer> seenNumbers = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(source)) {
            String header = reader.readLine();
            if (header == null) {
                throw new DataLoadingException("The data file is empty");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                Room room = parseLine(line, lineNumber);
                // Two rooms with the same number would make reservations ambiguous.
                if (!seenNumbers.add(room.getNumber())) {
                    throw new DataLoadingException(
                            "Line " + lineNumber + ": duplicate room number " + room.getNumber());
                }
                rooms.add(room);
            }
        } catch (IOException exception) {
            throw new DataLoadingException("Unable to read the data file", exception);
        }

        if (rooms.isEmpty()) {
            throw new DataLoadingException("The data file contains no room");
        }
        return rooms;
    }

    private Room parseLine(String line, int lineNumber) throws DataLoadingException {
        String[] fields = line.split(",");
        if (fields.length != EXPECTED_COLUMNS) {
            throw new DataLoadingException("Line " + lineNumber + ": expected "
                    + EXPECTED_COLUMNS + " columns but found " + fields.length);
        }
        try {
            int number = Integer.parseInt(fields[0].trim());
            RoomType type = RoomType.valueOf(fields[1].trim().toUpperCase());
            double pricePerNight = Double.parseDouble(fields[2].trim());
            int capacity = Integer.parseInt(fields[3].trim());
            return RoomFactory.create(type, number, pricePerNight, capacity);
        } catch (NumberFormatException exception) {
            throw new DataLoadingException("Line " + lineNumber + ": invalid number in \"" + line + "\"", exception);
        } catch (IllegalArgumentException exception) {
            // Unknown room type (RoomType.valueOf) or invalid value refused by the Room constructor.
            throw new DataLoadingException("Line " + lineNumber + ": " + exception.getMessage(), exception);
        }
    }
}
