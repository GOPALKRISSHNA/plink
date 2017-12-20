package com.example.windows.plink;

/**
 * Created by Gopal on 11/8/2017.
 */

public class Contact {
    String name, profile;

    public Contact() {
    }

    public Contact(String name, String profile) {
        this.name = name;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
