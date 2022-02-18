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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.palettegrap.etc.Join;
import com.example.palettegrap.R;
import com.example.palettegrap.etc.Join_pass;

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
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Activity_Profile extends AppCompatActivity {

    private final String TAG = "Activity_profile";

    int GALLEY_CODE = 1;
    public static final int REQUEST_PERMISSION = 11;

    String currentPhotoPath;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkPermission();

        Button btn_profile = (Button) findViewById(R.id.btn_profile);
        Button btn_skip = (Button) findViewById(R.id.btn_skip);
        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);

        //프로필 이미지 등록
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Profile.this);

                builder.setTitle("업로드할 이미지 선택").setMessage("\n");

                builder.setPositiveButton("사진촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        checkPermission();

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = null; //촬영한 사진을 저장할 파일 생성
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                        }

                        // 파일이 정상적으로 생성되었다면 진행
                        if (photoFile != null) {
                            //Uri 가져오기
                            Uri providerURI = FileProvider.getUriForFile( Activity_Profile.this , "com.example.palettegrap.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI); //intent에 uri담기
                            setResult(RESULT_OK);
                            result2Launcher.launch(takePictureIntent); //intent 실행
                        }
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                builder.setNeutralButton("앨범선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        checkPermission();

                        Intent intent =new Intent();
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

        //프로필 이미지 등록 후 회원가입
        btn_profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(profileimage.getDrawable() == null){
                    Toast.makeText(getApplicationContext(),"이미지를 등록해주세요", Toast.LENGTH_SHORT).show();
                }else{
                    register(); //회원가입 완료!
                    Intent intent = new Intent(Activity_Profile.this, Activity_Login.class);
                    Toast.makeText(getApplicationContext(),"회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            }
        });

        //건너뛰기
        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                register(); //회원가입 완료!

                Intent intent = new Intent(Activity_Profile.this, Activity_Login.class);
                Toast.makeText(getApplicationContext(),"회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });
    }

    //사진 촬영
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
                            profileimage.setImageBitmap(rotateBitmap); //프로필 이미지 등록!

                                saveFile(Uri.fromFile(file));

                                //쉐어드에 경로 저장
                                SharedPreferences sharedPreferences = getSharedPreferences("autologin", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("member_image", currentPhotoPath);
                                editor.apply();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    //앨범에서 선택
    ActivityResultLauncher<Intent> resultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);

                        Intent imagedata = result.getData();
                        Uri uri = imagedata.getData();

                        String photoroute = createCopyAndReturnRealPath(Activity_Profile.this,uri); // 절대경로 가져오기!

//                      Uri imageUrl=uri.parse(createCopyAndReturnRealPath(Activity_profile.this,uri));
                        Glide.with(Activity_Profile.this).load(uri).into(profileimage);

                        //쉐어드에 앨범에서 선택된 이미지 경로 저장
                        SharedPreferences sharedPreferences = getSharedPreferences("join", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("member_image", photoroute);
                        editor.apply();

                    }
                }
            });

    //회원등록!(이메일, 패스워드, 닉네임, 이미지)
    private void register()
    {
        ImageView profileimage = (ImageView) findViewById(R.id.profileimage);
        SharedPreferences pref = getSharedPreferences("join",MODE_PRIVATE);

        //건너뛰기 할 시(프로필 이미지 생략)
        if(profileimage.getDrawable() == null){
            String member_email = pref.getString("member_email","_");
            String member_pw = pref.getString("member_pw","_");
            String member_nick = pref.getString("member_nick","_");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Join_pass.Join_URL)
                    .addConverterFactory(ScalarsConverterFactory.create()) //HTTP 통신 시에 주고받는 데이터 형태를 변환시켜 주는 컨버터 지정 설정
                    .build();

            Join_pass api = retrofit.create(Join_pass.class);

            //RequestBody로 Multipart.part 객체 생성
            Call<String> call = api.getUserJoin(member_email, member_pw, member_nick);

            call.enqueue(new Callback<String>() //enqueue: 데이터를 입력하는 함수
            {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        Log.e("Success", "정상 입력");
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

            //프로필 이미지 포함 회원가입 시
        }else{
            String member_email2 = pref.getString("member_email","_");
            String member_pw2 = pref.getString("member_pw","_");
            String member_nick2 = pref.getString("member_nick","_");
            String member_image2 = pref.getString("member_image", "_");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Join.Join_URL)
                    .addConverterFactory(ScalarsConverterFactory.create()) //HTTP 통신 시에 주고받는 데이터 형태를 변환시켜 주는 컨버터 지정 설정
                    .build();

            Join api = retrofit.create(Join.class);

            File file = new File(member_image2); //절대경로를 가져와서 file 객체를 만들었다=> 이미지 파일을 만듦

            RequestBody requestBody1 = RequestBody.create(MediaType.parse("text/plain"), member_email2); //이메일
            RequestBody requestBody2 = RequestBody.create(MediaType.parse("text/plain"), member_pw2); //PW
            RequestBody requestBody3 = RequestBody.create(MediaType.parse("text/plain"), member_nick2); //닉네임
            RequestBody requestBody4 = RequestBody.create(MediaType.parse("image/*"), file); //프로필 이미지

            //RequestBody로 Multipart.part 객체 생성
            MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), requestBody4); //서버에서 받는 키값 String, 파일 이름 String, 파일 경로를 가지는 RequestBody 객체
            Call<String> call = api.getUserJoin(requestBody1, requestBody2, requestBody3,image);

            call.enqueue(new Callback<String>() //enqueue: 데이터를 입력하는 함수
            {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {


                        Log.e("Success", "정상 입력");



                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
    }


    //bitmap->string으로 바꾸기
    public String bitmapToString(Bitmap bitmap){
        String image = "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //JPEG가 속도가 가장 빠름
        byte[] byteArray = stream.toByteArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            image = Base64.getEncoder().encodeToString(byteArray); // base64의 라이브러리에서 encodeToString를 이용해서 byte[] 형식을 String 형식으로 변환합니다.
        }
        return image;
    }


    //절대경로 파악할 때 사용된 메서드
    public static String createCopyAndReturnRealPath(Context context, Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;
        //파일 경로 만듦
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            //매개변수로 받은 uri를 통해 이미지에 필요한 데이터를 불러들인다
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            //이미지 데이터를 다시 내보내면서 file객체에 만들었던 경로를 이용한다
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
                //권한이 취소되면 result 배열은 비어있다
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 확인", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();
                    finish(); // 권한이 없으면 앱 종료
                }
            }
        }
    }


    // 파일 생성
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 참고: getExternalFilesDir() 또는 getFilesDir()에서 제공한 디렉터리에 저장한 파일은 사용자가 앱을 제거할 때 삭제됩니다.

        File image = File.createTempFile(
                imageFileName,   //파일이름
                ".jpeg",    //파일형식
                storageDir      //경로
        );
        //ACTION_VIEW 인텐트를 사용할 경로(임시파일 경로)
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }


    // 파일 저장
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

                // 갱신
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
        int bufferSize = 1024; // 버퍼 크기
        byte[] buffer = new byte[bufferSize]; // 버퍼 배열

        int len = 0;
        // InputStream에서 읽어올 게 없을 때까지 바이트 배열에 쓴다.
        while ((len = iStream.read(buffer)) != -1)
            byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }

    // 갤러리 갱신
    private void galleryAddPic(String Image_Path) {

        Log.d("Woongs","갱신 : "+Image_Path);

        // 이전 사용 방식
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

    //권한 확인
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //권한이 없으면 권한 요청
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }
}