package ui;

import auth.SessionContext;
import domain.Booking;
import domain.Instrument;
import enums.BookingStatus;
import enums.InstrumentType;
import exceptions.NotAvailableException;
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
import ui.dialogs.FormDialogs;
import ui.dialogs.InstrumentTypeLocalization;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BookingTabView extends VBox {
    private final BookingManager bookingMgr;
    private final InstrumentManager instMgr;
    private final CheckoutManager checkoutMgr;
    private final UserManager userManager;

    private final TilePane toolGrid = new TilePane();
    private Booking selectedBooking;
    private StackPane selectedToolCard;

    private final VBox inspectorPanel = new VBox(14);
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
        setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("🧰 Ящик бронирований");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // 📦 Контейнер "Ящик"
        StackPane drawer = new StackPane();
        drawer.setPrefHeight(520);
        drawer.setPadding(new Insets(24));

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/toolbox/drawer_bg.jpg");
            if (is != null) {
                Image bgImg = new Image(is);
                if (!bgImg.isError()) {
                    BackgroundImage bImg = new BackgroundImage(
                            bgImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true)
                    );
                    drawer.setBackground(new Background(bImg));
                }
            }
            drawer.setStyle(
                    "-fx-border-color: #475569; -fx-border-width: 4px; -fx-border-radius: 16px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
            );
        } catch (Exception e) {
            drawer.setStyle("-fx-background-color: #334155; -fx-border-color: #475569; -fx-border-width: 4px; -fx-border-radius: 16px;");
        }

        // Сетка инструментов
        toolGrid.setPrefColumns(4);
        toolGrid.setHgap(20);
        toolGrid.setVgap(20);
        toolGrid.setAlignment(Pos.CENTER);
        toolGrid.setStyle("-fx-background-color: transparent;");

        ScrollPane drawerScroll = new ScrollPane(toolGrid);
        drawerScroll.setFitToWidth(true);
        drawerScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        drawer.getChildren().add(drawerScroll);

        // 📋 Боковая панель инспектора деталей
        setupInspectorPanel();

        HBox centerArea = new HBox(16, drawer, inspectorPanel);
        HBox.setHgrow(drawer, Priority.ALWAYS);
        centerArea.setAlignment(Pos.CENTER_LEFT);

        HBox btnBar = createButtonBar(onRefresh);
        getChildren().addAll(title, centerArea, btnBar);
    }

    private void setupInspectorPanel() {
        inspectorPanel.setPrefWidth(300);
        inspectorPanel.setMinWidth(300);
        inspectorPanel.setMaxWidth(320);
        inspectorPanel.setPadding(new Insets(18));
        inspectorPanel.setStyle(
                "-fx-background-color: #1e293b; " +
                        "-fx-border-color: #334155; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 14px; " +
                        "-fx-background-radius: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
        );
        showEmptyInspector();
    }

    private void showEmptyInspector() {
        inspectorPanel.getChildren().clear();
        inspectorPanel.setAlignment(Pos.CENTER);

        Label icon = new Label("🔬");
        icon.setStyle("-fx-font-size: 40px;");

        Label hint = new Label("Выберите инструмент в ящике для просмотра параметров и действий");
        hint.setWrapText(true);
        hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");

        inspectorPanel.getChildren().addAll(icon, hint);
    }

    private void updateInspector(Booking b) {
        inspectorPanel.getChildren().clear();
        inspectorPanel.setAlignment(Pos.TOP_LEFT);

        Instrument instrument = null;
        for (Instrument i : instMgr.getInstruments()) {
            if (i.getId() == b.getInstrumentId()) {
                instrument = i;
                break;
            }
        }

        boolean isOwn = SessionContext.isAuthenticated() && b.getOwnerUserId() == SessionContext.getCurrentUserId();

        // Заголовок
        Label header = new Label("📋 Детали брони #" + b.getId());
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Изображение
        int toolIndex = instrument != null ? getToolImageIndex(instrument.getType()) : 1;
        ImageView iv = new ImageView();
        try {
            iv.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/toolbox/tool_" + toolIndex + ".png"))));
        } catch (Exception ignored) {}
        iv.setFitWidth(80);
        iv.setFitHeight(80);
        iv.setPreserveRatio(true);

        HBox imgBox = new HBox(iv);
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPadding(new Insets(4, 0, 6, 0));

        // Название прибора
        String name = instrument != null ? InstrumentTypeLocalization.getRussianName(instrument.getType()) : "Прибор";
        Label instNameLabel = new Label(name);
        instNameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label instIdLabel = new Label("ID прибора: #" + b.getInstrumentId());
        instIdLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        // Статус
        Label statusChip = new Label(b.getStatus() == BookingStatus.ACTIVE ? "🟢 АКТИВНО" : "🔴 ОТМЕНЕНО");
        statusChip.setStyle(b.getStatus() == BookingStatus.ACTIVE
                ? "-fx-background-color: rgba(34, 197, 94, 0.2); -fx-text-fill: #4ade80; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Время
        Label startLabel = new Label("📅 Начало: " + (b.getStartAt() != null ? fmt.format(b.getStartAt()) : "-"));
        startLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

        Label endLabel = new Label("🏁 Конец:  " + (b.getEndAt() != null ? fmt.format(b.getEndAt()) : "-"));
        endLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

        // Владелец
        Label ownerLabel = new Label("👤 Владелец: " + userManager.getLoginById(b.getOwnerUserId()) + (isOwn ? " (Вы)" : ""));
        ownerLabel.setStyle(isOwn ? "-fx-text-fill: #4ade80; -fx-font-size: 12px; -fx-font-weight: bold;" : "-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        VBox detailsBox = new VBox(6, instNameLabel, instIdLabel, statusChip, startLabel, endLabel, ownerLabel);
        detailsBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; -fx-background-radius: 8;");

        // Кнопки действий
        VBox actionsBox = new VBox(8);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        if (isOwn && b.getStatus() == BookingStatus.ACTIVE) {
            Button rescheduleBtn = new Button("📅 Перенести время");
            rescheduleBtn.setMaxWidth(Double.MAX_VALUE);
            rescheduleBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            rescheduleBtn.setOnAction(e -> handleReschedule());

            Button cancelBtn = new Button("❌ Забрать бронь (отменить)");
            cancelBtn.setMaxWidth(Double.MAX_VALUE);
            cancelBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            cancelBtn.setOnAction(e -> handleCancel());

            actionsBox.getChildren().addAll(rescheduleBtn, cancelBtn);
        } else if (!isOwn) {
            Label notice = new Label("🔒 Чужое бронирование\n(изменение недоступно)");
            notice.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-style: italic;");
            notice.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            actionsBox.getChildren().add(notice);
        }

        inspectorPanel.getChildren().addAll(header, imgBox, detailsBox, actionsBox);
    }

    private StackPane createTool(Booking b) {
        boolean isOwn = SessionContext.isAuthenticated() && b.getOwnerUserId() == SessionContext.getCurrentUserId();

        int toolIndex = 1;
        try {
            for (Instrument i : instMgr.getInstruments()) {
                if (i.getId() == b.getInstrumentId()) {
                    toolIndex = getToolImageIndex(i.getType());
                    break;
                }
            }
        } catch (Exception ignored) {}

        ImageView toolView;
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/toolbox/tool_" + toolIndex + ".png")));
            toolView = new ImageView(img);
        } catch (Exception e) {
            toolView = new ImageView();
            toolView.setImage(new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));
        }

        toolView.setFitWidth(130);
        toolView.setFitHeight(130);
        toolView.setPreserveRatio(true);
        toolView.setSmooth(true);

        StackPane toolCard = new StackPane(toolView);
        toolCard.setPrefSize(170, 170);
        toolCard.setAlignment(Pos.CENTER);
        toolCard.setCursor(javafx.scene.Cursor.HAND);
        toolCard.setStyle("-fx-background-color: transparent;");

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

        ScaleTransition scale = new ScaleTransition(Duration.millis(150), toolCard);
        scale.setToX(1.12);
        scale.setToY(1.12);

        toolCard.setOnMouseEntered(e -> {
            scale.play();
            if (isOwn) toolCard.setEffect(new DropShadow(16, 0, 0, Color.web("#4ade80", 1.0)));
        });

        toolCard.setOnMouseExited(e -> {
            scale.stop();
            toolCard.setScaleX(1.0);
            toolCard.setScaleY(1.0);
            if (isOwn) toolCard.setEffect(glow);
            else toolCard.setEffect(normalShadow);
        });

        toolCard.setOnMouseClicked(e -> {
            if (selectedToolCard != null) {
                selectedToolCard.setStyle("-fx-background-color: transparent;");
            }
            selectedBooking = b;
            selectedToolCard = toolCard;
            toolCard.setStyle("-fx-background-color: rgba(56, 189, 248, 0.15); -fx-border-color: #38bdf8; -fx-border-width: 3px; -fx-border-radius: 12px; -fx-background-radius: 12px;");
            updateInspector(b);
        });

        return toolCard;
    }

    public void refresh() {
        selectedBooking = null;
        selectedToolCard = null;
        toolGrid.getChildren().clear();
        for (Booking b : bookingMgr.getBooks()) {
            toolGrid.getChildren().add(createTool(b));
        }
        showEmptyInspector();
    }

    private HBox createButtonBar(Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Обновить ящик");
        Button createBtn = new Button("➕ Положить бронь");
        Button cancelBtn = new Button("❌ Забрать бронь");
        Button rescheduleBtn = new Button("📅 Переложить");
        Button availBtn = new Button("✅ Проверить доступность");

        refreshBtn.setOnAction(e -> { onRefresh.run(); refresh(); });
        createBtn.setOnAction(e -> handleCreate());
        cancelBtn.setOnAction(e -> handleCancel());
        rescheduleBtn.setOnAction(e -> handleReschedule());
        availBtn.setOnAction(e -> handleAvailability());

        String btnStyle = "-fx-background-color: linear-gradient(to bottom, #475569, #1e293b); " +
                "-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-border-color: #64748b; -fx-border-width: 1px; -fx-padding: 8 14;";

        for (Button b : new Button[]{refreshBtn, createBtn, cancelBtn, rescheduleBtn, availBtn}) {
            b.setStyle(btnStyle);
        }

        return new HBox(15, refreshBtn, createBtn, cancelBtn, rescheduleBtn, availBtn);
    }

    private void handleCreate() {
        FormDialogs.showBookingCreateDialog(instMgr.getInstruments()).ifPresent(params -> {
            try {
                bookingMgr.createBooking(params.instrumentId(), params.startAt(), params.endAt());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь успешно создана!");
            } catch (NotAvailableException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "В это время данный инструмент уже занят");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
            }
        });
    }

    private void handleCancel() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выберите прибор в ящике кликом");
            return;
        }
        try {
            bookingMgr.cancelBooking(selectedBooking.getId());
            refresh();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь успешно отменена.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    private void handleReschedule() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выберите прибор в ящике кликом");
            return;
        }
        Instrument inst = null;
        for (Instrument i : instMgr.getInstruments()) {
            if (i.getId() == selectedBooking.getInstrumentId()) {
                inst = i;
                break;
            }
        }

        FormDialogs.showBookingRescheduleDialog(selectedBooking, inst).ifPresent(params -> {
            try {
                bookingMgr.rescheduleBooking(selectedBooking.getId(), params.startAt(), params.endAt());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Бронь успешно перенесена!");
            } catch (NotAvailableException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Нельзя перенести: в это время инструмент занят");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
            }
        });
    }

    private void handleAvailability() {
        FormDialogs.showAvailabilityDialog().ifPresent(params -> {
            try {
                var available = instMgr.instAvailable(checkoutMgr, bookingMgr, params.type(), params.startAt(), params.endAt());
                if (available.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Доступные приборы",
                            "Нет доступных приборов типа " + InstrumentTypeLocalization.getRussianName(params.type()) + " на выбранное время.");
                } else {
                    StringBuilder av = new StringBuilder("Свободные приборы:\n");
                    for (Instrument i : available) {
                        av.append("• ").append(InstrumentTypeLocalization.formatInstrument(i)).append("\n");
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Доступные приборы (" + available.size() + ")", av.toString());
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private int getToolImageIndex(InstrumentType type) {
        if (type == null) return 1;
        return switch (type) {
            case BEAKER -> 1;
            case MICROSCOPE -> 2;
            case SCALE -> 3;
            case PETRI_DISH -> 4;
            case SPIRIT_LAMP -> 5;
            case GLOVES -> 6;
            case TONG -> 7;
            case FUNNEL -> 8;
            case THERMOMETER -> 9;
            case PIPETTE -> 10;
        };
    }
}