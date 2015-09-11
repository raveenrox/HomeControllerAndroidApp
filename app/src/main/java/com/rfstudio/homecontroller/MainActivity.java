package com.rfstudio.homecontroller;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String PREF_NAME = "settings";
    private RecyclerView recyclerView;

    private ArrayList<Drawable> images;

    public SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    HelperDataClass helperDataClass;
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helperDataClass = (HelperDataClass) getIntent().getSerializableExtra("helper");
        helperClass = new HelperClass(this, helperDataClass);
        // TODO: 8/22/2015 remove
        //helperClass.decode();
        helperClass.parse();
        images = new ArrayList<>();
        images = helperClass.images;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.rav_grey));
        }

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        recyclerView = (RecyclerView) findViewById(R.id.mainRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, 1, false));
        recyclerView.addItemDecoration(new MainRecyclerViewItemDecoration(0));
        recyclerView.setAdapter(new CardViewAdapter(images, helperDataClass));

        String name = preferences.getString("name", "");
        if(!name.equals("")) {
            TextView lblWelcome = (TextView) findViewById(R.id.lblWelcome);
            lblWelcome.setText("Welcome " + name + ",");
        }


    }

    public void navAct(View view)
    {
        Intent intent = null;
        switch (view.getId())
        {
            case R.id.navDrawerSettings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void navDrawerClick(View view)
    {
        int itemNo = Integer.parseInt(view.getTag().toString());

        if(itemNo == helperDataClass.list_length)
        {
            Intent intent = new Intent(this, TaskSchedulerActivity.class);
            intent.putExtra("helper", helperDataClass);
            startActivity(intent);
        } else {

            Intent intent = new Intent(this, CardDetailsActivity.class);
            intent.putExtra("name", preferences.getString("name", ""));
            intent.putExtra("title", helperDataClass.titles.get(itemNo));
            intent.putExtra("image", Integer.toString(itemNo));
            intent.putExtra("helper", helperDataClass);
            try {
                intent.putStringArrayListExtra("children", helperDataClass.titlesChildren.get(itemNo));
                intent.putStringArrayListExtra("pin", helperDataClass.childCommands.get(itemNo));
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }

            startActivity(intent);
        }
    }


    public void showCardAni(View v)
    {
        TextView textView = (TextView) v.findViewById(R.id.card_text);
        ImageView imageView = (ImageView) v.findViewById(R.id.card_image);

        Intent intent = new Intent(this, CardDetailsActivity.class);
        intent.putExtra("name", preferences.getString("name", ""));
        intent.putExtra("title", textView.getText().toString());
        intent.putExtra("image", imageView.getTag().toString());
        intent.putExtra("helper", helperDataClass);
        try {
            intent.putStringArrayListExtra("children", helperDataClass.titlesChildren.get(Integer.parseInt(imageView.getTag().toString())));
            intent.putStringArrayListExtra("pin", helperDataClass.childCommands.get(Integer.parseInt(imageView.getTag().toString())));
        } catch (IndexOutOfBoundsException ex)
        {
            ex.printStackTrace();
        }

        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(imageView, (int) imageView.getX(), (int) imageView.getY(), imageView.getHeight(), imageView.getWidth());
        startActivity(intent, options.toBundle());
    }
}
