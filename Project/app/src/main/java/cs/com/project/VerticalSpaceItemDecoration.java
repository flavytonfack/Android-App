package cs.com.project;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int verticalSpaceHeight;

    public VerticalSpaceItemDecoration(int spacingHeight) {
        this.verticalSpaceHeight = spacingHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = verticalSpaceHeight;
    }
}
