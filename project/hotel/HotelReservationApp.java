package project.hotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project.hotel.model.Reservation;
import project.hotel.model.Room;
import project.hotel.util.ReservationGenerator;
import project.hotel.util.ReservationRepository;
import project.hotel.util.RoomRepository;
import project.hotel.util.StorageUtil;
import javafx.scene.control.TableCell;

public class HotelReservationApp extends Application {

    private Stage primaryStage;

    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private Room selectedRoom;

    private BorderPane rootLayout;
    private ScrollPane contentScrollPane;
    private VBox contentArea;

    private static final String NORMAL_TEXT_STYLE = "-fx-font-size: 15px;";
    private static final String HEADER_STYLE = "-fx-font-size: 22px; -fx-font-weight: bold;";
    private static final String BUTTON_STYLE = "-fx-font-size: 14px;";

    @Override
    public void start(Stage stage) {
        StorageUtil.ensureStorage();
        rooms.setAll(roomRepository.loadRooms());
        reservations.setAll(reservationRepository.loadReservations());

        this.primaryStage = stage;

        rootLayout = new BorderPane();
        contentArea = new VBox();
        contentArea.setFillWidth(true);

        contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(true);
        contentScrollPane.setPannable(true);

        rootLayout.setLeft(createSidebar());
        rootLayout.setCenter(contentScrollPane);

        BorderPane.setMargin(contentScrollPane, new Insets(16));

        setContent(createHomeView());

        Scene scene = new Scene(rootLayout, 1100, 760);
        scene.getStylesheets().add(
        getClass().getResource("/project/hotel/style.css").toExternalForm()
);

        primaryStage.setTitle("Hotel Reservation System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveRooms() {
        roomRepository.saveAllRooms(rooms);
    }

    private void saveReservations() {
        reservationRepository.saveAllReservations(reservations);
    }

    private VBox createSidebar() {
        Label title = new Label("HOTEL\nRESERVATION");
        title.getStyleClass().add("sidebar-title");

        Button homeButton = sidebarButton("Home");
        Button roomsButton = sidebarButton("View Rooms");
        Button reservationButton = sidebarButton("Make Reservation");
        Button manageRoomsButton = sidebarButton("Manage Rooms");
        Button manageReservationsButton = sidebarButton("Manage Reservations");
        Button exitButton = sidebarButton("Exit");

        homeButton.setOnAction(e -> setContent(createHomeView()));
        roomsButton.setOnAction(e -> setContent(createRoomsView()));
        reservationButton.setOnAction(e -> setContent(createReservationView()));
        manageRoomsButton.setOnAction(e -> setContent(createManageRoomsView()));
        manageReservationsButton.setOnAction(e -> setContent(createManageReservationsView()));
        exitButton.setOnAction(e -> primaryStage.close());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(
                14,
                title,
                homeButton,
                roomsButton,
                reservationButton,
                manageRoomsButton,
                manageReservationsButton,
                spacer,
                exitButton
        );
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(200);
        sidebar.getStyleClass().add("sidebar");

        return sidebar;
    }

    private Button sidebarButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.getStyleClass().add("sidebar-button");
        return button;
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    private VBox createHomeView() {
        Label title = new Label("HOTEL RESERVATION SYSTEM");
        title.getStyleClass().add("header-label");

        Label text = new Label("Choose an action from the left menu.");
        text.setStyle(NORMAL_TEXT_STYLE);

        VBox box = new VBox(18, title, text);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.getStyleClass().add("content-card");

        return box;
    }

    private VBox createRoomsView() {
        Label title = new Label("ROOMS");
        title.getStyleClass().add("header-label");

        ObservableList<Room> filteredRooms = FXCollections.observableArrayList();
        filteredRooms.setAll(rooms);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Available", "Reserved");
        statusFilter.setValue("All");

        ComboBox<String> bedsFilter = new ComboBox<>();
        bedsFilter.getItems().add("All");
        for (Integer beds : getUniqueBeds()) {
            bedsFilter.getItems().add(String.valueOf(beds));
        }
        bedsFilter.setValue("All");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All");
        typeFilter.getItems().addAll(getOrderedRoomTypes());
        typeFilter.setValue("All");

        DatePicker filterCheckInPicker = new DatePicker();
        DatePicker filterCheckOutPicker = new DatePicker();

        filterCheckInPicker.setPromptText("Check-in");
        filterCheckOutPicker.setPromptText("Check-out");

        filterCheckInPicker.setStyle(NORMAL_TEXT_STYLE);
        filterCheckOutPicker.setStyle(NORMAL_TEXT_STYLE);

        styleComboBox(statusFilter);
        styleComboBox(bedsFilter);
        styleComboBox(typeFilter);

        TableView<Room> roomTable = new TableView<>();
        roomTable.setItems(filteredRooms);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        TableColumn<Room, Integer> roomNoCol = new TableColumn<>("Room No");
        roomNoCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Room, Integer> bedsCol = new TableColumn<>("Beds");
        bedsCol.setCellValueFactory(new PropertyValueFactory<>("beds"));

        TableColumn<Room, String> priceCol = new TableColumn<>("Price per Night");
        priceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPriceFormatted()));

