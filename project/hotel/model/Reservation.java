package project.hotel.model;

import java.time.LocalDate;

public class Reservation {
    private String reservationId;
    private int roomNumber;
    private String roomType;
    private String firstName;
    private String lastName;
    private String passportId;
    private String phone;
    private String email;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private int guestsCount;
    private String paymentMethod;
    private String specialRequests;
    private int totalPrice;

    public Reservation(String reservationId,
                       int roomNumber,
                       String roomType,
                       String firstName,
                       String lastName,
                       String passportId,
                       String phone,
                       String email,
                       LocalDate checkInDate,
                       LocalDate checkOutDate,
                       int nights,
                       int guestsCount,
                       String paymentMethod,
                       String specialRequests,
                       int totalPrice) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.phone = phone;
        this.email = email;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.guestsCount = guestsCount;
        this.paymentMethod = paymentMethod;
        this.specialRequests = specialRequests;
        this.totalPrice = totalPrice;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGuestFullName() {
        return firstName + " " + lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public int getGuestsCount() {
        return guestsCount;
    }

    public void setGuestsCount(int guestsCount) {
        this.guestsCount = guestsCount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}