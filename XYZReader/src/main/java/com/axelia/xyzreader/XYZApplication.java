package com.axelia.xyzreader;

import android.app.Application;
import android.content.Context;

import com.axelia.xyzreader.di.ArticleComponent;
import com.axelia.xyzreader.di.DaggerArticleComponent;
import com.axelia.xyzreader.di.modules.AppExecutorsModule;
import com.axelia.xyzreader.di.modules.ArticleServiceModule;

import timber.log.Timber;

public class XYZApplication extends Application {

    private ArticleComponent component;

    public static ArticleComponent getComponent(Context context) {
        return ((XYZApplication) context.getApplicationContext()).component;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        component = DaggerArticleComponent.builder()
                .appExecutorsModule(new AppExecutorsModule())
                .articleServiceModule(new ArticleServiceModule())
                .build();
    }
}
