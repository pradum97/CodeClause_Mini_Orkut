package com.mini.orkut.controller.post;

import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.CommentModel;
import com.mini.orkut.util.OptionalMethod;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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
import java.util.ResourceBundle;

public class Comment implements Initializable {
    public TableView<CommentModel> tableview;
    public TableColumn<CommentModel, String> colComment;
    public TextField commentTf;
    private int postId;
    private ObservableList<CommentModel> commentList = FXCollections.observableArrayList();
    private OptionalMethod method;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();

        postId = (int) Main.primaryStage.getUserData();

        getAllComment();
    }

    public void sendCommentBnClick(ActionEvent actionEvent) {

        String commentStr = commentTf.getText();
        if (commentStr.isEmpty()) {
            ContextMenu form_Validator = new ContextMenu();
            form_Validator.setAutoHide(true);
            form_Validator.getItems().add(new MenuItem("Please write comment first."));
            form_Validator.show((Node) commentTf, Side.TOP, 10, 0);
            return;
        }


        Connection connection = null;
        PreparedStatement ps = null;

        try {

            connection = new DBConnection().getConnection();
            String query = """
                    
                    INSERT INTO COMMENT(POST_ID, COMMENT_TEXT, COMMENT_BY) VALUES (?,?,?)
                    
                    """;

            ps = connection.prepareStatement(query);
            ps.setInt(1,postId);
            ps.setString(2,commentStr);
            ps.setInt(3, Login.authInformation.getUserId());

            int res = ps.executeUpdate();

            if (res > 0){
                commentTf.setText("");
                getAllComment();
                Main.primaryStage.setUserData(true);
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    private void getAllComment() {

        commentList.clear();
        tableview.setItems(null);
        tableview.refresh();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            connection = new DBConnection().getConnection();

            String query = """
                    select *,to_char(c.comment_time,'dd-MM-yyyy') as comment_time, concat(u.first_name,' ',u.last_name) as fullName
                    from comment c
                             left join users u on u.user_id = c.comment_by where c.post_id = ?
                                        
                    """;
            ps = connection.prepareStatement(query);
            ps.setInt(1, postId);

            rs = ps.executeQuery();
            while (rs.next()) {

                int commentId = rs.getInt("comment_id");
                int commentBy = rs.getInt("comment_by");

                String fullName = rs.getString("fullName");

                String comment = rs.getString("comment_text");
                String commentTime = rs.getString("comment_time");

                InputStream profileImage = rs.getBinaryStream("profile_photo");

                CommentModel commentModel = new CommentModel(commentId, commentBy, fullName,
                        null == profileImage ? null : new Image(profileImage), comment, commentTime);

                commentList.add(commentModel);

            }
            tableview.setItems(commentList);
            setOptionalCell();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void setOptionalCell() {

        Callback<TableColumn<CommentModel, String>, TableCell<CommentModel, String>> cellPostFactory = (TableColumn<CommentModel, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    CommentModel commentModel = tableview.getItems().get(getIndex());

                    Image profileImg = null;

                    if (null == commentModel.getCommentByProfileImage()) {
                        profileImg = new ImageLoader().load("img/preview_ic.png");
                    } else {
                        profileImg = commentModel.getCommentByProfileImage();
                    }


                    Circle cir2 = new Circle(119, 119, 20);
                    cir2.setStroke(Color.WHITE);
                    cir2.setFill(Color.WHITE);
                    cir2.setEffect(new DropShadow(+25d, 0d, +2d, Color.WHITE));
                    cir2.setFill(new ImagePattern(profileImg));

                    Label nameL = new Label(commentModel.getCommentByName());
                    Label postTime = new Label(commentModel.getCommentDate());

                    // for profile,name and comment time

                    VBox nameContainer = new VBox(nameL, postTime);
                    nameContainer.setAlignment(Pos.CENTER_LEFT);
                    nameContainer.setSpacing(4);

                    HBox profileContainer = new HBox(cir2, nameContainer);
                    profileContainer.setAlignment(Pos.CENTER_LEFT);
                    profileContainer.setSpacing(8);

                    // post title

                    Text text = new Text(commentModel.getComment());
                    text.wrappingWidthProperty().bind(profileContainer.widthProperty().subtract(40));


                    VBox container2 = new VBox(profileContainer,new Separator(Orientation.HORIZONTAL), text);
                    container2.setSpacing(10);


                    VBox mainContainer = new VBox(container2);
                    mainContainer.setMinWidth(400);

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

        colComment.setCellFactory(cellPostFactory);
    }

    public void keyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER){
            sendCommentBnClick(null);
        }
    }
}
