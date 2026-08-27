package ui;

import auth.SessionContext;
import domain.Checkout;
import enums.ReturnCondition;
import exceptions.SecurityException;
import exceptions.UnderOneException;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.CheckoutManager;
import services.UserManager;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CheckoutTabView extends VBox {
    private final CheckoutManager checkoutMgr;
    private final UserManager userManager;

    private final TilePane stand = new TilePane();
    private Checkout selectedCheckout;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final CheckBox openOnlyCheck = new CheckBox("Только открытые");

    public CheckoutTabView(CheckoutManager checkoutMgr, Runnable onSave, Runnable onRefresh, UserManager u) {
        this.checkoutMgr = checkoutMgr;
        this.userManager = u;

        setPadding(new Insets(18));
        setSpacing(14);
        // Задний фон вкладки — мягкий градиент стен лаборатории
        setStyle("-fx-background-color: linear-gradient(to bottom, #f1f5f9, #cbd5e1);");

        Label title = new Label("📌 Доска выдачи оборудования");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: #1e293b; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 10, 0.0, 0, 1);");

        openOnlyCheck.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");
        openOnlyCheck.setOnAction(e -> refresh());

        // 🪵 Стилизация под настоящую пробочную доску (Corkboard)
        VBox shelfContainer = new VBox();
        shelfContainer.setPadding(new Insets(20));
        shelfContainer.setStyle(
                "-fx-background-color: #b1895a; " + // Цвет пробки
                        "-fx-border-color: #5c3a21; " +    // Деревянная текстура рамки
                        "-fx-border-width: 10; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.5), 20, 0.0, 0, 0), dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.0, 0, 5);"
        );

        stand.setPrefColumns(4);
        stand.setHgap(20);
        stand.setVgap(20);
        stand.setPadding(new Insets(10));
        stand.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(stand);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(410);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        shelfContainer.getChildren().add(scrollPane);

        HBox topBar = new HBox(15, title, openOnlyCheck);
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox btnBar = createButtonBar(onSave, onRefresh);
        btnBar.setStyle("-fx-padding: 10 0 0 0;");

        getChildren().addAll(topBar, shelfContainer, btnBar);
        refresh();
    }

    /** Создаёт карточку-стикер для выдачи */
    private VBox createCard(Checkout c) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(5, 10, 12, 10));
        card.setPrefWidth(180);
        card.setMaxWidth(180);
        card.setAlignment(Pos.TOP_CENTER);

        boolean isOwn = SessionContext.isAuthenticated() && c.getUserId() == SessionContext.getCurrentUserId();


        // 📌 Канцелярский гвоздик
        Label pin = new Label("📌");
        pin.setStyle("-fx-font-size: 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.0, 0, 2);");

        // Цветовая гамма стикера
        String cardBg = isOwn ? "#fef08a" : "#ffffff"; // Желтый для своих, белый для чужих
        String textColor = "#334155";

        card.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.0, 2, 4);",
                cardBg
        ));



        Label idLabel = new Label("Выдача #" + c.getId());
        idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + textColor + ";");

        Label instrLabel = new Label("Пользователь " + userManager.getLoginById(c.getUserId()));
        instrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        card.getChildren().addAll(pin, idLabel, instrLabel);

        // 🎬 Анимация при наведении
        TranslateTransition liftAnimation = new TranslateTransition(Duration.millis(150), card);

        // ✅ ДОБАВЛЕН liftAnimation.stop() ДЛЯ СБРОСА СОСТОЯНИЯ
        card.setOnMouseEntered(e -> {
            liftAnimation.stop();
            liftAnimation.setToY(-10);
            liftAnimation.play();
            card.setEffect(new DropShadow(16, 5, 12, Color.rgb(0, 0, 0, 0.35)));
        });

        card.setOnMouseExited(e -> {
            liftAnimation.stop();
            liftAnimation.setToY(0);
            liftAnimation.play();
            card.setEffect(new DropShadow(8, 2, 4, Color.rgb(0, 0, 0, 0.25)));
        });

        // 🖱 Выбор карточки при клике и показ подробностей
        card.setOnMouseClicked(e -> {
            selectedCheckout = c;
            updateSelectionHighlight();
            showDetails(c);
        });

        return card;
    }

    /** Подсветка выбранной карточки неоновой рамкой */
    private void updateSelectionHighlight() {
        for (javafx.scene.Node node : stand.getChildren()) {
            if (node instanceof VBox card) {
                card.setStyle(card.getStyle().replaceAll("-fx-border-color: #[a-fA-F0-9]{6};", "-fx-border-color: transparent;"));
            }
        }

        if (selectedCheckout != null) {
            for (javafx.scene.Node node : stand.getChildren()) {
                if (node instanceof VBox card && card.getChildren().size() > 2) {
                    Label lbl = (Label) card.getChildren().get(2); // Индекс 2 — это idLabel
                    if (lbl.getText().contains("#" + selectedCheckout.getId())) {
                        card.setStyle(card.getStyle().replace("-fx-border-color: transparent;", "-fx-border-color: #3b82f6;"));
                        break;
                    }
                }
            }
        }
    }

    private HBox createButtonBar(Runnable onSave, Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Refresh");
        Button takeBtn = new Button("📤 Оформить выдачу");
        Button returnBtn = new Button("📥 Вернуть");

        // Единый стиль кнопок
        String btnStyle = "-fx-background-color: #ffffff; -fx-text-fill: #334155; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4;";
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
    }

    // ==========================================
    // Обработчики логики
    // ==========================================

    private void handleTake() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Оформление выдачи");
        dlg.setHeaderText("Формат: instrumentId, comment");
        dlg.setContentText("Данные:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                parsers.GuiInputParser.CheckoutTakeParams params = parsers.GuiInputParser.parseCheckoutTake(input);
                checkoutMgr.takeCheckout(params.instrumentId(), params.comment());
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Выдача успешно оформлена!");
            } catch (exceptions.SecurityException e) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", e.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    private void handleReturn() {
        if (selectedCheckout == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите карточку выдачи на доске");
            return;
        }
        if (selectedCheckout.getUserId() != SessionContext.getCurrentUserId()) {
            showAlert(Alert.AlertType.ERROR, "Нет прав", "Вы не можете вернуть чужую выдачу");
            return;
        }
        TextInputDialog dlg = new TextInputDialog("OK");
        dlg.setTitle("Возврат прибора");
        dlg.setHeaderText("Состояние (OK или DAMAGED):");
        dlg.setContentText("Condition:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                ReturnCondition cond = parsers.GuiInputParser.parseReturnCondition(input);
                checkoutMgr.returnCheckout(selectedCheckout.getId(), cond);
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Прибор возвращён.");
            } catch (exceptions.SecurityException e) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", e.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    /** Вывод информации о выдаче при клике на стикер */
    private void showDetails(Checkout sel) {
        if (sel == null) return;
        String info = String.format("Checkout#%d\nПрибор: %d\nПользователь: %s\nВзято: %s\nВозврат: %s\nКоммент: %s\nСостояние: %s",
                sel.getId(),
                sel.getInstrumentId(),
                userManager.getLoginById(sel.getUserId()),
                sel.getTakenAt() != null ? fmt.format(sel.getTakenAt()) : "-",
                sel.getReturnedAt() != null ? fmt.format(sel.getReturnedAt()) : "-",
                sel.getComment() != null && !sel.getComment().isBlank() ? sel.getComment() : "-",
                sel.getReturnCondition() != null ? sel.getReturnCondition().name() : "-");
        showAlert(Alert.AlertType.INFORMATION, "Подробнее", info);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}