package com.example.palettegrap.view.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palettegrap.R;
import com.example.palettegrap.etc.GetMaster;
import com.example.palettegrap.etc.GetPainting;
import com.example.palettegrap.etc.MasterCheckInput;
import com.example.palettegrap.etc.PaintingCheckInput;
import com.example.palettegrap.item.MasterData;
import com.example.palettegrap.item.PaintingData;
import com.example.palettegrap.view.activity.Activity_Masterpiece;
import com.example.palettegrap.view.activity.Activity_MasterpieceDetail;
import com.example.palettegrap.view.activity.Activity_MasterpieceUpload;
import com.example.palettegrap.view.activity.Activity_PaintingDetail;
import com.example.palettegrap.view.activity.Activity_PaintingUpload;
import com.example.palettegrap.view.adapter.MasterpieceAdapter;
import com.example.palettegrap.view.adapter.PaintingAdapter;
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

public class Fragment_ArtStory extends Fragment {

    private MasterpieceAdapter masterpieceAdapter;
    private PaintingAdapter paintingAdapter;
    private RecyclerView recyclerView;

    ViewGroup rootView;

    public Fragment_ArtStory(){

    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences pref = this.getActivity().getSharedPreferences("autologin", MODE_PRIVATE);
        String loginemail = pref.getString("inputemail", null); //?????? ???????????? ??????

        Button story_upload = (Button) rootView.findViewById(R.id.upload);
        ImageView btn_masterpiece = (ImageView) rootView.findViewById(R.id.btn_masterpiece);
        TextView masterpiece_count = (TextView) rootView.findViewById(R.id.masterpiece_count);
        TextView painting_count = (TextView) rootView.findViewById(R.id.painting_count);

        //????????? ?????? & ???????????? ????????? ???
        ImageView emptyimage = (ImageView) rootView.findViewById(R.id.emptyimage);
        ImageView emptyimage2 = (ImageView) rootView.findViewById(R.id.emptyimage2);
        TextView emptytext = (TextView) rootView.findViewById(R.id.emptytext);
        TextView emptytext2 = (TextView) rootView.findViewById(R.id.emptytext2);

        //????????? ?????????(???????????? & ????????????)
        //????????????(kimheeje@naver.com)??? ????????? ????????? ???????????? ???????????? ???????????? ?????? ??????????????? ??????!
        story_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(loginemail.equals("kimheeje@naver.com")){
                    final String[] items ={"????????? ?????? ????????????", "???????????? ????????????","??????"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("PaletteGrap");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                Intent intent = new Intent(getActivity(), Activity_MasterpieceUpload.class);
                                startActivity(intent);
                            }if(i==1){
                                Intent intent = new Intent(getActivity(), Activity_PaintingUpload.class);
                                startActivity(intent);
                            } else{
                            }
                        }
                    });
                    builder.show();
                }else{
                    final String[] items ={"???????????? ?????????","??????"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("PaletteGrap");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                Intent intent = new Intent(getActivity(), Activity_PaintingUpload.class);
                                startActivity(intent);
                            }
                            else{
                            }
                        }
                    });
                    builder.show();
                }
            }
        });

        //????????? ????????? ??????
        btn_masterpiece.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Activity_Masterpiece.class);
                startActivity(intent);
            }
        });

        //??????????????? ??????
        Gson gson3 = new GsonBuilder().setLenient().create();

        Retrofit retrofit3 = new Retrofit.Builder()
                .baseUrl(GetMaster.GetMaster_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson3))
                .build();

        GetMaster api3 = retrofit3.create(GetMaster.class);
        Call<List<MasterData>> call3 = api3.GetMaster(loginemail);
        call3.enqueue(new Callback<List<MasterData>>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<List<MasterData>> call, @NonNull Response<List<MasterData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "call back ??????!");

                    generateFeedList(response.body());

                    masterpieceAdapter.setOnItemClickListener(new MasterpieceAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            MasterData masterData = response.body().get(position);

                            Intent intent = new Intent(getActivity(), Activity_MasterpieceDetail.class);
                            intent.putExtra("member_email", masterData.getMember_email());
                            intent.putExtra("master_id", masterData.getMaster_id());
                            intent.putExtra("master_title", masterData.getMaster_title());
                            intent.putExtra("master_artist", masterData.getMaster_artist());
                            intent.putExtra("master_image", masterData.getMaster_image());
                            intent.putExtra("master_story", masterData.getMaster_story());
                            intent.putExtra("master_created", masterData.getMaster_created());
                            startActivity(intent);

                            //?????? ???????????? ????????? ?????????/?????? ??????????????? mastercheck table??? ?????????
                            Gson gson = new GsonBuilder().setLenient().create();

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(MasterCheckInput.MasterCheckInput_URL)
                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                    .build();

                            MasterCheckInput api = retrofit.create(MasterCheckInput.class);

                            RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), loginemail); //?????????
                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), masterData.getMaster_id()); //?????? ????????????

                            Call<String> call = api.MasterCheckInput(requestBody1,requestBody2);
                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                            {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.e("Success", "mastercheck ??????!");

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
            }

            private void generateFeedList(List<MasterData> body){

                //?????????????????? ??????
                recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_masterpiece);

                masterpieceAdapter = new MasterpieceAdapter(getActivity(), body);
                recyclerView.setAdapter(masterpieceAdapter);

                //?????? ?????? ?????????
                masterpiece_count.setText(String.valueOf(body.size())+"???");

                //???????????? ????????? ???
                if(body.size()!=0){
                    emptyimage.setVisibility(View.INVISIBLE);
                    emptytext.setVisibility(View.INVISIBLE);
                }else{
                    emptyimage.setVisibility(View.VISIBLE);
                    emptytext.setVisibility(View.VISIBLE);
                }

                //?????????????????? ??????
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(linearLayoutManager);

                masterpieceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<List<MasterData>> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });

        //????????????????????? ??????
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GetPainting.GetPainting_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetPainting api = retrofit.create(GetPainting.class);
        Call<List<PaintingData>> call = api.GetPainting(loginemail);
        call.enqueue(new Callback<List<PaintingData>>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<List<PaintingData>> call, @NonNull Response<List<PaintingData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Success", "???????????? call back ??????!");

                    generateFeedList(response.body());

                    paintingAdapter.setOnItemClickListener(new PaintingAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            PaintingData paintingData = response.body().get(position);

                            Intent intent = new Intent(getActivity(), Activity_PaintingDetail.class);
                            intent.putExtra("member_email", paintingData.getMember_email());
                            intent.putExtra("member_image", paintingData.getMember_image());
                            intent.putExtra("member_nick", paintingData.getMember_nick());
                            intent.putExtra("like_count", paintingData.getLike_count());
                            intent.putExtra("painting_id", paintingData.getPainting_id());
                            intent.putExtra("painting_content_id", paintingData.getPainting_content_id());
                            intent.putExtra("painting_title", paintingData.getPainting_title());
                            intent.putExtra("painting_iamge_path", paintingData.getPainting_image_path());
                            intent.putExtra("painting_created", paintingData.getPainting_created());
                            intent.putExtra("painting_text", paintingData.getPainting_text());
                            intent.putExtra("position", position);
                            startActivity(intent);

                            //?????? ???????????? ????????? ?????????/?????? ??????????????? mastercheck table??? ?????????
                            Gson gson = new GsonBuilder().setLenient().create();

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(PaintingCheckInput.PaintingCheckInput_URL)
                                    .addConverterFactory(ScalarsConverterFactory.create()) // Response??? String ????????? ?????? ????????? ????????????!
                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                    .build();

                            PaintingCheckInput api = retrofit.create(PaintingCheckInput.class);

                            RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), loginemail); //?????????
                            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), paintingData.getPainting_id()); //???????????? id

                            Call<String> call = api.PaintingCheckInput(requestBody1,requestBody2);
                            call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                            {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.e("Success", "???????????? checkinput ??????!");

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
            }

            private void generateFeedList(List<PaintingData> body){

                //?????????????????? ??????
                recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_painting);

                paintingAdapter = new PaintingAdapter(getActivity(), body);
                recyclerView.setAdapter(paintingAdapter);

                //???????????? ?????? ?????????
                painting_count.setText(String.valueOf("("+body.size())+"???"+")");

                //???????????? ????????? ???
                if(body.size()!=0){
                    emptyimage2.setVisibility(View.INVISIBLE);
                    emptytext2.setVisibility(View.INVISIBLE);
                }else{
                    emptyimage2.setVisibility(View.VISIBLE);
                    emptytext2.setVisibility(View.VISIBLE);
                }

                //?????????????????? ??????
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(linearLayoutManager);

                paintingAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<List<PaintingData>> call, Throwable t) {
                Log.e("Fail", "call back ??????" + t.getMessage());

            }
        });
    }

    @Nullable
    @Override //fragment??? Mainfragment??? ???????????? ????????? ?????? ?????????
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //(????????? ??????, ?????? ?????? ???, T/F) -> ????????? ?????? ???????????? T / ?????????????????? ????????? F
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_artstory, container, false);

        return rootView;
    }
}
