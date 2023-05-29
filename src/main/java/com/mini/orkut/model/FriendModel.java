package com.mini.orkut.model;

import javafx.scene.image.Image;

public class FriendModel {
    private final int id, friendOf, friendUserId;
    private final String username, email, gender, firstName, lastName,fullName,
            phoneCode, phone, friendShipDate;
    private final Image profilePhoto;

    public FriendModel(int id, int friendOf, int friendUserId, String username, String email, String gender, String firstName, String lastName, String fullName,
                       String phoneCode, String phone, String friendShipDate, Image profilePhoto) {
        this.id = id;
        this.friendOf = friendOf;
        this.friendUserId = friendUserId;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.phoneCode = phoneCode;
        this.phone = phone;
        this.friendShipDate = friendShipDate;
        this.profilePhoto = profilePhoto;
    }

    public int getId() {
        return id;
    }

    public int getFriendOf() {
        return friendOf;
    }

    public int getFriendUserId() {
        return friendUserId;
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

    public String getFullName() {
        return fullName;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getFriendShipDate() {
        return friendShipDate;
    }

    public Image getProfilePhoto() {
        return profilePhoto;
    }
}
