package cs.com.project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    public interface ProjectItemClickListener {
        void onItemProjectClicked(String projectId);
    }

    private static final String TAG = "ProjectsAdapter";


    private final Context context;
    private final DatabaseReference databaseReference;
    private final ChildEventListener childEventListener;
    private final ProjectItemClickListener projectItemClickListener;
    private List<String> projectNames = new ArrayList<>();
    private List<String> projectIds = new ArrayList<>();


    public ProjectsAdapter(Context context, DatabaseReference databaseReference, ProjectItemClickListener clickListener) {
        this.context = context;
        this.databaseReference = databaseReference;
        this.projectItemClickListener = clickListener;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "new child added");
                projectNames.add(dataSnapshot.getValue().toString());
                projectIds.add(dataSnapshot.getKey());
                notifyItemInserted(projectNames.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "ERROR: " + databaseError.toException());
            }
        };
        databaseReference.addChildEventListener(childEventListener);

    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.projects_list_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, final int position) {
        String groupProject = projectNames.get(position);
        holder.titleView.setText(groupProject);
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (projectItemClickListener != null) {
                            projectItemClickListener.onItemProjectClicked(projectIds.get(position));
                        }
                    }
                }
        );
        holder.copyIdButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData=  ClipData.newPlainText("Project ID", projectIds.get(position));
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(context, "ID copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return projectNames.size();
    }

    public void cleanup() {
        databaseReference.removeEventListener(childEventListener);
    }


    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public Button copyIdButton;

        public ProjectViewHolder(View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.title_textview);
            copyIdButton = itemView.findViewById(R.id.copy_id_btn);
        }
    }
}
