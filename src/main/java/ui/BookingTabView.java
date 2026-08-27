package ui;

import auth.SessionContext;
import domain.Booking;
import domain.Instrument;
import enums.InstrumentType;
import exceptions.*;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.BookingManager;
import services.CheckoutManager;
import services.InstrumentManager;
import services.UserManager;
import validators.TimeValidator;


import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.Objects;


public class BookingTabView extends VBox {
    private final BookingManager bookingMgr;
    private final InstrumentManager instMgr;
    private final CheckoutManager checkoutMgr;
    private final UserManager userManager;

    private final TilePane toolGrid = new TilePane();
    private Booking selectedBooking;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public BookingTabView(BookingManager bookingMgr, CheckoutManager checkoutMgr,
                          InstrumentManager instMgr, Runnable onSave, Runnable onRefresh, UserManager u) {
        this.bookingMgr = bookingMgr;
        this.instMgr = instMgr;
        this.checkoutMgr = checkoutMgr;
        this.userManager = u;

        buildLayout(onRefresh);
        refresh();
    }

    private void buildLayout(Runnable onRefresh) {
        setPadding(new Insets(18));
        setSpacing(14);
        setStyle("-fx-background-color: #0f172a;"); // Темный фон вокруг ящика

        Label title = new Label("🧰 Ящик бронирований");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc; -fx-padding: 0 0 10 0;");

        // 📦 КОНТЕЙНЕР "ЯЩИК"
        StackPane drawer = new StackPane();
        drawer.setPrefHeight(500);
        drawer.setPadding(new Insets(30));

        // ✅ ЗАГРУЗКА ФОНА ЧЕРЕЗ ПОТОК БАЙТОВ (ОБХОД БАГА С КИРИЛЛИЦЕЙ И ПРОБЕЛАМИ)
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/toolbox/drawer_bg.jpg");
            if (is == null) {
                throw new Exception("Файл не найден в ресурсах!");
            }

            Image bgImg = new Image(is);
            if (bgImg.isError()) {
                throw new Exception("Файл поврежден или это не настоящий PNG (возможно WebP).");
            }

            BackgroundImage bImg = new BackgroundImage(
                    bgImg,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true) // Растягиваем на весь ящик (cover)
            );
            drawer.setBackground(new Background(bImg));

