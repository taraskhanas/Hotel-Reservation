package project.hotel.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageUtil {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path ROOMS_FILE = DATA_DIR.resolve("rooms.txt");
    private static final Path RESERVATIONS_FILE = DATA_DIR.resolve("reservations.txt");

    public static void ensureStorage() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }

            if (!Files.exists(ROOMS_FILE)) {
                Files.createFile(ROOMS_FILE);
            }

            if (!Files.exists(RESERVATIONS_FILE)) {
                Files.createFile(RESERVATIONS_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage: " + e.getMessage(), e);
        }
    }

    public static Path getRoomsFile() {
        return ROOMS_FILE;
    }

    public static Path getReservationsFile() {
        return RESERVATIONS_FILE;
    }
}