package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Profile {

    private String firstName;
    private String lastName;
    private String email;
    private String profileImage;
    private int age;
    private String address;
    private ArrayList<Post> postWatched;
    private ArrayList<Post> postPosted;

    public Profile(String firstName, String lastName, String email, String profileImage, int age, String address, ArrayList<Post> postWatched, ArrayList<Post> postPosted) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImage = profileImage;
        this.age = age;
        this.address = address;
        this.postWatched = postWatched;
        this.postPosted = postPosted;
    }

    /* Getters */

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<Post> getPostWatched() {
        return postWatched;
    }

    public ArrayList<Post> getPostPosted() {
        return postPosted;
    }

    /* Setters */

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostWatched(ArrayList<Post> postWatched) {
        this.postWatched = postWatched;
    }

    public void setPostPosted(ArrayList<Post> postPosted) {
        this.postPosted = postPosted;
    }

}