            // Задаем ТОЛЬКО рамку и тень в CSS, не трогая фон
            drawer.setStyle(
                    "-fx-border-color: #475569; " +
                            "-fx-border-width: 4px; " +
                            "-fx-border-radius: 16px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
            );
            System.out.println("✅ Фон ящика успешно загружен!");

        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки фона: " + e.getMessage());
            // Резервный синий фон, если что-то пошло не так
            drawer.setStyle("-fx-background-color: #334155; -fx-border-color: #475569; -fx-border-width: 4px; -fx-border-radius: 16px;");
        }

        // Сетка инструментов
        toolGrid.setPrefColumns(4);
        toolGrid.setHgap(20);
        toolGrid.setVgap(20);
        toolGrid.setAlignment(Pos.CENTER);
        toolGrid.setStyle("-fx-background-color: transparent;");

        drawer.getChildren().add(toolGrid);

        HBox btnBar = createButtonBar(onRefresh);
        getChildren().addAll(title, drawer, btnBar);
    }

    /** Создаёт карточку-инструмент на основе его реального типа */
    private StackPane createTool(Booking b) {
        boolean isOwn = SessionContext.isAuthenticated() && b.getOwnerUserId() == SessionContext.getCurrentUserId();

        // 🆕 НОВАЯ ЛОГИКА: Определяем индекс картинки (1-10) на основе типа прибора
        int toolIndex = 1; // Значение по умолчанию, если что-то пойдет не так
        try {
            // Через менеджер приборов находим сам прибор по его ID
            for (Instrument i: instMgr.getInstruments()){
                if (i.getId() == b.getInstrumentId()){
                    toolIndex = getToolImageIndex(i.getType());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Не удалось определить тип прибора для брони #" + b.getId() + ". Ошибка: " + e.getMessage());
        }

        ImageView toolView;
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/toolbox/tool_" + toolIndex + ".png")));
            toolView = new ImageView(img);
        } catch (Exception e) {
            // Если картинки нет, ставим прозрачную заглушку
            toolView = new ImageView();
            toolView.setImage(new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));
            System.err.println("⚠️ Картинка инструмента tool_" + toolIndex + ".png не найдена");
        }

        // Увеличенный размер предметов (как вы просили ранее)
        toolView.setFitWidth(130);
        toolView.setFitHeight(130);
        toolView.setPreserveRatio(true);
        toolView.setSmooth(true);

        // ✅ Контейнер инструмента
        StackPane toolCard = new StackPane(toolView);
        toolCard.setPrefSize(170, 170);
        toolCard.setAlignment(Pos.CENTER);
        toolCard.setCursor(javafx.scene.Cursor.HAND);
        toolCard.setStyle("-fx-background-color: transparent;");

        // 🎨 Эффекты для "Своих" и "Чужих"
        ColorAdjust dimEffect = new ColorAdjust();
        dimEffect.setBrightness(-0.4);
        dimEffect.setSaturation(-0.6);

        DropShadow glow = new DropShadow(12, 0, 0, Color.web("#4ade80", 0.9));
        DropShadow normalShadow = new DropShadow(8, 0, 0, Color.color(0, 0, 0, 0.5));

        if (isOwn) {
            toolView.setEffect(null);
            toolCard.setEffect(glow);
        } else {
            toolView.setEffect(dimEffect);
            toolCard.setEffect(normalShadow);
        }

        // 🎬 Анимация при наведении
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), toolCard);
        scale.setToX(1.15);
        scale.setToY(1.15);

        toolCard.setOnMouseEntered(e -> {
            scale.play();
            if (isOwn) toolCard.setEffect(new DropShadow(15, 0, 0, Color.web("#4ade80", 1.0)));
        });

        toolCard.setOnMouseExited(e -> {
            scale.stop();
            toolCard.setScaleX(1.0);
            toolCard.setScaleY(1.0);
            if (isOwn) toolCard.setEffect(glow);
            else toolCard.setEffect(normalShadow);
        });

        // 🖱 Клик
        toolCard.setOnMouseClicked(e -> {
            selectedBooking = b;
            showDetails(b);
        });

        return toolCard;
    }

    public void refresh() {
        selectedBooking = null;
        toolGrid.getChildren().clear();
        for (Booking b : bookingMgr.getBooks()) {
            toolGrid.getChildren().add(createTool(b));
        }
    }

    private HBox createButtonBar(Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Обновить ящик");
        Button createBtn = new Button("➕ Положить бронь");
        Button cancelBtn = new Button("❌ Забрать бронь");
        Button rescheduleBtn = new Button("📅 Переложить");
        Button availBtn = new Button("✅ Проверить");

        refreshBtn.setOnAction(e -> { onRefresh.run(); refresh(); });
        createBtn.setOnAction(e -> handleCreate());
        cancelBtn.setOnAction(e -> handleCancel());
        rescheduleBtn.setOnAction(e -> handleReschedule());
        availBtn.setOnAction(e -> handleAvailability());

        // Металлический стиль кнопок
        String btnStyle = "-fx-background-color: linear-gradient(to bottom, #475569, #1e293b); " +
                "-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-border-color: #64748b; -fx-border-width: 1px;";

        for (Button b : new Button[]{refreshBtn, createBtn, cancelBtn, rescheduleBtn, availBtn}) {
            b.setStyle(btnStyle);
        }

        return new HBox(15, refreshBtn, createBtn, cancelBtn, rescheduleBtn, availBtn);
    }


    private void handleCreate() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Новая бронь");
        dlg.setHeaderText("Формат: instrumentId, start (YYYY-MM-DD HH:MM), end (YYYY-MM-DD HH:MM)");
        dlg.setContentText("Данные:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                parsers.GuiInputParser.BookingCreateParams params = parsers.GuiInputParser.parseBookingCreate(input);
                bookingMgr.createBooking(params.instrumentId(), params.startAt(), params.endAt());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь успешно добавлена!");
            } catch (exceptions.NotAvailableException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "В это время инструмент занят");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
            }
        });
    }

    private void handleCancel() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите инструмент кликом");
            return;
        }
        try {
            bookingMgr.cancelBooking(selectedBooking.getId());
            refresh();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь отменена.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    private void handleReschedule() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите инструмент кликом");
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Перенос");
        dlg.setHeaderText("Новое время: start (YYYY-MM-DD HH:MM), end (YYYY-MM-DD HH:MM)");
        dlg.setContentText("Интервал:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                parsers.GuiInputParser.BookingRescheduleParams params = parsers.GuiInputParser.parseBookingReschedule(input);
                bookingMgr.rescheduleBooking(selectedBooking.getId(), params.startAt(), params.endAt());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь перенесена.");
            } catch (exceptions.NotAvailableException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Нельзя перенести: в это время инструмент занят");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
            }
        });
    }

    private void handleAvailability() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Доступность");
        dlg.setHeaderText("Формат: TYPE, start (YYYY-MM-DD HH:MM), end (YYYY-MM-DD HH:MM)");
        dlg.setContentText("Данные:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                parsers.GuiInputParser.AvailabilityParams params = parsers.GuiInputParser.parseAvailability(input);
                var available = instMgr.instAvailable(checkoutMgr, bookingMgr, params.type(), params.startAt(), params.endAt());
                if (available.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Доступные приборы", "Нет доступных приборов типа " + params.type() + " на указанное время.");
                } else {
                    StringBuilder av = new StringBuilder();
                    for (Instrument i : available) av.append(i.getId()).append(" ");
                    showAlert(Alert.AlertType.INFORMATION, "Доступные приборы", "IDs: " + av.toString().trim());
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    private void showDetails(Booking b) {
        String info = String.format(
                "📅 Бронирование #%d\n" +
                        "🔬 Прибор ID: %d\n" +
                        "👤 Владелец: %s\n" +
                        "📆 Начало: %s\n" +
                        "🏁 Конец: %s\n" +
                        "📊 Статус: %s",
                b.getId(), b.getInstrumentId(), userManager.getLoginById(b.getOwnerUserId()),
                b.getStartAt() != null ? fmt.format(b.getStartAt()) : "-",
                b.getEndAt() != null ? fmt.format(b.getEndAt()) : "-",
                b.getStatus() != null ? b.getStatus().name() : "-"
        );
        showAlert(Alert.AlertType.INFORMATION, "🔍 Информация об инструменте", info);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    /** Вспомогательный метод: сопоставляет тип прибора с номером картинки (1-10) */
    private int getToolImageIndex(InstrumentType type) {
        switch (type) {
            case BEAKER:    return 1;  // tool_1.png
            case MICROSCOPE:  return 2;  // tool_2.png
            case SCALE:    return 3;  // tool_3.png
            case PETRI_DISH:  return 4;  // tool_4.png
            case SPIRIT_LAMP:     return 5;  // tool_5.png
            case GLOVES:return 6;  // tool_6.png
            case TONG:       return 7;  // tool_7.png
            case FUNNEL:     return 8;  // tool_8.png
            case THERMOMETER:   return 9;  // tool_9.png
            case PIPETTE:      return 10; // tool_10.png
            default:            return 1;
        }
    }
}