package ui;

import auth.SessionContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import services.BookingManager;
import services.CheckoutManager;
import services.InstrumentManager;
import services.UserManager;
import storage.JdbcStorage;

public class MainApp extends Application {
    private Stage primaryStage;

    private final JdbcStorage jdbcStorage = new JdbcStorage();
    private final UserManager userMgr = new UserManager(jdbcStorage);
    private final BookingManager bookingMgr = new BookingManager(jdbcStorage);
    private final CheckoutManager checkoutMgr = new CheckoutManager(jdbcStorage);
    private final InstrumentManager instMgr = new InstrumentManager(jdbcStorage);

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        if (!jdbcStorage.testConnection()) {
            showAlert(Alert.AlertType.ERROR, "База данных недоступна",
                    "Не удалось подключиться к PostgreSQL.\nПроверьте настройки в db.properties и запустите сервер БД.");
            javafx.application.Platform.exit();
            return;
        }

        try {
            userMgr.loadUsers();
            bookingMgr.loadFromDb();
            checkoutMgr.loadFromDb();
            instMgr.loadFromDb();
            System.out.println("Данные успешно загружены из хранилищ.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Ошибка загрузки", "Не удалось загрузить данные: " + e.getMessage());
        }

        Runnable saveAction = () -> {
            showAlert(Alert.AlertType.INFORMATION, "Синхронизация",
                    "Данные автоматически сохраняются в БД при каждой операции.");
        };

        Runnable refreshAction = () -> {
            bookingMgr.loadFromDb();
            checkoutMgr.loadFromDb();
            instMgr.loadFromDb();
            userMgr.loadUsers();
        };


        LoginView loginView = new LoginView(userMgr, () -> {
            showMainWindow(saveAction, refreshAction);
        });
        loginView.show();
    }

    private void showMainWindow(Runnable saveAction, Runnable refreshAction) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                new Tab("Бронирования", new BookingTabView(bookingMgr, checkoutMgr, instMgr, saveAction, refreshAction, userMgr)),
                new Tab("Выдачи", new CheckoutTabView(checkoutMgr, instMgr, saveAction, refreshAction, userMgr)),
                new Tab("Инструменты", new InstrumentTabView(instMgr, refreshAction))
        );

        primaryStage.setTitle("Booking & Checkout System | " + SessionContext.getCurrentUser().getLogin());
        primaryStage.setScene(new Scene(tabPane, 1180, 720));
        primaryStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}