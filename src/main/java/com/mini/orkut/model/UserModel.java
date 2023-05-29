package com.mini.orkut.model;

import javafx.scene.image.Image;

public class UserModel {

    private final int userId;
    private final String username, email, gender, firstName, lastName,
            phoneCode, phone, createdDate, fullName;
    private final Image profilePhoto;

    public UserModel(int userId, String username, String email, String gender, String firstName,
                     String lastName, String phoneCode, String phone, String createdDate, String fullName, Image profilePhoto) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneCode = phoneCode;
        this.phone = phone;
        this.createdDate = createdDate;
        this.fullName = fullName;
        this.profilePhoto = profilePhoto;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getFullName() {
        return fullName;
    }

    public Image getProfilePhoto() {
        return profilePhoto;
    }
}
