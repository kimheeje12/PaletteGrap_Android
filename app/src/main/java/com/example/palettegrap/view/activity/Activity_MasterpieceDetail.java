package com.example.palettegrap.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.palettegrap.R;
import com.example.palettegrap.etc.FeedDelete;
import com.example.palettegrap.etc.MasterDelete;
import com.example.palettegrap.item.FeedData;
import com.example.palettegrap.item.MasterData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_MasterpieceDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masterpiece_detail);

        Button btn_back = (Button) findViewById(R.id.button_back);
        ImageView masterpiece = (ImageView) findViewById(R.id.image);
        ImageView setting = (ImageView) findViewById(R.id.setting);
        TextView masterpiece_created = (TextView) findViewById(R.id.masterpiece_created);
        TextView masterpiece_title = (TextView) findViewById(R.id.masterpiece_title);
        TextView masterpiece_artist = (TextView) findViewById(R.id.masterpiece_artist);
        TextView masterpiece_content = (TextView) findViewById(R.id.masterpiece_content);

        SharedPreferences sharedPreferences = getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        String loginemail=sharedPreferences.getString("inputemail",null);

        Intent intent = getIntent();
        String member_email = intent.getStringExtra("member_email");
        String master_id = intent.getStringExtra("master_id");
        String master_title = intent.getStringExtra("master_title");
        String master_artist = intent.getStringExtra("master_artist");
        String master_image = intent.getStringExtra("master_image");
        String master_story = intent.getStringExtra("master_story");
        String master_created = intent.getStringExtra("master_created");
        Log.e("????????? ??????", "????????? ??????"+master_created);

        Glide.with(Activity_MasterpieceDetail.this).load(master_image).into(masterpiece);
        masterpiece_title.setText(master_title);
        masterpiece_artist.setText(master_artist);
        masterpiece_content.setText(master_story);

        //?????????
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(master_created);
            DateFormat format2 = new SimpleDateFormat("yyyy??? M??? d???");
            masterpiece_created.setText(format2.format(date)); //?????????
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(member_email.equals(loginemail)){
            setting.setVisibility(View.VISIBLE);
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String[] items ={"????????? ?????? ??????", "????????? ?????? ??????", "??????"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_MasterpieceDetail.this);

                    builder.setTitle("PaletteGrap");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                Intent intent = new Intent(Activity_MasterpieceDetail.this, Activity_MasterpieceEdit.class);
                                intent.putExtra("member_email", member_email);
                                intent.putExtra("master_id", master_id);
                                intent.putExtra("master_title", master_title);
                                intent.putExtra("master_artist", master_artist);
                                intent.putExtra("master_image", master_image);
                                intent.putExtra("master_story", master_story);
                                intent.putExtra("master_created", master_created);
                                startActivity(intent);
                            }else if(i==1){
                                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_MasterpieceDetail.this);
                                builder.setTitle("?????? ?????? ???????????????????").setMessage("\n");
                                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Gson gson = new GsonBuilder().setLenient().create();

                                        Retrofit retrofit = new Retrofit.Builder()
                                                .baseUrl(MasterDelete.MasterDelete_URL)
                                                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                .addConverterFactory(GsonConverterFactory.create(gson))
                                                .build();

                                        MasterDelete api = retrofit.create(MasterDelete.class);
                                        Call<List<MasterData>> call = api.MasterDelete(master_id);
                                        call.enqueue(new Callback<List<MasterData>>() //enqueue: ???????????? ???????????? ??????
                                        {
                                            @Override
                                            public void onResponse(@NonNull Call<List<MasterData>> call, @NonNull Response<List<MasterData>> response) {
                                                if (response.isSuccessful() && response.body() != null) {

                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<List<MasterData>> call, Throwable t) {
                                                finish();
                                                Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }else{
                            }
                        }
                    });
                    builder.show();
                }
            });
        }else{
            setting.setVisibility(View.INVISIBLE);
        }

        //????????????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }
}