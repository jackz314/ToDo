package com.jackz314.todo;

/**
 * Created by jack on 2/28/18.
 */

import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.jackz314.todo.BuildConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class SetEdgeColor {
    private SetEdgeColor() {
        throw new AssertionError("No instances!");
    }

    private static final Class<ScrollView> CLASS_SCROLL_VIEW = ScrollView.class;
    private static final Field SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static final Field SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Class<ViewPager> CLASS_VIEW_PAGER = ViewPager.class;
    private static final Field VIEW_PAGER_FIELD_LEFT_EDGE;
    private static final Field VIEW_PAGER_FIELD_RIGHT_EDGE;

    private static final Class<HorizontalScrollView> CLASS_HORIZONTAL_SCROLL_VIEW = HorizontalScrollView.class;
    private static final Field HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT;
    private static final Field HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT;

    private static final Class<NestedScrollView> CLASS_NESTED_SCROLL_VIEW = NestedScrollView.class;
    private static final Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static final Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Class<RecyclerView> CLASS_RECYCLER_VIEW = RecyclerView.class;
    private static final Field RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP;
    private static final Field RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT;
    private static final Field RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT;
    private static final Field RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Class<AbsListView> CLASS_LIST_VIEW = AbsListView.class;
    private static final Field LIST_VIEW_FIELD_EDGE_GLOW_TOP;
    private static final Field LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Field EDGE_GLOW_FIELD_EDGE;
    private static final Field EDGE_GLOW_FIELD_GLOW;

    private static final Field EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT;

    static {
        Field edgeGlowTop = null, edgeGlowBottom = null, edgeGlowLeft = null, edgeGlowRight = null;

        for (Field f : CLASS_RECYCLER_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mTopGlow":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mBottomGlow":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
                case "mLeftGlow":
                    f.setAccessible(true);
                    edgeGlowLeft = f;
                    break;
                case "mRightGlow":
                    f.setAccessible(true);
                    edgeGlowRight = f;
                    break;
            }
        }

        RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;
        RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT = edgeGlowLeft;
        RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT = edgeGlowRight;

        for (Field f : CLASS_VIEW_PAGER.getDeclaredFields()) {
            switch (f.getName()) {
                case "mLeftEdge":
                    f.setAccessible(true);
                    edgeGlowLeft = f;
                    break;
                case "mRightEdge":
                    f.setAccessible(true);
                    edgeGlowRight = f;
                    break;
            }
        }

        VIEW_PAGER_FIELD_LEFT_EDGE = edgeGlowLeft;
        VIEW_PAGER_FIELD_RIGHT_EDGE = edgeGlowRight;

        for (Field f : CLASS_NESTED_SCROLL_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }

        NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;

        for (Field f : CLASS_SCROLL_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }

        SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;

        for (Field f : CLASS_HORIZONTAL_SCROLL_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowLeft":
                    f.setAccessible(true);
                    edgeGlowLeft = f;
                    break;
                case "mEdgeGlowRight":
                    f.setAccessible(true);
                    edgeGlowRight = f;
                    break;
            }
        }

        HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT = edgeGlowLeft;
        HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT = edgeGlowRight;

        for (Field f : CLASS_LIST_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }

        LIST_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Field edge = null, glow = null;

            Class cls = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                cls = EdgeEffect.class;
            } else {
                try {
                    cls = Class.forName("android.widget.EdgeGlow");
                } catch (ClassNotFoundException e) {
                    if (BuildConfig.DEBUG) e.printStackTrace();
                }
            }

            if (cls != null) {
                for (Field f : cls.getDeclaredFields()) {
                    switch (f.getName()) {
                        case "mEdge":
                            f.setAccessible(true);
                            edge = f;
                            break;
                        case "mGlow":
                            f.setAccessible(true);
                            glow = f;
                            break;
                    }
                }
            }

            EDGE_GLOW_FIELD_EDGE = edge;
            EDGE_GLOW_FIELD_GLOW = glow;
        } else {
            EDGE_GLOW_FIELD_EDGE = null;
            EDGE_GLOW_FIELD_GLOW = null;
        }

        Field efc = null;
        try {
            efc = EdgeEffectCompat.class.getDeclaredField("mEdgeEffect");
            efc.setAccessible(true);
        } catch (NoSuchFieldException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
        EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT = efc;
    }

    public @interface EdgeGlowColorApi {}

    public static final int ALWAYS = Integer.MAX_VALUE;
    /** Replace yellow glow in vanilla, blue glow on Samsung. */
    public static final int PRE_HONEYCOMB = Build.VERSION_CODES.HONEYCOMB;
    /** Replace Holo blue glow. */
    public static final int PRE_KITKAT = Build.VERSION_CODES.KITKAT;
    /** Replace Holo grey glow. */
    public static final int PRE_LOLLIPOP = Build.VERSION_CODES.LOLLIPOP;

    public static void setEdgeColor(
            @NonNull AbsListView listView, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(listView, color);
        }
    }

    public static void setEdgeColor(@NonNull AbsListView listView, @ColorInt int color) {
        try {
            Object ee;
            ee = LIST_VIEW_FIELD_EDGE_GLOW_TOP.get(listView);
            setEdgeColor(ee, color);
            ee = LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(listView);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setEdgeColor(
            @NonNull ScrollView scrollView, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(scrollView, color);
        }
    }

    public static void setEdgeColor(@NonNull ScrollView scrollView, @ColorInt int color) {
        try {
            Object ee;
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEdgeColor(ee, color);
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setEdgeColor(
            @NonNull HorizontalScrollView scrollView, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(scrollView, color);
        }
    }

    public static void setEdgeColor(@NonNull HorizontalScrollView scrollView, @ColorInt int color) {
        try {
            Object ee;
            ee = HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT.get(scrollView);
            setEdgeColor(ee, color);
            ee = HORIZONTAL_SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT.get(scrollView);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setEdgeColor(
            @NonNull ViewPager viewPager, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(viewPager, color);
        }
    }

    public static void setEdgeColor(@NonNull ViewPager viewPager, @ColorInt int color) {
        try {
            Object ee;
            ee = VIEW_PAGER_FIELD_LEFT_EDGE.get(viewPager);
            setEdgeColor(ee, color);
            ee = VIEW_PAGER_FIELD_RIGHT_EDGE.get(viewPager);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setEdgeColor(
            @NonNull NestedScrollView scrollView, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(scrollView, color);
        }
    }

    static void setEdgeColor(@NonNull NestedScrollView scrollView, @ColorInt int color) {
        try {
            Object ee;
            ee = NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEdgeColor(ee, color);
            ee = NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void setEdgeColor(
            @NonNull RecyclerView scrollView, @ColorInt int color, @EdgeGlowColorApi int when) {
        if (Build.VERSION.SDK_INT < when) {
            setEdgeColor(scrollView, color);
        }
    }

    static void setEdgeColor(@NonNull RecyclerView scrollView, @ColorInt int color) {
        try {
            Object ee;
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEdgeColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEdgeColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT.get(scrollView);
            setEdgeColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT.get(scrollView);
            setEdgeColor(ee, color);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void setEdgeColor(@NonNull Object edgeEffect, @ColorInt int color) {
        if (edgeEffect instanceof EdgeEffectCompat) {
            // EdgeEffectCompat
            try {
                edgeEffect = EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT.get(edgeEffect);
            } catch (IllegalAccessException e) {
                return;
            }
        }

        if (edgeEffect == null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // EdgeGlow or old EdgeEffect
            try {
                final Drawable mEdge = (Drawable) EDGE_GLOW_FIELD_EDGE.get(edgeEffect);
                final Drawable mGlow = (Drawable) EDGE_GLOW_FIELD_GLOW.get(edgeEffect);
                mEdge.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                mGlow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                mEdge.setCallback(null); // free up any references
                mGlow.setCallback(null); // free up any references
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        } else {
            // EdgeEffect
            ((EdgeEffect) edgeEffect).setColor(color);
            if(((EdgeEffect) edgeEffect).getColor() != color){
                ((EdgeEffect) edgeEffect).setColor(color);
            }
        }
    }
}

