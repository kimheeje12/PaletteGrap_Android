package com.example.palettegrap.view.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.palettegrap.R;
import com.example.palettegrap.etc.FeedUpload;
import com.example.palettegrap.etc.ItemTouchHelperCallback;
import com.example.palettegrap.etc.ItemTouchHelperListener;
import com.example.palettegrap.view.adapter.ImageUploadAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_FeedUpload extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    public static final int REQUEST_PERMISSION = 10;

    private RecyclerView recyclerView;
    private ImageUploadAdapter imageUploadAdapter;
    private ArrayList<Uri> uriList = new ArrayList<>();

    ItemTouchHelper helper;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_upload);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // edittext ????????? ???????????? UI ????????? ??? ??????

        Button feedback= (Button) findViewById(R.id.feed_back);
        Button feed_upload = (Button) findViewById(R.id.feed_upload);
        Button gallery = (Button) findViewById(R.id.gallery);
        EditText feed_text = (EditText) findViewById(R.id.feed_text);
        TextView feedcategory = (TextView) findViewById(R.id.feed_category);

        EditText drawingtool = (EditText) findViewById(R.id.drawingtool);
        EditText drawingtime = (EditText) findViewById(R.id.drawingtime);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_ImageUpload);
        recyclerView.setHasFixedSize(true);
        //setHasFixedSize? Adapter Item View??? ????????? ??????????????? '????????????????????? ????????? ???????????????'??? ??????
        //????????? ????????? ????????? ????????? ????????????????????? ????????? ???????????? ??????????????? ?????? ??? ????????? ???????????? ?????? ????????? ?????? ????????? ?????????.
        //setHasFixedSize??? true??? ?????????????????? ???????????? ???????????? ?????? ???????????? ??? ??????.

        imageUploadAdapter = new ImageUploadAdapter(uriList, getApplicationContext());

        recyclerView.setAdapter(imageUploadAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Activity_FeedUpload.this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.scrollToPosition(uriList.size()); //???????????? ????????????!

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(imageUploadAdapter));
        helper.attachToRecyclerView(recyclerView);

        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //????????????
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        //???????????? ??????
        feedcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items ={"????????????","??????","??????","??????","????????????","????????????","??????","??????","???????????????","??????"};

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_FeedUpload.this);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            feedcategory.setText("????????????");
                            editor.putString("feed_category", String.valueOf(0));
                            editor.apply();
                        }else if(i==1){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(1));
                            editor.apply();
                        }else if(i==2){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(2));
                            editor.apply();
                        }else if(i==3){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(3));
                            editor.apply();
                        }else if(i==4){
                            feedcategory.setText("????????????");
                            editor.putString("feed_category", String.valueOf(4));
                            editor.apply();
                        }else if(i==5){
                            feedcategory.setText("????????????");
                            editor.putString("feed_category", String.valueOf(5));
                            editor.apply();
                        }else if(i==6){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(6));
                            editor.apply();
                        }else if(i==7){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(7));
                            editor.apply();
                        }else if(i==8){
                            feedcategory.setText("???????????????");
                            editor.putString("feed_category", String.valueOf(8));
                            editor.apply();
                        }else if(i==9){
                            feedcategory.setText("??????");
                            editor.putString("feed_category", String.valueOf(9));
                            editor.apply();
                        }else{
                            Toast.makeText(getApplicationContext(), "??????????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //??????
        imageUploadAdapter.setOnItemClickListener(new ImageUploadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_FeedUpload.this);

                builder.setTitle("?????? ?????? ???????????????????").setMessage("\n");

                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        imageUploadAdapter.remove(position);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //?????? ?????? ?????????(????????? & text)
        feed_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uriList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                }else if(feedcategory.getText().toString().equals("????????????")){
                    Toast.makeText(getApplicationContext(), "??????????????? ??????????????????", Toast.LENGTH_SHORT).show();
                }
                else {
                    String member_email = pref.getString("inputemail", "_");
                    String feed_category = pref.getString("feed_category", "_");

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(FeedUpload.FeedUpload_URL)
                            .addConverterFactory(ScalarsConverterFactory.create()) //HTTP ?????? ?????? ???????????? ????????? ????????? ???????????? ?????? ????????? ?????? ??????
                            .build();
                    FeedUpload api = retrofit.create(FeedUpload.class);

                    ArrayList<MultipartBody.Part> files = new ArrayList<>(); // ?????? ???????????? ????????? arraylist

                    for (int k = 0; k < uriList.size(); ++k) {
                        String photorute = createCopyAndReturnRealPath(Activity_FeedUpload.this, uriList.get(k)); // ???????????? ????????????!

                        File file = new File(photorute);

                        RequestBody requestBody4 = RequestBody.create(MediaType.parse("image/*"), file);

                        //RequestBody??? Multipart.part ?????? ??????
                        MultipartBody.Part image = MultipartBody.Part.createFormData("image" + k, photorute, requestBody4); //???????????? ?????? ?????? String, ?????? ?????? String, ?????? ????????? ????????? RequestBody ??????
                        files.add(image);
                    }
                    RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), member_email); //?????????
                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), feed_text.getText().toString()); //?????? text
                    RequestBody requestBody3 = RequestBody.create(MediaType.parse("*/*"), String.valueOf(uriList.size())); //????????? ?????????
                    RequestBody requestBody4 = RequestBody.create(MediaType.parse("text/plain"), feed_category); //????????? ????????????
                    RequestBody requestBody5 = RequestBody.create(MediaType.parse("text/plain"), drawingtool.getText().toString()); //????????? ???
                    RequestBody requestBody6 = RequestBody.create(MediaType.parse("text/plain"), drawingtime.getText().toString()); //????????????

                    Call<String> call = api.FeedUpload(requestBody1, requestBody2, requestBody5, requestBody6, requestBody3, requestBody4, files);
                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.e("Success", "?????? ?????????, ????????? ??????????????? ??????");
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

        //????????? ??????
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();

                Intent intent =new Intent();
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // ?????? ????????? ????????? ??? ????????? ??????
                intent.setAction(intent.ACTION_GET_CONTENT);
                setResult(RESULT_OK);
                resultLauncher.launch(intent);

            }
        });
    }

    //???????????? ??????
    ActivityResultLauncher<Intent> resultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {

                        if (result == null) { // ????????? ???????????? ???????????? ?????? ??????
                            Toast.makeText(getApplicationContext(), "???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                        } else { // ???????????? ???????????? ????????? ??????
                            if (result.getData().getClipData() == null) {

                                Intent imagedata = result.getData();
                                uri = imagedata.getData();
                                uriList.add(uri);
                                imageUploadAdapter.seturiList(uriList);

                            } else { //???????????? ?????? ??? ????????? ??????
                                ClipData clipData = result.getData().getClipData();

                                if (clipData.getItemCount() > 10) { //????????? ???????????? 11??? ????????? ??????
                                    Toast.makeText(getApplicationContext(), "????????? 10????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                                } else {
                                    for (int i = 0; i < clipData.getItemCount(); i++) {
                                        Uri imageUri = clipData.getItemAt(i).getUri(); // ????????? ??????????????? Uri??? ?????????
                                        try {
                                            uriList.add(imageUri);
                                            imageUploadAdapter.seturiList(uriList);

                                            Log.d("?????????", "?????????"+uriList);

                                        } catch (Exception e) {
                                        }
                                    }
                                }
                            }
                        }
                        imageUploadAdapter.notifyDataSetChanged();
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