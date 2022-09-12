package com.coursee.free.rests;

import com.coursee.free.callbacks.CallbackAds;
import com.coursee.free.callbacks.CallbackCategories;
import com.coursee.free.callbacks.CallbackCategoryDetails;
import com.coursee.free.callbacks.CallbackListVideo;
import com.coursee.free.callbacks.CallbackUser;
import com.coursee.free.callbacks.CallbackVideoDetail;
import com.coursee.free.models.Setting;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your Videos Channel";

    @Headers({CACHE, AGENT})
    @GET("api/get_videos")
    Call<CallbackListVideo> getVideos(
            @Query("page") int page,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_post_detail")
    Call<CallbackVideoDetail> getVideoDetail(
            @Query("id") String id
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index")
    Call<CallbackCategories> getAllCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_videos")
    Call<CallbackCategoryDetails> getCategoryVideos(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results")
    Call<CallbackListVideo> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_ads")
    Call<CallbackAds> getAds(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_privacy_policy")
    Call<Setting> getPrivacyPolicy(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_user_token")
    Call<CallbackUser> getUserToken(
            @Query("user_unique_id") String user_unique_id
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_package_name")
    Call<Setting> getPackageName();

}
