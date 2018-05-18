package cs.com.project;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import cs.com.project.models.GroupProject;
import cs.com.project.models.User;
import cs.com.project.models.Utils;

/**
 * This is the screen where the user sees their project list and can create or join a new project
 */
public class MainActivity extends AppCompatActivity implements ProjectsAdapter.ProjectItemClickListener {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootDatabaseReference;


    private RecyclerView mProjectsList;
    private ProjectsAdapter mProjectsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // see if we have an authenticated firebase user. If not, kill this Activity and open the login activity instead
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            launchLoginActivity();
            finish();
        } else {
            setup();
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void setup() {
        showProfilePhoto();

        TextView usernameView = (TextView) findViewById(R.id.username_textview);
        usernameView.setText(mFirebaseAuth.getCurrentUser().getDisplayName());

        View newProjectButton = findViewById(R.id.new_project_btn);
        newProjectButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showNewProjectDialog();
                    }
                }
        );

        View joinGroupButton = findViewById(R.id.join_project_btn);
        joinGroupButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogToJoinGroup();
                    }
                }
        );

        mProjectsList = (RecyclerView) findViewById(R.id.projects_list);
        mProjectsList.setLayoutManager(new LinearLayoutManager(this));
        mProjectsList.addItemDecoration(new VerticalSpaceItemDecoration(this.getResources().getDimensionPixelSize(R.dimen.vertical_spacing)));

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootDatabaseReference = mFirebaseDatabase.getReference();


        fetchProjectMemberships();
    }

    private void showProfilePhoto() {
        ImageView profilePhotoImageView = (ImageView) findViewById(R.id.user_photo_imageview);

        Glide.with(this)
                .load(Utils.getFirebaseUser().getPhotoUrl())
                .into(profilePhotoImageView);
    }

    private void initializeProjectsList() {
        DatabaseReference userProjectsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(getCurrentUserUid()).child(FirebaseConstants.GROUP_PROJECTS);
        mProjectsListAdapter = new ProjectsAdapter(this, userProjectsDatabaseReference, this);
        mProjectsList.setAdapter(mProjectsListAdapter);
    }

    @Override
    protected void onStop() {
        if (mProjectsListAdapter != null) {
            mProjectsListAdapter.cleanup();
        }

        super.onStop();
    }

    private void createNewProject(String title) {
        final String currentUserUid = getCurrentUserUid();
        final String projectKey = mRootDatabaseReference.child(FirebaseConstants.GROUP_PROJECTS).push().getKey();
        GroupProject newProject = new GroupProject(title, currentUserUid);


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + FirebaseConstants.GROUP_PROJECTS + "/" + projectKey, title);
        // add to users list of group projects.
        childUpdates.put("/" + FirebaseConstants.USERS + "/" + currentUserUid + "/" + FirebaseConstants.GROUP_PROJECTS + "/" + projectKey, title);
        // add user as project member
        childUpdates.put("/" + FirebaseConstants.PROJECT_MEMBERS + "/" + projectKey + "/" + currentUserUid, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        mRootDatabaseReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (mProjectsList.getAdapter() == null) {
                        initializeProjectsList();
                    }
                }
            }
        });
    }

    private String getCurrentUserUid() {
        return mFirebaseAuth.getCurrentUser().getUid();

    }

    private void showNewProjectDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogContent = layoutInflater.inflate(R.layout.new_project_dialog, null);
        final EditText titleView = dialogContent.findViewById(R.id.new_project_title_edittext);
        dialogBuilder.setView(dialogContent);

        dialogBuilder.setPositiveButton(
                "Create",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String projectTitle = titleView.getText().toString();
                        if (projectTitle.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
                        } else {
                            dialogInterface.dismiss();
                            createNewProject(projectTitle);
                        }
                    }
                });

        dialogBuilder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        Dialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void showDialogToJoinGroup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogContent = layoutInflater.inflate(R.layout.join_group_dialog, null);
        final EditText titleView = dialogContent.findViewById(R.id.group_join_id_edittext);
        dialogBuilder.setView(dialogContent);

        dialogBuilder.setPositiveButton(
                "Join",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String projectId = titleView.getText().toString();
                        if (projectId.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Project id is required", Toast.LENGTH_SHORT).show();
                        } else {
                            dialogInterface.dismiss();
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                            Utils.addMemberToGroup(
                                    MainActivity.this,
                                    projectId,
                                    firebaseUser,
                                    new Utils.SimpleTaskCallback() {
                                        @Override
                                        public void onTaskSuccessful() {
                                            Log.d(TAG, "Successfully added user to group");
                                            if (mProjectsListAdapter == null) {
                                                initializeProjectsList();
                                            }
                                        }

                                        @Override
                                        public void onTaskFailed() {
                                            Log.e(TAG, "ERROR joining group: " + projectId);

                                        }
                                    });

                        }
                    }
                });

        dialogBuilder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        Dialog dialog = dialogBuilder.create();
        dialog.show();

    }

    private void fetchProjectMemberships() {
        String userId = mFirebaseAuth.getCurrentUser().getUid();
        mRootDatabaseReference.child(FirebaseConstants.USERS).child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        HashMap projectsMap = (HashMap<String, Object>) dataSnapshot.child("group_projects").getValue();
                        if (projectsMap != null) {
                            initializeProjectsList();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "ERROR fetching group memberships: " + databaseError.toException());
                    }
                }
        );
    }

    @Override
    public void onItemProjectClicked(String projectId) {
        Intent intent = new Intent(this, GroupProjectActivity.class);
        intent.putExtra(GroupProjectActivity.PROJECT_ID, projectId);
        startActivity(intent);
    }
}
