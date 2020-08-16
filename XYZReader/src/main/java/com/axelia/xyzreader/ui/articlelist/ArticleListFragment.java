package com.axelia.xyzreader.ui.articlelist;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.transition.TransitionInflater;

import com.axelia.xyzreader.R;
import com.axelia.xyzreader.XYZApplication;
import com.axelia.xyzreader.data.model.Article;
import com.axelia.xyzreader.databinding.FragmentArticleListBinding;
import com.axelia.xyzreader.ui.ArticlesViewModel;
import com.axelia.xyzreader.ui.HomeActivity;
import com.axelia.xyzreader.ui.details.ArticlesPagerFragment;
import com.axelia.xyzreader.utils.ItemOffsetDecoration;
import com.axelia.xyzreader.utils.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import timber.log.Timber;

public class ArticleListFragment extends Fragment {

    private ArticlesViewModel mViewModel;
    private FragmentArticleListBinding mBinding;
    private AtomicBoolean enterTransitionStarted;

    @Inject
    ViewModelFactory factory;

    public ArticleListFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XYZApplication.getComponent(Objects.requireNonNull(getActivity())).inject(this);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentArticleListBinding.inflate(inflater, container, false);
        mViewModel = ViewModelProviders.of(getActivity(), factory).get(ArticlesViewModel.class);
        setupTransitions();
        setupListAdapter();

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            updateRefreshingUI(true);
        }
        scrollToPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        insetLayout();
    }

    private void insetLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mBinding.coordinator.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {

                    ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams)
                            mBinding.toolbar.getLayoutParams();
                    lpToolbar.topMargin = insets.getSystemWindowInsetTop();
                    mBinding.toolbar.setLayoutParams(lpToolbar);

                    mBinding.recyclerviewArticles.setPadding(
                            mBinding.recyclerviewArticles.getPaddingLeft() + insets.getSystemWindowInsetLeft(),
                            mBinding.recyclerviewArticles.getPaddingTop(),
                            mBinding.recyclerviewArticles.getPaddingRight() + insets.getSystemWindowInsetRight(),
                            mBinding.recyclerviewArticles.getPaddingBottom() + insets.getSystemWindowInsetBottom());

                    v.setOnApplyWindowInsetsListener(null);
                    return insets.consumeSystemWindowInsets();
                }
            });
            ViewCompat.requestApplyInsets(mBinding.coordinator);
        }
    }

    private void scrollToPosition() {
        mBinding.recyclerviewArticles.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mBinding.recyclerviewArticles.removeOnLayoutChangeListener(this);
                final RecyclerView.LayoutManager layoutManager = mBinding.recyclerviewArticles.getLayoutManager();
                View viewAtPosition = layoutManager.findViewByPosition(mViewModel.getCurrentSelectedPosition());

                if (viewAtPosition == null
                        || !(layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)
                        || layoutManager.isViewPartiallyVisible(viewAtPosition, true, true))) {
                    mBinding.recyclerviewArticles.post(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPosition(mViewModel.getCurrentSelectedPosition());
                        }
                    });
                }
            }
        });
    }

    private void setupTransitions() {
        enterTransitionStarted = new AtomicBoolean();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(
                    TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Integer selectedPosition = mViewModel.getCurrentSelectedPosition();
                RecyclerView.ViewHolder selectedViewHolder = mBinding.recyclerviewArticles
                        .findViewHolderForAdapterPosition(selectedPosition);
                if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                    return;
                }

                sharedElements.put(names.get(0),
                        selectedViewHolder.itemView.findViewById(R.id.thumbnail));
            }
        });
    }

    private void setupListAdapter() {
        RecyclerView recyclerviewArticles = mBinding.recyclerviewArticles;
        final ArticlesAdapter adapter = new ArticlesAdapter(clickListener);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerviewArticles.setLayoutManager(sglm);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);
        recyclerviewArticles.addItemDecoration(itemDecoration);
        recyclerviewArticles.setAdapter(adapter);

        mViewModel.getArticlesListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Article>>() {
            @Override
            public void onChanged(List<Article> articles) {
                postponeEnterTransition();
                if (articles != null) {
                    Timber.d("Articles: " + articles.size());
                    updateRefreshingUI(false);
                    adapter.submitList(articles);
                }
            }
        });
    }

    private void updateRefreshingUI(boolean isRefreshing) {
        mBinding.swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    public interface ArticleItemsClickListener {
        void onClick(View sharedView, String sharedElementName, int selectedPosition);

        void onLoadCompleted(int position);
    }

    public ArticleItemsClickListener clickListener = new ArticleItemsClickListener() {
        @Override
        public void onClick(View sharedView, String sharedElementName, int selectedPosition) {
            mViewModel.setCurrentPosition(selectedPosition);

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(sharedView, sharedElementName)
                    .build();

            NavHostFragment.findNavController(ArticleListFragment.this).navigate(
                    R.id.action_article_list_dest_to_articles_pager_dest,
                    null,
                    null,
                    extras);
        }

        @Override
        public void onLoadCompleted(int position) {
            int selectedPosition = mViewModel.getCurrentSelectedPosition();
            if (selectedPosition != position) {
                return;
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return;
            }
            scheduleStartPostponedTransition();
        }
    };

    private void scheduleStartPostponedTransition() {
        mBinding.recyclerviewArticles.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBinding.recyclerviewArticles.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }
}
