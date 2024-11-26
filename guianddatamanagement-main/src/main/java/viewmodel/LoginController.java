package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML
    private GridPane rootpane;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMe;
    @FXML
    private Label statusLabel;

    private static final String USERS_FILE = "users.txt";
    private static final String SALT = "EduHub_Salt_2023";
    private final Preferences prefs = Preferences.userRoot().node("EduHub");

    @FXML
    public void initialize() {
        try {
            // This is to set the background image
            rootpane.setBackground(new Background(createImage("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png")));

            // This is to load the remembered credentials
            if (prefs.getBoolean("rememberMe", false)) {
                usernameField.setText(prefs.get("savedUsername", ""));
                passwordField.setText(prefs.get("savedPassword", ""));
                rememberMe.setSelected(true);
            }

            // This is to add the fade in animation
            rootpane.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(3), rootpane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error initializing login screen");
            e.printStackTrace();
        }
    }

    private BackgroundImage createImage(String url) {
        try {
            // This is to create a background image for the UI
            return new BackgroundImage(
                    new Image(url),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    new BackgroundPosition(Side.LEFT, 0, false, Side.TOP, 0, false),
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    protected void login(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // This is to check if the field is empty
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username and password are required.");
            return;
        }

        try {
            String hashedPassword = hashPassword(password);
            if (validateCredentials(username, hashedPassword)) {
                // This is to save credentials if remember me is checked
                if (rememberMe.isSelected()) {
                    prefs.put("savedUsername", username);
                    prefs.put("savedPassword", password);
                    prefs.putBoolean("rememberMe", true);
                } else {
                    prefs.remove("savedUsername");
                    prefs.remove("savedPassword");
                    prefs.putBoolean("rememberMe", false);
                }

                // This is to create user session
                UserSession.getInstance(username, hashedPassword);

                // This is to load main interface
                loadMainInterface(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Login failed. Please try again.");
        }
    }

    // This is to check if the entered username and password match the stored ones
    private boolean validateCredentials(String username, String hashedPassword) {
        try {
            return Files.lines(Paths.get(USERS_FILE))
                    .anyMatch(line -> {
                        String[] parts = line.split(":");
                        return parts.length == 2 &&
                                parts[0].equals(username) &&
                                parts[1].equals(hashedPassword);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // This is so that no one can see the password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + SALT;
            byte[] hash = digest.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // This is to open the main app screen after logging in successfully
    private void loadMainInterface(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
            Scene scene = new Scene(root);

            // This is to apply a light theme to the interface
            String themePath = "/css/lightTheme.css";
            if (getClass().getResource(themePath) != null) {
                scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("EduHub - Student Management");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load main interface. Error: " + e.getMessage());
        }
    }

    @FXML
    protected void signUp(ActionEvent event) {
        try {
            // This is to navigate to the sign up screen
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root);

            // This is to apply a light theme to the sign up screen
            String themePath = "/css/lightTheme.css";
            if (getClass().getResource(themePath) != null) {
                scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("EduHub - Sign Up");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load sign up screen.");
        }
    }

    // This is to display an alert dialog with a message
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("EduHub - " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
