package ui;

import auth.SessionContext;
import domain.Checkout;
import domain.Instrument;
import enums.ReturnCondition;
import exceptions.SecurityException;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.CheckoutManager;
import services.InstrumentManager;
import services.UserManager;
import ui.dialogs.FormDialogs;
import ui.dialogs.InstrumentTypeLocalization;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CheckoutTabView extends VBox {
    private final CheckoutManager checkoutMgr;
    private final InstrumentManager instMgr;
    private final UserManager userManager;

    private final TilePane stand = new TilePane();
    private Checkout selectedCheckout;

    private final VBox inspectorPanel = new VBox(14);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final CheckBox openOnlyCheck = new CheckBox("Только открытые");

    public CheckoutTabView(CheckoutManager checkoutMgr, InstrumentManager instMgr,
                           Runnable onSave, Runnable onRefresh, UserManager u) {
        this.checkoutMgr = checkoutMgr;
        this.instMgr = instMgr;
        this.userManager = u;

        setPadding(new Insets(18));
        setSpacing(14);
        setStyle("-fx-background-color: linear-gradient(to bottom, #f1f5f9, #cbd5e1);");

        Label title = new Label("📌 Доска выдачи оборудования");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 10, 0.0, 0, 1);");

        openOnlyCheck.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");
        openOnlyCheck.setOnAction(e -> refresh());

        // 🪵 Стилизация под пробковую доску (Corkboard)
        VBox shelfContainer = new VBox();
        shelfContainer.setPadding(new Insets(16));
        shelfContainer.setStyle(
                "-fx-background-color: #b1895a; " +
                        "-fx-border-color: #5c3a21; " +
                        "-fx-border-width: 10; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.5), 20, 0.0, 0, 0), dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.0, 0, 5);"
        );

        stand.setPrefColumns(4);
        stand.setHgap(18);
        stand.setVgap(18);
        stand.setPadding(new Insets(10));
        stand.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(stand);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(480);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        shelfContainer.getChildren().add(scrollPane);

        // 📋 Боковая панель инспектора выдачи
        setupInspectorPanel();

        HBox topBar = new HBox(15, title, openOnlyCheck);
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox centerArea = new HBox(16, shelfContainer, inspectorPanel);
        HBox.setHgrow(shelfContainer, Priority.ALWAYS);
        centerArea.setAlignment(Pos.CENTER_LEFT);

        HBox btnBar = createButtonBar(onSave, onRefresh);
        btnBar.setStyle("-fx-padding: 6 0 0 0;");

        getChildren().addAll(topBar, centerArea, btnBar);
        refresh();
    }

    private void setupInspectorPanel() {
        inspectorPanel.setPrefWidth(300);
        inspectorPanel.setMinWidth(300);
        inspectorPanel.setMaxWidth(320);
        inspectorPanel.setPadding(new Insets(18));
        inspectorPanel.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #94a3b8; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);"
        );
        showEmptyInspector();
    }

    private void showEmptyInspector() {
        inspectorPanel.getChildren().clear();
        inspectorPanel.setAlignment(Pos.CENTER);

        Label pin = new Label("📌");
        pin.setStyle("-fx-font-size: 36px;");

        Label hint = new Label("Выберите стикер выдачи на доске для просмотра параметров и возврата");
        hint.setWrapText(true);
        hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        hint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");

        inspectorPanel.getChildren().addAll(pin, hint);
    }

    private void updateInspector(Checkout c) {
        inspectorPanel.getChildren().clear();
        inspectorPanel.setAlignment(Pos.TOP_LEFT);

        Instrument instrument = null;
        for (Instrument i : instMgr.getInstruments()) {
            if (i.getId() == c.getInstrumentId()) {
                instrument = i;
                break;
            }
        }

        boolean isOwn = SessionContext.isAuthenticated() && c.getUserId() == SessionContext.getCurrentUserId();
        boolean isOpen = c.getReturnedAt() == null;

        Label header = new Label("📌 Выдача #" + c.getId());
        header.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String instName = instrument != null
                ? InstrumentTypeLocalization.formatInstrument(instrument)
                : ("Прибор #" + c.getInstrumentId());

        Label instLabel = new Label(instName);
        instLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        Label statusChip;
        if (isOpen) {
            statusChip = new Label("🟡 НА РУКАХ");
            statusChip.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            boolean isOk = c.getReturnCondition() == ReturnCondition.OK;
            statusChip = new Label(isOk ? "🟢 ВОЗВРАЩЁН (OK)" : "🔴 ПОВРЕЖДЁН (DAMAGED)");
            statusChip.setStyle(isOk
                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        Label borrowerLabel = new Label("👤 Кому выдан: " + userManager.getLoginById(c.getUserId()) + (isOwn ? " (Вы)" : ""));
        borrowerLabel.setStyle(isOwn ? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 12px;" : "-fx-text-fill: #475569; -fx-font-size: 12px;");

        Label takenLabel = new Label("📤 Взято: " + (c.getTakenAt() != null ? fmt.format(c.getTakenAt()) : "-"));
        takenLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        Label returnedLabel = new Label("📥 Возврат: " + (c.getReturnedAt() != null ? fmt.format(c.getReturnedAt()) : "-"));
        returnedLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        Label commentLabel = new Label("💬 Комментарий:\n" + (c.getComment() != null && !c.getComment().isBlank() ? c.getComment() : "(нет комментария)"));
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-style: italic;");

        VBox detailsBox = new VBox(8, instLabel, statusChip, borrowerLabel, takenLabel, returnedLabel, commentLabel);
        detailsBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8;");

        VBox actionsBox = new VBox(8);
        actionsBox.setPadding(new Insets(8, 0, 0, 0));

        if (isOpen && isOwn) {
            Button returnActionBtn = new Button("📥 Оформить возврат прибора");
            returnActionBtn.setMaxWidth(Double.MAX_VALUE);
            returnActionBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 6;");
            returnActionBtn.setOnAction(e -> handleReturn());
            actionsBox.getChildren().add(returnActionBtn);
        } else if (isOpen) {
            Label notice = new Label("🔒 Прибор на руках у другого пользователя");
            notice.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-style: italic;");
            actionsBox.getChildren().add(notice);
        } else {
            Label doneLabel = new Label("✅ Прибор уже возвращён в лабораторию");
            doneLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 11px;");
            actionsBox.getChildren().add(doneLabel);
        }

        inspectorPanel.getChildren().addAll(header, detailsBox, actionsBox);
    }

    /** Создаёт карточку-стикер для выдачи */
    private VBox createCard(Checkout c) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(5, 10, 12, 10));
        card.setPrefWidth(170);
        card.setMaxWidth(170);
        card.setAlignment(Pos.TOP_CENTER);

        boolean isOwn = SessionContext.isAuthenticated() && c.getUserId() == SessionContext.getCurrentUserId();

        Label pin = new Label("📌");
        pin.setStyle("-fx-font-size: 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.0, 0, 2);");

        String cardBg = isOwn ? "#fef08a" : "#ffffff";
        String textColor = "#334155";

        card.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0.0, 1, 3);",
                cardBg
        ));

        Label idLabel = new Label("Выдача #" + c.getId());
        idLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Label instrLabel = new Label(userManager.getLoginById(c.getUserId()));
        instrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        Label statusLabel = new Label(c.getReturnedAt() == null ? "● На руках" : "✓ Сдан");
        statusLabel.setStyle(c.getReturnedAt() == null
                ? "-fx-text-fill: #d97706; -fx-font-size: 10px; -fx-font-weight: bold;"
                : "-fx-text-fill: #16a34a; -fx-font-size: 10px;");

        card.getChildren().addAll(pin, idLabel, instrLabel, statusLabel);

        TranslateTransition liftAnimation = new TranslateTransition(Duration.millis(150), card);
        card.setOnMouseEntered(e -> {
            liftAnimation.stop();
            liftAnimation.setToY(-8);
            liftAnimation.play();
            card.setEffect(new DropShadow(14, 4, 10, Color.rgb(0, 0, 0, 0.3)));
        });

        card.setOnMouseExited(e -> {
            liftAnimation.stop();
            liftAnimation.setToY(0);
            liftAnimation.play();
            card.setEffect(new DropShadow(6, 1, 3, Color.rgb(0, 0, 0, 0.2)));
        });

        card.setOnMouseClicked(e -> {
            selectedCheckout = c;
            updateSelectionHighlight();
            updateInspector(c);
        });

        return card;
    }

    private void updateSelectionHighlight() {
        for (javafx.scene.Node node : stand.getChildren()) {
            if (node instanceof VBox card) {
                card.setStyle(card.getStyle().replaceAll("-fx-border-color: #[a-fA-F0-9]{6};", "-fx-border-color: transparent;"));
            }
        }

        if (selectedCheckout != null) {
            for (javafx.scene.Node node : stand.getChildren()) {
                if (node instanceof VBox card && card.getChildren().size() > 2) {
                    Label lbl = (Label) card.getChildren().get(1);
                    if (lbl.getText().contains("#" + selectedCheckout.getId())) {
                        card.setStyle(card.getStyle().replace("-fx-border-color: transparent;", "-fx-border-color: #2563eb;"));
                        break;
                    }
                }
            }
        }
    }

    private HBox createButtonBar(Runnable onSave, Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Обновить доску");
        Button takeBtn = new Button("📤 Оформить выдачу");
        Button returnBtn = new Button("📥 Вернуть прибор");

        String btnStyle = "-fx-background-color: #ffffff; -fx-text-fill: #334155; -fx-font-weight: bold; " +
                "-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 14;";
        refreshBtn.setStyle(btnStyle);
        takeBtn.setStyle(btnStyle);
        returnBtn.setStyle(btnStyle);

        refreshBtn.setOnAction(e -> { onRefresh.run(); refresh(); });
        takeBtn.setOnAction(e -> handleTake());
        returnBtn.setOnAction(e -> handleReturn());

        return new HBox(15, refreshBtn, takeBtn, returnBtn);
    }

    public void refresh() {
        selectedCheckout = null;
        stand.getChildren().clear();

        List<Checkout> source = openOnlyCheck.isSelected()
                ? checkoutMgr.getCheckouts().stream().filter(c -> c.getReturnedAt() == null).collect(Collectors.toList())
                : checkoutMgr.getCheckouts();

        for (Checkout c : source) {
            stand.getChildren().add(createCard(c));
        }
        showEmptyInspector();
    }

    private void handleTake() {
        FormDialogs.showCheckoutTakeDialog(instMgr.getInstruments()).ifPresent(params -> {
            try {
                checkoutMgr.takeCheckout(params.instrumentId(), params.comment());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Выдача успешно оформлена!");
            } catch (SecurityException e) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", e.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    private void handleReturn() {
        if (selectedCheckout == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выберите стикер выдачи на доске");
            return;
        }
        if (selectedCheckout.getUserId() != SessionContext.getCurrentUserId()) {
            showAlert(Alert.AlertType.ERROR, "Нет прав", "Вы не можете вернуть чужую выдачу");
            return;
        }

        String instName = "Прибор #" + selectedCheckout.getInstrumentId();
        for (Instrument i : instMgr.getInstruments()) {
            if (i.getId() == selectedCheckout.getInstrumentId()) {
                instName = InstrumentTypeLocalization.formatInstrument(i);
                break;
            }
        }

        FormDialogs.showCheckoutReturnDialog(selectedCheckout, instName).ifPresent(condition -> {
            try {
                checkoutMgr.returnCheckout(selectedCheckout.getId(), condition);
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Прибор успешно возвращён в лабораторию.");
            } catch (SecurityException e) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", e.getMessage());
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
}