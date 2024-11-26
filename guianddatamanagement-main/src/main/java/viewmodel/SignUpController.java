package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SignUpController {

    private static final String USERS_FILE = "users.txt"; // This is the file to store user data
    private static final String SALT = "EduHub_Salt_2023"; // This is the salt for password hashing

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    // This is to handle the sign up process when the sign up button is clicked
    @FXML
    public void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // This is to check if all the fields are filled
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }

        // This is to check if the passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        // This is the password strength
        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, one number, and one special character.");
            return;
        }

        try {
            // This is to check if the username already exists
            if (userExists(username)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Username already exists.");
                return;
            }

            // This is to hash the password and save the user
            String hashedPassword = hashPassword(password);
            saveUser(username, hashedPassword);

            // This is to show success message and redirect to login screen
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
            redirectToLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error creating account. Please try again.");
        }
    }

    // The password strength
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?].*");
    }

    // This is to check if the username already exists in the users file
    private boolean userExists(String username) throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return false;
        }

        // This is to check each line in the file to see if the username is already taken
        return Files.lines(file.toPath())
                .map(line -> line.split(":")[0])
                .anyMatch(username::equals);
    }

    // This is to hash the password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + SALT;
            byte[] hash = digest.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // This is to save the new person to the file
    private void saveUser(String username, String hashedPassword) throws IOException {
        File file = new File(USERS_FILE);
        String userEntry = username + ":" + hashedPassword + "\n";

        if (!file.exists()) {
            Files.write(file.toPath(), userEntry.getBytes(), StandardOpenOption.CREATE);
        } else {
            Files.write(file.toPath(), userEntry.getBytes(), StandardOpenOption.APPEND);
        }
    }

    // This is to redirect the person to the login screen after successful sign-up
    @FXML
    public void redirectToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);

            // This is to apply theme
            String themePath = "/css/theme.css";
            if (getClass().getResource(themePath) != null) {
                scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("EduHub - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to login screen.");
        }
    }

    // This is to show a pop up alert with a message
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("EduHub - " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
