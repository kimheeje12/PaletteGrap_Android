package com.example.palettegrap.view.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.palettegrap.R;
import com.example.palettegrap.etc.FollowCancel;
import com.example.palettegrap.etc.FollowClick;
import com.example.palettegrap.etc.GetFollower;
import com.example.palettegrap.etc.GetFollowing;
import com.example.palettegrap.etc.SearchFollower;
import com.example.palettegrap.item.FeedData;
import com.example.palettegrap.view.activity.Activity_Follow;
import com.example.palettegrap.view.activity.Activity_Main;
import com.example.palettegrap.view.adapter.FollowerAdapter;
import com.example.palettegrap.view.adapter.FollowingAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Fragment_Follower extends Fragment {

    public static List<FeedData> feedList;
    private FollowerAdapter followerAdapter;
    private RecyclerView recyclerView;

    ViewGroup rootView;

    public Fragment_Follower() {
    }

    @Override
    public void onStart() {
        super.onStart();

        EditText search_follower = (EditText) rootView.findViewById(R.id.search_follower); //Search follower ???
        TextView follower_not_found = (TextView) rootView.findViewById(R.id.follower_not_found); //Search ?????? ??? ?????? ??????

        ImageView follow_add = (ImageView) rootView.findViewById(R.id.follow_add); //????????? ????????? ??? ?????????
        TextView follow_add2 = (TextView) rootView.findViewById(R.id.follow_add2); //????????? ????????? ??? ??????1(?????????)
        TextView follow_add3 = (TextView) rootView.findViewById(R.id.follow_add3); //????????? ????????? ??? ??????2

        SharedPreferences pref = this.getActivity().getSharedPreferences("autologin", MODE_PRIVATE);
        String email = pref.getString("inputemail", null); //?????? ???????????? ??????

        SharedPreferences pref2 = this.getActivity().getSharedPreferences("tmp_follow", MODE_PRIVATE);
        String mypage_member_email = pref2.getString("mypage_member_email", null); //mypage?????? ????????? ????????? ??????(?????? ???????????? ???????????? ?????? ??????, ?????? ?????? ??????)
        String otherpage_member_email = pref2.getString("otherpage_member_email", null); //?????? ?????? mypage?????? ????????? ????????? ??????

        //????????????????????? ????????? ???
        try {
            if (mypage_member_email != null) {
                Gson gson3 = new GsonBuilder().setLenient().create();

                Retrofit retrofit3 = new Retrofit.Builder()
                        .baseUrl(GetFollower.GetFollower_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson3))
                        .build();

                GetFollower api3 = retrofit3.create(GetFollower.class);
                Call<List<FeedData>> call3 = api3.GetFollower(mypage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "follower call back ??????!");

                            generateFeedList(response.body());

                            //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                            followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    try {
                                        if (feedData.getMember_email().equals(email)) {
                                            Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                            intent2.putExtra("mypage", 1);
                                            startActivity(intent2);
                                        } else {
                                            //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                            SharedPreferences pref = getContext().getSharedPreferences("otherprofile", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("otheremail", feedData.getMember_email());
                                            editor.apply();
                                            Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                            intent2.putExtra("mypage", 2);
                                            startActivity(intent2);
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            });

                            //????????? ???????????? -> ????????? '??????'(??????->??????)
                            followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    Gson gson = new GsonBuilder().setLenient().create();

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(FollowCancel.FollowCancel_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

                                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                    FollowCancel api = retrofit.create(FollowCancel.class);
                                    Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                    {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Log.e("Success", "followClick ??????!");

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Log.e("Fail", "call back ??????" + t.getMessage());

                                        }
                                    });
                                }
                            });

                            //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                            followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    Gson gson = new GsonBuilder().setLenient().create();

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(FollowClick.FollowClick_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

                                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                    FollowClick api = retrofit.create(FollowClick.class);
                                    Call<String> call = api.FollowClick(requestBody, requestBody2);
                                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                    {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Log.e("Success", "followClick ??????!");

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Log.e("Fail", "call back ??????" + t.getMessage());

                                        }
                                    });
                                }
                            });

                            //edittext ?????????????????? ?????? ?????? & ???????????? ??????
                            search_follower.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                    if (search_follower.getText().toString().equals("")) { //Search Follower ?????? ?????? ??????!

                                        Gson gson3 = new GsonBuilder().setLenient().create();

                                        Retrofit retrofit3 = new Retrofit.Builder()
                                                .baseUrl(GetFollower.GetFollower_URL)
                                                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                .addConverterFactory(GsonConverterFactory.create(gson3))
                                                .build();

                                        GetFollower api3 = retrofit3.create(GetFollower.class);
                                        Call<List<FeedData>> call3 = api3.GetFollower(mypage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                                        call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                                        {
                                            @Override
                                            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    Log.e("Success", "follower call back ??????!");

                                                    generateFeedList(response.body());

                                                    //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                                                    followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            try {
                                                                if (feedData.getMember_email().equals(email)) {
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 1);
                                                                    startActivity(intent2);
                                                                } else {
                                                                    //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                                                    SharedPreferences pref = getContext().getSharedPreferences("otherprofile", MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = pref.edit();
                                                                    editor.putString("otheremail", feedData.getMember_email());
                                                                    Log.e("otheremail check", "otheremail check" + feedData.getMember_email());
                                                                    editor.apply();
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 2);
                                                                    startActivity(intent2);
                                                                }
                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    });

                                                    //????????? ???????????? -> ????????? '??????'(??????->??????)
                                                    followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {





                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowCancel.FollowCancel_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowCancel api = retrofit.create(FollowCancel.class);
                                                            Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });

                                                    //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                                                    followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowClick.FollowClick_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowClick api = retrofit.create(FollowClick.class);
                                                            Call<String> call = api.FollowClick(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                if(response.body()==null){
                                                    follower_not_found.setVisibility(View.VISIBLE);
                                                    generateFeedList(null);
                                                }else{
                                                    follower_not_found.setVisibility(View.INVISIBLE);
                                                }

                                            }

                                            private void generateFeedList(List<FeedData> body) {

                                                //?????????????????? ??????
                                                recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                                                followerAdapter = new FollowerAdapter(getActivity(), body);
                                                recyclerView.setAdapter(followerAdapter);

                                                //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                                                //???????????? ????????? ???
                                                try{
                                                    if(body.size()!=0){
                                                        follow_add.setVisibility(View.INVISIBLE);
                                                        follow_add2.setVisibility(View.INVISIBLE);
                                                        follow_add3.setVisibility(View.INVISIBLE);
                                                    }else{
                                                        follow_add.setVisibility(View.VISIBLE);
                                                        follow_add2.setVisibility(View.VISIBLE);
                                                        follow_add3.setVisibility(View.VISIBLE);
                                                    }
                                                }catch (Exception e){

                                                }

                                                //?????????????????? ??????
                                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                                recyclerView.setLayoutManager(linearLayoutManager);

                                                followerAdapter.notifyDataSetChanged();

                                            }

                                            @Override
                                            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                                Log.e("Fail", "call back ??????" + t.getMessage());

                                            }
                                        });
                                    }

                                    //Search Follower ?????? ??????!
                                    if (!search_follower.getText().toString().equals("")) {
                                        {
                                            //????????? ?????? ?????????
                                            Gson gson3 = new GsonBuilder().setLenient().create();

                                            Retrofit retrofit3 = new Retrofit.Builder()
                                                    .baseUrl(SearchFollower.SearchFollower_URL)
                                                    .addConverterFactory(ScalarsConverterFactory.create()) //Response??? String ????????? ?????? ????????? ????????????!
                                                    .addConverterFactory(GsonConverterFactory.create(gson3))
                                                    .build();

                                            SearchFollower api3 = retrofit3.create(SearchFollower.class);
                                            Call<List<FeedData>> call3 = api3.SearchFollower(search_follower.getText().toString(), mypage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                                            call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                                            {
                                                @Override
                                                public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        Log.e("Success", "follower call back ??????!");

                                                        generateFeedList(response.body());

                                                        //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                                                        followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(View view, int position) {

                                                                FeedData feedData = response.body().get(position);

                                                                try {
                                                                    if (feedData.getMember_email().equals(email)) {
                                                                        Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                        intent2.putExtra("mypage", 1);
                                                                        startActivity(intent2);
                                                                    } else {
                                                                        //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                                                        SharedPreferences pref = getContext().getSharedPreferences("otherprofile", MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = pref.edit();
                                                                        editor.putString("otheremail", feedData.getMember_email());
                                                                        Log.e("otheremail check", "otheremail check" + feedData.getMember_email());
                                                                        editor.apply();
                                                                        Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                        intent2.putExtra("mypage", 2);
                                                                        startActivity(intent2);
                                                                    }
                                                                } catch (Exception e) {

                                                                }
                                                            }
                                                        });

                                                        //????????? ???????????? -> ????????? '??????'(??????->??????)
                                                        followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(View view, int position) {



                                                                FeedData feedData = response.body().get(position);

                                                                Gson gson = new GsonBuilder().setLenient().create();

                                                                Retrofit retrofit = new Retrofit.Builder()
                                                                        .baseUrl(FollowCancel.FollowCancel_URL)
                                                                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                                                        .build();

                                                                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                                FollowCancel api = retrofit.create(FollowCancel.class);
                                                                Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                                                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                                {
                                                                    @Override
                                                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                        if (response.isSuccessful() && response.body() != null) {
                                                                            Log.e("Success", "followClick ??????!");

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {
                                                                        Log.e("Fail", "call back ??????" + t.getMessage());

                                                                    }
                                                                });
                                                            }
                                                        });

                                                        //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                                                        followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(View view, int position) {

                                                                FeedData feedData = response.body().get(position);

                                                                Gson gson = new GsonBuilder().setLenient().create();

                                                                Retrofit retrofit = new Retrofit.Builder()
                                                                        .baseUrl(FollowClick.FollowClick_URL)
                                                                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                                                        .build();

                                                                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                                RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                                FollowClick api = retrofit.create(FollowClick.class);
                                                                Call<String> call = api.FollowClick(requestBody, requestBody2);
                                                                call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                                {
                                                                    @Override
                                                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                        if (response.isSuccessful() && response.body() != null) {
                                                                            Log.e("Success", "followClick ??????!");

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {
                                                                        Log.e("Fail", "call back ??????" + t.getMessage());

                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                    if(response.body()==null){
                                                        follower_not_found.setVisibility(View.VISIBLE);
                                                        generateFeedList(null);

                                                    }else{
                                                        follower_not_found.setVisibility(View.INVISIBLE);
                                                    }
                                                }

                                                private void generateFeedList(List<FeedData> body) {

                                                    //?????????????????? ??????
                                                    recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                                                    followerAdapter = new FollowerAdapter(getActivity(), body);
                                                    recyclerView.setAdapter(followerAdapter);

                                                    //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                                                    //???????????? ????????? ???
                                                    try{
                                                        if(body.size()!=0){
                                                            follow_add.setVisibility(View.INVISIBLE);
                                                            follow_add2.setVisibility(View.INVISIBLE);
                                                            follow_add3.setVisibility(View.INVISIBLE);
                                                        }else{
                                                            follow_add.setVisibility(View.VISIBLE);
                                                            follow_add2.setVisibility(View.VISIBLE);
                                                            follow_add3.setVisibility(View.VISIBLE);
                                                        }
                                                    }catch (Exception e){

                                                    }

                                                    //?????????????????? ??????
                                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                                    recyclerView.setLayoutManager(linearLayoutManager);

                                                    followerAdapter.notifyDataSetChanged();

                                                }

                                                @Override
                                                public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }

                    private void generateFeedList(List<FeedData> body) {

                        //?????????????????? ??????
                        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                        followerAdapter = new FollowerAdapter(getActivity(), body);
                        recyclerView.setAdapter(followerAdapter);

                        //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                        //???????????? ????????? ???
                        try{
                            if(body.size()!=0){
                                follow_add.setVisibility(View.INVISIBLE);
                                follow_add2.setVisibility(View.INVISIBLE);
                                follow_add3.setVisibility(View.INVISIBLE);
                            }else{
                                follow_add.setVisibility(View.VISIBLE);
                                follow_add2.setVisibility(View.VISIBLE);
                                follow_add3.setVisibility(View.VISIBLE);
                            }
                        }catch (Exception e){

                        }

                        //?????????????????? ??????
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(linearLayoutManager);

                        followerAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(Call<List<FeedData>> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });
            }


            //?????? ????????????????????? ????????? ?????? ???
            else {
                //????????? ?????? ?????????
                Gson gson3 = new GsonBuilder().setLenient().create();

                Retrofit retrofit3 = new Retrofit.Builder()
                        .baseUrl(GetFollower.GetFollower_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                        .addConverterFactory(GsonConverterFactory.create(gson3))
                        .build();

                GetFollower api3 = retrofit3.create(GetFollower.class);
                Call<List<FeedData>> call3 = api3.GetFollower(otherpage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                {
                    @Override
                    public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("Success", "call back ??????!");

                            generateFeedList(response.body());

                            //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                            followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    try {
                                        if (feedData.getMember_email().equals(email)) {
                                            Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                            intent2.putExtra("mypage", 1);
                                            startActivity(intent2);
                                        } else {
                                            //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                            SharedPreferences pref = getActivity().getPreferences(MODE_PRIVATE);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("otheremail", feedData.getMember_email());
                                            editor.apply();
                                            Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                            intent2.putExtra("mypage", 2);
                                            startActivity(intent2);
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            });

                            //????????? ???????????? -> ????????? '??????'(??????->??????)
                            followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    Gson gson = new GsonBuilder().setLenient().create();

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(FollowCancel.FollowCancel_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

                                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                    FollowCancel api = retrofit.create(FollowCancel.class);
                                    Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                    {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Log.e("Success", "followClick ??????!");

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Log.e("Fail", "call back ??????" + t.getMessage());

                                        }
                                    });
                                }
                            });

                            //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                            followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    FeedData feedData = response.body().get(position);

                                    Gson gson = new GsonBuilder().setLenient().create();

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(FollowClick.FollowClick_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

                                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                    FollowClick api = retrofit.create(FollowClick.class);
                                    Call<String> call = api.FollowClick(requestBody, requestBody2);
                                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                    {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Log.e("Success", "followClick ??????!");

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Log.e("Fail", "call back ??????" + t.getMessage());

                                        }
                                    });
                                }
                            });
                            search_follower.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                    if (search_follower.getText().toString().equals("")) { //Search Follower ?????? ?????? ??????!
                                        Gson gson3 = new GsonBuilder().setLenient().create();

                                        Retrofit retrofit3 = new Retrofit.Builder()
                                                .baseUrl(GetFollower.GetFollower_URL)
                                                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                .addConverterFactory(GsonConverterFactory.create(gson3))
                                                .build();

                                        GetFollower api3 = retrofit3.create(GetFollower.class);
                                        Call<List<FeedData>> call3 = api3.GetFollower(otherpage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                                        call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                                        {
                                            @Override
                                            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    Log.e("Success", "call back ??????!");

                                                    generateFeedList(response.body());

                                                    //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                                                    followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            try {
                                                                if (feedData.getMember_email().equals(email)) {
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 1);
                                                                    startActivity(intent2);
                                                                } else {
                                                                    //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                                                    SharedPreferences pref = getActivity().getPreferences(MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = pref.edit();
                                                                    editor.putString("otheremail", feedData.getMember_email());
                                                                    editor.apply();
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 2);
                                                                    startActivity(intent2);
                                                                }
                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    });

                                                    //????????? ???????????? -> ????????? '??????'(??????->??????)
                                                    followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowCancel.FollowCancel_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowCancel api = retrofit.create(FollowCancel.class);
                                                            Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });

                                                    //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                                                    followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowClick.FollowClick_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowClick api = retrofit.create(FollowClick.class);
                                                            Call<String> call = api.FollowClick(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                if(response.body()==null){
                                                    follower_not_found.setVisibility(View.VISIBLE);
                                                    generateFeedList(null);

                                                }else{
                                                    follower_not_found.setVisibility(View.INVISIBLE);
                                                }
                                            }

                                            private void generateFeedList(List<FeedData> body) {

                                                //?????????????????? ??????
                                                recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                                                followerAdapter = new FollowerAdapter(getActivity(), body);
                                                recyclerView.setAdapter(followerAdapter);

                                                //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                                                //???????????? ????????? ???
                                                try{
                                                    if(body.size()!=0){
                                                        follow_add.setVisibility(View.INVISIBLE);
                                                        follow_add2.setVisibility(View.INVISIBLE);
                                                        follow_add3.setVisibility(View.INVISIBLE);
                                                    }else{
                                                        follow_add.setVisibility(View.VISIBLE);
                                                        follow_add2.setVisibility(View.VISIBLE);
                                                        follow_add3.setVisibility(View.VISIBLE);
                                                    }
                                                }catch (Exception e){

                                                }

                                                //?????????????????? ??????
                                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                                recyclerView.setLayoutManager(linearLayoutManager);

                                                followerAdapter.notifyDataSetChanged();

                                            }

                                            @Override
                                            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                                Log.e("Fail", "call back ??????" + t.getMessage());

                                            }
                                        });
                                    }
                                    if (!search_follower.getText().toString().equals("")) {//Search Follower ?????? ??????!
                                        //????????? ?????? ?????????
                                        Gson gson3 = new GsonBuilder().setLenient().create();

                                        Retrofit retrofit3 = new Retrofit.Builder()
                                                .baseUrl(SearchFollower.SearchFollower_URL)
                                                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                .addConverterFactory(GsonConverterFactory.create(gson3))
                                                .build();

                                        SearchFollower api3 = retrofit3.create(SearchFollower.class);
                                        Call<List<FeedData>> call3 = api3.SearchFollower(search_follower.getText().toString(), otherpage_member_email, email); //????????? ???????????? ?????? ????????? ?????????/????????? ??? ??? ????????????(????????? ???????????? ?????? ?????? ?????? ????????????)
                                        call3.enqueue(new Callback<List<FeedData>>() //enqueue: ???????????? ???????????? ??????
                                        {
                                            @Override
                                            public void onResponse(@NonNull Call<List<FeedData>> call, @NonNull Response<List<FeedData>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    Log.e("Success", "call back ??????!");

                                                    generateFeedList(response.body());

                                                    //?????? ????????? ????????? ?????? ??? ?????? ?????????????????? ??????
                                                    followerAdapter.setOnItemClickListener(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            try {
                                                                if (feedData.getMember_email().equals(email)) {
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 1);
                                                                    startActivity(intent2);
                                                                } else {
                                                                    //?????? ?????? ????????? ?????? ?????????(?????? ?????? ??????????????? ???????????? ??? ???????????? ???????????? ??????)
                                                                    SharedPreferences pref = getActivity().getPreferences(MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = pref.edit();
                                                                    editor.putString("otheremail", feedData.getMember_email());
                                                                    editor.apply();
                                                                    Intent intent2 = new Intent(getActivity(), Activity_Main.class);
                                                                    intent2.putExtra("mypage", 2);
                                                                    startActivity(intent2);
                                                                }
                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    });

                                                    //????????? ???????????? -> ????????? '??????'(??????->??????)
                                                    followerAdapter.setOnItemClickListener2(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowCancel.FollowCancel_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowCancel api = retrofit.create(FollowCancel.class);
                                                            Call<String> call = api.FollowCancel(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });

                                                    //????????? ????????????(?????? ?????? ???????????????) -> ?????????(??????->??????)
                                                    followerAdapter.setOnItemClickListener3(new FollowerAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(View view, int position) {

                                                            FeedData feedData = response.body().get(position);

                                                            Gson gson = new GsonBuilder().setLenient().create();

                                                            Retrofit retrofit = new Retrofit.Builder()
                                                                    .baseUrl(FollowClick.FollowClick_URL)
                                                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .build();

                                                            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), email); //?????? ????????? ?????? ?????????
                                                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feedData.getMember_email()); //?????? ?????????

                                                            FollowClick api = retrofit.create(FollowClick.class);
                                                            Call<String> call = api.FollowClick(requestBody, requestBody2);
                                                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                                                            {
                                                                @Override
                                                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Log.e("Success", "followClick ??????!");

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<String> call, Throwable t) {
                                                                    Log.e("Fail", "call back ??????" + t.getMessage());

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                if(response.body()==null){
                                                    follower_not_found.setVisibility(View.VISIBLE);
                                                    generateFeedList(null);

                                                }else{
                                                    follower_not_found.setVisibility(View.INVISIBLE);
                                                }
                                            }

                                            private void generateFeedList(List<FeedData> body) {

                                                //?????????????????? ??????
                                                recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                                                followerAdapter = new FollowerAdapter(getActivity(), body);
                                                recyclerView.setAdapter(followerAdapter);

                                                //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                                                //???????????? ????????? ???
                                                try{
                                                    if(body.size()!=0){
                                                        follow_add.setVisibility(View.INVISIBLE);
                                                        follow_add2.setVisibility(View.INVISIBLE);
                                                        follow_add3.setVisibility(View.INVISIBLE);
                                                    }else{
                                                        follow_add.setVisibility(View.VISIBLE);
                                                        follow_add2.setVisibility(View.VISIBLE);
                                                        follow_add3.setVisibility(View.VISIBLE);
                                                    }
                                                }catch (Exception e){

                                                }

                                                //?????????????????? ??????
                                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                                recyclerView.setLayoutManager(linearLayoutManager);

                                                followerAdapter.notifyDataSetChanged();

                                            }

                                            @Override
                                            public void onFailure(Call<List<FeedData>> call, Throwable t) {
                                                Log.e("Fail", "call back ??????" + t.getMessage());

                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }

                    private void generateFeedList(List<FeedData> body) {

                        //?????????????????? ??????
                        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_follower);

                        followerAdapter = new FollowerAdapter(getActivity(), body);
                        recyclerView.setAdapter(followerAdapter);

                        //follower_count.setText(String.valueOf(body.size())); // ????????? ??????(???????????? int -> String?????? ?????????!)

                        //???????????? ????????? ???
                        try{
                            if(body.size()!=0){
                                follow_add.setVisibility(View.INVISIBLE);
                                follow_add2.setVisibility(View.INVISIBLE);
                                follow_add3.setVisibility(View.INVISIBLE);
                            }else{
                                follow_add.setVisibility(View.VISIBLE);
                                follow_add2.setVisibility(View.VISIBLE);
                                follow_add3.setVisibility(View.VISIBLE);
                            }
                        }catch (Exception e){

                        }

                        //?????????????????? ??????
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(linearLayoutManager);

                        followerAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(Call<List<FeedData>> call, Throwable t) {
                        Log.e("Fail", "call back ??????" + t.getMessage());

                    }
                });
            }

        } catch (Exception e) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_follower, container, false);

        return rootView;

    }

}