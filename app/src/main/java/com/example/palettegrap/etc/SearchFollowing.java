package com.example.palettegrap.etc;

import com.example.palettegrap.item.FeedData;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SearchFollowing {

    String SearchFollowing_URL = "http://3.35.11.53/";

    @FormUrlEncoded
    @POST("SearchFollowing.php")
    Call<List<FeedData>> SearchFollowing(
            @Field("search_nick") String search_nick,
            @Field("member_email") String member_email,
            @Field("login_email") String login_email);
}
