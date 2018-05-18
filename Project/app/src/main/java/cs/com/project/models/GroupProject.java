package cs.com.project.models;

public class GroupProject {
    public String title;
    public String ownerId;

    public GroupProject() {
        // Default constructor required for calls to DataSnapshot.getValue(GroupProject.class)
    }

    public GroupProject(String title, String ownerId) {
        this.title = title;
        this.ownerId = ownerId;
    }

//    public Map<String, Object> toMap() {
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("title", title);
//    }
}
