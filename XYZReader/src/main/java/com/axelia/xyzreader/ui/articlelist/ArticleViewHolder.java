package com.axelia.xyzreader.ui.articlelist;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.axelia.xyzreader.R;
import com.axelia.xyzreader.data.model.Article;
import com.axelia.xyzreader.databinding.ListItemArticleBinding;
import com.axelia.xyzreader.ui.articlelist.ArticleListFragment.ArticleItemsClickListener;
import com.axelia.xyzreader.utils.UiUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.Date;

import timber.log.Timber;

public class ArticleViewHolder extends RecyclerView.ViewHolder {

    private final ListItemArticleBinding binding;
    private ArticleItemsClickListener listener;

    public ArticleViewHolder(@NonNull ListItemArticleBinding binding, ArticleItemsClickListener listener) {
        super(binding.getRoot());

        this.binding = binding;
        this.listener = listener;
    }

    public static ArticleViewHolder create(ViewGroup parent, ArticleItemsClickListener listener) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemArticleBinding binding =
                ListItemArticleBinding.inflate(layoutInflater, parent, false);
        return new ArticleViewHolder(binding, listener);
    }

    public void bindTo(final Article article) {
        final int adapterPosition = getAdapterPosition();

        binding.textviewArticleTitle.setText(article.getTitle());
        Date publishedDate = UiUtils.parsePublishedDate(article.getPublished_date());
        binding.textviewArticleSubtitle.setText(UiUtils.formatArticleByline(publishedDate, article.getAuthor()));
        binding.thumbnail.setAspectRatio(article.getAspect_ratio());
        Glide.with(binding.getRoot().getContext())
                .asBitmap()
                .load(article.getThumb_url())
                .dontAnimate()
                .placeholder(R.color.photo_placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(
                        (int) UiUtils.dipToPixels(binding.getRoot().getContext(), 6))))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        listener.onLoadCompleted(adapterPosition);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                                   DataSource dataSource, boolean isFirstResource) {

                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette p) {
                                Palette.Swatch swatch = UiUtils.getDominantColor(p);
                                if (swatch != null) {
                                    MaterialCardView cardView = (MaterialCardView) binding.getRoot();
                                    cardView.setCardBackgroundColor(swatch.getRgb());
                                    cardView.setStrokeColor(swatch.getRgb());
                                }
                            }
                        });
                        listener.onLoadCompleted(adapterPosition);
                        return false;
                    }
                })
                .into(binding.thumbnail);
        ViewCompat.setTransitionName(binding.thumbnail, String.valueOf(article.getId()));
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("Article Clicked at position: " + adapterPosition + ", ID of: " + article.getId());
                listener.onClick(binding.thumbnail, String.valueOf(article.getId()), adapterPosition);
            }
        });

        binding.executePendingBindings();
    }
}
