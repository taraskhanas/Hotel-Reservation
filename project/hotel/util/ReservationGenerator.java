package project.hotel.util;

import java.util.List;

import project.hotel.model.Reservation;

public class ReservationGenerator {

    public static String generateReservationId(List<Reservation> reservations) {
        int max = 1000;

        for (Reservation reservation : reservations) {
            String id = reservation.getReservationId();
            if (id != null && id.startsWith("RES-")) {
                try {
                    int number = Integer.parseInt(id.substring(4));
                    if (number > max) {
                        max = number;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return "RES-" + (max + 1);
    }
}