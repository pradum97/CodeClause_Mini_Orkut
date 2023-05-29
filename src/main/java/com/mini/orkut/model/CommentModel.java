package com.mini.orkut.model;

import javafx.scene.image.Image;

public class CommentModel {
    private int commentId,commentBy;
    private String commentByName;
    private Image commentByProfileImage;
    private String comment,commentDate;

    public CommentModel(int commentId, int commentBy, String commentByName,
                        Image commentByProfileImage, String comment, String commentDate) {
        this.commentId = commentId;
        this.commentBy = commentBy;
        this.commentByName = commentByName;
        this.commentByProfileImage = commentByProfileImage;
        this.comment = comment;
        this.commentDate = commentDate;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getCommentBy() {
        return commentBy;
    }

    public void setCommentBy(int commentBy) {
        this.commentBy = commentBy;
    }

    public String getCommentByName() {
        return commentByName;
    }

    public void setCommentByName(String commentByName) {
        this.commentByName = commentByName;
    }

    public Image getCommentByProfileImage() {
        return commentByProfileImage;
    }

    public void setCommentByProfileImage(Image commentByProfileImage) {
        this.commentByProfileImage = commentByProfileImage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }
}
