package com.mini.orkut.model;

import javafx.scene.image.Image;

public class PostModel {
    private int postId, post_by;
    private String fullName, body;
    private Image postImage,profileImage;
    private String postTime;

    public PostModel(int postId, int post_by, String fullName, String body, Image postImage,
                     Image profileImage, String postTime) {
        this.postId = postId;
        this.post_by = post_by;
        this.fullName = fullName;
        this.body = body;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.postTime = postTime;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getPost_by() {
        return post_by;
    }

    public void setPost_by(int post_by) {
        this.post_by = post_by;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }



    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Image getPostImage() {
        return postImage;
    }

    public void setPostImage(Image postImage) {
        this.postImage = postImage;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }
}
