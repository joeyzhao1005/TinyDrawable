package com.kit.tinydrawable;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.LruCache;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kit.utils.ApiLevel;
import com.kit.utils.ColorUtils;
import com.kit.utils.DarkMode;
import com.kit.utils.DensityUtils;
import com.kit.utils.ResWrapper;
import com.kit.utils.ValueOf;
import com.kit.utils.log.Zog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TinyDrawable
 * 用来代码替代 原本 需要xml编码的资源文件
 * 使用LRU管理
 *
 * @author Zhao
 */
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

    @NonNull
    public Drawable get() {
        return get(false);
    }

    @NonNull
    public Drawable get(boolean noCache) {
        if (width <= 0) {
            width = DensityUtils.dip2px(20);
        }

        if (height <= 0) {
            height = DensityUtils.dip2px(20);
        }

        if (ripple) {
            noCache = true;
        }

        String key = getFullKey();

        Drawable saved = drawableLruCache.get(key);
        if (!noCache && saved != null) {
            return saved;
        } else {
            Drawable drawable;
            if (ripple) {
                if (ApiLevel.ATLEAST_LOLLIPOP) {
                    Drawable mask = getDrawable(Color.BLACK, noCache);
                    Drawable content = getDrawable(noCache);
                    if (colorStateList == null) {
                        Zog.d("You better set colorStateList first !");
                        if (rippleColor != 0) {
                            colorStateList = ColorUtils.createColorStateList(color, rippleColor, rippleColor, color);
                        } else {
                            int pressed = DarkMode.isDarkMode() ? ColorUtils.getLighterColor(color, 0.1f) : ColorUtils.getDarkerColor(color, 0.1f);
                            colorStateList = ColorUtils.createColorStateList(color, pressed, pressed, color);
                        }
                    }
                    drawable = new RippleDrawable(colorStateList, content, mask);
                } else {
                    drawable = getDrawable(noCache);
                }
            } else {
                drawable = getDrawable(noCache);
            }
            if (!noCache) {
                drawableLruCache.put(key, drawable);
            }
            return drawable;

        }
    }


    private String getFullKey() {
        return getNoRippleKey(color) + rippleColor;
    }


    private String getNoRippleKey(int drawableColor) {
        String solidStr;
        if (colorStateList != null) {
            solidStr = colorStateList.toString();
        } else {
            solidStr = ValueOf.toString(drawableColor);
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
            radiiStr.append(ValueOf.toString(radius));
        }

        return shape + solidStr + stroke + strokeColor + radiiStr;
    }


    private Drawable getDrawable(boolean noCache) {
        return getDrawable(color, noCache);
    }

    private Drawable getDrawable(int drawableColor, boolean noCache) {
        String key = getNoRippleKey(drawableColor);
        Drawable saved = drawableLruCache.get(key);
        if (!noCache && saved != null) {
            return saved;
        } else {
            return createDrawable(key, drawableColor, noCache);
        }
    }

    private Drawable createDrawable(String key, int drawableColor, boolean noCache) {
        GradientDrawable drawable = new GradientDrawable();

        //设置圆环宽高
        drawable.setSize(width, height);

        //设置形状为圆环
        drawable.setShape(shape);

        //设置圆环内部填充颜色
        if (colorStateList != null && ApiLevel.ATLEAST_LOLLIPOP) {
            drawable.setColor(colorStateList);
        } else {
            drawable.setColor(drawableColor);
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

        if (!noCache) {
            drawableLruCache.put(key, drawable);
        }

        return drawable;
    }


    /**
     * ripple
     *
     * @param rippleColorResId ripple 颜色
     * @return
     */
    public TinyDrawable rippleColorResId(@ColorRes int rippleColorResId) {
        this.ripple = true;
        this.rippleColor = ResWrapper.getColor(rippleColorResId);
        return this;
    }


    /**
     * ripple
     *
     * @param rippleColor ripple 颜色
     * @return
     */
    public TinyDrawable rippleColor(@ColorInt int rippleColor) {
        this.ripple = true;
        this.rippleColor = rippleColor;
        return this;
    }


    public TinyDrawable cornerRadii(@Nullable float[] radii) {
        this.radii = radii;
        return this;
    }

    /**
     * 圆角
     *
     * @param radiusDimenResId 圆角dimen id
     * @return
     */
    public TinyDrawable cornerRadius(@DimenRes int radiusDimenResId) {
        this.radius = ResWrapper.getDimension(radiusDimenResId);
        return this;
    }


    /**
     * 圆角
     *
     * @param radius 圆角
     * @return
     */
    public TinyDrawable cornerRadius(float radius) {
        this.radius = radius;
        return this;
    }

    /**
     * 边框颜色
     *
     * @param strokeColorId 边框颜色资源id
     * @return
     */
    public TinyDrawable strokeColorResId(@ColorRes int strokeColorId) {
        this.strokeColor = ResWrapper.getColor(strokeColorId);
        return this;
    }

    /**
     * 边框颜色
     *
     * @param strokeColor 边框颜色
     * @return
     */
    public TinyDrawable strokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }


    /**
     * 边框宽度
     *
     * @param stroke 边框宽度
     * @return
     */
    public TinyDrawable strokeResId(@DimenRes int stroke) {
        this.stroke = ValueOf.toInt(ResWrapper.getDimension(stroke));
        return this;
    }

    /**
     * 边框宽度
     *
     * @param stroke 边框宽度
     * @return
     */
    public TinyDrawable stroke(int stroke) {
        this.stroke = stroke;
        return this;
    }

    public TinyDrawable colorStateList(ColorStateList colorStateList) {
        this.colorStateList = colorStateList;
        return this;
    }


    /**
     * 填充色
     *
     * @param colorResId 填充色资源id
     * @return
     */
    public TinyDrawable colorResId(@ColorRes int colorResId) {
        this.color = ResWrapper.getColor(colorResId);
        return this;
    }

    /**
     * 填充色
     *
     * @param solid 填充色
     * @return
     */
    public TinyDrawable color(@ColorInt int solid) {
        this.color = solid;
        return this;
    }

    public TinyDrawable shape(@Shape int shape) {
        this.shape = shape;
        return this;
    }

    public TinyDrawable width(int width) {
        this.width = width;
        return this;
    }

    public TinyDrawable height(int height) {
        this.height = height;
        return this;
    }

    public TinyDrawable attach(View view) {
        this.width = view.getMeasuredWidth();
        this.height = view.getMeasuredHeight();
        return this;
    }


    private int shape = RECTANGLE;
    private int color;
    private int stroke;
    private int strokeColor;
    private int width, height;
    private ColorStateList colorStateList;

    private float[] radii;
    private float radius;
    private boolean ripple;
    private int rippleColor;


    /**
     * Shape is a rectangle, possibly with rounded corners
     */
    public static final int RECTANGLE = 0;

    /**
     * Shape is an ellipse
     */
    public static final int OVAL = 1;

    /**
     * Shape is a line
     */
    public static final int LINE = 2;

    /**
     * Shape is a ring.
     */
    public static final int RING = 3;

    @IntDef({RECTANGLE, OVAL, LINE, RING})
    @Retention(RetentionPolicy.SOURCE)
    @interface Shape {
    }

}
