package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Profile {

    private String name;
    private String profileImage;
    private int age;
    private String address;
    private ArrayList<Post> postWatched;
    private ArrayList<Post> postPosted;

    public Profile(String name, String profileImage, int age, String address, ArrayList<Post> postWatched, ArrayList<Post> postPosted) {
        this.name = name;
        this.profileImage = profileImage;
        this.age = age;
        this.address = address;
        this.postWatched = postWatched;
        this.postPosted = postPosted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Post> getPostWatched() {
        return postWatched;
    }

    public void setPostWatched(ArrayList<Post> postWatched) {
        this.postWatched = postWatched;
    }

    public ArrayList<Post> getPostPosted() {
        return postPosted;
    }

    public void setPostPosted(ArrayList<Post> postPosted) {
        this.postPosted = postPosted;
    }
}
