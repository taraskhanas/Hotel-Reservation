package project.hotel.util;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import project.hotel.model.Reservation;

public class ReservationRepository {

    public List<Reservation> loadReservations() {
        StorageUtil.ensureStorage();

        try {
            List<String> lines = Files.readAllLines(StorageUtil.getReservationsFile());
            List<Reservation> reservations = new ArrayList<>();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 14) {
                    continue;
                }

                reservations.add(new Reservation(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5],
                        parts[6],
                        parts[7],
                        LocalDate.parse(parts[8]),
                        LocalDate.parse(parts[9]),
                        Integer.parseInt(parts[10]),
                        Integer.parseInt(parts[11]),
                        parts[12],
                        parts[13],
                        Integer.parseInt(parts[14])
                ));
            }

            return reservations;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reservations: " + e.getMessage(), e);
        }
    }

    public void saveAllReservations(List<Reservation> reservations) {
        StorageUtil.ensureStorage();

        List<String> lines = new ArrayList<>();
        for (Reservation r : reservations) {
            lines.add(
                    r.getReservationId() + "|" +
                    r.getRoomNumber() + "|" +
                    r.getRoomType() + "|" +
                    r.getFirstName() + "|" +
                    r.getLastName() + "|" +
                    r.getPassportId() + "|" +
                    r.getPhone() + "|" +
                    r.getEmail() + "|" +
                    r.getCheckInDate() + "|" +
                    r.getCheckOutDate() + "|" +
                    r.getNights() + "|" +
                    r.getGuestsCount() + "|" +
                    r.getPaymentMethod() + "|" +
                    r.getSpecialRequests() + "|" +
                    r.getTotalPrice()
            );
        }

        try {
            Files.write(StorageUtil.getReservationsFile(), lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reservations: " + e.getMessage(), e);
        }
    }
}