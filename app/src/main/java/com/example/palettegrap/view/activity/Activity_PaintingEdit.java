package com.example.palettegrap.view.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.palettegrap.R;
import com.example.palettegrap.etc.PaintingEdit;
import com.example.palettegrap.etc.PaintingUpload;
import com.example.palettegrap.item.FeedData;
import com.example.palettegrap.item.PaintingData;
import com.example.palettegrap.item.PaintingUploadData;
import com.example.palettegrap.view.adapter.PaintingUploadAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_PaintingEdit extends AppCompatActivity {

    public static final int REQUEST_PERMISSION = 3;

    private RecyclerView recyclerView;
    private PaintingUploadAdapter paintingUploadAdapter;
    private List<PaintingUploadData> paintingUploadDataList = new ArrayList<>(); //?????????????????????
    private List<PaintingUploadData> paintingUploadDataList2 = new ArrayList<>(); //????????? ????????? + text??? ????????? ???????????? ????????? ????????? ?????? ??????
    private List<PaintingUploadData> paintingUploadDataList3 = new ArrayList<>(); //?????? ????????? + text??? ????????? ????????? + text ????????? ?????? ?????? ??????

    public List<PaintingData> paintingDataList;

    String photoroute;
    TextView image2;

    String tmp_PaintingPath;

    @Override
    protected void onStart() {
        super.onStart();

        Button btn_back = (Button) findViewById(R.id.button_back);
        Button painting_upload = (Button) findViewById(R.id.painting_upload);
        EditText title = (EditText) findViewById(R.id.title);
        ImageView painting_add = (ImageView) findViewById(R.id.painting_add);
        TextView painting_text = (TextView) findViewById(R.id.painting_text); //??????????????? ?????????????????? ??????
        ImageView image = (ImageView) findViewById(R.id.image);
        EditText painting_explain = (EditText) findViewById(R.id.painting_explain);
        image2 = (TextView) findViewById(R.id.imageupload2); //????????? ????????? ??? ??????(????????? ??????)
        TextView add = (TextView) findViewById(R.id.add);

        //?????? ??? ????????????
        ImageView painting_edit = (ImageView) findViewById(R.id.painting_edit);
        TextView edit = (TextView) findViewById(R.id.edit);

        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
        String loginemail = pref.getString("inputemail", "_");

        //????????? ????????? ????????? ???????????? ?????????????????? ????????????!
        SharedPreferences pref2 = getSharedPreferences("painting", MODE_PRIVATE);
        String painting_id = pref2.getString("painting_id", null);
        String painting_title = pref2.getString("painting_title", null);


        title.setText(painting_title); //???????????? ??????!

        paintingDataList = (List<PaintingData>) getIntent().getSerializableExtra("paintingdata");
        for (int k = 0; k < paintingDataList.size(); ++k) {

            PaintingData paintingData = paintingDataList.get(k);

            PaintingUploadData paintingUploadData = new PaintingUploadData();

            paintingUploadData.setPainting_image_path(paintingData.getPainting_image_path());
            paintingUploadData.setPainting_text(paintingData.getPainting_text());

            paintingUploadDataList.add(paintingUploadData);
            paintingUploadDataList3.add(paintingUploadData); //???????????????

        }
        paintingDataList.clear(); //???????????? ???????????? ??? ???????????? ?????????!

        //?????????????????? ??????
        recyclerView = (RecyclerView) findViewById(R.id.recycler_paintingupload);
        recyclerView.setHasFixedSize(true);

        paintingUploadAdapter = new PaintingUploadAdapter(Activity_PaintingEdit.this, paintingUploadDataList);
        recyclerView.setAdapter(paintingUploadAdapter);

        //???????????? ????????? ???
        if(paintingUploadDataList.size()!=0){
            painting_text.setVisibility(View.INVISIBLE);
        }else{
            painting_text.setVisibility(View.VISIBLE);
        }

        //?????????????????? ??????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Activity_PaintingEdit.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        paintingUploadAdapter.notifyDataSetChanged();


        //?????? ????????? ?????? ??? ????????? ????????? ??????????????? ??????! + ?????? ?????? ??????!
        paintingUploadAdapter.setOnItemClickListener(new PaintingUploadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PaintingUploadData paintingData = paintingUploadDataList.get(position);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(30));
                Glide.with(Activity_PaintingEdit.this).load(paintingData.getPainting_image_path()).apply(requestOptions).into(image);

                painting_explain.setText(paintingData.getPainting_text());
                image2.setVisibility(View.INVISIBLE);

                //?????? ?????? ??????!
                painting_edit.setVisibility(View.VISIBLE);
                edit.setVisibility(View.VISIBLE);

                painting_add.setVisibility(View.INVISIBLE);
                add.setVisibility(View.INVISIBLE);

                //??????????????? ????????? ?????? ????????? ?????? ???????????????!
                painting_edit.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        PaintingUploadData paintingUploadData = new PaintingUploadData();

                        if (painting_explain.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                        } else {
                            if(photoroute==null){
                                paintingUploadData.setPainting_image_path(paintingData.getPainting_image_path());
                                paintingUploadData.setPainting_text(painting_explain.getText().toString());

                                paintingUploadDataList.set(position, paintingUploadData);
                                paintingUploadDataList3.set(position, paintingUploadData);

                                //?????? ?????? ?????? -> ?????? ??????
                                painting_edit.setVisibility(View.INVISIBLE);
                                edit.setVisibility(View.INVISIBLE);

                                painting_add.setVisibility(View.VISIBLE);
                                add.setVisibility(View.VISIBLE);

                                RequestOptions requestOptions = new RequestOptions();
                                requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(30));
                                Glide.with(Activity_PaintingEdit.this).load("").apply(requestOptions).into(image);
                                image2.setVisibility(View.VISIBLE);
                                painting_explain.setText("");

                            }else{
                                paintingUploadData.setPainting_image_path(photoroute);
                                paintingUploadData.setPainting_text(painting_explain.getText().toString());

                                paintingUploadDataList.set(position, paintingUploadData);

                                paintingUploadDataList2.add(paintingUploadData); // ????????? ????????? ??????(????????? ?????????)
                                paintingUploadDataList3.set(position, paintingUploadData);

                                //?????? ?????? ?????? -> ?????? ??????
                                painting_edit.setVisibility(View.INVISIBLE);
                                edit.setVisibility(View.INVISIBLE);

                                painting_add.setVisibility(View.VISIBLE);
                                add.setVisibility(View.VISIBLE);
                            }
                        }
                        paintingUploadAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        //????????? ??????
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermission();

                Intent intent =new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                intent.putExtra("galleryimage", 1);
                setResult(RESULT_OK);
                resultLauncher.launch(intent);

            }
        });

        //??? ????????? ??????
        painting_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (photoroute==null || painting_explain.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                } else {

                    PaintingUploadData paintingUploadData = new PaintingUploadData();

                    paintingUploadData.setPainting_image_path(photoroute);
                    paintingUploadData.setPainting_text(painting_explain.getText().toString());

                    paintingUploadDataList.add(paintingUploadData);

                    paintingUploadDataList2.add(paintingUploadData); //????????? ???????????? ????????? ????????? ??????
                    paintingUploadDataList3.add(paintingUploadData); //?????? ?????????

                    paintingUploadAdapter.notifyDataSetChanged();
                }
            }
        });

        //???????????????(????????? ??????!)
        painting_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title.getText().toString().equals("") || paintingUploadDataList.size()==0){
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????", Toast.LENGTH_SHORT).show();
                }else{
                    Gson gson = new GsonBuilder().setLenient().create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(PaintingEdit.PaintingEdit_URL)
                            .addConverterFactory(ScalarsConverterFactory.create()) //HTTP ?????? ?????? ???????????? ????????? ????????? ???????????? ?????? ????????? ?????? ??????
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                    PaintingEdit api = retrofit.create(PaintingEdit.class);

                    //????????? ?????? ?????????(????????? + text)
                    //???????????? ?????????
                    ArrayList<MultipartBody.Part> files = new ArrayList<>();

                    for (int k = 0; k < paintingUploadDataList2.size(); ++k) {

                        File file = new File(paintingUploadDataList2.get(k).getPainting_image_path());
                        Log.e("????????? ?????? ??????", "????????? ?????? ??????"+paintingUploadDataList2.get(k).getPainting_image_path());

                        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);

                        MultipartBody.Part image = MultipartBody.Part.createFormData("image" + k, file.getName(), requestBody); //???????????? ?????? ?????? String, ?????? ?????? String, ?????? ????????? ????????? RequestBody ??????
                        files.add(image);
                    }

                    //???????????? ?????????
                    for (int k = 0; k < paintingUploadDataList2.size(); ++k) {

                        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), paintingUploadDataList2.get(k).getPainting_text());

                        MultipartBody.Part text = MultipartBody.Part.createFormData("text" + k, paintingUploadDataList2.get(k).getPainting_text(), requestBody); //???????????? ?????? ?????? String, ?????? ?????? String, ?????? ????????? ????????? RequestBody ??????
                        files.add(text);
                    }
                    Log.e("paintingUploadDataList3 check", "paintingUploadDataList3 check"+paintingUploadDataList3);

                    //?????? ????????? + text ????????? ?????????!
                    for (int i = 0; i < paintingUploadDataList3.size(); ++i) {
                        tmp_PaintingPath = tmp_PaintingPath + "///" + paintingUploadDataList3.get(i).getPainting_image_path() + "///" + paintingUploadDataList3.get(i).getPainting_text() + "///" + i + "///"; //????????? ????????? ?????? String ?????????
                    }
                    RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), painting_id); //???????????? id
                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), loginemail); //?????????
                    RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), title.getText().toString()); //??????
                    RequestBody requestBody4 = RequestBody.create(MediaType.parse("*/*"), String.valueOf(paintingUploadDataList2.size())); //?????????(?????????+text) ?????????
                    RequestBody requestBody5 = RequestBody.create(MediaType.parse("*/*"), String.valueOf(paintingUploadDataList3.size())); //?????? ?????????(?????????+text) ?????????
                    RequestBody requestBody6 = RequestBody.create(MediaType.parse("text/plain"), tmp_PaintingPath); //?????? ?????????

                    Call<String> call = api.PaintingEdit(requestBody1, requestBody2, requestBody3, requestBody4, requestBody5, requestBody6, files, files);
                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.e("Success", "???????????? ??????????????? ??????!");
                                finish();
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });
                }
            }
        });

        //????????????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        //??????
        paintingUploadAdapter.setOnItemClickListener2(new PaintingUploadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_PaintingEdit.this);

                builder.setTitle("?????? ?????? ???????????????????").setMessage("\n");

                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        paintingUploadAdapter.remove(position);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting_edit);


    }

    //???????????? ??????
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {

                        ImageView image = (ImageView) findViewById(R.id.image);

                        Intent imagedata = result.getData();
                        Uri uri = imagedata.getData();

                        photoroute = createCopyAndReturnRealPath(Activity_PaintingEdit.this,uri); // ???????????? ????????????!
                        image2.setVisibility(View.INVISIBLE);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(30));

                        Glide.with(Activity_PaintingEdit.this).load(uri).apply(requestOptions).into(image);

                    }
                }
            });

    //???????????? ????????? ??? ????????? ?????????
    public static String createCopyAndReturnRealPath(Context context, Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;
        //?????? ?????? ??????
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            //??????????????? ?????? uri??? ?????? ???????????? ????????? ???????????? ???????????????
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            //????????? ???????????? ?????? ??????????????? file????????? ???????????? ????????? ????????????
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
            return null;
        }
        return file.getAbsolutePath();
    }

    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //????????? ????????? ?????? ??????
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "??? ?????? ???????????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }
}