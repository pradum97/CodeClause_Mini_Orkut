package com.mini.orkut.controller.auth;


import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.user.OperationType;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.AuthInformation;
import com.mini.orkut.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Login implements Initializable {

    public TextField usernameTf;
    public TextField passwordTf;
    public Button login_button;
    public HBox passwordContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    public static AuthInformation authInformation;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();


        passwordMaskFiled();
    }

    public void passwordMaskFiled() {

        usernameTf.setFocusTraversable(true);
        passwordTf = new TextField();
        passwordTf.setManaged(false);
        passwordTf.setVisible(false);
        final PasswordField passwordField = new PasswordField();
        ImageView icon = new ImageView(new ImageLoader().load("img/showPassword.png"));
        icon.setFitHeight(26);
        icon.setFitWidth(26);
        icon.setStyle("-fx-cursor: hand");

        passwordField.prefHeight(passwordTf.getPrefHeight());
        passwordField.prefWidth(passwordTf.getPrefWidth());

        passwordField.setPromptText("Enter password");
        passwordTf.setPromptText("Enter password");

        passwordField.setMinWidth(385);
        passwordField.setMinHeight(46);

        passwordTf.setMinWidth(385);
        passwordTf.setMinHeight(46);

        passwordTf.managedProperty().bind(icon.pressedProperty());
        passwordTf.visibleProperty().bind(icon.pressedProperty());

        HBox.setMargin(icon, new Insets(0, 8, 0, 0));
        passwordField.setStyle("-fx-background-radius: 4;-fx-border-radius:4;-fx-border-color: white;-fx-background-color: white;-fx-font-size: 14;-fx-font-family: Arial");
        passwordTf.setStyle("-fx-background-radius: 4;-fx-border-radius:4;-fx-border-color: white;-fx-background-color: white;-fx-font-size: 14;-fx-font-family: Arial");

        passwordField.managedProperty().bind(icon.pressedProperty().not());
        passwordField.visibleProperty().bind(icon.pressedProperty().not());
        passwordTf.textProperty().bindBidirectional(passwordField.textProperty());
        passwordContainer.getChildren().addAll(passwordTf, passwordField, icon);

    }

    public void forget_password_bn(ActionEvent event) {
        customDialog.showFxmlDialog2("auth/forgotPassword.fxml", "FORGOT PASSWORD");
    }

    public void enterPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            callThread();
        }
    }

    public void loginBn(ActionEvent event) {
        callThread();
    }

    private void callThread() {

        String username = usernameTf.getText();
        String password = passwordTf.getText();

        if (username.isEmpty()) {
            method.show_popup("Please enter username", usernameTf);
            return;
        } else if (password.isEmpty()) {
            method.show_popup("Please enter password", passwordContainer);
            return;
        }


        MyAsyncTask myAsyncTask = new MyAsyncTask(username, password);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

    }

    public void createAccount(MouseEvent mouseEvent) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", OperationType.CREATE);

        Main.primaryStage.setUserData(map);
        new Main().changeScene("auth/signup.fxml", "CREATE NEW ACCOUNT");
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        String username;
        String password;

        public MyAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {

            startLogin(username, password);

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void startLogin(String username, String password) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String query = "select * from users where username = ? and password = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);

            rs = ps.executeQuery();

            if (rs.next()) {

                int userId = rs.getInt("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gender = rs.getString("gender");
                String email = rs.getString("email");
                String usernameStr = rs.getString("username");
                String phone = rs.getString("phone");
                String phoneCode = rs.getString("phone_code");
                String createdDate = rs.getString("created_date");

                InputStream is = rs.getBinaryStream("profile_photo");

                authInformation = new AuthInformation(userId, usernameStr, email, gender,
                        firstName, lastName, phoneCode, phone, createdDate, is);

                Platform.runLater(() -> new Main().changeScene("dashboard.fxml", "FEED"));


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
}
