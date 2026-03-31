package project.hotel.util;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import project.hotel.model.Room;

public class RoomRepository {

    public List<Room> loadRooms() {
        StorageUtil.ensureStorage();

        try {
            List<String> lines = Files.readAllLines(StorageUtil.getRoomsFile());
            List<Room> rooms = new ArrayList<>();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) {
                    continue;
                }

                rooms.add(new Room(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        parts[4]
                ));
            }

            if (rooms.isEmpty()) {
                rooms = getDefaultRooms();
                saveAllRooms(rooms);
            }

            return rooms;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rooms: " + e.getMessage(), e);
        }
    }

    public void saveAllRooms(List<Room> rooms) {
        StorageUtil.ensureStorage();

        List<String> lines = new ArrayList<>();
        for (Room room : rooms) {
            lines.add(
                    room.getRoomNumber() + "|" +
                    room.getType() + "|" +
                    room.getBeds() + "|" +
                    room.getPricePerNight() + "|" +
                    room.getStatus()
            );
        }

        try {
            Files.write(StorageUtil.getRoomsFile(), lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save rooms: " + e.getMessage(), e);
        }
    }

    private List<Room> getDefaultRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(101, "Single", 1, 50, "Available"));
        rooms.add(new Room(102, "Double", 2, 80, "Available"));
        rooms.add(new Room(201, "Deluxe", 2, 120, "Reserved"));
        rooms.add(new Room(305, "Family", 4, 170, "Available"));
        return rooms;
    }
}