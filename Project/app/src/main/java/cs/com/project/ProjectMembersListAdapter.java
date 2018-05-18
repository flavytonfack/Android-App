package cs.com.project;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ProjectMembersListAdapter extends RecyclerView.Adapter<ProjectMembersListAdapter.MembersListViewHolder> {

    public interface ProjectItemClickListener {
        void onItemProjectClicked(String projectId);
    }

    private static final String TAG = "TaskListAdapter";


    private final Context context;
    private final DatabaseReference databaseReference;
    private final ChildEventListener childEventListener;
    private final ProjectItemClickListener projectItemClickListener;
    private List<String> mMemberNames = new ArrayList<>();
    private List<String> memberIds = new ArrayList<>();


    public ProjectMembersListAdapter(Context context, DatabaseReference databaseReference, ProjectItemClickListener clickListener) {
        this.context = context;
        this.databaseReference = databaseReference;
        this.projectItemClickListener = clickListener;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                boolean isInitialCall = true;
                if (isInitialCall) {
                    isInitialCall = false;
                    Log.d(TAG, "got initial list of mMemberNames");
                } else {
                    Log.d(TAG, "new member added");

                }

                mMemberNames.add(dataSnapshot.getValue().toString());
                memberIds.add(dataSnapshot.getKey());
                notifyItemInserted(mMemberNames.size() - 1);
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
    public MembersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.project_members_list_item, parent, false);
        return new MembersListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersListViewHolder holder, final int position) {
        String name = mMemberNames.get(position);
        holder.titleView.setText(name);
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (projectItemClickListener != null) {
                            projectItemClickListener.onItemProjectClicked(memberIds.get(position));
                        }
                    }
                }
        );
    }


    @Override
    public int getItemCount() {
        return mMemberNames.size();
    }

    public void cleanup() {
        databaseReference.removeEventListener(childEventListener);
    }


    public static class MembersListViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;

        public MembersListViewHolder(View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.project_member_textview);
        }
    }
}
