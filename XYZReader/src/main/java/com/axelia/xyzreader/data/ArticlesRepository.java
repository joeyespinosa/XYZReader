package com.axelia.xyzreader.data;

import com.axelia.xyzreader.data.model.Article;
import com.axelia.xyzreader.remote.ArticleService;
import com.axelia.xyzreader.utils.AppExecutors;

import java.util.Collections;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@Singleton
public class ArticlesRepository {

    private final AppExecutors mExecutors;
    private final ArticleService mArticleService;

    @Inject
    public ArticlesRepository(ArticleService articleService,
                              AppExecutors executors) {
        mArticleService = articleService;
        mExecutors = executors;
    }

    public LiveData<List<Article>> getArticles() {
        final MutableLiveData<List<Article>> articleListLiveData = new MutableLiveData<>();
        mArticleService.getArticles().enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                if (response.isSuccessful()) {
                    List<Article> data = response.body();
                    List<Article> articleList = data != null ? data : Collections.<Article>emptyList();
                    Timber.d("Parsing finished. number of articles: %s", articleList.size());
                    articleListLiveData.postValue(articleList);
                } else {
                    Timber.d("Error code: %s", response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                Timber.d("Unknown error: %s", t.getMessage());
            }
        });
        return articleListLiveData;
    }
}
