package ui;

import domain.Instrument;
import exceptions.SecurityException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.InstrumentManager;
import ui.dialogs.FormDialogs;
import ui.dialogs.InstrumentTypeLocalization;

public class InstrumentTabView extends VBox {
    private final InstrumentManager manager;

    private final ObservableList<Instrument> data = FXCollections.observableArrayList();
    private final TableView<Instrument> table = new TableView<>(data);

    public InstrumentTabView(InstrumentManager manager, Runnable onRefresh) {
        this.manager = manager;

        setPadding(new Insets(14));
        setSpacing(12);

        Label title = new Label("🔬 Список лабораторных приборов");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        initTable();
        HBox btnBar = createButtonBar(onRefresh);
        getChildren().addAll(title, table, btnBar);
        refresh();
    }

    private void initTable() {
        TableColumn<Instrument, String> idCol = col("ID", i -> String.valueOf(i.getId()));
        idCol.setPrefWidth(80);

        TableColumn<Instrument, String> typeCol = col("Тип оборудования", i -> {
            if (i.getType() == null) return "-";
            return InstrumentTypeLocalization.getRussianName(i.getType()) + " (" + i.getType().name() + ")";
        });
        typeCol.setPrefWidth(350);

        table.getColumns().addAll(idCol, typeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Список приборов пуст"));
    }

    private <T> TableColumn<T, String> col(String name, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> c = new TableColumn<>(name);
        c.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(mapper.apply(cell.getValue())));
        return c;
    }

    private HBox createButtonBar(Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Обновить список");
        Button addBtn = new Button("➕ Добавить прибор");

        String btnStyle = "-fx-background-color: #ffffff; -fx-text-fill: #334155; -fx-font-weight: bold; " +
                "-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 14;";
        refreshBtn.setStyle(btnStyle);
        addBtn.setStyle(btnStyle);

        refreshBtn.setOnAction(e -> { onRefresh.run(); refresh(); });
        addBtn.setOnAction(e -> handleAdd());

        return new HBox(12, refreshBtn, addBtn);
    }

    public void refresh() {
        data.clear();
        data.addAll(manager.getInstruments());
    }

    private void handleAdd() {
        FormDialogs.showAddInstrumentDialog().ifPresent(type -> {
            try {
                manager.addInstrument(type);
                refresh();
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Прибор '" + InstrumentTypeLocalization.getRussianName(type) + "' успешно добавлен в систему!");
            } catch (SecurityException ex) {
                showAlert(Alert.AlertType.WARNING, "Нет прав", ex.getMessage());
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