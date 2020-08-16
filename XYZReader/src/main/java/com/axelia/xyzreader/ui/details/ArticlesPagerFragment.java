package com.axelia.xyzreader.ui.details;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.TransitionInflater;
import androidx.viewpager.widget.ViewPager;

import com.axelia.xyzreader.R;
import com.axelia.xyzreader.XYZApplication;
import com.axelia.xyzreader.data.model.Article;
import com.axelia.xyzreader.ui.ArticlesViewModel;
import com.axelia.xyzreader.ui.HomeActivity;
import com.axelia.xyzreader.utils.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;


public class ArticlesPagerFragment extends Fragment {

    private ArticlesViewModel mViewModel;
    private ArticlesPagerAdapter mPagerAdapter;
    private ViewPager mPager;

    @Inject
    ViewModelFactory factory;

    public ArticlesPagerFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            postponeEnterTransition();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(
                    TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }

        XYZApplication.getComponent(Objects.requireNonNull(getActivity())).inject(this);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity(), factory).get(ArticlesViewModel.class);
        setupPagerAdapter(view);
        setupSharedElementTransition();
    }

    private void setupSharedElementTransition() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Integer selectedPosition = mViewModel.getCurrentSelectedPosition();
                Fragment currentFragment = (Fragment) mPagerAdapter.instantiateItem(mPager, selectedPosition);
                View view = currentFragment.getView();
                if (view == null) {
                    return;
                }

                sharedElements.put(names.get(0), view.findViewById(R.id.photo));
            }
        });
    }

    private void setupPagerAdapter(View view) {

        mPagerAdapter = new ArticlesPagerAdapter(getChildFragmentManager());
        mPager = view.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mViewModel.getArticlesListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Article>>() {
            @Override
            public void onChanged(List<Article> articles) {
                if (articles != null) {
                    mPagerAdapter.submitList(articles);
                    mPager.setCurrentItem(mViewModel.getCurrentSelectedPosition(), false);
                }
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mViewModel.setCurrentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