        TableColumn<Room, String> statusCol = new TableColumn<>("Booking Status");
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        getBookingStatusForRange(
                                cellData.getValue(),
                                filterCheckInPicker.getValue(),
                                filterCheckOutPicker.getValue()
                        )
                ));
         statusCol.setCellFactory(column -> new TableCell<>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setStyle("");
        } else {
            setText(item);

            if ("Available".equalsIgnoreCase(item)) {
                setStyle("-fx-text-fill: #19c37d; -fx-font-weight: bold;");
            } else if ("Reserved".equalsIgnoreCase(item)) {
                setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
            } else {
                setStyle("-fx-text-fill: #e8e8e8;");
            }
        }
    }
});       

        roomTable.getColumns().addAll(roomNoCol, typeCol, bedsCol, priceCol, statusCol);

        Runnable applyFilters = () -> {
            filteredRooms.clear();

            LocalDate filterIn = filterCheckInPicker.getValue();
            LocalDate filterOut = filterCheckOutPicker.getValue();

            for (Room room : rooms) {
                boolean matches = true;
                String bookingStatus = getBookingStatusForRange(room, filterIn, filterOut);

                if (!"All".equals(statusFilter.getValue())
                        && !bookingStatus.equalsIgnoreCase(statusFilter.getValue())) {
                    matches = false;
                }

                if (!"All".equals(bedsFilter.getValue())
                        && room.getBeds() != Integer.parseInt(bedsFilter.getValue())) {
                    matches = false;
                }

                if (!"All".equals(typeFilter.getValue())
                        && !room.getType().equalsIgnoreCase(typeFilter.getValue())) {
                    matches = false;
                }

                if (matches) {
                    filteredRooms.add(room);
                }
            }

            roomTable.refresh();
        };

        statusFilter.setOnAction(e -> applyFilters.run());
        bedsFilter.setOnAction(e -> applyFilters.run());
        typeFilter.setOnAction(e -> applyFilters.run());
        filterCheckInPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        filterCheckOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());

        VBox statusBox = new VBox(6, normalLabel("Status"), statusFilter);
        VBox bedsBox = new VBox(6, normalLabel("Beds"), bedsFilter);
        VBox typeBox = new VBox(6, normalLabel("Type"), typeFilter);
        VBox checkInBox = new VBox(6, normalLabel("Check-in"), filterCheckInPicker);
        VBox checkOutBox = new VBox(6, normalLabel("Check-out"), filterCheckOutPicker);

        HBox filtersBox = new HBox(14, statusBox, bedsBox, typeBox, checkInBox, checkOutBox);
        filtersBox.setAlignment(Pos.BOTTOM_LEFT);

        Button selectButton = standardButton("Select Room");
        Button infoButton = standardButton("Open Reservation Info");
        Button allReservationsButton = standardButton("Room Reservations");
        Button refreshButton = standardButton("Refresh");

        selectButton.setOnAction(e -> {
            Room room = roomTable.getSelectionModel().getSelectedItem();

            if (room == null) {
                showError("Please select a room.");
                return;
            }

            selectedRoom = room;
            setContent(createReservationView());
        });

        infoButton.setOnAction(e -> {
            Room room = roomTable.getSelectionModel().getSelectedItem();

            if (room == null) {
                showError("Please select a room.");
                return;
            }

            Reservation reservation = findLatestReservationByRoomNumber(
                    room.getRoomNumber(),
                    filterCheckInPicker.getValue(),
                    filterCheckOutPicker.getValue()
            );

            if (reservation == null) {
                showError("No reservation was found for this room in the selected period.");
                return;
            }

            openReservationInfoPopup(reservation);
        });

        allReservationsButton.setOnAction(e -> {
            Room room = roomTable.getSelectionModel().getSelectedItem();

            if (room == null) {
                showError("Please select a room.");
                return;
            }

            openRoomReservationsPopup(
                    room,
                    filterCheckInPicker.getValue(),
                    filterCheckOutPicker.getValue()
            );
        });

        refreshButton.setOnAction(e -> {
            rooms.setAll(roomRepository.loadRooms());
            reservations.setAll(reservationRepository.loadReservations());

            bedsFilter.getItems().clear();
            bedsFilter.getItems().add("All");
            for (Integer beds : getUniqueBeds()) {
                bedsFilter.getItems().add(String.valueOf(beds));
            }
            bedsFilter.setValue("All");

            typeFilter.getItems().clear();
            typeFilter.getItems().add("All");
            typeFilter.getItems().addAll(getOrderedRoomTypes());
            typeFilter.setValue("All");

            statusFilter.setValue("All");
            filterCheckInPicker.setValue(null);
            filterCheckOutPicker.setValue(null);

            filteredRooms.setAll(rooms);
            roomTable.refresh();
        });

        HBox buttons = new HBox(10, selectButton, infoButton, allReservationsButton, refreshButton);

        VBox box = new VBox(16, title, filtersBox, roomTable, buttons);
        box.setPadding(new Insets(8));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(roomTable, Priority.ALWAYS);
        box.getStyleClass().add("content-card");

        return box;
    }

    private VBox createReservationView() {
        Label title = new Label("MAKE A RESERVATION");
        title.getStyleClass().add("header-label");

        Label selectedRoomLabel = new Label(
                selectedRoom == null
                        ? "Selected room: none"
                        : "Selected room: " + selectedRoom.getRoomNumber() + " | "
                        + selectedRoom.getType() + " | " + selectedRoom.getPriceFormatted()
                        + " | Max guests: " + selectedRoom.getBeds()
        );
        selectedRoomLabel.setStyle(NORMAL_TEXT_STYLE);

        DatePicker checkInPicker = new DatePicker();
        DatePicker checkOutPicker = new DatePicker();

        Label nightsLabel = new Label("-");
        nightsLabel.setStyle(NORMAL_TEXT_STYLE);

        Label guestsValueLabel = new Label("1");
        guestsValueLabel.setStyle(NORMAL_TEXT_STYLE);

        Button minusBtn = standardButton("-");
        Button plusBtn = standardButton("+");
        minusBtn.setPrefWidth(45);
        plusBtn.setPrefWidth(45);

        minusBtn.setOnAction(e -> {
            int current = Integer.parseInt(guestsValueLabel.getText());
            if (current > 1) {
                guestsValueLabel.setText(String.valueOf(current - 1));
            }
        });

        plusBtn.setOnAction(e -> {
            if (selectedRoom == null) {
                showError("Please select a room first.");
                return;
            }

            int current = Integer.parseInt(guestsValueLabel.getText());
            if (current < selectedRoom.getBeds()) {
                guestsValueLabel.setText(String.valueOf(current + 1));
            }
        });

        HBox guestsBox = new HBox(12, minusBtn, guestsValueLabel, plusBtn);
        guestsBox.setAlignment(Pos.CENTER_LEFT);

        TextField firstNameField = styledTextFieldWithPrompt("First name");
        TextField lastNameField = styledTextFieldWithPrompt("Last name");
        TextField passportField = styledTextFieldWithPrompt("Passport / ID");
        TextField phoneField = styledTextFieldWithPrompt("Phone number");
        TextField emailField = styledTextFieldWithPrompt("Email");

        TextArea requestsArea = new TextArea();
        requestsArea.setPromptText("Special requests");
        requestsArea.setPrefRowCount(4);
        requestsArea.setStyle(NORMAL_TEXT_STYLE);
        requestsArea.setMaxWidth(Double.MAX_VALUE);

        checkInPicker.setStyle(NORMAL_TEXT_STYLE);
        checkOutPicker.setStyle(NORMAL_TEXT_STYLE);
        checkInPicker.setMaxWidth(Double.MAX_VALUE);
        checkOutPicker.setMaxWidth(Double.MAX_VALUE);

        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        Runnable updateNights = () -> {
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                nightsLabel.setText(String.valueOf(nights));
            } else {
                nightsLabel.setText("-");
            }
        };

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNights.run());
        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNights.run());

        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton cashButton = new RadioButton("Cash");
        RadioButton cardButton = new RadioButton("Card");
        RadioButton transferButton = new RadioButton("Bank Transfer");

        styleRadio(cashButton);
        styleRadio(cardButton);
        styleRadio(transferButton);

        cashButton.setToggleGroup(paymentGroup);
        cardButton.setToggleGroup(paymentGroup);
        transferButton.setToggleGroup(paymentGroup);
        cashButton.setSelected(true);

        HBox paymentBox = new HBox(18, cashButton, cardButton, transferButton);
        paymentBox.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12, 0, 12, 0));
        form.setHgap(14);
        form.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(150);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        form.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        form.add(normalLabel("Check-in date:"), 0, row);
        form.add(checkInPicker, 1, row++);

        form.add(normalLabel("Check-out date:"), 0, row);
        form.add(checkOutPicker, 1, row++);

        form.add(normalLabel("Number of nights:"), 0, row);
        form.add(nightsLabel, 1, row++);

        form.add(normalLabel("Guests count:"), 0, row);
        form.add(guestsBox, 1, row++);

        form.add(normalLabel("First name:"), 0, row);
        form.add(firstNameField, 1, row++);

        form.add(normalLabel("Last name:"), 0, row);
        form.add(lastNameField, 1, row++);

        form.add(normalLabel("Passport / ID:"), 0, row);
        form.add(passportField, 1, row++);

        form.add(normalLabel("Phone number:"), 0, row);
        form.add(phoneField, 1, row++);

        form.add(normalLabel("Email:"), 0, row);
        form.add(emailField, 1, row++);

        form.add(normalLabel("Special requests:"), 0, row);
        form.add(requestsArea, 1, row++);

        form.add(normalLabel("Payment method:"), 0, row);
        form.add(paymentBox, 1, row++);

        GridPane.setHgrow(firstNameField, Priority.ALWAYS);
        GridPane.setHgrow(lastNameField, Priority.ALWAYS);
        GridPane.setHgrow(passportField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(checkInPicker, Priority.ALWAYS);
        GridPane.setHgrow(checkOutPicker, Priority.ALWAYS);
        GridPane.setHgrow(requestsArea, Priority.ALWAYS);

        Button confirmButton = standardButton("Confirm Reservation");
        Button clearButton = standardButton("Clear");
        Button chooseRoomButton = standardButton("Choose Room");

        confirmButton.setOnAction(e -> {
            if (selectedRoom == null) {
                showError("Please select a room first.");
                return;
            }

            if (firstNameField.getText().trim().isEmpty()) {
                showError("First name cannot be empty.");
                return;
            }

            if (lastNameField.getText().trim().isEmpty()) {
                showError("Last name cannot be empty.");
                return;
            }

            if (passportField.getText().trim().isEmpty()) {
                showError("Passport / ID is required.");
                return;
            }

            LocalDate checkInDate = checkInPicker.getValue();
            LocalDate checkOutDate = checkOutPicker.getValue();

            if (checkInDate == null || checkOutDate == null) {
                showError("Please select dates.");
                return;
            }

            if (!checkOutDate.isAfter(checkInDate)) {
                showError("Check-out must be after check-in.");
                return;
            }

            int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            int guestsCount = Integer.parseInt(guestsValueLabel.getText());

            if (guestsCount > selectedRoom.getBeds()) {
                showError("Too many guests for this room.");
                return;
            }

            if (hasReservationIntersection(selectedRoom.getRoomNumber(), checkInDate, checkOutDate, null)) {
                showError("This room is already reserved for the selected dates.");
                return;
            }

            String paymentMethod = ((RadioButton) paymentGroup.getSelectedToggle()).getText();
            int totalPrice = nights * selectedRoom.getPricePerNight();

            Reservation reservation = new Reservation(
                    ReservationGenerator.generateReservationId(reservations),
                    selectedRoom.getRoomNumber(),
                    selectedRoom.getType(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    passportField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    checkInDate,
                    checkOutDate,
                    nights,
                    guestsCount,
                    paymentMethod,
                    requestsArea.getText().trim(),
                    totalPrice
            );

            reservations.add(reservation);
            saveReservations();

            openReservationInfoPopup(reservation);
            setContent(createManageReservationsView());
        });

        clearButton.setOnAction(e -> {
            checkInPicker.setValue(null);
            checkOutPicker.setValue(null);
            nightsLabel.setText("-");
            guestsValueLabel.setText("1");
            firstNameField.clear();
            lastNameField.clear();
            passportField.clear();
            phoneField.clear();
            emailField.clear();
            requestsArea.clear();
            cashButton.setSelected(true);
        });

        chooseRoomButton.setOnAction(e -> setContent(createRoomsView()));

        HBox buttons = new HBox(10, confirmButton, clearButton, chooseRoomButton);

        VBox box = new VBox(14, title, selectedRoomLabel, form, buttons);
        box.setPadding(new Insets(8));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.getStyleClass().add("content-card");

        return box;
    }

    private VBox createManageRoomsView() {
        Label title = new Label("MANAGE ROOMS");
        title.getStyleClass().add("header-label");

        TableView<Room> roomTable = new TableView<>();
        roomTable.setItems(rooms);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        TableColumn<Room, Integer> roomNoCol = new TableColumn<>("Room No");
        roomNoCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Room, Integer> bedsCol = new TableColumn<>("Beds");
        bedsCol.setCellValueFactory(new PropertyValueFactory<>("beds"));

        TableColumn<Room, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPriceFormatted()));

        roomTable.getColumns().addAll(roomNoCol, typeCol, bedsCol, priceCol);

        Button addButton = standardButton("Add Room");
        Button editButton = standardButton("Edit Selected");
        Button deleteButton = standardButton("Delete Selected");
        Button refreshButton = standardButton("Refresh");

        addButton.setOnAction(e -> openAddRoomPopup(roomTable));

        editButton.setOnAction(e -> {
            Room selected = roomTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a room first.");
                return;
            }

            openEditRoomPopup(selected, roomTable);
        });

        deleteButton.setOnAction(e -> {
            Room selected = roomTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a room first.");
                return;
            }

            boolean hasReservations = reservations.stream()
                    .anyMatch(r -> r.getRoomNumber() == selected.getRoomNumber());

            if (hasReservations) {
                showError("Cannot delete a room that has reservations.");
                return;
            }

            rooms.remove(selected);
            saveRooms();
            roomTable.refresh();
        });

        refreshButton.setOnAction(e -> {
            rooms.setAll(roomRepository.loadRooms());
            reservations.setAll(reservationRepository.loadReservations());
            roomTable.refresh();
        });

        HBox buttons = new HBox(10, addButton, editButton, deleteButton, refreshButton);

        VBox box = new VBox(16, title, roomTable, buttons);
        box.setPadding(new Insets(8));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.getStyleClass().add("content-card");

        return box;
    }

    private VBox createManageReservationsView() {
        Label title = new Label("MANAGE RESERVATIONS");
        title.getStyleClass().add("header-label");

        TableView<Reservation> reservationTable = new TableView<>();
        reservationTable.setItems(reservations);
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(reservationTable, Priority.ALWAYS);

        TableColumn<Reservation, String> idCol = new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));

        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGuestFullName()));

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoomNumber() + " - " + cellData.getValue().getRoomType()));

        TableColumn<Reservation, String> datesCol = new TableColumn<>("Dates");
        datesCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckInDate() + " - " + cellData.getValue().getCheckOutDate()));

        reservationTable.getColumns().addAll(idCol, guestCol, roomCol, datesCol);

        Button editButton = standardButton("Edit Selected");
        Button deleteButton = standardButton("Delete Selected");
        Button refreshButton = standardButton("Refresh");

        editButton.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a reservation first.");
                return;
            }

            openEditReservationPopup(selected, reservationTable);
        });

        deleteButton.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a reservation first.");
                return;
            }

            reservations.remove(selected);
            saveReservations();
            reservationTable.refresh();
        });

        refreshButton.setOnAction(e -> {
            rooms.setAll(roomRepository.loadRooms());
            reservations.setAll(reservationRepository.loadReservations());
            reservationTable.refresh();
        });

        HBox buttons = new HBox(10, editButton, deleteButton, refreshButton);

        VBox box = new VBox(16, title, reservationTable, buttons);
        box.setPadding(new Insets(8));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.getStyleClass().add("content-card");

        return box;
    }

    private void openAddRoomPopup(TableView<Room> roomTable) {
        Stage popup = createPopupStage("Add Room");

        TextField roomNumberField = styledTextField("");
        TextField typeField = styledTextField("");
        TextField bedsField = styledTextField("");
        TextField priceField = styledTextField("");

        GridPane form = buildResponsiveTwoColumnForm(120);

        int row = 0;
        form.add(normalLabel("Room number:"), 0, row);
        form.add(roomNumberField, 1, row++);

        form.add(normalLabel("Type:"), 0, row);
        form.add(typeField, 1, row++);

        form.add(normalLabel("Beds:"), 0, row);
        form.add(bedsField, 1, row++);

        form.add(normalLabel("Price:"), 0, row);
        form.add(priceField, 1, row++);

        Button applyButton = standardButton("Apply Changes");
        Button cancelButton = standardButton("Cancel");

        applyButton.setOnAction(e -> {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                int beds = Integer.parseInt(bedsField.getText().trim());
                int price = Integer.parseInt(priceField.getText().trim());
                String type = typeField.getText().trim();

                if (type.isEmpty()) {
                    showError("Type is required.");
                    return;
                }

                for (Room room : rooms) {
                    if (room.getRoomNumber() == roomNumber) {
                        showError("Room number already exists.");
                        return;
                    }
                }

                rooms.add(new Room(roomNumber, type, beds, price, "Available"));
                saveRooms();
                roomTable.refresh();
                popup.close();
            } catch (NumberFormatException ex) {
                showError("Room number, beds, and price must be numbers.");
            }
        });

        cancelButton.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, applyButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, form, buttons);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("popup-panel");

        Scene scene = new Scene(root, 460, 280);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void openEditRoomPopup(Room room, TableView<Room> roomTable) {
        Stage popup = createPopupStage("Edit Room");

        TextField roomNumberField = styledTextField(String.valueOf(room.getRoomNumber()));
        TextField typeField = styledTextField(room.getType());
        TextField bedsField = styledTextField(String.valueOf(room.getBeds()));
        TextField priceField = styledTextField(String.valueOf(room.getPricePerNight()));

        GridPane form = buildResponsiveTwoColumnForm(120);

        int row = 0;
        form.add(normalLabel("Room number:"), 0, row);
        form.add(roomNumberField, 1, row++);

        form.add(normalLabel("Type:"), 0, row);
        form.add(typeField, 1, row++);

        form.add(normalLabel("Beds:"), 0, row);
        form.add(bedsField, 1, row++);

        form.add(normalLabel("Price:"), 0, row);
        form.add(priceField, 1, row++);

        Button applyButton = standardButton("Apply Changes");
        Button cancelButton = standardButton("Cancel");

        applyButton.setOnAction(e -> {
            try {
                int newRoomNumber = Integer.parseInt(roomNumberField.getText().trim());
                int beds = Integer.parseInt(bedsField.getText().trim());
                int price = Integer.parseInt(priceField.getText().trim());

                for (Room otherRoom : rooms) {
                    if (otherRoom != room && otherRoom.getRoomNumber() == newRoomNumber) {
                        showError("Another room already uses this number.");
                        return;
                    }
                }

                int oldRoomNumber = room.getRoomNumber();

                room.setRoomNumber(newRoomNumber);
                room.setType(typeField.getText().trim());
                room.setBeds(beds);
                room.setPricePerNight(price);

                for (Reservation reservation : reservations) {
                    if (reservation.getRoomNumber() == oldRoomNumber) {
                        reservation.setRoomNumber(newRoomNumber);
                        reservation.setRoomType(room.getType());
                        reservation.setTotalPrice(reservation.getNights() * room.getPricePerNight());
                    }
                }

                saveRooms();
                saveReservations();
                roomTable.refresh();
                popup.close();
            } catch (NumberFormatException ex) {
                showError("Room number, beds, and price must be numbers.");
            }
        });

        cancelButton.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, applyButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, form, buttons);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("popup-panel");

        Scene scene = new Scene(root, 460, 280);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void openEditReservationPopup(Reservation reservation, TableView<Reservation> reservationTable) {
        Stage popup = createPopupStage("Edit Reservation");

        Room room = findRoomByNumber(reservation.getRoomNumber());
        if (room == null) {
            showError("Linked room not found.");
            return;
        }

        Label roomInfoLabel = new Label(
                "Room: " + room.getRoomNumber() + " | " + room.getType() + " | Max guests: " + room.getBeds()
        );
        roomInfoLabel.setStyle(NORMAL_TEXT_STYLE);

        TextField firstNameField = styledTextField(reservation.getFirstName());
        TextField lastNameField = styledTextField(reservation.getLastName());
        TextField passportField = styledTextField(reservation.getPassportId());
        TextField phoneField = styledTextField(reservation.getPhone());
        TextField emailField = styledTextField(reservation.getEmail());

        DatePicker checkInPicker = new DatePicker(reservation.getCheckInDate());
        DatePicker checkOutPicker = new DatePicker(reservation.getCheckOutDate());
        checkInPicker.setStyle(NORMAL_TEXT_STYLE);
        checkOutPicker.setStyle(NORMAL_TEXT_STYLE);

        Label nightsLabel = new Label(String.valueOf(reservation.getNights()));
        nightsLabel.setStyle(NORMAL_TEXT_STYLE);

        Label guestsValueLabel = new Label(String.valueOf(reservation.getGuestsCount()));
        guestsValueLabel.setStyle(NORMAL_TEXT_STYLE);

        Button minusBtn = standardButton("-");
        Button plusBtn = standardButton("+");
        minusBtn.setPrefWidth(45);
        plusBtn.setPrefWidth(45);

        minusBtn.setOnAction(e -> {
            int current = Integer.parseInt(guestsValueLabel.getText());
            if (current > 1) {
                guestsValueLabel.setText(String.valueOf(current - 1));
            }
        });

        plusBtn.setOnAction(e -> {
            int current = Integer.parseInt(guestsValueLabel.getText());
            if (current < room.getBeds()) {
                guestsValueLabel.setText(String.valueOf(current + 1));
            }
        });

        HBox guestsBox = new HBox(12, minusBtn, guestsValueLabel, plusBtn);
        guestsBox.setAlignment(Pos.CENTER_LEFT);

        TextField paymentField = styledTextField(reservation.getPaymentMethod());

        TextArea requestsArea = new TextArea(reservation.getSpecialRequests());
        requestsArea.setPrefRowCount(4);
        requestsArea.setStyle(NORMAL_TEXT_STYLE);
        requestsArea.setMaxWidth(Double.MAX_VALUE);

        Runnable updateNights = () -> {
            LocalDate in = checkInPicker.getValue();
            LocalDate out = checkOutPicker.getValue();
            if (in != null && out != null && out.isAfter(in)) {
                nightsLabel.setText(String.valueOf(ChronoUnit.DAYS.between(in, out)));
            } else {
                nightsLabel.setText("-");
            }
        };

        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNights.run());
        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNights.run());

        GridPane form = buildResponsiveTwoColumnForm(120);

        int row = 0;
        form.add(normalLabel("First name:"), 0, row);
        form.add(firstNameField, 1, row++);

        form.add(normalLabel("Last name:"), 0, row);
        form.add(lastNameField, 1, row++);

        form.add(normalLabel("Passport / ID:"), 0, row);
        form.add(passportField, 1, row++);

        form.add(normalLabel("Phone:"), 0, row);
        form.add(phoneField, 1, row++);

        form.add(normalLabel("Email:"), 0, row);
        form.add(emailField, 1, row++);

        form.add(normalLabel("Check-in:"), 0, row);
        form.add(checkInPicker, 1, row++);

        form.add(normalLabel("Check-out:"), 0, row);
        form.add(checkOutPicker, 1, row++);

        form.add(normalLabel("Nights:"), 0, row);
        form.add(nightsLabel, 1, row++);

        form.add(normalLabel("Guests:"), 0, row);
        form.add(guestsBox, 1, row++);

        form.add(normalLabel("Payment:"), 0, row);
        form.add(paymentField, 1, row++);

        form.add(normalLabel("Special requests:"), 0, row);
        form.add(requestsArea, 1, row++);

        Button applyButton = standardButton("Apply Changes");
        Button cancelButton = standardButton("Cancel");

        applyButton.setOnAction(e -> {
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if (firstNameField.getText().trim().isEmpty()) {
                showError("First name cannot be empty.");
                return;
            }

            if (lastNameField.getText().trim().isEmpty()) {
                showError("Last name cannot be empty.");
                return;
            }

            if (passportField.getText().trim().isEmpty()) {
                showError("Passport / ID is required.");
                return;
            }

            if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                showError("Invalid dates.");
                return;
            }

            int guests = Integer.parseInt(guestsValueLabel.getText());
            if (guests > room.getBeds()) {
                showError("This room allows maximum " + room.getBeds() + " guest(s).");
                return;
            }

            if (hasReservationIntersection(reservation.getRoomNumber(), checkIn, checkOut, reservation)) {
                showError("This room is already reserved for the selected dates.");
                return;
            }

            int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

            reservation.setFirstName(firstNameField.getText().trim());
            reservation.setLastName(lastNameField.getText().trim());
            reservation.setPassportId(passportField.getText().trim());
            reservation.setPhone(phoneField.getText().trim());
            reservation.setEmail(emailField.getText().trim());
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setGuestsCount(guests);
            reservation.setNights(nights);
            reservation.setPaymentMethod(paymentField.getText().trim());
            reservation.setSpecialRequests(requestsArea.getText().trim());
            reservation.setTotalPrice(nights * room.getPricePerNight());

            saveReservations();
            reservationTable.refresh();
            popup.close();
        });

        cancelButton.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, applyButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, roomInfoLabel, form, buttons);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("popup-panel");

        Scene scene = new Scene(root, 620, 680);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void openReservationInfoPopup(Reservation reservation) {
        Stage popup = createPopupStage("Reservation Info");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(320);
        infoArea.setStyle(NORMAL_TEXT_STYLE);

        infoArea.setText(
                "Reservation ID: " + reservation.getReservationId() + "\n" +
                "Guest name: " + reservation.getGuestFullName() + "\n" +
                "Room number: " + reservation.getRoomNumber() + "\n" +
                "Room type: " + reservation.getRoomType() + "\n" +
                "Passport / ID: " + reservation.getPassportId() + "\n" +
                "Phone: " + reservation.getPhone() + "\n" +
                "Email: " + reservation.getEmail() + "\n" +
                "Check-in date: " + reservation.getCheckInDate() + "\n" +
                "Check-out date: " + reservation.getCheckOutDate() + "\n" +
                "Number of nights: " + reservation.getNights() + "\n" +
                "Guests count: " + reservation.getGuestsCount() + "\n" +
                "Payment method: " + reservation.getPaymentMethod() + "\n" +
                "Total price: $" + reservation.getTotalPrice() + "\n" +
                "Special requests: " + reservation.getSpecialRequests()
        );

        Button closeButton = standardButton("Close");
        closeButton.setOnAction(e -> popup.close());

        HBox buttons = new HBox(closeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, infoArea, buttons);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("popup-panel");

        Scene scene = new Scene(root, 500, 440);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void openRoomReservationsPopup(Room room, LocalDate filterCheckIn, LocalDate filterCheckOut) {
        Stage popup = createPopupStage("Room Reservations");

        List<Reservation> roomReservations;

        if (hasValidDateRange(filterCheckIn, filterCheckOut)) {
            roomReservations = getReservationsForRoomInRange(room.getRoomNumber(), filterCheckIn, filterCheckOut);
        } else {
            roomReservations = getReservationsForRoom(room.getRoomNumber());
        }

        ObservableList<Reservation> popupReservations = FXCollections.observableArrayList(roomReservations);

        Label infoLabel;
        if (hasValidDateRange(filterCheckIn, filterCheckOut)) {
            infoLabel = new Label(
                    "Reservations for room " + room.getRoomNumber() +
                    " from " + filterCheckIn + " to " + filterCheckOut
            );
        } else {
            infoLabel = new Label("All reservations for room " + room.getRoomNumber());
        }
        infoLabel.setStyle(NORMAL_TEXT_STYLE);

        TableView<Reservation> table = new TableView<>();
        table.setItems(popupReservations);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Reservation, String> idCol = new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));

        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGuestFullName()));

        TableColumn<Reservation, String> datesCol = new TableColumn<>("Dates");
        datesCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getCheckInDate() + " - " + cellData.getValue().getCheckOutDate()
                ));

        TableColumn<Reservation, String> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getGuestsCount())));

        TableColumn<Reservation, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        table.getColumns().addAll(idCol, guestCol, datesCol, guestsCol, paymentCol);

        Button openInfoButton = standardButton("Open Selected Info");
        Button closeButton = standardButton("Close");

        openInfoButton.setOnAction(e -> {
            Reservation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a reservation first.");
                return;
            }
            openReservationInfoPopup(selected);
        });

        closeButton.setOnAction(e -> popup.close());

        HBox buttons = new HBox(10, openInfoButton, closeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, infoLabel, table, buttons);
        root.setPadding(new Insets(15));
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getStyleClass().add("popup-panel");

        Scene scene = new Scene(root, 760, 420);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        popup.setScene(scene);
        popup.showAndWait();
    }

    private Stage createPopupStage(String title) {
        Stage popup = new Stage();
        popup.initOwner(primaryStage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(title);
        return popup;
    }

    private GridPane buildResponsiveTwoColumnForm(double firstColumnWidth) {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(firstColumnWidth);
        col1.setPrefWidth(firstColumnWidth);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        form.getColumnConstraints().addAll(col1, col2);
        return form;
    }

    private TextField styledTextField(String value) {
        TextField field = new TextField();
        field.setStyle(NORMAL_TEXT_STYLE);
        field.setMaxWidth(Double.MAX_VALUE);
        if (value != null && !value.isBlank()) {
            field.setText(value);
        }
        return field;
    }

    private TextField styledTextFieldWithPrompt(String prompt) {
        TextField field = new TextField();
        field.setStyle(NORMAL_TEXT_STYLE);
        field.setMaxWidth(Double.MAX_VALUE);
        if (prompt != null && !prompt.isBlank()) {
            field.setPromptText(prompt);
        }
        return field;
    }

    private Label normalLabel(String text) {
        Label label = new Label(text);
        label.setStyle(NORMAL_TEXT_STYLE);
        return label;
    }

    private Button standardButton(String text) {
        Button button = new Button(text);
        button.setStyle(BUTTON_STYLE);
        return button;
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle(NORMAL_TEXT_STYLE);
        comboBox.setPrefWidth(120);
    }

    private void styleRadio(RadioButton radioButton) {
        radioButton.setStyle(NORMAL_TEXT_STYLE);
    }

    private boolean hasValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        return checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean hasReservationIntersection(int roomNumber, LocalDate checkIn, LocalDate checkOut, Reservation excludeReservation) {
        for (Reservation reservation : reservations) {
            if (reservation.getRoomNumber() != roomNumber) {
                continue;
            }

            if (excludeReservation != null &&
                    reservation.getReservationId().equals(excludeReservation.getReservationId())) {
                continue;
            }

            if (datesOverlap(
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    checkIn,
                    checkOut
            )) {
                return true;
            }
        }

        return false;
    }

    private List<Reservation> getReservationsForRoom(int roomNumber) {
        List<Reservation> result = new ArrayList<>();

        for (Reservation reservation : reservations) {
            if (reservation.getRoomNumber() == roomNumber) {
                result.add(reservation);
            }
        }

        result.sort((a, b) -> a.getCheckInDate().compareTo(b.getCheckInDate()));
        return result;
    }

    private List<Reservation> getReservationsForRoomInRange(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        List<Reservation> result = new ArrayList<>();

        for (Reservation reservation : reservations) {
            if (reservation.getRoomNumber() != roomNumber) {
                continue;
            }

            if (datesOverlap(
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    checkIn,
                    checkOut
            )) {
                result.add(reservation);
            }
        }

        result.sort((a, b) -> a.getCheckInDate().compareTo(b.getCheckInDate()));
        return result;
    }

    private String getBookingStatusForRange(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (hasValidDateRange(checkIn, checkOut)) {
            return hasReservationIntersection(room.getRoomNumber(), checkIn, checkOut, null)
                    ? "Reserved"
                    : "Available";
        }

        return getReservationsForRoom(room.getRoomNumber()).isEmpty()
                ? "Available"
                : "Reserved";
    }

    private Reservation findLatestReservationByRoomNumber(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        List<Reservation> roomReservations;

        if (hasValidDateRange(checkIn, checkOut)) {
            roomReservations = getReservationsForRoomInRange(roomNumber, checkIn, checkOut);
        } else {
            roomReservations = getReservationsForRoom(roomNumber);
        }

        Reservation latest = null;

        for (Reservation reservation : roomReservations) {
            if (latest == null || reservation.getCheckInDate().isAfter(latest.getCheckInDate())) {
                latest = reservation;
            }
        }

        return latest;
    }

    private Set<Integer> getUniqueBeds() {
        Set<Integer> beds = new java.util.TreeSet<>();
        for (Room room : rooms) {
            beds.add(room.getBeds());
        }
        return beds;
    }

    private Set<String> getOrderedRoomTypes() {
        List<String> preferredOrder = List.of(
                "Single",
                "Double",
                "Deluxe",
                "Family",
                "Suite"
        );

        Set<String> existingTypes = new LinkedHashSet<>();
        for (Room room : rooms) {
            existingTypes.add(room.getType());
        }

        Set<String> orderedTypes = new LinkedHashSet<>();

        for (String type : preferredOrder) {
            for (String existing : existingTypes) {
                if (existing.equalsIgnoreCase(type)) {
                    orderedTypes.add(existing);
                }
            }
        }

        for (String existing : existingTypes) {
            boolean alreadyAdded = false;
            for (String ordered : orderedTypes) {
                if (ordered.equalsIgnoreCase(existing)) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                orderedTypes.add(existing);
            }
        }

        return orderedTypes;
    }

    private Room findRoomByNumber(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}