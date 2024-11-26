package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    // This is just the list of majors
    public enum Major {
        COMPUTER_SCIENCE("Computer Science"),
        INFORMATION_TECHNOLOGY("Information Technology"),
        DATA_SCIENCE("Data Science"),
        CYBERSECURITY("Cybersecurity"),
        SOFTWARE_ENGINEERING("Software Engineering");

        private final String displayName;

        Major(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName; // This is to show the display name for the major
        }

        public static Major fromString(String text) {
            for (Major major : Major.values()) {
                if (major.displayName.equalsIgnoreCase(text)) {
                    return major; // This is to match the major by the display name
                }
            }
            throw new IllegalArgumentException("No major with display name: " + text);
        }
    }

    public static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    StorageUploader store = new StorageUploader();
    @FXML
    ProgressBar progressBar;

    @FXML
    private MenuItem editItem, deleteItem, ClearItem, CopyItem, importItem, exportItem;

    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<Major> major;
    @FXML
    Button addBtn, btnEdit, btnDelete;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass(); // This is the helper for working with the database
    private final ObservableList<Person> data = cnUtil.getData(); // This is the load data from the database

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // This is to arrange table columns to match the person details
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            // This is to set up the major combobox with the choices
            major.setItems(FXCollections.observableArrayList(Major.values()));

            // This is to add checks to the form fields to make sure the input is correct
            first_name.textProperty().addListener((obs, old, newValue) -> validateForm());
            last_name.textProperty().addListener((obs, old, newValue) -> validateForm());
            department.textProperty().addListener((obs, old, newValue) -> validateForm());
            email.textProperty().addListener((obs, old, newValue) -> validateForm());
            major.valueProperty().addListener((obs, old, newValue) -> validateForm());

            // This is to enable and disable the menu items based on what is selected in the table
            editItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            ClearItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            CopyItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            // This is to disable the add button until the form is valid
            addBtn.setDisable(true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private Label statusLabel;

    // This is to update the status label with a message
    private void updateStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + message);
            statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        });
    }

    @FXML
    protected void lightTheme(ActionEvent event) {
        try {
            // This is to apply the light theme
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void darkTheme(ActionEvent event) {
        try {
            // This is to apply the dark theme
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            // This is to fill the form fields with data from the selected item
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            try {
                major.setValue(Major.fromString(p.getMajor()));
            } catch (IllegalArgumentException e) {
                updateStatus("Invalid major value in record", true);
            }
            btnEdit.setDisable(false);
            btnDelete.setDisable(false);
        } else {
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        if (validateForm()) {
            Major selectedMajor = major.getValue();
            Person updatedPerson = new Person(index + 1, first_name.getText(), last_name.getText(),
                    department.getText(), selectedMajor.toString(), email.getText(), imageURL.getText());
            cnUtil.editUser(p.getId(), updatedPerson);
            data.remove(p);
            data.add(index, updatedPerson);
        }
        tv.getSelectionModel().select(index);
        clearForm();
        updateStatus("Record updated successfully!", false);
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            // This is to confirm before deleting a record
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete this record?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                int index = data.indexOf(p);
                cnUtil.deleteRecord(p);
                data.remove(index); // This is to remove record from the table
                tv.getSelectionModel().select(index); // This is to reselect the updated record
            }
        }
    }

    @FXML
    protected void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(tv.getScene().getWindow());

        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    String[] fields = line.split(",");
                    if (fields.length == 6) {
                        Person p = new Person(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);
                        data.add(p);
                        cnUtil.insertUser(p);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(tv.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // This is to write all table data to a csv file
                for (Person p : data) {
                    writer.println(String.join(",", p.getFirstName(), p.getLastName(), p.getDepartment(),
                            p.getMajor(), p.getEmail(), p.getImageURL()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void addNewRecord() {
        if (validateForm()) {
            Major selectedMajor = major.getValue();
            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    selectedMajor.toString(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
            updateStatus("Record added successfully!", false);
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));

            Task<Void> uploadTask = createUploadTask(file, progressBar);
            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This is to check the form fields if it's right and update the add button
    private boolean validateForm() {
        String nameRegex = "^[A-Za-z]+(?: [A-Za-z]+)*$";
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        String departmentRegex = "^[A-Za-z]+(?: [A-Za-z]+)*$";

        boolean isValid = true;

        // This is to check if the first name is valid
        if (!first_name.getText().matches(nameRegex)) {
            first_name.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            first_name.setStyle("");
        }

        if (!last_name.getText().matches(nameRegex)) {
            last_name.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            last_name.setStyle("");
        }

        if (!department.getText().matches(departmentRegex)) {
            department.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            department.setStyle("");
        }

        // This is to check if the email is valid
        if (!email.getText().matches(emailRegex)) {
            email.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            email.setStyle("");
        }

        if (major.getValue() == null) {
            major.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            major.setStyle("");
        }

        // This is to enable and disable the add button
        addBtn.setDisable(!isValid);
        return isValid;
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        email.setText("");
        major.setValue(null);
        imageURL.setText("");

        first_name.setStyle("");
        last_name.setStyle("");
        department.setStyle("");
        email.setStyle("");
        major.setStyle("");
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify details");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField textField1 = new TextField("First Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email");
        ObservableList<Major> options = FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();

        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(), textField2.getText(), comboBox.getValue());
            }
            return null;
        });

        Optional<Results> result = dialog.showAndWait();
        result.ifPresent(details -> {
            MyLogger.makeLog(details.fname + " " + details.lname + " " + details.major);
        });
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (InputStream fileInputStream = Files.newInputStream(file.toPath());
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        final double progress = (double) uploadedBytes / fileSize;
                        Platform.runLater(() -> progressBar.setProgress(progress));
                    }
                }

                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    updateStatus("Image uploaded successfully!", false);
                });

                return null;
            }
        };
    }
}
