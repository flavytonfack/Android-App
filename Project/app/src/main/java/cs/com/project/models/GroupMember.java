package cs.com.project.models;

public class GroupMember {

    public String key;
    public String userId;
    public String name;

    public GroupMember() {
        // empty constructor required for firebase
    }

    public GroupMember(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}
