package com.mini.orkut.model;

import javafx.scene.image.Image;

public class FriendRequestModel {

    private int requestId;
    private Image profileImage;
    private String name,gender ;

    private int request_to,request_by;

    public FriendRequestModel(int requestId, Image profileImage, String name, String gender, int request_to, int request_by) {
        this.requestId = requestId;
        this.profileImage = profileImage;
        this.name = name;
        this.gender = gender;
        this.request_to = request_to;
        this.request_by = request_by;
    }

    public int getRequest_to() {
        return request_to;
    }

    public void setRequest_to(int request_to) {
        this.request_to = request_to;
    }

    public int getRequest_by() {
        return request_by;
    }

    public void setRequest_by(int request_by) {
        this.request_by = request_by;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
