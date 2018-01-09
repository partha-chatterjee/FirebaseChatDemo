package partha.firebasechatdemo.firebaseChat;

/**
 * Created by DAT-Asset-110 on 01-08-2017.
 */

public class User {
    private String name;
    private String photoUrl;
    private String email;
    private String userId;
    private String userIdToken;

    public User(){

    }

    public User(String name, String photoUrl, String email, String userId, String userIdToken){
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
        this.userId = userId;
        this.userIdToken = userIdToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdToken() {
        return userIdToken;
    }

    public void setUserIdToken(String userIdToken) {
        this.userIdToken = userIdToken;
    }
}
