package com.tsk.thanks4giving;

public class Comment {

    private String uid;
    private String userName;
    private String text;

    public Comment(String uid, String userName, String text) {
        this.uid = uid;
        this.userName = userName;
        this.text = text;
    }

    public Comment() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
}
