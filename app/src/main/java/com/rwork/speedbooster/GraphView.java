package com.rwork.speedbooster;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by super2lao on 2/3/2016.
 */
public class GraphView extends View {

    public enum Direction {
        LeftToRight, RightToLeft
    };

    private int color = 0;
    private int samples = 10;
    private float strokeWidth = 1f;
    private Direction direction = Direction.RightToLeft;
    private ArrayList<Float> data = new ArrayList<>();
    private Paint paint = new Paint();


    public GraphView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        readAttrs(context, attrs, defStyle);
    }

    public GraphView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphView(final Context context) {
        this(context, null, 0);
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
//        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GraphView, defStyle, 0);
//        strokeWidth = a.getFloat(R.styleable.GraphView_strokeWidth, 1f);
//        a.recycle();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        if (samples < 2)
            samples = 2;
        this.samples = samples;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        paint.setStrokeWidth(strokeWidth);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void addSample(float value) {
        data.add(value);
        if (data.size() > samples)
            data.remove(0);
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        Globals globals = Globals.getInstance(getContext());

        if (getWidth() == 0 || getHeight() == 0)
            return;

        if (data.isEmpty())
            return;

        int width = getWidth();
        int height = getHeight();
        float maxValue = data.get(0);
        float minValue = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            maxValue = Math.max(maxValue, data.get(i));
            minValue = Math.min(minValue, data.get(i));
        }

        for (int i = 0; i < data.size() - 1; i++) {
            int x = (samples - data.size() + i) * width / (samples - 1);
            int x1 = (samples - data.size() + i + 1) * width / (samples - 1);
            int y, y1;
            if (minValue == maxValue) {
                y = height / 2;
                y1 = height / 2;
            } else {
                y = height - (int) ((data.get(i) - minValue) / (maxValue - minValue) * height);
                y1 = height - (int) ((data.get(i + 1) - minValue) / (maxValue - minValue) * height);
            }
            paint.setAntiAlias(true);
            canvas.drawLine(x, y, x1, y1, paint);
        }
    }

}
