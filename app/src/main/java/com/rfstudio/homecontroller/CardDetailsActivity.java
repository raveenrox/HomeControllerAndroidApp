package com.rfstudio.homecontroller;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CardDetailsActivity extends Activity {

    private ArrayList<String> children;
    private ArrayList<String> commandNum;
    private ArrayList<String> titles;

    private HelperDataClass helperDataClass;
    private HelperClass helperClass;

    private Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        myActivity = this;

        helperDataClass = (HelperDataClass) getIntent().getSerializableExtra("helper");
        helperClass = new HelperClass(this, helperDataClass);
        titles = helperDataClass.titles;

        children = new ArrayList<>();
        commandNum = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        TextView textView = (TextView) findViewById(R.id.cd_title);
        ImageView imageView = (ImageView) findViewById(R.id.cd_image);

        String name = bundle.getString("name", "");
        if(!name.equals("")) {
            TextView lblWelcome = (TextView) findViewById(R.id.lblWelcome);
            lblWelcome.setText("Welcome " + name + ",");
        }

        textView.setText(bundle.getString("title", "ERROR"));

        try {
            children = getIntent().getStringArrayListExtra("children");
            commandNum = getIntent().getStringArrayListExtra("pin");
            TextView textView1 = (TextView) findViewById(R.id.cd_details);
            for(int i=children.size()-1; i>=0; i--)
            {
                textView1.append(children.get(i) + "\t\t- " + commandNum.get(i) + '\n');
            }
            imageView.setImageDrawable(drawableFromFile(Integer.parseInt(bundle.getString("image"))));
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            Palette palette  = Palette.generate(bitmap);
            Palette.Swatch swatch = palette.getLightVibrantSwatch();

            if(swatch!=null) {
                textView.setBackgroundColor(swatch.getRgb());
                textView.setTextColor(swatch.getBodyTextColor());
            }
        }catch (Exception ex)
        {

        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cdRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new CardDetailsAdapter(children.size(), helperDataClass, Integer.parseInt(bundle.getString("image")), this));
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

    public Drawable drawableFromFile(int no) throws IOException {

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = openFileInput(no + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            return new BitmapDrawable(getResources(), bitmap);
        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            return getResources().getDrawable( R.drawable.img_not_found);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
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
            intent.putExtra("title", helperDataClass.titles.get(itemNo).toString());
            intent.putExtra("image", Integer.toString(itemNo));
            intent.putExtra("helper", helperDataClass);
            try {
                intent.putStringArrayListExtra("children", helperDataClass.titlesChildren.get(itemNo));
                intent.putStringArrayListExtra("pin", helperDataClass.childCommands.get(itemNo));
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }

            startActivity(intent);
            finish();
        }
    }

    public void cardAct(final View view)
    {
        int newStatus=0;
        try{
            if(helperDataClass.state.get(Integer.parseInt(view.getTag().toString())))
            {
                newStatus=0;
            }else {
                newStatus=1;
            }
        } catch (IndexOutOfBoundsException ex)
        {
            newStatus=1;
        }

        helperClass.sendRequest("X" + view.getTag().toString() + "Y" + newStatus + "Z");

        final TextView textView = (TextView) view;

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    //helperClass.checkStatus();
                    helperClass.getStatus();
                    myActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try {
                                if (helperDataClass.state.get(Integer.parseInt(view.getTag().toString()))) {
                                    textView.setBackground(getDrawable(R.drawable.ripple_effect_cd_on));
                                    textView.setTextColor(getResources().getColor(R.color.rav_black));
                                } else {
                                    textView.setBackground(getDrawable(R.drawable.ripple_effect_cd_off));
                                    textView.setTextColor(getResources().getColor(R.color.rav_white));
                                }
                            } catch (IndexOutOfBoundsException ex) {}
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d("Output", helperDataClass.status.get(Integer.parseInt(view.getTag().toString())).toString());
            }
        }.start();
    }

}
