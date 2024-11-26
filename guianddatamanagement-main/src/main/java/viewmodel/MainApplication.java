package viewmodel;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApplication extends Application {
    private Stage primaryStage;

    @Override
    public void init() {
        // This is just the code to set things up if necessary
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;

            // This is to load application icon
            Image icon = new Image(getClass().getResourceAsStream("/images/DollarClouddatabase.png"));
            primaryStage.getIcons().add(icon);

            // This is to configure primary stage
            primaryStage.setResizable(false);
            primaryStage.setTitle("FSC CSC311 _ Database Project");

            // This is to show splash screen
            showSplashScreen();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void showSplashScreen() {
        try {
            // This is to load splash screen
            Parent root = FXMLLoader.load(getClass().getResource("/view/splashscreen.fxml"));
            Scene scene = new Scene(root, 900, 600);

            // This is to apply theme
            String cssPath = "/css/lightTheme.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            // This is to show splash screen
            primaryStage.setScene(scene);
            primaryStage.show();

            // This is to start the transition to login screen
            transitionToLogin();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void transitionToLogin() {
        try {
            // This is to load login screen
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene loginScene = new Scene(loginRoot, 900, 600);

            // This is to apply theme
            String cssPath = "/css/lightTheme.css";
            if (getClass().getResource(cssPath) != null) {
                loginScene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            // This is to create the fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // This is to handle the transition completion
            fadeOut.setOnFinished((EventHandler<ActionEvent>) event -> {
                primaryStage.setScene(loginScene);
                primaryStage.show();
            });

            // This is to start the transition
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Cleanup code if needed
    }
}