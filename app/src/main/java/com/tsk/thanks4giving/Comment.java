package com.tsk.thanks4giving;

public class Comment {

    private int id;
    private int userID;
    private String text;

    public Comment(int userID, String text) {
        this.userID = userID;
        this.text = text;
    }

    /* Getters */

    public int getID() {
        return id;
    }

    public int getUserID() {
        return userID;
    }

    public String getText() {
        return text;
    }

    /* Setters */

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setText(String text) {
        this.text = text;
    }
}
