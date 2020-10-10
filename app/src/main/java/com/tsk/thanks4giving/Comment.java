package com.tsk.thanks4giving;

public class Comment {

    public String userID;
    public String userName;
    public String text;
    public String date;

    public Comment() {

    }

    public Comment(String userID, String userName, String text, String date) {
        this.userID = userID;
        this.userName = userName;
        this.text = text;
        this.date = date;
    }

    /* Getters */

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /* Setters */

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }
}
