package project.hotel.model;

public class Room {
    private int roomNumber;
    private String type;
    private int beds;
    private int pricePerNight;
    private String status;

    public Room(int roomNumber, String type, int beds, int pricePerNight, String status) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.beds = beds;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBeds() {
        return beds;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public int getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(int pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriceFormatted() {
        return "$" + pricePerNight;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return roomNumber + " - " + type;
    }
}