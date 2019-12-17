package com.kit.tinydrawable;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.LruCache;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.kit.utils.ApiLevel;
import com.kit.utils.DensityUtils;
import com.kit.utils.ValueOf;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.graphics.drawable.GradientDrawable.LINE;
import static android.graphics.drawable.GradientDrawable.OVAL;
import static android.graphics.drawable.GradientDrawable.RECTANGLE;
import static android.graphics.drawable.GradientDrawable.RING;

/**
 * @author Zhao
 */
@SuppressWarnings("AlibabaAvoidDoubleOrFloatEqualCompare")
public class TinyDrawable {
    private volatile static LruCache<String, Drawable> drawableLruCache;

    public static void init(int size) {
        if (drawableLruCache != null) {
            return;
        }
        if (size > 0) {
            drawableLruCache = new LruCache<>(size);
        } else {
            drawableLruCache = new LruCache<>(30);
        }
    }

    public static TinyDrawable setup() {
        if (drawableLruCache == null) {
            init(30);
        }
        return new TinyDrawable();
    }


    public Drawable get() {
        String solidStr = "";
        if (colorStateList != null) {
            solidStr = colorStateList.toString();
        } else {
            solidStr = ValueOf.toString(solid);
        }

        StringBuilder radiiStr = new StringBuilder();
        if (radii != null) {
            boolean allEquals = true;
            float temp = radii[0];
            for (float f : radii) {
                allEquals = allEquals && (temp - f < 0.01);
            }
            if (allEquals) {
                radiiStr = new StringBuilder(ValueOf.toString(radii[0]));
            } else {
                for (float f : radii) {
                    radiiStr.append(ValueOf.toString(f));
                }
            }

        } else {
            solidStr = ValueOf.toString(radius);
        }


        String key = shape + solidStr + stroke + strokeColor + radiiStr;

        Drawable saved = drawableLruCache.get(key);
        if (saved != null) {
            return saved;
        } else {
            GradientDrawable drawable = new GradientDrawable();
            if (width <= 0) {
                width = DensityUtils.dip2px(20);
            }

            if (height <= 0) {
                height = DensityUtils.dip2px(20);
            }
            //设置圆环宽高
            drawable.setSize(width, height);

            //设置形状为圆环
            drawable.setShape(shape);

            //设置圆环内部填充颜色
            if (colorStateList != null && ApiLevel.ATLEAST_LOLLIPOP) {
                drawable.setColor(colorStateList);
            } else {
                drawable.setColor(solid);
            }

            //设置圆环宽度以及颜色
            if (stroke > 0) {
                drawable.setStroke(stroke, strokeColor);
            }
            if (radii != null) {
                drawable.setCornerRadii(radii);
            } else if (radius > 0) {
                drawable.setCornerRadius(radius);
            }
            //设置圆环中心点位置
//        drawable.setGradientCenter(100, 200);

            drawableLruCache.put(key, drawable);
            return drawable;
        }
    }


    public TinyDrawable width(int width) {
        this.width = width;
        return this;
    }

    public TinyDrawable height(int height) {
        this.height = height;
        return this;
    }

    public TinyDrawable setCornerRadii(@Nullable float[] radii) {
        this.radii = radii;
        return this;
    }


    public TinyDrawable setCornerRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public TinyDrawable strokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public TinyDrawable stroke(int stroke) {
        this.stroke = stroke;
        return this;
    }

    public TinyDrawable colorStateList(ColorStateList colorStateList) {
        this.colorStateList = colorStateList;
        return this;
    }

    public TinyDrawable solid(@ColorInt int solid) {
        this.solid = solid;
        return this;
    }

    public TinyDrawable shape(@Shape int shape) {
        this.shape = shape;
        return this;
    }


    private int shape = RECTANGLE;
    private int solid;
    private int stroke;
    private int strokeColor;
    private int width, height;
    private ColorStateList colorStateList;

    private float[] radii;
    private float radius;


    @IntDef({RECTANGLE, OVAL, LINE, RING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Shape {
    }

}
