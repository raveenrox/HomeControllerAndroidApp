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
public class CardDetailsAdapter extends RecyclerView.Adapter<CardDetailsAdapter.CardDetailsViewHolder> {

    private int noOfItems;
    private int position;
    private HelperDataClass helperDataClass;

    public CardDetailsAdapter(int noOfItems, HelperDataClass helperDataClass, int position, Context context)
    {
        this.noOfItems = noOfItems;
        this.helperDataClass = helperDataClass;
        this.position = position;

        HelperClass helperClass = new HelperClass(context,helperDataClass);
        helperClass.getStatus();
    }

    @Override
    public CardDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_details_row, parent, false);
        CardDetailsViewHolder viewHolder = new CardDetailsViewHolder(linearLayout, helperDataClass, position);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardDetailsViewHolder cardDetailsViewHolder, int i)
    {
        cardDetailsViewHolder.setText(i);
        cardDetailsViewHolder.setState(i, helperDataClass.state);
    }

    @Override
    public int getItemCount() { return noOfItems; }

    public static class CardDetailsViewHolder extends RecyclerView.ViewHolder
    {
        private ArrayList<String> names;
        private ArrayList<String> command;

        public TextView rowTitle;

        private Context context;

        public CardDetailsViewHolder(LinearLayout itemView, HelperDataClass helperDataClass, int position)
        {
            super(itemView);

            context = itemView.getContext();

            rowTitle = (TextView) itemView.findViewById(R.id.rowTitle);

            names = helperDataClass.titlesChildren.get(position);
            command = helperDataClass.childCommands.get(position);
        }

        public void setText(int i)
        {
            rowTitle.setText(names.get(i));
            rowTitle.setTag(command.get(i));
        }

        public void setState(int i, ArrayList<Boolean> state)
        {
            try {
                if (state.get(Integer.parseInt(command.get(i)))) {

                    rowTitle.setBackground(context.getDrawable(R.drawable.ripple_effect_cd_on));
                    rowTitle.setTextColor(context.getResources().getColor(R.color.rav_black));
                } else {
                    rowTitle.setBackground(context.getDrawable(R.drawable.ripple_effect_cd_off));
                    rowTitle.setTextColor(context.getResources().getColor(R.color.rav_white));
                }
            } catch (IndexOutOfBoundsException ex) {}
        }
    }
}
