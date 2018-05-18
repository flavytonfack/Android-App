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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cs.com.project.models.Utils;

public class GroupProjectActivity extends AppCompatActivity {

    public static final String PROJECT_ID = "project_id";

    private static final String TAG = "GroupProjectActivity";

    private String mProjectId;

    private FirebaseAuth mFirebaseAuth;

    private RecyclerView mTaskList;
    private ProjectTaskListAdapter mTaskListAdapter;
    private RecyclerView mMemberList;
    private ProjectMembersListAdapter mMemberListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_project);

        mTaskList = (RecyclerView) findViewById(R.id.project_tasks_list);
        mTaskList.setLayoutManager(new LinearLayoutManager(this));
        mTaskList.addItemDecoration(new VerticalSpaceItemDecoration(this.getResources().getDimensionPixelSize(R.dimen.vertical_spacing)));

        mMemberList = (RecyclerView) findViewById(R.id.project_members);
        mMemberList.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(PROJECT_ID);
        if (mProjectId == null || mProjectId.isEmpty()) {
            Log.e(TAG, "was launched without a projectId");
            finish();
        } else {
            Log.e(TAG, "Setting up project: " + mProjectId);
            setup();
        }
    }

    private void setup() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        View newTaskButton = findViewById(R.id.new_task_btn);
        newTaskButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogForNewTask();
                    }
                }
        );

        updateTitleWithProjectName();


        initializeTaskListIfAnyTasksAvailable();
        initializeMemberList();
    }

    private void updateTitleWithProjectName() {
        DatabaseReference taskObjectInDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.GROUP_PROJECTS).child(mProjectId);
        taskObjectInDatabase.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        setTitle(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    private DatabaseReference getTaskListDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_TASKS).child(mProjectId);
    }

    private DatabaseReference getMembersListDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PROJECT_MEMBERS).child(mProjectId);
    }

    private void initializeMemberList() {
        DatabaseReference membersDatabaseReference = getMembersListDatabaseReference();
        mMemberListAdapter = new ProjectMembersListAdapter(this, membersDatabaseReference, null);
        mMemberList.setAdapter(mMemberListAdapter);
    }

    private void initializeTaskList() {
        if (mTaskListAdapter == null) {
            DatabaseReference databaseReference = getTaskListDatabaseReference();
            mTaskListAdapter = new ProjectTaskListAdapter(this, mProjectId, databaseReference, null);
            mTaskList.setAdapter(mTaskListAdapter);
        }
    }

    private void initializeTaskListIfAnyTasksAvailable() {
        DatabaseReference databaseReference = getTaskListDatabaseReference();
        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        initializeTaskList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Exception fetching initial task list: " + databaseError.toException());

                    }
                }
        );

    }

    private void showDialogForNewTask() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogContent = layoutInflater.inflate(R.layout.new_task_dialog, null);
        final EditText titleView = dialogContent.findViewById(R.id.new_task_title_edittext);
        dialogBuilder.setView(dialogContent);

        dialogBuilder.setPositiveButton(
                "Create",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String projectTitle = titleView.getText().toString();
                        if (projectTitle.isEmpty()) {
                            Toast.makeText(GroupProjectActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
                        } else {
                            dialogInterface.dismiss();
                            Utils.createTask(
                                    mProjectId,
                                    projectTitle,
                                    new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                initializeTaskList();
                                            }
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

    @Override
    protected void onStop() {
        if (mTaskListAdapter != null) {
            mTaskListAdapter.cleanup();
        }
        if (mMemberListAdapter != null) {
            mMemberListAdapter.cleanup();
        }
        super.onStop();
    }
}
