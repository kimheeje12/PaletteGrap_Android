package com.example.palettegrap.view.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
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
import com.example.palettegrap.R;
import com.example.palettegrap.etc.MasterEdit;
import com.example.palettegrap.etc.MasterpieceUpload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_MasterpieceEdit extends AppCompatActivity {

    public static final int REQUEST_PERMISSION = 8;

    String photoroute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masterpiece_edit);

        Button masterpiece_upload = (Button) findViewById(R.id.masterpiece_upload);
        Button btn_back = (Button) findViewById(R.id.button_back);
        ImageView imageupload = (ImageView) findViewById(R.id.imageupload);
        ImageView masterpiece_image = (ImageView) findViewById(R.id.masterpiece_image);
        EditText masterpiece_title = (EditText) findViewById(R.id.masterpiece_title);
        EditText masterpiece_artist = (EditText) findViewById(R.id.masterpiece_artist);
        EditText masterpiece_story = (EditText) findViewById(R.id.masterpiece_story);
        TextView masterpiece_add = (TextView) findViewById(R.id.masterpiece_add);

        SharedPreferences sharedPreferences = getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        String loginemail=sharedPreferences.getString("inputemail",null);

        Intent intent = getIntent();
        String master_id = intent.getStringExtra("master_id");
        String master_title = intent.getStringExtra("master_title");
        String master_artist = intent.getStringExtra("master_artist");
        String master_image = intent.getStringExtra("master_image");
        String master_story = intent.getStringExtra("master_story");

        masterpiece_title.setText(master_title);
        masterpiece_artist.setText(master_artist);
        masterpiece_story.setText(master_story);
        Glide.with(Activity_MasterpieceEdit.this).load(master_image).into(masterpiece_image);

        //????????? ????????? ??????
        if(masterpiece_image.getDrawable()==null){
            masterpiece_add.setVisibility(View.VISIBLE);
        }else{
            masterpiece_add.setVisibility(View.INVISIBLE);
        }

        //????????????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        //????????? ??????
        imageupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermission();

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                intent.putExtra("galleryimage", 1);
                setResult(RESULT_OK);
                resultLauncher.launch(intent);

            }
        });

        //?????? ?????????
        masterpiece_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (masterpiece_image.getDrawable() == null) {
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????????", Toast.LENGTH_SHORT).show();
                } else if (masterpiece_title.getText().toString().equals("") || masterpiece_artist.getText().toString().equals("") ||
                        masterpiece_story.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????", Toast.LENGTH_SHORT).show();
                }else if(photoroute!=null){
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(MasterEdit.MasterEdit_URL)
                            .addConverterFactory(ScalarsConverterFactory.create()) //HTTP ?????? ?????? ???????????? ????????? ????????? ???????????? ?????? ????????? ?????? ??????
                            .build();
                    MasterEdit api = retrofit.create(MasterEdit.class);

                    File file = new File(photoroute); //??????????????? ???????????? file ????????? ????????????=> ????????? ????????? ??????

                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), master_id);
                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_title.getText().toString());
                    RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_artist.getText().toString());
                    RequestBody requestBody4 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_story.getText().toString());
                    RequestBody requestBody5 = RequestBody.create(MediaType.parse("image/*"), file); //?????????

                    //RequestBody??? Multipart.part ?????? ??????
                    MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), requestBody5);
                    Call<String> call = api.MasterEdit(requestBody, requestBody2, requestBody3, requestBody4, image);
                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.e("Success", "masterpieceuploade ?????? ??????");

                                Intent intent = new Intent(Activity_MasterpieceEdit.this, Activity_Masterpiece.class);
                                Toast.makeText(getApplicationContext(), "????????? ????????? ??????????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }else{
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(MasterEdit.MasterEdit_URL)
                            .addConverterFactory(ScalarsConverterFactory.create()) //HTTP ?????? ?????? ???????????? ????????? ????????? ???????????? ?????? ????????? ?????? ??????
                            .build();
                    MasterEdit api = retrofit.create(MasterEdit.class);

                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), master_id);
                    RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_title.getText().toString());
                    RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_artist.getText().toString());
                    RequestBody requestBody4 = RequestBody.create(MediaType.parse("text/plain"), masterpiece_story.getText().toString());
                    RequestBody requestBody5 = RequestBody.create(MediaType.parse("image/*"), ""); //?????????

                    //RequestBody??? Multipart.part ?????? ??????
                    MultipartBody.Part image = MultipartBody.Part.createFormData("image", "", requestBody5);
                    Call<String> call = api.MasterEdit(requestBody, requestBody2, requestBody3, requestBody4, image);
                    call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.e("Success", "masterpieceuploade ?????? ??????");

                                Intent intent = new Intent(Activity_MasterpieceEdit.this, Activity_Masterpiece.class);
                                Toast.makeText(getApplicationContext(), "????????? ????????? ??????????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });

                }
            }
        });
    }

    //???????????? ??????
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        ImageView masterpiece_image = (ImageView) findViewById(R.id.masterpiece_image);

                        Intent imagedata = result.getData();
                        Uri uri = imagedata.getData();

                        photoroute = createCopyAndReturnRealPath(Activity_MasterpieceEdit.this, uri); // ???????????? ????????????!

                        Glide.with(Activity_MasterpieceEdit.this).load(uri).into(masterpiece_image);

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

    //?????? ??????
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