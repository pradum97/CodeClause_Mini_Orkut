package com.mini.orkut.controller.friend;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.UserModel;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class SendFriendRequest implements Initializable {
    int rowsPerPage = 15;
    public TableView<UserModel> tableview;
    public TableColumn<UserModel, Integer> colSrNo;
    public TableColumn<UserModel, String> colProfileImage;
    public TableColumn<UserModel, String> colName;
    public TableColumn<UserModel, String> colGender;
    public TableColumn<UserModel, String> colUsername;
    public TableColumn<UserModel, String> colAction;
    public TextField searchTf;
    public Pagination pagination;

    private ObservableList<UserModel> userList = FXCollections.observableArrayList();
    private FilteredList<UserModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        getAllUser();
    }

    private static String getCheckFriend(int userID) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer ids = new StringBuffer();

        try {

            connection = new DBConnection().getConnection();

            String query = """

                    select coalesce(friend_user_id,0)as friend_user_id from friends where friend_of = ?
                                                      
                     """;
            ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();

            while (rs.next()) {

                int friendUserID = rs.getInt("friend_user_id");

                ids.append(friendUserID);

                if (!rs.isLast()) {
                    ids.append(",");
                }

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return ids.toString();

    }

    private static String getFriendRequest(int userID) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer ids = new StringBuffer();

        try {

            connection = new DBConnection().getConnection();

            String query = """

                    select concat(request_to,',') as request_to from friend_request where request_by = ?
                                                       
                      """;
            ps = connection.prepareStatement(query);
            ps.setInt(1, userID);
            rs = ps.executeQuery();

            while (rs.next()) {

                String request_to = rs.getString("request_to");

                ids.append(request_to);

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

        return ids.toString();

    }

    private static String rePlaceFirst(String str) {

        if (null == str || str.isEmpty()){
            return String.valueOf(0);
        }else {
            if (String.valueOf(str.charAt(0)).equals(",")) {
                str = str.substring(1);
            }

            return str;
        }

    }

    private static String replaceLast(String str) {

        if (null == str || str.isEmpty()){
            return String.valueOf(0);
        }else {
            int strLen = str.length();
            if (String.valueOf(str.charAt(strLen-1)).equals(",")){
                return str.substring(0,strLen-1);
            }else {
                return str;
            }

        }
    }

    private void getAllUser() {

        userList.clear();
        if (null != filteredData) {
            filteredData.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {

            connection = new DBConnection().getConnection();

            int currentLoginId = Login.authInformation.getUserId();

            String requestData = getFriendRequest(currentLoginId);
            String friendData = getCheckFriend(currentLoginId);

            String txt = rePlaceFirst((replaceLast((((requestData + "," + friendData)+","+currentLoginId)
                    .replaceAll(",,", ","))))).replaceAll(",,",",");

            String query = "select * from users u where user_id not in(" + txt + ")";
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {

                int userId = rs.getInt("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gender = rs.getString("gender");
                String email = rs.getString("email");
                String usernameStr = rs.getString("username");
                String phone = rs.getString("phone");
                String phoneCode = rs.getString("phone_code");
                String createdDate = rs.getString("created_date");
                InputStream profilePhoto = rs.getBinaryStream("profile_photo");

                String fullName = firstName + " " + (null == lastName ||
                        lastName.isEmpty() ? "" : lastName);

                UserModel userModel = new UserModel(userId, usernameStr, email, gender,
                        firstName, lastName, phoneCode, phone, createdDate, fullName,
                        null == profilePhoto ? null : new Image(profilePhoto));
                userList.add(userModel);
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
        SortedList<UserModel> sortedData = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
    }

    private void setOptionalCell() {

        Callback<TableColumn<UserModel, String>, TableCell<UserModel, String>> cellProfileFactory =
                (TableColumn<UserModel, String> param) -> new TableCell<>() {
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


                            Circle cir2 = new Circle(119, 119, 25);
                            cir2.setStroke(Color.SEAGREEN);
                            cir2.setFill(Color.SNOW);
                            cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.DARKSEAGREEN));
                            cir2.setFill(new ImagePattern(profileImage));

                            cir2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {

                                    Main.primaryStage.setUserData( tableview.getItems().get(getIndex()).getUserId());
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

        Callback<TableColumn<UserModel, String>, TableCell<UserModel, String>> cellActionFactory =
                (TableColumn<UserModel, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);

                        } else {

                            Button sendRequest = new Button("SEND REQUEST");
                            sendRequest.setStyle("-fx-background-color: #003366; -fx-background-radius: 100;-fx-text-fill: white;" +
                                    "-fx-padding: 3 8 3 8");

                            sendRequest.setOnAction(new EventHandler<>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    tableview.getSelectionModel().select(getIndex());
                                    UserModel userModel = tableview.getSelectionModel().getSelectedItem();

                                    Connection connection = null;
                                    PreparedStatement ps = null;

                                    try {
                                        connection = new DBConnection().getConnection();
                                        String query = "INSERT INTO FRIEND_REQUEST(REQUEST_TO, REQUEST_BY, REQUEST_NOTES) VALUES (?,?,?)";

                                        ps = connection.prepareStatement(query);
                                        ps.setInt(1, userModel.getUserId());
                                        ps.setInt(2, Login.authInformation.getUserId());
                                        ps.setNull(3, Types.NULL);

                                        int res = ps.executeUpdate();

                                        if (res > 0) {
                                            getAllUser();
                                        }

                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    } finally {
                                        DBConnection.closeConnection(connection, ps, null);
                                    }
                                }
                            });


                            HBox managebtn = new HBox(sendRequest);
                            managebtn.setStyle("-fx-alignment:center");
                            HBox.setMargin(sendRequest, new Insets(0, 0, 0, 0));

                            setGraphic(managebtn);

                            setText(null);

                        }
                    }

                };

        colProfileImage.setCellFactory(cellProfileFactory);
        colAction.setCellFactory(cellActionFactory);
    }
}
