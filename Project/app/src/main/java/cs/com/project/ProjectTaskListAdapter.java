package cs.com.project;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import cs.com.project.models.Task;
import cs.com.project.models.Utils;

public class ProjectTaskListAdapter extends RecyclerView.Adapter<ProjectTaskListAdapter.TaskListViewHolder> {

    public interface ProjectItemClickListener {
        void onItemProjectClicked(String projectId);
    }

    private static final String TAG = "TaskListAdapter";


    private final Context context;
    private final DatabaseReference databaseReference;
    private final String projectId;
    private final ChildEventListener childEventListener;
    private final ProjectItemClickListener projectItemClickListener;
    private List<Task> tasks = new ArrayList<>();
    private List<String> taskIds = new ArrayList<>();


    public ProjectTaskListAdapter(Context context, String projectId, DatabaseReference databaseReference, ProjectItemClickListener clickListener) {
        this.context = context;
        this.projectId = projectId;
        this.databaseReference = databaseReference;
        this.projectItemClickListener = clickListener;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "new child added");
                Task task = dataSnapshot.getValue(Task.class);
                tasks.add(task);
                taskIds.add(dataSnapshot.getKey());
                notifyItemInserted(tasks.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String changedTaskId = dataSnapshot.getKey();
                Task updatedTask = dataSnapshot.getValue(Task.class);
                int position = -1;
                for (int i = 0; i < taskIds.size(); i++) {
                    String taskFromList = taskIds.get(i);
                    if (taskFromList.equals(changedTaskId)) {
                        position = i;
                        break;
                    }
                }
                if (position != -1) {
                    tasks.set(position, updatedTask);
                    notifyDataSetChanged();
                }
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
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.project_tasks_list_item, parent, false);
        return new TaskListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, final int position) {
        Task task = tasks.get(position);
        holder.checkbox.setText(task.title);
        holder.checkbox.setChecked(task.isCompleted);
        holder.checkbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        Utils.updateTaskStatus(projectId, taskIds.get(position), isChecked);
                    }
                }
        );
        String ownerName = task.ownerName;
        holder.ownerName.setText(task.ownerName);

        if (ownerName.equals(Utils.getUserDisplayName())) {
            holder.claimButton.setEnabled(false);
        } else {
            holder.claimButton.setEnabled(true);
        }

        holder.claimButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.claimTask(projectId, taskIds.get(position));
                    }
                }
        );
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void cleanup() {
        databaseReference.removeEventListener(childEventListener);
    }


    public static class TaskListViewHolder extends RecyclerView.ViewHolder {

        public CheckBox checkbox;
        public TextView ownerName;
        public Button claimButton;

        public TaskListViewHolder(View itemView) {
            super(itemView);

            checkbox = (CheckBox) itemView.findViewById(R.id.task_title_textview);
            ownerName = itemView.findViewById(R.id.current_owner_textview);
            claimButton = itemView.findViewById(R.id.claim_task_btn);
        }
    }
}
