package com.mini.orkut.controller.user;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.AuthInformation;
import com.mini.orkut.util.OptionalMethod;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class Profile implements Initializable {
    public HBox profilePhotoImg;
    public Label fullNameL, genderL;
    public Label emailL;
    public Label usernameL;
    public Label phoneL;
    public Label createdDateL;
    public Button editBn;

    private OptionalMethod method;
    private CustomDialog customDialog;
    int userId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

         userId = (int) Main.primaryStage.getUserData();

        if (userId < 1) {

            Platform.runLater(() -> {

                Stage stage = (Stage) fullNameL.getScene().getWindow();

                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
            });

        } else {
            getUserDataById(userId);
        }
        editBn.setVisible(userId == Login.authInformation.getUserId());

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


        String fullName = authInfo.getFirstName() + " " +( null == authInfo.getLastName() ||
                authInfo.getLastName().isEmpty() ? "" : authInfo.getLastName());

        fullNameL.setText(fullName);
        genderL.setText(authInfo.getGender());
        emailL.setText(authInfo.getEmail());
        usernameL.setText(authInfo.getUsername());
        createdDateL.setText(authInfo.getCreatedDate());

        if (!Objects.equals(authInfo.getPhone(), "")  || !authInfo.getPhone().isEmpty()){
            phoneL.setText(("+"+authInfo.getPhoneCode()+"-"+ authInfo.getPhone()));
        }

        Image image ;

        if (null == authInfo.getProfilePhoto()){
             image = new ImageLoader().load("img/preview_ic.png");
        }else {

            image = new Image(authInfo.getProfilePhoto());

        }

        Circle cir2 = new Circle(119,119,50);
        cir2.setStroke(Color.SEAGREEN);
        cir2.setFill(Color.SNOW);
        cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
        cir2.setFill(new ImagePattern(image));
        profilePhotoImg.getChildren().clear();
        profilePhotoImg.getChildren().add(cir2);
    }

    public void editProfile(ActionEvent actionEvent) {

        Map<String,Object> map = new HashMap<>();
        map.put("user_id",userId);
        map.put("type",OperationType.UPDATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlDialog2("auth/signup.fxml", "UPDATE PROFILE");

        if (Main.primaryStage.getUserData() instanceof Boolean){

            if ((boolean)Main.primaryStage.getUserData()){
                getUserDataById(userId);
            }
        }
    }


}
