package com.mini.orkut.controller.friend;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.FriendModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.util.Callback;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class Friends implements Initializable {
    int rowsPerPage = 15;
    public TableView<FriendModel> tableview;
    public TableColumn<FriendModel, Integer> colSrNo;
    public TableColumn<FriendModel, String> colProfileImage;
    public TableColumn<FriendModel, String> colName;
    public TableColumn<FriendModel, String> colGender;
    public TableColumn<FriendModel, String> colUsername;
    public TableColumn<FriendModel, String> colAction;
    public TextField searchTf;
    public Pagination pagination;
    private ObservableList<FriendModel> userList = FXCollections.observableArrayList();
    private FilteredList<FriendModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getAllFriends();

    }

    private void getAllFriends() {

        userList.clear();
        if (null != filteredData) {
            filteredData.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String query = "select * from friends f left join users u on f.friend_user_id = u.user_id where friend_of = ?";
            ps = connection.prepareStatement(query);
            ps.setInt(1, Login.authInformation.getUserId());


            rs = ps.executeQuery();
            while (rs.next()) {

                int id = rs.getInt("id");
                int friendOf = rs.getInt("friend_of");
                int friendUserId = rs.getInt("friend_user_id");

                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gender = rs.getString("gender");
                String email = rs.getString("email");
                String usernameStr = rs.getString("username");
                String phone = rs.getString("phone");
                String phoneCode = rs.getString("phone_code");
                String friendShipDate = rs.getString("friend_ship_date");
                InputStream profilePhoto = rs.getBinaryStream("profile_photo");

                String fullName = firstName + " " + (null == lastName ||
                        lastName.isEmpty() ? "" : lastName);

                FriendModel friendModel = new FriendModel(id, friendOf, friendUserId, usernameStr, email, gender,
                        firstName, lastName, fullName, phoneCode, phone, friendShipDate,
                        null == profilePhoto ? null : new Image(profilePhoto));


                userList.add(friendModel);
            }

            if (userList.size() > 0) {
                pagination.setVisible(true);
                search_Item();
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    public void checkFriendRequest(ActionEvent actionEvent) {

        new CustomDialog().showFxmlDialog2("friend/friendsRequestList.fxml", "FRIEND REQUEST LIST");
    }

    private void search_Item() {

        filteredData = new FilteredList<>(userList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(user -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return String.valueOf(user.getEmail()).toLowerCase().contains(lowerCaseFilter);
            });

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);
        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener((observable1, oldValue1, newValue1) -> {
            changeTableView(newValue1.intValue(), rowsPerPage);
        });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new
                ReadOnlyObjectWrapper<>(tableview.getItems().indexOf(cellData.getValue()) + 1));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, userList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<FriendModel> sortedData = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<FriendModel, String>, TableCell<FriendModel, String>> cellProfileFactory =
                (TableColumn<FriendModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {

                            Image profileImage = null;

                            if (null == tableview.getItems().get(getIndex()).getProfilePhoto()) {
                                profileImage = new ImageLoader().load("img/preview_ic.png");
                            } else {
                                profileImage = tableview.getItems().get(getIndex()).getProfilePhoto();
                            }


                            Circle cir2 = new Circle(119, 119, 20);
                            cir2.setStroke(Color.SEAGREEN);
                            cir2.setFill(Color.SNOW);
                            cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
                            cir2.setFill(new ImagePattern(profileImage));

                            cir2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {

                                    Main.primaryStage.setUserData(tableview.getItems().get(getIndex()).getFriendUserId());
                                    new CustomDialog().showFxmlDialog2("user/profile.fxml", "PROFILE");
                                }
                            });

                            HBox managebtn = new HBox(cir2);
                            managebtn.setStyle("-fx-alignment:center");
                            HBox.setMargin(cir2, new Insets(0, 0, 0, 0));

                            setGraphic(managebtn);

                            setText(null);

                        }
                    }

                };

        Callback<TableColumn<FriendModel, String>, TableCell<FriendModel, String>> cellDeleteFriendFactory =
                (TableColumn<FriendModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {

                            Button deleteFriendBn = new Button();

                            ImageView iv = new ImageView();
                            iv.setFitWidth(20);
                            iv.setFitHeight(20);
                            iv.setImage(new ImageLoader().load("img/delete_ic_white.png"));
                            deleteFriendBn.setGraphic(iv);

                            deleteFriendBn.setStyle("-fx-background-color: red; -fx-border-color:grey;" +
                                    "-fx-padding: 3 6 3 6;-fx-border-radius: 5;-fx-background-radius: 5");

                            deleteFriendBn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    tableview.getSelectionModel().select(getIndex());
                                    FriendModel friendModel = tableview.getSelectionModel().getSelectedItem();

                                    ImageView image = new ImageView(new ImageLoader().load("img/warning_ic.png"));
                                    image.setFitWidth(45);
                                    image.setFitHeight(45);
                                    Alert alert = new Alert(Alert.AlertType.NONE);
                                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Warning ");
                                    alert.setGraphic(image);
                                    alert.setHeaderText("Are you sure you want to remove " + friendModel.getFullName() + " from your friend list?");
                                    alert.initModality(Modality.APPLICATION_MODAL);
                                    alert.initOwner(Main.primaryStage);
                                    Optional<ButtonType> result = alert.showAndWait();
                                    ButtonType button = result.orElse(ButtonType.CANCEL);
                                    if (button == ButtonType.OK) {

                                        Connection connection = null;
                                        PreparedStatement ps = null;

                                        try {
                                            connection = new DBConnection().getConnection();
                                            String query = "DELETE FROM friends WHERE id = ?";

                                            ps = connection.prepareStatement(query);
                                            ps.setInt(1, friendModel.getId());

                                            int res = ps.executeUpdate();

                                            if (res > 0) {
                                                getAllFriends();
                                            }

                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            DBConnection.closeConnection(connection, ps, null);
                                        }
                                    } else {
                                        alert.close();
                                    }

                                }
                            });


                            HBox managebtn = new HBox(deleteFriendBn);
                            managebtn.setStyle("-fx-alignment:center");
                            HBox.setMargin(deleteFriendBn, new Insets(0, 0, 0, 0));

                            setGraphic(managebtn);

                            setText(null);

                        }
                    }

                };


        colProfileImage.setCellFactory(cellProfileFactory);
        colAction.setCellFactory(cellDeleteFriendFactory);
    }
}
