package com.rfstudio.homecontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Raveen on 8/12/2015.
 */
public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> {

    private int length;

    private HelperDataClass helperDataClass;
    private HelperClass helperClass;

    ArrayList<String> arrayList;

    public NavigationAdapter(Context context)
    {
        helperDataClass = new HelperDataClass();
        helperClass = new HelperClass(context, helperDataClass);
        helperClass.parse();
        arrayList = helperDataClass.titles;
        arrayList.add("Task Scheduler");
        length = arrayList.size();
    }


    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_row, parent, false);
        NavigationViewHolder viewHolder = new NavigationViewHolder(linearLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationViewHolder navigationViewHolder, int i)
    {
        navigationViewHolder.setText(i, arrayList);
    }

    @Override
    public int getItemCount() { return length;}

    public static class NavigationViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title;

        public NavigationViewHolder(LinearLayout itemView)
        {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.rowTitle);
        }

        public void setText(int position, ArrayList<String> arrayList)
        {
            title.setText(arrayList.get(position).toString());
            title.setTag(position);
        }
    }
}
