package com.axelia.xyzreader.di;

import com.axelia.xyzreader.di.modules.AppExecutorsModule;
import com.axelia.xyzreader.di.modules.ArticleServiceModule;
import com.axelia.xyzreader.ui.HomeActivity;
import com.axelia.xyzreader.ui.articlelist.ArticleListFragment;
import com.axelia.xyzreader.ui.details.ArticlesPagerFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppExecutorsModule.class,
        ArticleServiceModule.class
})
public interface ArticleComponent {

    void inject(ArticleListFragment articleListFragment);
    void inject(ArticlesPagerFragment articlesPagerFragment);
}
