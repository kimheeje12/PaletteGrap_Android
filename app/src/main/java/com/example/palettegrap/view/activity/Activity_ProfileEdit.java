package com.example.palettegrap.view.activity;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.palettegrap.R;
import com.example.palettegrap.etc.EmailCheck;
import com.example.palettegrap.etc.GetNick;
import com.example.palettegrap.etc.Join;
import com.example.palettegrap.view.fragment.Fragment_Chat;
import com.example.palettegrap.view.fragment.Fragment_Mypage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_ProfileEdit extends AppCompatActivity {
    public static final int REQUEST_PERMISSION = 10;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Button btn_back = (Button) findViewById(R.id.backbtn);
        Button profileimage_edit = (Button) findViewById(R.id.profileimage_edit);
        TextView email = (TextView) findViewById(R.id.email);
        TextView emailedit = (TextView) findViewById(R.id.emailedit);
        TextView nickname = (TextView) findViewById(R.id.nickname);
        TextView nickedit = (TextView) findViewById(R.id.nickedit);
        TextView pwedit = (TextView) findViewById(R.id.pwedit);
        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);

        //?????? ????????? ????????? & ?????? ????????? ?????? ????????? ??????!
        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
        String profile_image = pref.getString("inputimage","_");
        String member_email = pref.getString("inputemail","_");
        Glide.with(Activity_ProfileEdit.this).load(profile_image).into(profileimage);
        email.setText(member_email);

        //????????? ????????????!(?????? ???????????? ??????)
        getnickname();
        String member_nick = pref.getString("inputnick","_");
        nickname.setText(member_nick);

        //?????? ??????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //????????? ??????
        emailedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Activity_ProfileEdit.this, Activity_EmailEdit.class);
                startActivity(intent);
                finish();
            }
        });

        //????????? ??????
        nickedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_ProfileEdit.this, Activity_NickEdit.class);
                startActivity(intent);
                finish();

            }
        });

        //???????????? ??????
        pwedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_ProfileEdit.this, Activity_PwEdit.class);
                startActivity(intent);
                finish();

            }
        });

        //????????? ????????? ??????
        profileimage_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_ProfileEdit.this);

                builder.setTitle("???????????? ????????? ??????").setMessage("\n");

                builder.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        checkPermission();

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = null; //????????? ????????? ????????? ?????? ??????
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e("Woongs",ex.getMessage().toString());
                        }

                        // ????????? ??????????????? ?????????????????? ??????
                        if (photoFile != null) {
                            //Uri ????????????
                            Uri providerURI = FileProvider.getUriForFile(Activity_ProfileEdit.this, "com.example.palettegrap.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI); //intent??? uri??????
                            setResult(RESULT_OK);
                            result2Launcher.launch(takePictureIntent); //intent ??????
                        }
                    }
                });

                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                builder.setNeutralButton("????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        checkPermission();

                        Intent intent =new Intent();
//                      intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                        intent.setType("image/*");
                        intent.setAction(intent.ACTION_GET_CONTENT);
                        intent.putExtra("galleryimage", 1);
                        setResult(RESULT_OK);
                        resultLauncher.launch(intent);
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    //?????? ??????
    ActivityResultLauncher<Intent> result2Launcher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {

                        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);

                        Intent imagedata = result.getData();

                        File file = new File(currentPhotoPath);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.fromFile(file));

                            if(bitmap !=null){

                                ExifInterface ei = new ExifInterface(currentPhotoPath);
                                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                        ExifInterface.ORIENTATION_UNDEFINED);

                                Bitmap rotateBitmap = null;
                                switch(orientation){
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        rotateBitmap = rotateImage(bitmap,90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        rotateBitmap = rotateImage(bitmap,180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        rotateBitmap = rotateImage(bitmap,270);
                                        break;
                                    case ExifInterface.ORIENTATION_NORMAL:
                                    default:
                                        rotateBitmap=bitmap;
                                        break;
                                }
                                profileimage.setImageBitmap(rotateBitmap); //????????? ????????? ??????!

                                saveFile(Uri.fromFile(file));

                                //???????????? ?????? ??????
                                SharedPreferences sharedPreferences = getSharedPreferences("profileupdate", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("profileimage", String.valueOf(file));
                                editor.apply();
                                imageupdate();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    //???????????? ??????
    ActivityResultLauncher<Intent> resultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);

                        Intent imagedata = result.getData();
                        Uri uri = imagedata.getData();

                        String photoroute = createCopyAndReturnRealPath(Activity_ProfileEdit.this,uri); // ???????????? ????????????!

//                      Uri imageUrl=uri.parse(createCopyAndReturnRealPath(Activity_profile.this,uri));
                        Glide.with(Activity_ProfileEdit.this).load(uri).into(profileimage);

                        //???????????? ???????????? ????????? ????????? ?????? ??????
                        SharedPreferences sharedPreferences = getSharedPreferences("profileupdate", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profileimage", photoroute);
                        editor.apply();
                        imageupdate();

                    }
                }
            });


    //????????? ????????????!
    private void imageupdate(){
        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
        SharedPreferences pref2 = getSharedPreferences("profileupdate", MODE_PRIVATE);

        String member_email = pref.getString("inputemail","_");
        String member_image = pref2.getString("profileimage", "_");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Join.Join_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) //HTTP ?????? ?????? ???????????? ????????? ????????? ???????????? ?????? ????????? ?????? ??????
                .build();

        Join api = retrofit.create(Join.class);

        File file = new File(member_image); //??????????????? ???????????? file ????????? ????????????=> ????????? ????????? ??????

        RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), member_email); //?????????
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), ""); //PW
        RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), ""); //?????????
        RequestBody requestBody4 = RequestBody.create(MediaType.parse("image/*"), file); //????????? ?????????
        Log.d("???????????????", "?????????"+requestBody4);


        //RequestBody??? Multipart.part ?????? ??????
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), requestBody4); //???????????? ?????? ?????? String, ?????? ?????? String, ?????? ????????? ????????? RequestBody ??????
        Call<String> call = api.getUserJoin(requestBody1, requestBody2, requestBody3,image);
        Log.d("???????????????2", "?????????2"+image);

        call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e("Success", "?????? ??????");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    //bitmap->string?????? ?????????
    public String bitmapToString(Bitmap bitmap){
        String image = "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //JPEG??? ????????? ?????? ??????
        byte[] byteArray = stream.toByteArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            image = Base64.getEncoder().encodeToString(byteArray); // base64??? ????????????????????? encodeToString??? ???????????? byte[] ????????? String ???????????? ???????????????.
        }
        return image;
    }


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


    public void onRequestPermissionResult(int requestCode, String permission[], int[] grantResults) {
        switch (1000) {
            case REQUEST_PERMISSION: {
                //????????? ???????????? result ????????? ????????????
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "?????? ??????", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "?????? ??????", Toast.LENGTH_LONG).show();
                    finish(); // ????????? ????????? ??? ??????
                }
            }
        }
    }


    // ?????? ??????
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // ??????: getExternalFilesDir() ?????? getFilesDir()?????? ????????? ??????????????? ????????? ????????? ???????????? ?????? ????????? ??? ???????????????.

        File image = File.createTempFile(
                imageFileName,   //????????????
                ".jpeg",    //????????????
                storageDir      //??????
        );
        //ACTION_VIEW ???????????? ????????? ??????(???????????? ??????)
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }


    // ?????? ??????
    private void saveFile(Uri image_uri) {

        ContentValues values = new ContentValues();
        String fileName = System.currentTimeMillis()+".jpeg";
        values.put(MediaStore.Images.Media.DISPLAY_NAME,fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver contentResolver = getContentResolver();
        Uri item = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            ParcelFileDescriptor pdf = contentResolver.openFileDescriptor(item, "w", null);
            if (pdf == null) {
                Log.d("Woongs", "null");
            } else {
                byte[] inputData = getBytes(image_uri);
                FileOutputStream fos = new FileOutputStream(pdf.getFileDescriptor());
                fos.write(inputData);
                fos.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    contentResolver.update(item, values, null, null);
                }

                // ??????
                galleryAddPic(fileName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("Woongs", "FileNotFoundException  : "+e.getLocalizedMessage());
        } catch (Exception e) {
            Log.d("Woongs", "FileOutputStream = : " + e.getMessage());
        }
    }

    // Uri to ByteArr
    public byte[] getBytes(Uri image_uri) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(image_uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024; // ?????? ??????
        byte[] buffer = new byte[bufferSize]; // ?????? ??????

        int len = 0;
        // InputStream?????? ????????? ??? ?????? ????????? ????????? ????????? ??????.
        while ((len = iStream.read(buffer)) != -1)
            byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }

    // ????????? ??????
    private void galleryAddPic(String Image_Path) {

        Log.d("Woongs","?????? : "+Image_Path);

        // ?????? ?????? ??????
        /*Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(Image_Path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.context.sendBroadcast(mediaScanIntent);*/

        File file = new File(Image_Path);
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{file.toString()},
                null, null);
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

    // ?????? ??????
    private void getnickname() {

        SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);

        String member_email = pref.getString("inputemail","_");

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GetNick.GetNick_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetNick api = retrofit.create(GetNick.class);
        Call<String> call = api.getNick(member_email);
        call.enqueue(new Callback<String>() //enqueue: ???????????? ???????????? ??????
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body();
                    SharedPreferences pref = getSharedPreferences("autologin", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("inputnick", jsonResponse);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}