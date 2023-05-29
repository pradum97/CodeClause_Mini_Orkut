package com.mini.orkut.controller.auth;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.user.OperationType;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.AuthInformation;
import com.mini.orkut.util.CommonUtility;
import com.mini.orkut.util.OptionalMethod;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class SignUp implements Initializable {
    public TextField firstNameTf;
    public TextField lastNameTf;
    public ComboBox<String> genderCom;
    public TextField usernameTf;
    public TextField emailTf;
    public TextField phoneTf;
    public PasswordField passwordTf;
    public Button resetBn;
    public Button submit_bn;
    public HBox profileImg;
    public Button choosePhoto;
    public VBox passwordContainer;
    public HBox createAccountContainer;
    private File imageFile;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private OperationType operationType;
    private boolean isImageAvailable = false;

    private int userId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

        Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();

        operationType = (OperationType) map.get("type");
        genderCom.setItems(CommonUtility.genders);

        if (operationType == OperationType.CREATE) {
            setData();

        } else if (operationType == OperationType.UPDATE) {

            int userId = (int) map.get("user_id");
            getUserDataById(userId);
            method.hideElement(passwordContainer, createAccountContainer);
            choosePhoto.setText("UPDATE PHOTO");
            submit_bn.setText("UPDATE");
        }

    }

    private void getUserDataById(int id) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String query = "select * from users where user_id = ?";
            ps = connection.prepareStatement(query);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {

                userId = rs.getInt("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gender = rs.getString("gender");
                String email = rs.getString("email");
                String usernameStr = rs.getString("username");
                String phone = rs.getString("phone");
                String phoneCode = rs.getString("phone_code");
                String createdDate = rs.getString("created_date");
                InputStream is = rs.getBinaryStream("profile_photo");

                AuthInformation authInformation = new AuthInformation(userId, usernameStr, email, gender,
                        firstName, lastName, phoneCode, phone, createdDate, is);

                setUserDate(authInformation);

            } else {
                customDialog.showAlertBox("Login failed !!", "Incorrect username or password");
            }


        } catch (SQLException e) {
            customDialog.showAlertBox("Login failed !!", "Something went wrong. Please try again !!");
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void setUserDate(AuthInformation authInfo) {


        firstNameTf.setText(authInfo.getFirstName());
        lastNameTf.setText(authInfo.getLastName());
        genderCom.getSelectionModel().select(authInfo.getGender());
        usernameTf.setText(authInfo.getUsername());
        phoneTf.setText(authInfo.getPhone());
        emailTf.setText(authInfo.getEmail());

        Image image;

        if (null == authInfo.getProfilePhoto()) {
            image = new ImageLoader().load("img/preview_ic.png");
        } else {
            isImageAvailable = true;
            image = new Image(authInfo.getProfilePhoto());

        }

        Circle cir2 = new Circle(119, 119, 40);
        cir2.setStroke(Color.SEAGREEN);
        cir2.setFill(Color.SNOW);
        cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
        cir2.setFill(new ImagePattern(image));
        profileImg.getChildren().clear();
        profileImg.getChildren().add(cir2);
    }

    private void setData() {

        Image image = new ImageLoader().load("img/preview_ic.png");
        Circle cir2 = new Circle(119, 119, 40);
        cir2.setStroke(Color.SEAGREEN);
        cir2.setFill(Color.SNOW);
        cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
        cir2.setFill(new ImagePattern(image));
        profileImg.getChildren().add(cir2);
    }

    public void enterPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER) {
            submit_bn(null);
        }
    }

    public void resetBn(ActionEvent actionEvent) {

        firstNameTf.setText("");
        lastNameTf.setText("");

        usernameTf.setText("");
        emailTf.setText("");
        phoneTf.setText("");
        passwordTf.setText("");

        if (genderCom.getSelectionModel().getSelectedItem().isEmpty()) {
            genderCom.getSelectionModel().clearSelection();
        }

    }

    public void submit_bn(ActionEvent actionEvent) {

        String firstName = firstNameTf.getText();
        String lastName = lastNameTf.getText();
        String username = usernameTf.getText();
        String email = emailTf.getText();
        String phone = phoneTf.getText();
        String password = passwordTf.getText();

        String gender = genderCom.getSelectionModel().getSelectedItem();

        if (firstName.isEmpty()) {
            method.show_popup("Please enter first name", firstNameTf);
            return;
        } else if (gender.isEmpty()) {
            method.show_popup("Please select gender", genderCom);
            return;
        } else if (username.isEmpty()) {
            method.show_popup("Please enter username", usernameTf);
            return;
        } else if (email.isEmpty()) {
            method.show_popup("Please enter valid email", emailTf);
            return;
        }


        Connection connection = null;
        PreparedStatement ps = null;

        if (operationType == OperationType.CREATE) {
            if (password.isEmpty()) {
                method.show_popup("Please enter password", passwordTf);
                return;
            }

            try {

                connection = new DBConnection().getConnection();

                String query = """

                        INSERT INTO USERS(FIRST_NAME, LAST_NAME, GENDER, EMAIL, USERNAME, PHONE, PHONE_CODE, PASSWORD,
                         PROFILE_PHOTO) VALUES (?,?,?,?,?,?,?,?,?)
                        """;
                ps = connection.prepareStatement(query);
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, gender);
                ps.setString(4, email);
                ps.setString(5, username);
                ps.setString(6, phone);
                ps.setString(7, "91");
                ps.setString(8, password);


                File img = new File(imageFile.getAbsolutePath());
                FileInputStream fin = new FileInputStream(img);

                if (null == imageFile) {
                    ps.setString(9, null);
                } else {
                    ps.setBinaryStream(9, fin, (int) img.length());
                    ;
                }

                int res = ps.executeUpdate();


                if (res > 0) {
                    customDialog.showAlertBox("Success", "Successfully account created");
                    resetBn(null);
                    new Main().changeScene("auth/login.fxml", "Login");
                } else {
                    customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                }


            } catch (SQLException e) {
                customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeConnection(connection, ps, null);
            }

        } else if (operationType == OperationType.UPDATE) {

            try {

                connection = new DBConnection().getConnection();

                String query = """

                        UPDATE USERS SET FIRST_NAME=?, LAST_NAME=?, GENDER=?, EMAIL=?, USERNAME=?, PHONE=? where user_id = ?
                        """;
                ps = connection.prepareStatement(query);
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, gender);
                ps.setString(4, email);
                ps.setString(5, username);
                ps.setString(6, phone);
                ps.setInt(7, userId);

                int res = ps.executeUpdate();


                if (res > 0) {
                    customDialog.showAlertBox("Success", "Successfully updated");
                    Main.primaryStage.setUserData(true);

                    Stage stage = (Stage) usernameTf.getScene().getWindow();

                    if (null != stage && stage.isShowing()) {
                        stage.close();
                    }

                } else {
                    customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                }


            } catch (SQLException e) {
                customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeConnection(connection, ps, null);
            }
        }
    }

    public void chooseProfilePhoto(ActionEvent actionEvent) {

        FileChooser chooser = new FileChooser();

        FileChooser.ExtensionFilter fileExtensions =
                new FileChooser.ExtensionFilter("Image files", "*.jpeg", "*.jpg", "*.png");
        chooser.getExtensionFilters().add(fileExtensions);
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(Main.primaryStage);
        if (file != null) {
            imageFile = file;
            String imagepath = file.getPath();
            Image image = new Image(imagepath);

            Circle cir2 = new Circle(119, 119, 50);
            cir2.setStroke(Color.SEAGREEN);
            cir2.setFill(Color.SNOW);
            cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
            cir2.setFill(new ImagePattern(image));

            if (operationType == OperationType.CREATE) {
                profileImg.getChildren().clear();
                profileImg.getChildren().add(cir2);
                choosePhoto.setText("Change Photo");
            } else if (operationType == OperationType.UPDATE) {
                Connection connection = null;
                PreparedStatement ps = null;

                try {

                    connection = new DBConnection().getConnection();

                    String query = """

                            UPDATE USERS SET profile_photo = pg_read_binary_file(?) where user_id = ?
                            """;
                    ps = connection.prepareStatement(query);
                    ps.setString(1, file.getAbsolutePath());
                    ps.setInt(2, userId);
                    int res = ps.executeUpdate();
                    if (res > 0) {
                        Main.primaryStage.setUserData(true);
                        profileImg.getChildren().clear();
                        profileImg.getChildren().add(cir2);

                    } else {
                        customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                    }


                } catch (SQLException e) {
                    customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
                    throw new RuntimeException(e);
                } finally {
                    DBConnection.closeConnection(connection, ps, null);
                }
            }
        }
    }

    public void login(MouseEvent mouseEvent) {

        new Main().changeScene("auth/login.fxml", "LOGIN");
    }
}
