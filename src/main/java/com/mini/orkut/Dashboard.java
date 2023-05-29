package com.mini.orkut;

import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.controller.post.PostType;
import com.mini.orkut.controller.user.OperationType;
import com.mini.orkut.model.AuthInformation;
import com.mini.orkut.util.OptionalMethod;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dashboard extends OptionalMethod implements Initializable {

    public Button logoutBn;

    public Label fullName;
    public Label username;
    public StackPane mainContainer;
    public HBox topProfileImg;
    public Separator topSeparator;
    public Button profileBn;
    public Button friendRequestBn;
    public Button friendsBn;
    public Button feedBn;
    public Button savedBn;
    public Button myPostBn;
    public Button viewNotificationBn;

    @FXML
    ImageView hideIv, showIv;
    public VBox menuContainer, topUserContainer;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        feedBnClick(null);

        customDialog = new CustomDialog();
        config();

        Platform.runLater(() -> {
            Stage stage = (Stage) logoutBn.getScene().getWindow();

            stage.setMinWidth(1120);
            stage.setMinHeight(655);

        });

        setUserData();
    }

    private void setUserData() {

        AuthInformation authInformation = Login.authInformation;

        String name = authInformation.getFirstName() + " " + (null == authInformation.getLastName() ||
                authInformation.getLastName().isEmpty() ? "" : authInformation.getLastName());

        fullName.setText(name);
        username.setText(authInformation.getUsername());
        Image image;

        if (null == authInformation.getProfilePhoto()) {
            image = new ImageLoader().load("img/preview_ic.png");
        } else {

            image = new Image(authInformation.getProfilePhoto());

        }

        Circle cir2 = new Circle(119, 119, 50);
        cir2.setStroke(Color.SEAGREEN);
        cir2.setFill(Color.SNOW);
        cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
        cir2.setFill(new ImagePattern(image));
        topProfileImg.getChildren().add(cir2);
    }

    private void config() {
        hideElement(showIv);
        hideMenu(null);
    }


    public void hideMenu(MouseEvent mouseEvent) {
        feedBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        friendRequestBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        friendsBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        profileBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logoutBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        savedBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        myPostBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        showIv.setVisible(true);
        hideElement(topProfileImg, fullName, username, topSeparator, hideIv);
        menuContainer.setStyle("-fx-padding:0 10 0 10");
    }

    public void showMenu(MouseEvent mouseEvent) {
        feedBn.setContentDisplay(ContentDisplay.LEFT);
        friendsBn.setContentDisplay(ContentDisplay.LEFT);
        friendRequestBn.setContentDisplay(ContentDisplay.LEFT);
        profileBn.setContentDisplay(ContentDisplay.LEFT);
        logoutBn.setContentDisplay(ContentDisplay.LEFT);
        savedBn.setContentDisplay(ContentDisplay.LEFT);
        myPostBn.setContentDisplay(ContentDisplay.LEFT);

        topProfileImg.setVisible(true);
        fullName.setVisible(true);
        username.setVisible(true);
        topSeparator.setVisible(true);

        hideIv.setVisible(true);
        menuContainer.setStyle("-fx-padding: 0 10 0 10");
        hideElement(showIv);
    }


    void toggleButtonSize(Node node) {

        switch (node.getId()) {
            case "profile" -> {
                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: green;" +
                        "-fx-font-size: 13;-fx-background-radius:30;-fx-border-radius: 30;-fx-text-fill: white;" +
                        "-fx-font-weight: bold;-fx-cursor: hand");
            }
            case "edit" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #006666;-fx-font-size: 13;" +
                        "-fx-border-radius: 30;-fx-background-radius:30;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
            }

            case "changePassword" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #7e0101;-fx-font-size: 13;" +
                        "-fx-border-radius: 30;-fx-background-radius:30;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
            }

        }

    }

    public void logoutBnClick(ActionEvent event) {

        ImageView image = new ImageView(new ImageLoader().load("img/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to logout?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            new Main().changeScene("auth/login.fxml", "LOGIN HERE");
            Login.authInformation = null;
        } else {
            alert.close();
        }
    }

    public void profileBnClick(ActionEvent actionEvent) {
        Main.primaryStage.setUserData(Login.authInformation.getUserId());
        customDialog.showFxmlDialog2("user/profile.fxml", "PROFILE");
    }

    public void friendRequestBnClick(ActionEvent actionEvent) {

        changeTitle("FRIENDS REQUESTS");

        unselectedBg(feedBn, friendsBn, profileBn, savedBn, myPostBn);
        selectedBg(friendRequestBn);

        Main.primaryStage.setUserData(null);
        replaceScene("friend/sendFriendRequest.fxml");
    }

    public void friendsBnClick(ActionEvent actionEvent) {

        changeTitle("FRIENDS");

        unselectedBg(feedBn, profileBn, friendRequestBn, savedBn, myPostBn);
        selectedBg(friendsBn);

        Main.primaryStage.setUserData(null);
        replaceScene("friend/friends.fxml");
    }

    public void feedBnClick(ActionEvent actionEvent) {

        changeTitle("FEED");

        unselectedBg(friendsBn, profileBn, friendRequestBn, savedBn, myPostBn);
        selectedBg(feedBn);

        Main.primaryStage.setUserData(PostType.ALL_POST);
        replaceScene("feed/feeds.fxml");
    }

    public void savedBnClick(ActionEvent actionEvent) {

        changeTitle("SAVED POST");

        unselectedBg(friendsBn, profileBn, friendRequestBn, feedBn, myPostBn);
        selectedBg(savedBn);

        Main.primaryStage.setUserData(PostType.SAVED_POST);
        replaceScene("feed/feeds.fxml");

    }

    public void myPostBnClick(ActionEvent actionEvent) {

        changeTitle("SAVED POST");

        unselectedBg(friendsBn, profileBn, friendRequestBn, feedBn, savedBn);
        selectedBg(myPostBn);

        Main.primaryStage.setUserData(PostType.MY_POST);
        replaceScene("feed/feeds.fxml");
    }


    void unselectedBg(Node... nodes) {
        for (Node node : nodes) {
            node.setStyle("-fx-background-color: transparent");
            node.setDisable(false);
        }
    }

    void selectedBg(Node node) {
        node.setDisable(true);
        node.setStyle("""
                 -fx-border-radius: 4;
                    -fx-background-color: #006666;
                    -fx-text-fill: white;
                """);
    }


    private void changeTitle(String str) {
        Main.primaryStage.setTitle(AppConfig.APPLICATION_NAME + "( " + "DASHBOARD->" + str + " )");
    }

    private void replaceScene(String fxml_file_name) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml_file_name)));
            mainContainer.getChildren().removeAll();
            mainContainer.getChildren().setAll(parent);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    public void createNewPostClick(ActionEvent actionEvent) {

        Map<String,Object> map = new HashMap<>();

        map.put("type", OperationType.CREATE);
        map.put("data", null);
        Main.primaryStage.setUserData(map);

        customDialog.showFxmlDialog2("post/createPost.fxml", "CREATE NEW POST");
    }

    public void viewNotificationBnClick(ActionEvent actionEvent) {


        customDialog.showFxmlDialog2("post/notifications.fxml","Notifications");
    }
}
