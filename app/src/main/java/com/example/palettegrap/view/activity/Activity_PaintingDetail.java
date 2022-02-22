package com.example.palettegrap.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.palettegrap.etc.GetPainting;
import com.example.palettegrap.etc.GetPaintingDetail;
import com.example.palettegrap.etc.MasterDelete;
import com.example.palettegrap.etc.PaintingDelete;
import com.example.palettegrap.item.MasterData;
import com.example.palettegrap.item.PaintingData;
import com.example.palettegrap.view.adapter.MasterAdapter;
import com.example.palettegrap.view.adapter.PaintingAdapter;
import com.example.palettegrap.view.adapter.PaintingDetailAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

public class Activity_PaintingDetail extends AppCompatActivity {

    private PaintingDetailAdapter paintingDetailAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onStart() {
        super.onStart();

        Button btn_back = (Button) findViewById(R.id.button_back);
        TextView title = (TextView) findViewById(R.id.title);
        ImageView setting = (ImageView) findViewById(R.id.setting);
        TextView painting_created = (TextView) findViewById(R.id.painting_created);
        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);
        TextView nickname = (TextView) findViewById(R.id.nickname);
        TextView like_count = (TextView) findViewById(R.id.like_count);
        ImageView like = (ImageView) findViewById(R.id.like); // 빨간 하트 (누르면 다시 빈 하트로) - unlike버튼
        ImageView unlike = (ImageView) findViewById(R.id.unlike); //빈 하트 (누르면 빨강으로) - like버튼

        SharedPreferences sharedPreferences = getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        String loginemail=sharedPreferences.getString("inputemail",null);

        Intent intent = getIntent();
        String member_email = intent.getStringExtra("member_email");
        String member_image = intent.getStringExtra("member_image");
        String member_nick = intent.getStringExtra("member_nick");
        String likecount = intent.getStringExtra("like_count");
        String painting_id = intent.getStringExtra("painting_id");
        String painting_title = intent.getStringExtra("painting_title");
        String painting_image_path = intent.getStringExtra("painting_image_path");
        String created_date = intent.getStringExtra("painting_created");
        String painting_text = intent.getStringExtra("painting_text");

        //설정(아이템 클릭시 얻어온 이메일과 현재 로그인 되어있는 이메일이 같다면 설정창 보이도록!) / 수정&삭제
        if(member_email.equals(loginemail)){
            setting.setVisibility(View.VISIBLE);
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String[] items ={"그림강좌 수정", "그림강좌 삭제", "취소"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_PaintingDetail.this);

                    builder.setTitle("PaletteGrap");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
//                                Intent intent = new Intent(Activity_PaintingDetail.this, Activity_PaintingEdit.class);
//                                intent.putExtra("member_email", member_email);
//                                intent.putExtra("master_id", master_id);
//                                intent.putExtra("master_title", master_title);
//                                intent.putExtra("master_artist", master_artist);
//                                intent.putExtra("master_image", master_image);
//                                intent.putExtra("master_story", master_story);
//                                intent.putExtra("master_created", master_created);
//                                startActivity(intent);
                            }else if(i==1){
                                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_PaintingDetail.this);
                                builder.setTitle("정말 삭제 하시겠습니까?").setMessage("\n");
                                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Gson gson = new GsonBuilder().setLenient().create();

                                        Retrofit retrofit = new Retrofit.Builder()
                                                .baseUrl(PaintingDelete.PaintingDelete_URL)
                                                .addConverterFactory(ScalarsConverterFactory.create()) // Response를 String 형태로 받고 싶다면 사용하기!
                                                .addConverterFactory(GsonConverterFactory.create(gson))
                                                .build();

                                        PaintingDelete api = retrofit.create(PaintingDelete.class);
                                        Call<List<PaintingData>> call = api.PaintingDelete(painting_id);
                                        call.enqueue(new Callback<List<PaintingData>>() //enqueue: 데이터를 입력하는 함수
                                        {
                                            @Override
                                            public void onResponse(@NonNull Call<List<PaintingData>> call, @NonNull Response<List<PaintingData>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    finish();
                                                    Toast.makeText(getApplicationContext(), "그림강좌가 삭제되었습니다", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<List<PaintingData>> call, Throwable t) {

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

        //그림강좌 제목
        title.setText(painting_title);


        //하단 프로필, 닉네임
        Glide.with(Activity_PaintingDetail.this).load(member_image).circleCrop().into(profileimage);
        nickname.setText(member_nick);

        //그림강좌 리스트 형성
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GetPaintingDetail.GetPaintingDetail_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response를 String 형태로 받고 싶다면 사용하기!
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetPaintingDetail api = retrofit.create(GetPaintingDetail.class);
        Call<List<PaintingData>> call = api.GetPaintingDetail(painting_id);
        call.enqueue(new Callback<List<PaintingData>>() //enqueue: 데이터를 입력하는 함수
        {
            @Override
            public void onResponse(@NonNull Call<List<PaintingData>> call, @NonNull Response<List<PaintingData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "그림강좌 call back 정상!");

                    generateFeedList(response.body());

                }
            }

            private void generateFeedList(List<PaintingData> body){

                //리사이클러뷰 형성
                recyclerView = (RecyclerView) findViewById(R.id.recycler_paintingdetail);

                paintingDetailAdapter = new PaintingDetailAdapter(Activity_PaintingDetail.this, body);
                recyclerView.setAdapter(paintingDetailAdapter);

                //게시글이 비었을 때
//                if(body.size()!=0){
//                    emptyimage2.setVisibility(View.INVISIBLE);
//                    emptytext2.setVisibility(View.INVISIBLE);
//                }else{
//                    emptyimage2.setVisibility(View.VISIBLE);
//                    emptytext2.setVisibility(View.VISIBLE);
//                }

                //리사이클러뷰 연결
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Activity_PaintingDetail.this,LinearLayoutManager.VERTICAL,false);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(linearLayoutManager);

                paintingDetailAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<List<PaintingData>> call, Throwable t) {
                Log.e("Fail", "call back 실패" + t.getMessage());

            }
        });

        //작성일
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(created_date);
            DateFormat format2 = new SimpleDateFormat("yyyy년 M월 d일");
            painting_created.setText(format2.format(date)); //작성일
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //뒤로가기
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting_detail);
    }
}