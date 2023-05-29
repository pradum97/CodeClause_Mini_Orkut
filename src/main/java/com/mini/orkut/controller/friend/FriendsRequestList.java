package com.mini.orkut.controller.friend;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.FriendRequestModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FriendsRequestList implements Initializable {
    int rowsPerPage = 15;
    public TableView<FriendRequestModel> tableview;
    public TableColumn<FriendRequestModel, String> colProfileImage;
    public TableColumn<FriendRequestModel, String> colName;
    public TableColumn<FriendRequestModel, String> colGender;
    public TableColumn<FriendRequestModel, String> colAction;
    public TextField searchTf;
    public Pagination pagination;

    private ObservableList<FriendRequestModel> userList = FXCollections.observableArrayList();
    private FilteredList<FriendRequestModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getAllAllFriendsRequest();
    }

    private void getAllAllFriendsRequest() {

        userList.clear();
        if (null != filteredData) {
            filteredData.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {

            connection = new DBConnection().getConnection();

            String query = "select * from friend_request fr left join users u on fr.request_by =u.user_id where fr.request_to = ?";
            ps = connection.prepareStatement(query);
            ps.setInt(1, Login.authInformation.getUserId());

            rs = ps.executeQuery();
            while (rs.next()) {

                int request_id = rs.getInt("request_id");
                int request_to = rs.getInt("request_to");
                int request_by = rs.getInt("request_by");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gender = rs.getString("gender");
                InputStream profilePhoto = rs.getBinaryStream("profile_photo");

                String fullName = firstName + " " + (null == lastName ||
                        lastName.isEmpty() ? "" : lastName);

                FriendRequestModel frm = new FriendRequestModel(request_id, null == profilePhoto ? null : new Image(profilePhoto),
                        fullName, gender, request_to, request_by);


                userList.add(frm);
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

    private void search_Item() {

        filteredData = new FilteredList<>(userList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(user -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return String.valueOf(user.getName()).toLowerCase().contains(lowerCaseFilter);
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

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, userList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<FriendRequestModel> sortedData = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<FriendRequestModel, String>, TableCell<FriendRequestModel, String>> cellProfileFactory =
                (TableColumn<FriendRequestModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {

                            Image profileImage = null;

                            if (null == tableview.getItems().get(getIndex()).getProfileImage()) {
                                profileImage = new ImageLoader().load("img/preview_ic.png");
                            } else {
                                profileImage = tableview.getItems().get(getIndex()).getProfileImage();
                            }


                            Circle cir2 = new Circle(119, 119, 25);
                            cir2.setStroke(Color.SEAGREEN);
                            cir2.setFill(Color.SNOW);
                            cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
                            cir2.setFill(new ImagePattern(profileImage));

                            cir2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {

                                    Main.primaryStage.setUserData(tableview.getItems().get(getIndex()).getRequest_by());
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

        Callback<TableColumn<FriendRequestModel, String>, TableCell<FriendRequestModel, String>> cellActionFactory =
                (TableColumn<FriendRequestModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {

                            Button removeRequest = new Button("REMOVE");
                            Button acceptRequest = new Button("ACCEPT");

                            removeRequest.setStyle("-fx-background-color: red; -fx-background-radius: 100;-fx-text-fill: white;" +
                                    "-fx-padding: 3 8 3 8");

                            acceptRequest.setStyle("-fx-background-color: green; -fx-background-radius: 100;-fx-text-fill: white;" +
                                    "-fx-padding: 3 8 3 8");

                            removeRequest.setOnAction(actionEvent -> {
                                tableview.getSelectionModel().select(getIndex());
                                FriendRequestModel friendRequestModel = tableview.getSelectionModel().getSelectedItem();

                                Connection connection = null;
                                PreparedStatement ps = null;

                                try {
                                    connection = new DBConnection().getConnection();
                                    String query = "DELETE FROM FRIEND_REQUEST WHERE request_id = ?";

                                    ps = connection.prepareStatement(query);
                                    ps.setInt(1, friendRequestModel.getRequestId());

                                    int res = ps.executeUpdate();

                                    if (res > 0) {
                                        getAllAllFriendsRequest();
                                    }

                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    DBConnection.closeConnection(connection, ps, null);
                                }
                            });

                            acceptRequest.setOnAction(actionEvent -> {
                                tableview.getSelectionModel().select(getIndex());
                                FriendRequestModel friendRequestModel = tableview.getSelectionModel().getSelectedItem();

                                Connection connection = null;
                                PreparedStatement ps = null;

                                try {
                                    connection = new DBConnection().getConnection();
                                    connection.setAutoCommit(false);
                                    String query = "INSERT INTO FRIENDS(FRIEND_OF, FRIEND_USER_ID) VALUES (?,?)";

                                    ps = connection.prepareStatement(query);
                                    ps.setInt(1, friendRequestModel.getRequest_to());
                                    ps.setInt(2, friendRequestModel.getRequest_by());

                                    int res = ps.executeUpdate();

                                    if (res > 0) {
                                        ps = null;
                                        res = 0;

                                        String qry = "DELETE FROM FRIEND_REQUEST WHERE request_id = ?";

                                        ps = connection.prepareStatement(qry);
                                        ps.setInt(1, friendRequestModel.getRequestId());
                                        res = ps.executeUpdate();

                                        if (res>0) {
                                            connection.commit();
                                            getAllAllFriendsRequest();
                                        }
                                    }

                                } catch (SQLException e) {
                                    DBConnection.rollBack(connection);
                                    throw new RuntimeException(e);
                                } finally {
                                    DBConnection.closeConnection(connection, ps, null);
                                }
                            });


                            HBox managebtn = new HBox(removeRequest, acceptRequest);
                            managebtn.setStyle("-fx-alignment:center");
                            HBox.setMargin(acceptRequest, new Insets(0, 0, 0, 20));

                            setGraphic(managebtn);

                            setText(null);

                        }
                    }

                };

        colProfileImage.setCellFactory(cellProfileFactory);
        colAction.setCellFactory(cellActionFactory);
    }
}
