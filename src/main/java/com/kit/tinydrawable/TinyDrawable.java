package com.kit.tinydrawable;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.LruCache;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kit.utils.ApiLevel;
import com.kit.utils.ColorUtils;
import com.kit.utils.DarkMode;
import com.kit.utils.DensityUtils;
import com.kit.utils.ValueOf;
import com.kit.utils.log.Zog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
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
        String solidStr;
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
        if (saved != null && !noCache) {
            return saved;
        } else {
            Drawable drawable;
            if (ripple) {
                if (ApiLevel.ATLEAST_LOLLIPOP) {
                    Drawable mask = getDrawable(Color.BLACK);
                    Drawable content = getDrawable();
                    if (colorStateList == null) {
                        Zog.d("You better set colorStateList first !");
                        if (rippleColor != 0) {
                            colorStateList = ColorUtils.createColorStateList(solid, rippleColor, rippleColor, solid);
                        } else {
                            int pressed = DarkMode.isDarkMode() ? ColorUtils.getLighterColor(solid, 0.1f) : ColorUtils.getDarkerColor(solid, 0.1f);
                            colorStateList = ColorUtils.createColorStateList(solid, pressed, pressed, solid);
                        }
                    }
                    drawable = new RippleDrawable(colorStateList, content, mask);
                } else {
                    drawable = getDrawable();
                }
            } else {
                drawable = getDrawable();
            }
            if (!noCache) {
                drawableLruCache.put(key, drawable);
            }
            return drawable;

        }
    }

    private Drawable getDrawable() {
        return getDrawable(solid);
    }

    private Drawable getDrawable(int drawableColor) {
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
        return drawable;
    }


    public TinyDrawable rippleColor(int rippleColor) {
        this.ripple = true;
        this.rippleColor = rippleColor;
        return this;
    }


    public TinyDrawable cornerRadii(@Nullable float[] radii) {
        this.radii = radii;
        return this;
    }


    public TinyDrawable cornerRadius(float radius) {
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

    public TinyDrawable width(int width) {
        this.width = width;
        return this;
    }

    public TinyDrawable height(int height) {
        this.height = height;
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
