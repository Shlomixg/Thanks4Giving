package com.tsk.thanks4giving;

public class Comment {

    public String userID;
    public String userName;
    public String text;
    public String date;

    public Comment(String userID, String userName, String text, String date) {
        this.userID = userID;
        this.userName = userName;
        this.text = text;
        this.date = date;
    }

    public Comment() {

    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
