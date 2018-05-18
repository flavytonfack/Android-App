package cs.com.project.models;


public class User {

    public String name;
    public String uid;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

//    public static HashMap<String, Object> newUserMap(String uid, String name) {
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("name", name);
////        result.put()
//    }

}
