package com.mini.orkut.controller.post;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.ImageLoader;
import com.mini.orkut.Main;
import com.mini.orkut.controller.auth.Login;
import com.mini.orkut.controller.user.OperationType;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.model.PostModel;
import com.mini.orkut.util.OptionalMethod;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class CreatePost implements Initializable {
    public ImageView postImage;
    public Button chooseImageBn;
    public TextArea postBody;
    public Label titleL;
    public Button submitButton;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private String imagePath;

   private OperationType operationType;
   private PostModel postModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

        Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
        operationType = (OperationType) map.get("type");

        if (operationType == OperationType.UPDATE){
            titleL.setText("UPDATE POST");
            submitButton.setText("UPDATE");

             postModel = (PostModel) map.get("data");

            setData(postModel);
        }

    }

    private void setData(PostModel data) {
        postBody.setText(data.getBody());

        Image image ;

        if (null == data.getPostImage()){
            image = new ImageLoader().load("img/preview_squre_ic.png");
        }else {
            image = data.getPostImage();
        }

        postImage.setImage(image);

        saveToFile(image);
    }

    public static void main(String[] args) {

        System.out.println(getTempFile());
    }

    public static String getTempFile() {
        String folderLocation = System.getenv("temp");
        String fileName = "img.jpg";
        File temp = new File(folderLocation + File.separator + fileName);
        return temp.getAbsolutePath();
    }

    public void saveToFile(Image image) {

        String tempPath = getTempFile();
        imagePath = tempPath;
        File outputFile = new File(tempPath);
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "jpg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chooseImageClick(ActionEvent actionEvent) {

        FileChooser chooser = new FileChooser();

        FileChooser.ExtensionFilter fileExtensions =
                new FileChooser.ExtensionFilter("Image files", "*.jpeg", "*.jpg", "*.png");
        chooser.getExtensionFilters().add(fileExtensions);
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(Main.primaryStage);
        if (file != null) {
            imagePath = file.getAbsolutePath();
            String imagepath = file.getPath();
            Image image = new Image(imagepath);
            postImage.setImage(image);
            chooseImageBn.setText("Change Image");
        }
    }

    public void resetClick(ActionEvent actionEvent) {
        postBody.setText("");
        imagePath = null;
        postImage.setImage(new ImageLoader().load("img/preview_ic.png"));
    }

    public void submitClick(ActionEvent actionEvent) {
        String body = postBody.getText();


        if (body.isEmpty() && null == imagePath) {
            method.show_popup("Please write something or select image !!", postBody);
            return;
        }

        Connection connection = null;
        PreparedStatement ps = null;

        if (operationType == OperationType.CREATE){
            try {
                connection = new DBConnection().getConnection();

                String query = "INSERT INTO POSTS(BODY, IMAGE, POSTED_BY) VALUES (?,?,?)";
                ps = connection.prepareStatement(query);


                File img = new File(imagePath);
                FileInputStream fin = new FileInputStream(img);


                ps.setString(1, body);
                ps.setBinaryStream(2, fin, (int) img.length());
                ps.setInt(3, Login.authInformation.getUserId());

                int res = ps.executeUpdate();

                if (res > 0) {
                    resetClick(null);
                    customDialog.showAlertBox("Success", "Post successfully created.");
                } else {
                    customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");
                }


            } catch (SQLException e) {
                customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeConnection(connection, ps, null);
            }

        }else if (operationType == OperationType.UPDATE){
            try {
                connection = new DBConnection().getConnection();

                String query = "UPDATE POSTS SET BODY = ?, IMAGE = ? WHERE post_id = ?";
                ps = connection.prepareStatement(query);

                File img = new File(imagePath);
                FileInputStream fin = new FileInputStream(img);


                ps.setString(1, body);
                ps.setBinaryStream(2, fin, (int) img.length());
                ps.setInt(3, postModel.getPostId());
                int res = ps.executeUpdate();

                if (res > 0) {
                    Main.primaryStage.setUserData(true);
                    customDialog.showAlertBox("Success", "Post successfully updated.");
                    Stage stage = (Stage) postBody.getScene().getWindow();

                    if (null != stage && stage.isShowing()){
                        stage.close();
                    }


                } else {
                    customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");
                }


            } catch (SQLException e) {
                customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeConnection(connection, ps, null);
            }

        }

    }

}
