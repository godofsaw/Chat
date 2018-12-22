package com.example.gos.chat;

import android.widget.ImageView;

public class ContactDetails  {

    private String userName;
    private String userEmail;
    private ImageView userImage;
    private String userImagePath;
    private String userUid;

    public ContactDetails(String userName, String userEmail,ImageView userImage) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userImage = userImage;
    }
    public ContactDetails(String userName, String userEmail,String userImage,String userUid) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userImagePath = userImage;
        this.userUid = userUid;
    }
    public ContactDetails(String userName, String userEmail){
        this.userName = userName;
        this.userEmail = userEmail;
    }
    public ContactDetails(){}

    public String getUserImagePath() {
        return userImagePath;
    }

    public void setUserImagePath(String userImagePath) {
        this.userImagePath = userImagePath;
    }

    public ImageView getUserImage() {
        return userImage;
    }

    public void setUserImage(ImageView userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}