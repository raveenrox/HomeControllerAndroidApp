package com.rfstudio.homecontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Raveen on 8/12/2015.
 */
public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.CardViewHolder> {

    private ArrayList<Drawable> images;
    private int length;
    ArrayList<String> arrayList;

    HelperDataClass helperDataClass;

    public CardViewAdapter(ArrayList<Drawable> images, HelperDataClass helperDataClass) {
        this.helperDataClass = helperDataClass;
        length = helperDataClass.list_length;
        this.images = images;
        arrayList = new ArrayList<>();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_single, parent, false);
        CardViewHolder viewHolder = new CardViewHolder(linearLayout, images, helperDataClass);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i)
    {
        cardViewHolder.setText(i);
        cardViewHolder.setCard(i);
    }

    @Override
    public int getItemCount() { return length;}

    public static class CardViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title;
        private ImageView imageView;
        private ArrayList<Drawable> images;
        private ArrayList<String> titles;

        HelperDataClass helperDataClass;

        public CardViewHolder(LinearLayout itemView, ArrayList<Drawable> images, HelperDataClass helperDataClass)
        {
            super(itemView);
            this.helperDataClass = helperDataClass;
            title = (TextView) itemView.findViewById(R.id.card_text);
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            this.images = images;
            titles = helperDataClass.titles;
        }

        public void setCard(int position)
        {
            imageView.setImageDrawable(images.get(position));
            imageView.setTag(position);
            imageView.setTransitionName("cd_image_cov");

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            Palette palette  = Palette.generate(bitmap);
            Palette.Swatch swatch = palette.getLightVibrantSwatch();

            if(swatch!=null) {
                title.setBackgroundColor(swatch.getRgb());
                title.setTextColor(swatch.getBodyTextColor());
            }
            title.setText(titles.get(position));
        }

        public void setText(int position)
        {
            title.setText(titles.get(position).toString());
            title.setTag(position);
        }
    }
}
