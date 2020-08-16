package com.axelia.xyzreader.ui.details;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.axelia.xyzreader.R;
import com.axelia.xyzreader.data.model.Article;
import com.axelia.xyzreader.databinding.FragmentArticleDetailBinding;
import com.axelia.xyzreader.ui.HomeActivity;
import com.axelia.xyzreader.utils.UiUtils;
import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import timber.log.Timber;

public class ArticleDetailFragment extends Fragment {

    public static final String ARGS_ARTICLE_DATA = "ARGS_ARTICLE_DATA";

    private FragmentArticleDetailBinding mBinding;
    private Article mArticle;

    public ArticleDetailFragment() { }

    public static ArticleDetailFragment newInstance(Article article) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_ARTICLE_DATA, article);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARGS_ARTICLE_DATA)) {
            mArticle = getArguments().getParcelable(ARGS_ARTICLE_DATA);
        }
        setHasOptionsMenu(true);
    }

    public HomeActivity getActivityCast() {
        return (HomeActivity) getActivity();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView");
        mBinding = FragmentArticleDetailBinding.inflate(inflater, container, false);
        ViewCompat.setTransitionName(mBinding.photo, String.valueOf(mArticle.getId()));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        insetLayout();
        setupUi();
    }

    private void insetLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            final CoordinatorLayout coordinatorLayout = mBinding.drawInsetsFrameLayout;
            ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) mBinding.toolbar.getLayoutParams();
                    lpToolbar.topMargin = insets.getSystemWindowInsetTop();
                    mBinding.toolbar.setLayoutParams(lpToolbar);

                    v.setOnApplyWindowInsetsListener(null);
                    return insets.consumeSystemWindowInsets();
                }
            });
            ViewCompat.requestApplyInsets(coordinatorLayout);
        }
    }

    private void setupToolbar() {
        final Toolbar toolbar = mBinding.toolbar;
        getActivityCast().setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ArticleDetailFragment.this).navigateUp();
            }
        });

        if (getActivityCast().getSupportActionBar() != null) {
            getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            handleCollapsedToolbarTitle();
        }
    }

    private void handleCollapsedToolbarTitle() {
        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    mBinding.collapsingToolbar.setTitle(mArticle.getTitle());
                    isShow = true;
                } else if (isShow) {
                    mBinding.collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }


    private void setupUi() {
        mBinding.textviewArticleTitle.setText(mArticle.getTitle());
        Date publishedDate = UiUtils.parsePublishedDate(mArticle.getPublished_date());
        mBinding.textviewArticleByline.setText(UiUtils.formatArticleByline(publishedDate, mArticle.getAuthor()));
        mBinding.textviewArticleBody.setText(Html.fromHtml(mArticle.getBody().replaceAll("(\r\n|\n)", "<br />")));

        Glide.with(this)
                .asBitmap()
                .load(mArticle.getPhoto_url())
                .dontAnimate()
                .placeholder(R.color.photo_placeholder)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object
                            model, Target<Bitmap> target, boolean isFirstResource) {
                        scheduleStartPostponedTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette p) {
                                Palette.Swatch swatch = UiUtils.getDominantColor(p);
                                if (swatch != null) {
                                    mBinding.metaBar.setBackgroundColor(swatch.getRgb());
                                    mBinding.collapsingToolbar.setContentScrimColor(swatch.getRgb());
                                    if (mBinding.cardContentContainer != null) {
                                        mBinding.cardContentContainer.setStrokeColor(swatch.getRgb());
                                    }
                                }
                            }
                        });
                        scheduleStartPostponedTransition();
                        return false;
                    }
                })
                .into(mBinding.photo);

        mBinding.executePendingBindings();
    }

    private void scheduleStartPostponedTransition() {
        mBinding.getRoot().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBinding.getRoot().getViewTreeObserver().removeOnPreDrawListener(this);
                getParentFragment().startPostponedEnterTransition();
                return true;
            }
        });
    }
}
