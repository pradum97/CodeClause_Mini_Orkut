package com.mini.orkut.controller.auth;

import com.mini.orkut.CustomDialog;
import com.mini.orkut.database.DBConnection;
import com.mini.orkut.mail.SendMail;
import com.mini.orkut.util.OptionalMethod;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ForgotPassword extends OptionalMethod implements Initializable {

    public VBox verificationContainer;
    public TextField emailTf;
    public VBox passwordContainer;
    public TextField passwordTf;
    public TextField confirmPasswordTf;
    public VBox otpContainer;
    public TextField otpTf;
    private CustomDialog customDialog;
    private OptionalMethod method;
    private String otp, email;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();

        hideElement(otpContainer, passwordContainer);

    }


    public void check(ActionEvent actionEvent) {

        String emailStr = emailTf.getText();

        if (emailStr.isEmpty()) {
            method.show_popup("Please enter valid email.", emailTf);
            return;
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {

            connection = new DBConnection().getConnection();

            String query = "select email from users where email = ?";

            ps = connection.prepareStatement(query);
            ps.setString(1, emailStr);

            rs = ps.executeQuery();

            if (rs.next()) {

                sendEmail(emailStr);

            } else {

                customDialog.showAlertBox("Authentication failed !!", "Email not registered. Please enter valid email id");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, rs);
        }

    }

    private void sendEmail(String email) {

        otp = SendMail.sendOtp(email);

        if (otp.isEmpty()) {
            customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");

        } else {
            method.hideElement(verificationContainer);
            otpContainer.setVisible(true);
        }

    }

    public void changePassword(ActionEvent actionEvent) {

        String password = passwordTf.getText();
        String confirmPassword = confirmPasswordTf.getText();

        if (password.isEmpty()) {
            method.show_popup("Please enter password.", passwordTf);
            return;
        } else if (confirmPassword.isEmpty()) {
            method.show_popup("Please enter confirm password.", confirmPasswordTf);
            return;
        } else if (!confirmPassword.equals(password)) {
            method.show_popup("Password confirmation doesn't match", confirmPasswordTf);
            return;
        }

        Connection connection = null;
        PreparedStatement ps = null;

        try {

            connection = new DBConnection().getConnection();
            String query = "update users set password = ? where email = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, confirmPassword);
            ps.setString(2, emailTf.getText());
            int res = ps.executeUpdate();

            if (res > 0) {
                Stage stage = (Stage) confirmPasswordTf.getScene().getWindow();

                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
                customDialog.showAlertBox("Success", "Password successfully updated");

            } else {
                customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");

            }

        } catch (SQLException e) {
            customDialog.showAlertBox("Failed !!", "Something went wrong. Please try again !!");
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeConnection(connection, ps, null);
        }


    }

    public void validOtp(ActionEvent actionEvent) {

        String inputOtp = otpTf.getText();

        if (inputOtp.isEmpty()) {
            method.show_popup("Please enter valid otp.", otpTf);
            return;
        }

        if (otp.equals(inputOtp)) {
            method.hideElement(otpContainer);
            passwordContainer.setVisible(true);
        } else {
            customDialog.showAlertBox("Wrong otp !!", "Incorrect otp.Please enter valid otp.");
        }
    }
}
