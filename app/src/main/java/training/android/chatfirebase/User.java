package training.android.chatfirebase;

public class User {
    private String name;
    private String email;
    private String id;
    private String avatarURL;

    public User() {
    }

    public User(String name, String email, String id, String avatarURL) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.avatarURL = avatarURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
