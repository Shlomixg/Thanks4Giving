package com.tsk.thanks4giving;

public class Comment {

    private String Token;
    private String userName;
    private String text;

    public Comment(String token, String userName, String text) {
        Token = token;
        this.userName = userName;
        this.text = text;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
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
