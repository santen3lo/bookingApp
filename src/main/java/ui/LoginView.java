package ui;

import auth.SessionContext;
import domain.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UserManager;

import java.util.Optional;

public class LoginView {
    private final UserManager userManager;

    private final Runnable onLoginSuccess;

    public LoginView(UserManager userManager, Runnable onLoginSuccess) {
        this.userManager = userManager;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void show() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Авторизация");
        loginStage.setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Вход в систему");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");

        Button loginBtn = new Button("Войти");
        loginBtn.setStyle("-fx-base: #4CAF50; -fx-text-fill: white;");

        Button registerBtn = new Button("Регистрация");
        registerBtn.setStyle("-fx-font-size: 11px; -fx-background-color: transparent; -fx-text-fill: #0066CC; -fx-underline: true;");

        loginBtn.setOnAction(e -> {
            String login = loginField.getText().trim();
            String pass = passField.getText().trim();
            Optional<User> userOpt = userManager.login(login, pass);

            if (userOpt.isPresent()) {
                SessionContext.login(userOpt.get());
                try {
                    onLoginSuccess.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть главное окно: " + ex.getMessage());
                    return;
                }
                loginStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Неверный логин или пароль");
            }
        });

        registerBtn.setOnAction(e -> handleRegistration());

        root.getChildren().addAll(title, loginField, passField, loginBtn, registerBtn);
        loginStage.setScene(new Scene(root, 320, 260));
        loginStage.show();
    }

    private void handleRegistration() {
        TextInputDialog loginDlg = new TextInputDialog();
        loginDlg.setTitle("Регистрация");
        loginDlg.setHeaderText("Придумайте логин:");
        loginDlg.showAndWait().ifPresent(login -> {
            TextInputDialog passDlg = new TextInputDialog();
            passDlg.setHeaderText("Придумайте пароль:");
            passDlg.showAndWait().ifPresent(pass -> {
                try {
                    long id = userManager.register(login.trim(), pass.trim());
                    showAlert(Alert.AlertType.INFORMATION,"Успех", "Пользователь создан. Теперь войдите в систему.");
                } catch (IllegalArgumentException ex) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", ex.getMessage());
                }
            });
        });
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}