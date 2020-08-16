package com.axelia.xyzreader.utils;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.palette.graphics.Palette;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;


public class UiUtils {

    private static SimpleDateFormat outputFormat = new SimpleDateFormat();
    private static GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public static Palette.Swatch getDominantColor(Palette palette) {
        Palette.Swatch result;
        if (palette.getLightVibrantSwatch() != null) {
            result = palette.getLightVibrantSwatch();
        } else {
            result = palette.getDominantSwatch();
        }
        return result;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static Date parsePublishedDate(String publishDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
        try {
            String date = publishDate;
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.e("Passing today's date");
            return new Date();
        }
    }

    public static Spanned formatArticleByline(Date publishedDate, String author) {
        Spanned byline;
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            byline = Html.fromHtml(DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + " by <font color='#ffffff'>" + author + "</font>");
        } else {

            byline = Html.fromHtml(outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                    + author + "</font>");
        }
        return byline;
    }
}
