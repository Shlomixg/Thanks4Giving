package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Profile {

    private String fullName;
    //private String email;
    private String profileImage;
    private int age;
    private String address;
    private ArrayList<Post> postWatched;
    private ArrayList<Post> postPosted;

    public Profile(String name /*,String email*/, String profileImage, int age, String address, ArrayList<Post> postWatched, ArrayList<Post> postPosted) {
        this.fullName = name;
        //this.email = email;
        this.profileImage = profileImage;
        this.age = age;
        this.address = address;
        this.postWatched = postWatched;
        this.postPosted = postPosted;
    }

    /* Getters */

    public String getFullName() {
        return fullName;
    }

   /* public String getEmail() {
        return email;
    }*/

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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

   /* public void setEmail(String email) {
        this.email = email;
    }*/

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
