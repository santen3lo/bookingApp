package ui.dialogs;

import domain.Booking;
import domain.Checkout;
import domain.Instrument;
import enums.InstrumentType;
import enums.ReturnCondition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import parsers.GuiInputParser;

import java.time.*;
import java.util.List;
import java.util.Optional;

public final class FormDialogs {

    private FormDialogs() {}

    /** Диалог создания нового бронирования с выбором прибора, календарем и селектором времени */
    public static Optional<GuiInputParser.BookingCreateParams> showBookingCreateDialog(List<Instrument> instruments) {
        if (instruments == null || instruments.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Нет доступных приборов", "Список приборов пуст. Сначала добавьте приборы в систему.");
            return Optional.empty();
        }

        Dialog<GuiInputParser.BookingCreateParams> dialog = new Dialog<>();
        dialog.setTitle("Новое бронирование");
        dialog.setHeaderText("Выберите прибор и интервал времени");

        ButtonType submitButtonType = new ButtonType("Забронировать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = createGrid();

        // 1. Выбор прибора
        ComboBox<Instrument> instCombo = new ComboBox<>();
        instCombo.getItems().addAll(instruments);
        instCombo.setConverter(createInstrumentConverter());
        instCombo.getSelectionModel().selectFirst();
        instCombo.setMaxWidth(Double.MAX_VALUE);

        // 2. Начало
        DatePicker startDate = new DatePicker(LocalDate.now());
        Spinner<Integer> startHour = new Spinner<>(0, 23, Math.min(23, LocalTime.now().getHour() + 1));
        Spinner<Integer> startMin = new Spinner<>(0, 59, 0, 5);
        startHour.setPrefWidth(70);
        startMin.setPrefWidth(70);
        HBox startTimeBox = new HBox(8, startHour, new Label(":"), startMin);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);

        // 3. Конец
        DatePicker endDate = new DatePicker(LocalDate.now());
        Spinner<Integer> endHour = new Spinner<>(0, 23, Math.min(23, startHour.getValue() + 1));
        Spinner<Integer> endMin = new Spinner<>(0, 59, 0, 5);
        endHour.setPrefWidth(70);
        endMin.setPrefWidth(70);
        HBox endTimeBox = new HBox(8, endHour, new Label(":"), endMin);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Прибор:"), 0, 0);
        grid.add(instCombo, 1, 0, 2, 1);
        grid.add(new Label("Дата начала:"), 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(startTimeBox, 2, 1);
        grid.add(new Label("Дата окончания:"), 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(endTimeBox, 2, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Instrument selected = instCombo.getValue();
                if (selected == null) return null;

                Instant start = makeInstant(startDate.getValue(), startHour.getValue(), startMin.getValue());
                Instant end = makeInstant(endDate.getValue(), endHour.getValue(), endMin.getValue());
                return new GuiInputParser.BookingCreateParams(selected.getId(), start, end);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /** Диалог переноса бронирования с предзаполненными текущими датами */
    public static Optional<GuiInputParser.BookingRescheduleParams> showBookingRescheduleDialog(Booking booking, Instrument instrument) {
        Dialog<GuiInputParser.BookingRescheduleParams> dialog = new Dialog<>();
        dialog.setTitle("Перенос бронирования #" + booking.getId());
        String instInfo = instrument != null ? InstrumentTypeLocalization.formatInstrument(instrument) : ("ID #" + booking.getInstrumentId());
        dialog.setHeaderText("Прибор: " + instInfo + "\nУкажите новое время бронирования:");

        ButtonType submitButtonType = new ButtonType("Перенести", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = createGrid();

        LocalDateTime currStart = LocalDateTime.ofInstant(booking.getStartAt(), ZoneId.systemDefault());
        LocalDateTime currEnd = LocalDateTime.ofInstant(booking.getEndAt(), ZoneId.systemDefault());

        DatePicker startDate = new DatePicker(currStart.toLocalDate());
        Spinner<Integer> startHour = new Spinner<>(0, 23, currStart.getHour());
        Spinner<Integer> startMin = new Spinner<>(0, 59, currStart.getMinute(), 5);
        startHour.setPrefWidth(70);
        startMin.setPrefWidth(70);
        HBox startTimeBox = new HBox(8, startHour, new Label(":"), startMin);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker endDate = new DatePicker(currEnd.toLocalDate());
        Spinner<Integer> endHour = new Spinner<>(0, 23, currEnd.getHour());
        Spinner<Integer> endMin = new Spinner<>(0, 59, currEnd.getMinute(), 5);
        endHour.setPrefWidth(70);
        endMin.setPrefWidth(70);
        HBox endTimeBox = new HBox(8, endHour, new Label(":"), endMin);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Новая дата начала:"), 0, 0);
        grid.add(startDate, 1, 0);
        grid.add(startTimeBox, 2, 0);
        grid.add(new Label("Новая дата конца:"), 0, 1);
        grid.add(endDate, 1, 1);
        grid.add(endTimeBox, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Instant start = makeInstant(startDate.getValue(), startHour.getValue(), startMin.getValue());
                Instant end = makeInstant(endDate.getValue(), endHour.getValue(), endMin.getValue());
                return new GuiInputParser.BookingRescheduleParams(start, end);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /** Диалог оформления выдачи инструмента */
    public static Optional<GuiInputParser.CheckoutTakeParams> showCheckoutTakeDialog(List<Instrument> instruments) {
        if (instruments == null || instruments.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Нет приборов", "В лаборатории нет зарегистрированных приборов.");
            return Optional.empty();
        }

        Dialog<GuiInputParser.CheckoutTakeParams> dialog = new Dialog<>();
        dialog.setTitle("Оформление выдачи");
        dialog.setHeaderText("Выберите прибор для выдачи и укажите комментарий");

        ButtonType submitButtonType = new ButtonType("Выдать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = createGrid();

        ComboBox<Instrument> instCombo = new ComboBox<>();
        instCombo.getItems().addAll(instruments);
        instCombo.setConverter(createInstrumentConverter());
        instCombo.getSelectionModel().selectFirst();
        instCombo.setMaxWidth(Double.MAX_VALUE);

        TextField commentField = new TextField();
        commentField.setPromptText("Например: Для проведения титрования");

        grid.add(new Label("Прибор:"), 0, 0);
        grid.add(instCombo, 1, 0);
        grid.add(new Label("Комментарий:"), 0, 1);
        grid.add(commentField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Instrument selected = instCombo.getValue();
                if (selected == null) return null;
                return new GuiInputParser.CheckoutTakeParams(selected.getId(), commentField.getText().trim());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /** Диалог возврата прибора с радиокнопками состояния */
    public static Optional<ReturnCondition> showCheckoutReturnDialog(Checkout checkout, String instrumentName) {
        Dialog<ReturnCondition> dialog = new Dialog<>();
        dialog.setTitle("Возврат оборудования");
        dialog.setHeaderText("Возврат выдачи #" + checkout.getId() + "\nПрибор: " + instrumentName);

        ButtonType submitButtonType = new ButtonType("Принять возврат", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox box = new VBox(12);
        box.setPadding(new Insets(10));

        Label label = new Label("Выберите состояние оборудования при возврате:");
        label.setStyle("-fx-font-weight: bold;");

        ToggleGroup group = new ToggleGroup();
        RadioButton okBtn = new RadioButton("🟢 В полном порядке (OK)");
        okBtn.setToggleGroup(group);
        okBtn.setSelected(true);
        okBtn.setUserData(ReturnCondition.OK);

        RadioButton damagedBtn = new RadioButton("🔴 Повреждён или загрязнён (DAMAGED)");
        damagedBtn.setToggleGroup(group);
        damagedBtn.setUserData(ReturnCondition.DAMAGED);

        box.getChildren().addAll(label, okBtn, damagedBtn);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return (ReturnCondition) group.getSelectedToggle().getUserData();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /** Диалог добавления нового прибора с выбором типа из списка на русском */
    public static Optional<InstrumentType> showAddInstrumentDialog() {
        Dialog<InstrumentType> dialog = new Dialog<>();
        dialog.setTitle("Добавление оборудования");
        dialog.setHeaderText("Выберите тип химического прибора");

        ButtonType submitButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = createGrid();

        ComboBox<InstrumentType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(InstrumentType.values());
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(InstrumentType type) {
                return type != null ? (InstrumentTypeLocalization.getRussianName(type) + " (" + type.name() + ")") : "";
            }
            @Override
            public InstrumentType fromString(String string) { return null; }
        });
        typeCombo.getSelectionModel().selectFirst();
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Тип инструмента:"), 0, 0);
        grid.add(typeCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return typeCombo.getValue();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /** Диалог проверки доступности прибора */
    public static Optional<GuiInputParser.AvailabilityParams> showAvailabilityDialog() {
        Dialog<GuiInputParser.AvailabilityParams> dialog = new Dialog<>();
        dialog.setTitle("Проверка доступности");
        dialog.setHeaderText("Укажите тип прибора и проверяемый временной интервал");

        ButtonType submitButtonType = new ButtonType("Проверить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = createGrid();

        ComboBox<InstrumentType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(InstrumentType.values());
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(InstrumentType type) {
                return type != null ? (InstrumentTypeLocalization.getRussianName(type) + " (" + type.name() + ")") : "";
            }
            @Override
            public InstrumentType fromString(String string) { return null; }
        });
        typeCombo.getSelectionModel().selectFirst();

        DatePicker startDate = new DatePicker(LocalDate.now());
        Spinner<Integer> startHour = new Spinner<>(0, 23, Math.min(23, LocalTime.now().getHour() + 1));
        Spinner<Integer> startMin = new Spinner<>(0, 59, 0, 5);
        startHour.setPrefWidth(70);
        startMin.setPrefWidth(70);
        HBox startTimeBox = new HBox(8, startHour, new Label(":"), startMin);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker endDate = new DatePicker(LocalDate.now());
        Spinner<Integer> endHour = new Spinner<>(0, 23, Math.min(23, startHour.getValue() + 2));
        Spinner<Integer> endMin = new Spinner<>(0, 59, 0, 5);
        endHour.setPrefWidth(70);
        endMin.setPrefWidth(70);
        HBox endTimeBox = new HBox(8, endHour, new Label(":"), endMin);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Тип прибора:"), 0, 0);
        grid.add(typeCombo, 1, 0, 2, 1);
        grid.add(new Label("Дата начала:"), 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(startTimeBox, 2, 1);
        grid.add(new Label("Дата окончания:"), 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(endTimeBox, 2, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Instant start = makeInstant(startDate.getValue(), startHour.getValue(), startMin.getValue());
                Instant end = makeInstant(endDate.getValue(), endHour.getValue(), endMin.getValue());
                return new GuiInputParser.AvailabilityParams(typeCombo.getValue(), start, end);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        return grid;
    }

    private static StringConverter<Instrument> createInstrumentConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Instrument i) {
                return InstrumentTypeLocalization.formatInstrument(i);
            }
            @Override
            public Instrument fromString(String string) { return null; }
        };
    }

    private static Instant makeInstant(LocalDate date, int hour, int minute) {
        if (date == null) date = LocalDate.now();
        LocalDateTime ldt = LocalDateTime.of(date, LocalTime.of(hour, minute));
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
