package com.mini.orkut.controller.feed;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.controller.post.PostType;
import com.mini.orkut.controller.user.OperationType;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.PostModel;
import com.mini.orkut.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Feeds implements Initializable {
    int rowsPerPage = 20;
    public TableView<PostModel> tableview;
    public TableColumn<PostModel, String> colAction;
    @FXML
    public TextField searchTf;

    private ObservableList<PostModel> postList = FXCollections.observableArrayList();
    private FilteredList<PostModel> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        PostType postType = (PostType) Main.primaryStage.getUserData();

        new MyAsyncTask(postType).execute();


    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private PostType postType;
        private OptionalMethod method;

        public MyAsyncTask(PostType postType) {
            this.postType = postType;
            method = new OptionalMethod();
        }

        @Override
        public void onPreExecute() {
            if (null != tableview) {
                tableview.setItems(null);
            }
            assert tableview != null;
            tableview.setPlaceholder(method.getProgressBar(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {
            getAllPost(postType);
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableview.setPlaceholder(new Label("Post not available"));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getAllPost(PostType postType) {

        postList.clear();
        if (null != filteredData) {
            filteredData.clear();
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String query = null;

            if (postType == PostType.MY_POST) {

                query = """
                        select *,to_char(p.post_time,'dd-MM-yyyy') as post_time, concat(u.first_name,' ',u.last_name) as fullName
                        from posts p
                                 left join users u on u.user_id = p.posted_by where p.posted_by = ?
                        """;

            } else if (postType == PostType.SAVED_POST) {
                query = """

                        select *,to_char(p.post_time,'dd-MM-yyyy') as post_time, concat(u.first_name,' ',u.last_name) as fullName from bookmark b
                        left join posts p on b.post_id = p.post_id
                        left join users u on u.user_id = p.posted_by  where b.user_id = ?
                         """;
            } else {
                query = """
                        select *,to_char(p.post_time,'dd-MM-yyyy') as post_time, concat(u.first_name,' ',u.last_name) as fullName
                        from posts p
                        left join users u on u.user_id = p.posted_by
                                            
                        """;
            }


            ps = connection.prepareStatement(query);

            if (postType == PostType.MY_POST || postType == PostType.SAVED_POST) {
                ps.setInt(1, Login.authInformation.getUserId());
            }

            rs = ps.executeQuery();
            while (rs.next()) {

                int postId = rs.getInt("post_id");
                int postBy = rs.getInt("posted_by");

                String fullName = rs.getString("fullName");

                String body = rs.getString("body");
                String postTime = rs.getString("post_time");

                InputStream postImage = rs.getBinaryStream("image");
                InputStream profileImage = rs.getBinaryStream("profile_photo");

                PostModel frm = new PostModel(postId, postBy, fullName, body,
                        null == postImage ? null : new Image(postImage),
                        null == profileImage ? null : new Image(profileImage), postTime);

                postList.add(frm);
            }

            if (postList.size() > 0) {
                search_Item();
            }


        } catch (SQLException e) {
            tableview.setPlaceholder(new Label("Post not available"));
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void search_Item() {

        filteredData = new FilteredList<>(postList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(post -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return String.valueOf(post.getBody()).toLowerCase().contains(lowerCaseFilter);
            });
            changeTableView(0, rowsPerPage);
        });

        changeTableView(0, rowsPerPage);
    }

    private void changeTableView(int index, int limit) {

        setOptionalCell();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, postList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<PostModel> sortedData = new SortedList<>(FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
        tableview.setSelectionModel(null);

        tableview.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PostModel item, boolean empty) {
                super.updateItem(item, empty);

                if (null == item) {
                    setStyle("-fx-background-color: white");
                } else {
                    setStyle("-fx-background-color: white;" +
                            "-fx-border-color:white white #d5d7d9 white ;");

                }
            }
        });

    }

    private void setOptionalCell() {

        Callback<TableColumn<PostModel, String>, TableCell<PostModel, String>> cellPostFactory = (TableColumn<PostModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    PostModel postModel = tableview.getItems().get(getIndex());

                    Image profileImg = null;

                    if (null == tableview.getItems().get(getIndex()).getProfileImage()) {
                        profileImg = new ImageLoader().load("img/preview_ic.png");
                    } else {
                        profileImg = tableview.getItems().get(getIndex()).getProfileImage();
                    }


                    Circle cir2 = new Circle(119, 119, 20);
                    cir2.setStroke(Color.WHITE);
                    cir2.setFill(Color.WHITE);
                    cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.WHITE));
                    cir2.setFill(new ImagePattern(profileImg));

                    Label nameL = new Label(postModel.getFullName().toUpperCase());
                    Label postTime = new Label(postModel.getPostTime());

                    // for profile,name and post time

                    VBox nameContainer = new VBox(nameL, postTime);
                    nameContainer.setAlignment(Pos.CENTER_LEFT);
                    nameContainer.setSpacing(4);
                    Button editButton = new Button();
                    editButton.setMinHeight(35);

                    editButton.setVisible(postModel.getPost_by() == Login.authInformation.getUserId());

                    editButton.setOnAction(new EventHandler<>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {

                            Map<String, Object> map = new HashMap<>();

                            map.put("type", OperationType.UPDATE);
                            map.put("data", postModel);
                            Main.primaryStage.setUserData(map);

                            new CustomDialog().showFxmlDialog2("post/createPost.fxml", "UPDATE POST");

                        }
                    });

                    ImageLoader imageLoader = new ImageLoader();

                    ImageView editIv = new ImageView(imageLoader.load("img/edit_ic.png"));
                    editIv.setFitHeight(30);
                    editIv.setFitWidth(30);
                    editIv.setPreserveRatio(true);

                    editButton.setGraphic(editIv);
                    editButton.setStyle("-fx-border-color: white;-fx-background-color: white");


                    HBox profileContainer = new HBox(cir2, nameContainer, editButton);
                    profileContainer.setAlignment(Pos.CENTER_LEFT);
                    profileContainer.setSpacing(8);
                    HBox.setHgrow(nameContainer, Priority.ALWAYS);


                    // post title

                    Text text = new Text(postModel.getBody());
                    text.wrappingWidthProperty().bind(profileContainer.widthProperty().subtract(70));

                    ImageView postImage = new ImageView(tableview.getItems().get(getIndex()).getPostImage());

                    postImage.setFitWidth(706);
                    postImage.setFitHeight(450);
                    postImage.setSmooth(true);

                    Image image = tableview.getItems().get(getIndex()).getPostImage();


                    ImageView likeImg = new ImageView(imageLoader.load("img/like_ic.png"));
                    likeImg.setFitHeight(23);
                    likeImg.setFitWidth(23);
                    likeImg.setPreserveRatio(true);

                    ImageView commentImg = new ImageView(imageLoader.load("img/comment_ic.png"));
                    commentImg.setFitHeight(24);
                    commentImg.setFitWidth(24);
                    commentImg.setPreserveRatio(true);

                    ImageView saveImg = new ImageView(imageLoader.load("img/save_ic.png"));
                    saveImg.setFitHeight(22);
                    saveImg.setFitWidth(22);
                    saveImg.setPreserveRatio(true);

                    Button likeButton = new Button();
                    Button commentButton = new Button();
                    Button saveButton = new Button("SAVE");


                    likeButton.setGraphic(likeImg);
                    commentButton.setGraphic(commentImg);
                    saveButton.setGraphic(saveImg);

                    likeButton.setStyle("-fx-background-color: white;-fx-border-color: white;");
                    commentButton.setStyle("-fx-background-color: white;-fx-border-color: white");
                    saveButton.setStyle("-fx-background-color: white;-fx-border-color: white");

                    HBox socialContainer = new HBox(likeButton, commentButton, saveButton);
                    socialContainer.setAlignment(Pos.CENTER);
                    socialContainer.setSpacing(100);

                    Separator separator = new Separator();
                    separator.setOrientation(Orientation.HORIZONTAL);

                    VBox container2 = new VBox(profileContainer, text, null == image ? new HBox() : postImage
                            , separator, socialContainer);
                    container2.setSpacing(10);

                    CustomDialog customDialog = new CustomDialog();

                    boolean checkIsLiked = checkLiked(postModel.getPostId());

                    if (checkIsLiked) {
                        Image likedImage = new ImageLoader().load("img/liked_icon.png");
                        likeImg.setImage(likedImage);
                    }

                    boolean checkIsSaved = checkPostIsSaved(postModel.getPostId());

                    if (checkIsSaved) {
                        Image img = new ImageLoader().load("img/saved_ic.png");
                        saveImg.setImage(img);
                    }

                    likeButton.setContentDisplay(ContentDisplay.TOP);
                    commentButton.setContentDisplay(ContentDisplay.TOP);
                    saveButton.setContentDisplay(ContentDisplay.TOP);

                    likeCount(postModel.getPostId(), likeButton);
                    bookmarkCount(postModel.getPostId(), saveButton);
                    commentCount(postModel.getPostId(), commentButton);


                    likeButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            boolean checkIsLiked = checkLiked(postModel.getPostId());

                            if (checkIsLiked) {
                                removeLike(postModel, likeImg, likeButton);
                            } else {
                                addLike(postModel, likeImg, likeButton);
                            }

                        }
                    });

                    saveButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {

                            if (checkPostIsSaved(postModel.getPostId())) {
                                removeFromBookmark(postModel, saveImg, saveButton);
                            } else {
                                addToBookmark(postModel, saveImg, saveButton);
                            }

                        }
                    });

                    commentButton.setOnAction(actionEvent -> {
                        Main.primaryStage.setUserData(postModel.getPostId());
                        customDialog.showFxmlDialog2("post/comment.fxml", "");

                        if (Main.primaryStage.getUserData() instanceof Boolean) {
                            boolean isCommentAdded = (boolean) Main.primaryStage.getUserData();
                            if (isCommentAdded) {
                                commentCount(postModel.getPostId(), commentButton);
                            }

                        }
                    });

                    VBox mainContainer = new VBox(container2);
                    mainContainer.setMinWidth(750);

                    mainContainer.setStyle("-fx-border-color: white;-fx-background-color:white;" +
                            "-fx-effect:  dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);-fx-padding:10 20 10 20;" +
                            "-fx-border-radius: 5;-fx-background-radius: 5");
                    HBox managebtn = new HBox(mainContainer);

                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(cir2, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellPostFactory);

    }

    private void addToBookmark(PostModel postModel, ImageView saveImg, Button saveButton) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "INSERT INTO BOOKMARK(post_id, user_id) VALUES (?,?)";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postModel.getPostId());
            ps.setInt(2, Login.authInformation.getUserId());

            int res = ps.executeUpdate();

            if (res > 0) {
                Image likedImage = new ImageLoader().load("img/saved_ic.png");
                saveImg.setImage(likedImage);
                bookmarkCount(postModel.getPostId(), saveButton);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private void removeFromBookmark(PostModel postModel, ImageView saveImg, Button saveButton) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "DELETE FROM bookmark WHERE POST_ID = ? AND user_id = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postModel.getPostId());
            ps.setInt(2, Login.authInformation.getUserId());

            int res = ps.executeUpdate();

            if (res > 0) {
                Image likeImage = new ImageLoader().load("img/save_ic.png");
                saveImg.setImage(likeImage);
                bookmarkCount(postModel.getPostId(), saveButton);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }

    }

    private void likeCount(int postId, Button likeButton) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select count(POST_ID) as totalLiked from like_post where POST_ID = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postId);
            rs = ps.executeQuery();

            if (rs.next()) {

                long totalLike = rs.getLong("totalLiked");
                likeButton.setText("LIKE(" + totalLike + ")");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void bookmarkCount(int postId, Button likeButton) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select count(POST_ID) as total from bookmark where POST_ID = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postId);
            rs = ps.executeQuery();

            if (rs.next()) {

                long total = rs.getLong("total");
                likeButton.setText("SAVE(" + total + ")");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void commentCount(int postId, Button commentButton) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "select count(POST_ID) as total from comment where POST_ID = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postId);
            rs = ps.executeQuery();

            if (rs.next()) {

                long total = rs.getLong("total");
                commentButton.setText("COMMENT(" + total + ")");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }
    }

    private void addLike(PostModel postModel, ImageView likeImg, Button likeButton) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "INSERT INTO like_post(post_id, like_by) VALUES (?,?)";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postModel.getPostId());
            ps.setInt(2, Login.authInformation.getUserId());

            int res = ps.executeUpdate();

            if (res > 0) {
                Image likedImage = new ImageLoader().load("img/liked_icon.png");
                likeImg.setImage(likedImage);
                likeCount(postModel.getPostId(), likeButton);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private void removeLike(PostModel postModel, ImageView likeImg, Button likeButton) {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = new DBConnection().getConnection();

            String qry = "DELETE FROM LIKE_POST WHERE POST_ID = ? AND LIKE_BY = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postModel.getPostId());
            ps.setInt(2, Login.authInformation.getUserId());

            int res = ps.executeUpdate();

            if (res > 0) {
                Image likeImage = new ImageLoader().load("img/like_ic.png");
                likeImg.setImage(likeImage);
                likeCount(postModel.getPostId(), likeButton);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }
    }

    private boolean checkPostIsSaved(int postId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "SELECT user_id  FROM bookmark WHERE POST_ID = ? and user_id = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postId);
            ps.setInt(2, Login.authInformation.getUserId());

            rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }


    private boolean checkLiked(int postId) {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String qry = "SELECT LIKE_BY FROM LIKE_POST WHERE POST_ID = ? and LIKE_BY = ?";
            ps = connection.prepareStatement(qry);
            ps.setInt(1, postId);
            ps.setInt(2, Login.authInformation.getUserId());

            rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }


}
