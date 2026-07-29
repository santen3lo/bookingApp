package ui;

import domain.Instrument;
import enums.InstrumentType;
import exceptions.SecurityException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.InstrumentManager;


public class InstrumentTabView extends VBox {
    private final InstrumentManager manager;

    private final ObservableList<Instrument> data = FXCollections.observableArrayList();
    private final TableView<Instrument> table = new TableView<>(data);

    public InstrumentTabView(InstrumentManager manager,
                             Runnable onRefresh) {
        this.manager = manager;

        setPadding(new Insets(10));
        setSpacing(10);

        initTable();
        HBox btnBar = createButtonBar(onRefresh);
        getChildren().addAll(new Label("🔬 Инструменты"), table, btnBar);
        refresh();
    }

    private void initTable() {
        table.getColumns().addAll(
                col("ID", i -> String.valueOf(i.getId())),
                col("Тип", i -> i.getType() != null ? i.getType().name() : "-")
        );
    }

    private <T> TableColumn<T, String> col(String name, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> c = new TableColumn<>(name);
        c.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(mapper.apply(cell.getValue())));
        return c;
    }

    private HBox createButtonBar(Runnable onRefresh) {
        Button refreshBtn = new Button("🔄 Refresh");
        Button addBtn = new Button("➕ Добавить");


        refreshBtn.setOnAction(e -> { onRefresh.run(); refresh(); });
        addBtn.setOnAction(e -> handleAdd());

        return new HBox(10, refreshBtn, addBtn);
    }

    public void refresh() {
        data.clear();
        data.addAll(manager.getInstruments());
    }

    private void handleAdd() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Добавление инструмента");
        dlg.setHeaderText("Введите тип прибора (например: PH_METER, SCALE)");
        dlg.setContentText("Тип:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                InstrumentType type = InstrumentType.valueOf(input.trim().toUpperCase());
                manager.addInstrument(type);
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Инструмент добавлен. Нажмите 🔄 Refresh.");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Неверный тип. Проверьте написание enum.");
            } catch (SecurityException ex) {
                showAlert(Alert.AlertType.WARNING, "Нет прав", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}