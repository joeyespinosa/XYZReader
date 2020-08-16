package com.axelia.xyzreader.remote;

import com.axelia.xyzreader.data.model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ArticleService {

    @GET("data.json")
    Call<List<Article>> getArticles();
}
