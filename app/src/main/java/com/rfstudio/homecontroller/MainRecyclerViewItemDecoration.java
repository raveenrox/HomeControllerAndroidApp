package com.rfstudio.homecontroller;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Raveen on 8/12/2015.
 */
public class MainRecyclerViewItemDecoration extends RecyclerView.ItemDecoration {

    private final int mVerticalSpaceHeight;

    public MainRecyclerViewItemDecoration(int mVerticalSpaceHeight) {
        this.mVerticalSpaceHeight = mVerticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = mVerticalSpaceHeight;
    }
}