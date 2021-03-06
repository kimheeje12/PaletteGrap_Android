package com.example.palettegrap.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.palettegrap.R;
import com.example.palettegrap.etc.FeedDelete;
import com.example.palettegrap.etc.GetMyStory;
import com.example.palettegrap.etc.LikeCheck;
import com.example.palettegrap.etc.LikeDelete;
import com.example.palettegrap.etc.LikeInput;
import com.example.palettegrap.etc.ScrapCheck;
import com.example.palettegrap.etc.ScrapDelete;
import com.example.palettegrap.etc.ScrapInput;
import com.example.palettegrap.item.FeedData;
import com.example.palettegrap.view.adapter.ImageSliderAdapter;
import com.example.palettegrap.view.fragment.Fragment_Mypage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_MyStory extends AppCompatActivity {

    public List<FeedData> myList;
    private ViewPager2 viewPager2;
    private ImageSliderAdapter imageSliderAdapter;
    private LinearLayout layoutIndicator;

    //???????????? ??????????????? ??????!(????????????)
    @Override
    protected void onStart() {
        super.onStart();

        Button btn_back = (Button) findViewById(R.id.back); //????????????
        ImageView member_profile = (ImageView) findViewById(R.id.member_profile); //?????? ?????????
        TextView member_nick = (TextView) findViewById(R.id.member_nick); //?????? ?????????
        TextView feed_text = (TextView) findViewById(R.id.feedtext); //?????? text
        TextView feed_drawingtool = (TextView) findViewById(R.id.feed_drawingtool); //????????????
        TextView feed_drawingtime = (TextView) findViewById(R.id.feed_drawingtime); //????????????
        TextView feed_created = (TextView) findViewById(R.id.feed_created); //?????????
        TextView likecount = (TextView) findViewById(R.id.likecount); //????????? ??????
        TextView replycount = (TextView) findViewById(R.id.replycount); //?????? ??????

        ImageView feed_setting = (ImageView) findViewById(R.id.feed_setting); //??????
        ImageView scrap = (ImageView) findViewById(R.id.scrap); //?????????(????????? ?????? ????????? ???)
        ImageView scrap2 = (ImageView) findViewById(R.id.scrap2); //?????????2(????????? ?????? ???)
        ImageView like = (ImageView) findViewById(R.id.like); //?????????(?????? ?????? ???)
        ImageView unlike = (ImageView) findViewById(R.id.unlike); //?????????(?????? ?????? ????????? ???)
        ImageView reply = (ImageView) findViewById(R.id.reply); //??????

        viewPager2 = findViewById(R.id.viewpager2);
        layoutIndicator = findViewById(R.id.layoutIndicators);

        //????????????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//????????? ??????
            }
        });

        //???????????????(?????????????????? ????????? ???????????? ?????? ????????? ????????? ?????????)
        Intent intent = getIntent();
        String feed_id = intent.getStringExtra("feed_id"); //?????? ????????????
        String member_email = intent.getStringExtra("member_email"); //?????? ?????????
        String member_image = intent.getStringExtra("member_image"); //????????? ?????????
        String membernick = intent.getStringExtra("member_nick"); //?????????
        String feedtext = intent.getStringExtra("feed_text"); //?????? text
        String feeddrawingtool = intent.getStringExtra("feed_drawingtool"); //????????????
        String feeddrawingtime = intent.getStringExtra("feed_drawingtime"); //????????????
        String feedcreated = intent.getStringExtra("feed_created"); //?????????
        String feed_category = intent.getStringExtra("feed_category"); //?????? ????????????

        Glide.with(Activity_MyStory.this).load(member_image).circleCrop().into(member_profile); //????????? ?????????
        member_nick.setText(membernick); //?????????
        feed_text.setText(feedtext); //?????? text
        feed_drawingtool.setText(feeddrawingtool); //????????????
        feed_drawingtime.setText(feeddrawingtime); //????????????
        feed_created.setText(feedcreated); //?????????

        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
        String nickname = pref.getString("inputnick", null);
        String email = pref.getString("inputemail", null);

        //?????? ??? ????????????(????????? ??? & ???????????? ???)
        //?????? ????????? ???????????? ???????????? ???????????? ?????? ??? ????????? ?????????
        if(member_email.equals(email)){
            feed_setting.setVisibility(View.VISIBLE);
        }else{
            feed_setting.setVisibility(View.INVISIBLE);
        }

        //????????? ????????? '???????????????'??? ??????!
        member_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(member_nick.getText().toString().equals(nickname)){
                    Intent intent = new Intent(Activity_MyStory.this, Activity_Main.class);
                    intent.putExtra("mypage",1);
                    startActivity(intent);

                }else{
                    //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                    SharedPreferences pref = getSharedPreferences("otherprofile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("otheremail", member_email);
                    editor.apply();
                    Intent intent = new Intent(Activity_MyStory.this, Activity_Main.class);
                    intent.putExtra("mypage",2);
                    startActivity(intent);

                }
            }
        });

        //??????????????? ????????? ????????????!
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GetMyStory.GetMyStory_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetMyStory api = retrofit.create(GetMyStory.class);
        Call<List<FeedData>> call = api.getMyStory(feed_id);
        call.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");

                    myList = response.body();

                    FeedData feedData = myList.get(0);

                    Glide.with(Activity_MyStory.this).load(feedData.getmember_image()).circleCrop().into(member_profile); //????????? ?????????
                    member_nick.setText(feedData.getmember_nick()); //?????????
                    feed_text.setText(feedData.getfeed_text()); //?????? text
                    feed_drawingtool.setText(feedData.getfeed_drawingtool()); //????????????
                    feed_drawingtime.setText(feedData.getfeed_drawingtime()); //????????????

                    //?????????
                    String stringDate = feedData.getfeed_created();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = format.parse(stringDate);
                        DateFormat format2 = new SimpleDateFormat("yyyy??? M??? d???");
                        feed_created.setText(format2.format(date)); //?????????
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //????????? ?????? ???????????? ????????????!
                    SharedPreferences pref = getSharedPreferences("mystoryedit", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("feed_text", feedData.getfeed_text());
                    editor.putString("feed_drawingtool", feedData.getfeed_drawingtool());
                    editor.putString("feed_drawingtime", feedData.getfeed_drawingtime());
                    editor.putString("feed_category",feedData.getFeed_category());
                    editor.putString("feed_id",feedData.getfeed_id());
                    editor.putString("member_email",feedData.getMember_email());
                    editor.apply();

                    //????????????
                    viewPager2.setOffscreenPageLimit(1);
                    imageSliderAdapter = new ImageSliderAdapter(Activity_MyStory.this, myList);
                    viewPager2.setAdapter(imageSliderAdapter);

                    viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            setCurrentIndicator(position);
                        }
                    });

                    imageSliderAdapter.setimagelist(myList);
                    setupIndicators(myList.size());
                    imageSliderAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });

        //?????? ???(??????/??????)
        feed_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] items ={"????????? ??????", "????????? ??????", "??????"};

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_MyStory.this);

                builder.setTitle("PaletteGrap");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent = new Intent(Activity_MyStory.this, Activity_MyStoryEdit.class);
                            intent.putExtra("myList", (Serializable) myList);
                            startActivity(intent);
                        }else if(i==1){
                            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_MyStory.this);
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
                                            .baseUrl(FeedDelete.FeedDelete_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

                                    FeedDelete api = retrofit.create(FeedDelete.class);
                                    Call<List<FeedData>> call = api.FeedDelete(feed_id);
                                    call.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                                    {
                                        @Override
                                        public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                            if (response.isSuccessful() && response.body() != null) {

                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                            finish(); //????????? ????????????(activity ??????)
                                            Toast.makeText(getApplicationContext(), "???????????? ?????????????????????", Toast.LENGTH_SHORT).show();
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

        //????????? ?????? ??????(php?????? ??????(?????? ????????????+???????????????) -> 1-click / 0-unclick)
        Gson gson3 = new GsonBuilder().setLenient().create();

        Retrofit retrofit3 = new Retrofit.Builder()
                .baseUrl(LikeCheck.LikeCheck_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson3))
                .build();

        LikeCheck api3 = retrofit3.create(LikeCheck.class);

        RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
        RequestBody requestBody4 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

        Call<String> call3 = api3.LikeCheck(requestBody3,requestBody4);
        call3.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");

                    if (response.body().contains("click")) { //????????? ?????? ?????? ??????
                        like.setVisibility(View.INVISIBLE);
                        unlike.setVisibility(View.VISIBLE);
                    }if (response.body().contains("unclick")) {//????????? ?????? ?????? ????????? ??????
                        unlike.setVisibility(View.INVISIBLE);
                        like.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });


        //????????? ?????? ?????????
        Gson gson4 = new GsonBuilder().setLenient().create();

        Retrofit retrofit4 = new Retrofit.Builder()
                .baseUrl(GetMyStory.GetMyStory_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson4))
                .build();

        GetMyStory api4 = retrofit4.create(GetMyStory.class);
        Call<List<FeedData>> call4 = api4.getMyStory(feed_id);
        call4.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");
                    myList = response.body();

                    FeedData feedData = myList.get(0);
                    likecount.setText(feedData.getLike_count());

                }
            }

            @Override
            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });

        //?????????
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Gson gson = new GsonBuilder().setLenient().create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(LikeInput.LikeInput_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                LikeInput api = retrofit.create(LikeInput.class);

                RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

                Call<String> call = api.LikeInput(requestBody1,requestBody2);
                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "Like ??????!");

                            Gson gson4 = new GsonBuilder().setLenient().create();

                            Retrofit retrofit4 = new Retrofit.Builder()
                                    .baseUrl(GetMyStory.GetMyStory_URL)
                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                    .addConverterFactory(GsonConverterFactory.create(gson4))
                                    .build();

                            GetMyStory api4 = retrofit4.create(GetMyStory.class);
                            Call<List<FeedData>> call4 = api4.getMyStory(feed_id);
                            call4.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                            {
                                @Override
                                public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.e("Success", "call back ??????!");
                                        myList = response.body();

                                        FeedData feedData = myList.get(0);
                                        likecount.setText(feedData.getLike_count());

                                    }
                                }

                                @Override
                                public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });

                //????????? click/unclick
                like.setVisibility(View.INVISIBLE);
                unlike.setVisibility(View.VISIBLE);
            }
        });

        //????????? ??????
        unlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Gson gson = new GsonBuilder().setLenient().create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(LikeDelete.LikeDelete_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                LikeDelete api = retrofit.create(LikeDelete.class);

                RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

                Call<String> call = api.LikeDelete(requestBody1,requestBody2);
                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "Likedelete ??????!");
                            Gson gson4 = new GsonBuilder().setLenient().create();

                            Retrofit retrofit4 = new Retrofit.Builder()
                                    .baseUrl(GetMyStory.GetMyStory_URL)
                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                    .addConverterFactory(GsonConverterFactory.create(gson4))
                                    .build();

                            GetMyStory api4 = retrofit4.create(GetMyStory.class);
                            Call<List<FeedData>> call4 = api4.getMyStory(feed_id);
                            call4.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                            {
                                @Override
                                public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.e("Success", "call back ??????!");
                                        myList = response.body();

                                        FeedData feedData = myList.get(0);
                                        likecount.setText(feedData.getLike_count());

                                    }
                                }

                                @Override
                                public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });

                //????????? click/unclick
                like.setVisibility(View.VISIBLE);
                unlike.setVisibility(View.INVISIBLE);
            }
        });

        //?????? ?????? ?????????
        Gson gson5 = new GsonBuilder().setLenient().create();

        Retrofit retrofit5 = new Retrofit.Builder()
                .baseUrl(GetMyStory.GetMyStory_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson5))
                .build();

        GetMyStory api5 = retrofit5.create(GetMyStory.class);
        Call<List<FeedData>> call5 = api5.getMyStory(feed_id);
        call5.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");
                    myList = response.body();

                    FeedData feedData = myList.get(0);
                    replycount.setText(feedData.getReply_count());

                }
            }

            @Override
            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });


        //??????????????? ??????
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_MyStory.this, Activity_Reply.class);
                intent.putExtra("feed_id",feed_id);
                startActivity(intent);

            }
        });

        //????????? ?????? ??????(php?????? ??????(?????? ????????????+???????????????) -> 1-click / 0-unclick)
        Gson gson2 = new GsonBuilder().setLenient().create();

        Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl(ScrapCheck.ScrapCheck_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson2))
                .build();

        ScrapCheck api2 = retrofit2.create(ScrapCheck.class);

        RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

        Call<String> call2 = api2.ScrapCheck(requestBody1,requestBody2);
        call2.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");

                    if (response.body().contains("click")) { //????????? ?????? ?????? ??????
                        scrap2.setVisibility(View.VISIBLE);
                        scrap.setVisibility(View.INVISIBLE);
                    }if (response.body().contains("unclick")) {//????????? ?????? ?????? ????????? ??????
                        scrap.setVisibility(View.VISIBLE);
                        scrap2.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });

        //????????? ??? ???
        scrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Gson gson = new GsonBuilder().setLenient().create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ScrapInput.ScrapInput_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                ScrapInput api = retrofit.create(ScrapInput.class);

                RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

                Call<String> call = api.ScrapInput(requestBody1,requestBody2);
                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "scrapinput ??????!");

                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });

                //????????? click/unclick
                scrap2.setVisibility(View.VISIBLE);
                scrap.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "?????? ???????????? ?????????????????????", Toast.LENGTH_SHORT).show();
            }
        });

        //????????? ????????? ???
        scrap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new GsonBuilder().setLenient().create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ScrapDelete.ScrapDelete_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                ScrapDelete api = retrofit.create(ScrapDelete.class);

                RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), email); //?????????
                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_id); //?????? ?????????

                Call<String> call = api.ScrapDelete(requestBody1,requestBody2);
                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "scrapdelete ??????!");

                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });

                //????????? click/unclick
                scrap.setVisibility(View.VISIBLE);
                scrap2.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_story);

    }

    private void setupIndicators (int count){
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        layoutIndicator.removeAllViews();

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }

    private void setCurrentIndicator (int position){

        int childCount = layoutIndicator.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_inactive
                ));
            }
        }
    }

}