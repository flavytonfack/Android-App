package cs.com.project.models;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import cs.com.project.FirebaseConstants;

public class Utils {

    public interface SimpleTaskCallback {
        void onTaskSuccessful();
        void onTaskFailed();
    }

    private static final String TAG = "Utils";

    public static FirebaseUser getFirebaseUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        return firebaseUser;
    }

    public static String getUserDisplayName() {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            return firebaseUser.getDisplayName();
        }
        return null;
    }

    public static String getUserUid() {
        FirebaseUser firebaseUser = getFirebaseUser();
        if (firebaseUser != null) {
            return firebaseUser.getUid();
        }
        return null;
    }

    public static void addMemberToGroup(final Context context, final String groupId, final FirebaseUser user, final SimpleTaskCallback callback) {
//        DatabaseReference projectMembersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_MEMBERS).child(groupId);
//        projectMembersDatabaseRef.push().setValue(user);
        DatabaseReference projectMembersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_MEMBERS);
        projectMembersDatabaseRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(groupId)) {
                            DatabaseReference membersListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_MEMBERS).child(groupId);
                            HashMap<String, Object> childUpdatesToAddToMembersList = new HashMap<>();
                            childUpdatesToAddToMembersList.put("/" + user.getUid(), user.getDisplayName());
                            membersListDatabaseReference.updateChildren(childUpdatesToAddToMembersList);

//
                            // fetch group name
                            DatabaseReference groupProjectsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.GROUP_PROJECTS).child(groupId);
                            groupProjectsDatabaseReference.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            DatabaseReference userGroupProjectsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(user.getUid()).child(FirebaseConstants.GROUP_PROJECTS);
                                            HashMap<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("/" + dataSnapshot.getKey(), dataSnapshot.getValue());
                                            userGroupProjectsDatabaseReference.updateChildren(childUpdates);

                                            if (callback != null) {
                                                callback.onTaskSuccessful();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            if (callback != null) {
                                                callback.onTaskFailed();
                                            }

                                        }
                                    }
                            );




                        } else {
                            Toast.makeText(context, "Sorry, the group you entered doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    public static void createTask(final String groupId, String taskTitle, OnCompleteListener onCompleteListener) {
        DatabaseReference projectMembersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_TASKS).child(groupId);
        projectMembersDatabaseRef.push()
                .setValue(new Task(taskTitle))
        .addOnCompleteListener(onCompleteListener);
    }

    public static void updateTaskStatus(String projectId, String taskId, boolean isCompleted) {
        DatabaseReference taskObjectInDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_TASKS).child(projectId).child(taskId);
        taskObjectInDatabase.child(FirebaseConstants.IS_COMPLETED).setValue(isCompleted);
    }

    public static void claimTask(String projectId, String taskId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference taskObjectInDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_TASKS).child(projectId).child(taskId);
        taskObjectInDatabase.child(FirebaseConstants.TASK_OWNER_NAME).setValue(firebaseUser.getDisplayName());
    }
}
