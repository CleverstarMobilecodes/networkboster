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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by super2lao on 2/3/2016.
 */
public class GaugeView extends SurfaceView {

    private float value = 0f;

    final float fMarginAngle = 2f;//(float)(1 / Math.PI);
    final float fBaseAngle = -210f;//(float)(-45 / Math.PI);
    final float fLastAngle = 30f;//(float)(225 / Math.PI);
    final float fBaseMetrics = 0f;
    final float fLastMetrics = 50f;
    final float[] fMetricsRange = {0, 1, 2, 3, 4, 5, 10, 15, 20, 25, 30, 40, 50};

    private float fFlowAngle = 0f;
    private int nFlowCountdown = 0;

    final float fFlowStart = -227.3f;
    final float fFlowNode = 9.2f;
    final float fFlowMax = 84.7f;

    Timer timer = null;

    public GaugeView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        readAttrs(context, attrs, defStyle);
        init();
    }

    public GaugeView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(final Context context) {
        this(context, null, 0);
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        value = a.getFloat(R.styleable.GaugeView_speed, 0f);
        a.recycle();
    }

    @TargetApi(11)
    private void init() {
        // TODO Why isn't this working with HA layer?
        // The needle is not displayed although the onDraw() is being triggered by invalidate()
        // calls.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        setValue(((Activity) getContext()).findViewById(android.R.id.content), value);
    }

    public void setValue(View rootView, float value) {
        float fFrom = getAngle(this.value) + 90;
        float fTo = getAngle(value) + 90;
        View imgCursor = rootView.findViewById(R.id.imgCursor);

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        final RotateAnimation animRotate = new RotateAnimation(fFrom, fTo,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(2000);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        imgCursor.startAnimation(animSet);

        this.value = value;
        invalidate();
    }

    public void startFlowAnimation() {
        fFlowAngle = 0f;
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GaugeView.this.postInvalidate();
                        }
                    });
                    if (fFlowAngle > fFlowMax + fFlowNode) {
                        if (nFlowCountdown-- <= 0)
                            fFlowAngle = 0f;
                    } else if (fFlowAngle > fFlowMax) {
                        fFlowAngle += fFlowNode;
                        nFlowCountdown = 25;
                    } else
                        fFlowAngle += 0.4;
                }
            }, 0, 100);
        }
    }

    public void stopFlowAnimation() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    float getAngle(float value) {
        value /= 1024f;
        float fAngle = fLastAngle;
        int count = (fMetricsRange.length - 1);
        for (int i = 0; i < fMetricsRange.length - 1; i++) {
            if (fMetricsRange[i] <= value && fMetricsRange[i + 1] > value) {
                fAngle = fBaseAngle + (fLastAngle - fBaseAngle) * i / count +
                        (fLastAngle - fBaseAngle) / count * (value - fMetricsRange[i]) / (fMetricsRange[i + 1] - fMetricsRange[i]);
                break;
            }
        }
        return fAngle;
    }

    Bitmap bmpMeterHot;

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        bmpMeterHot = BitmapFactory.decodeResource(getResources(), R.drawable.meter_hot);
        bmpMeterHot = Bitmap.createScaledBitmap(bmpMeterHot, getWidth(), getHeight(), false);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        Globals globals = Globals.getInstance(getContext());

        if (getWidth() == 0 || getHeight() == 0)
            return;

        if (bmpMeterHot == null) {
            bmpMeterHot = BitmapFactory.decodeResource(getResources(), R.drawable.meter_hot);
            bmpMeterHot = Bitmap.createScaledBitmap(bmpMeterHot, getWidth(), getHeight(), false);
        }

        int width = getWidth();
        int height = getHeight();
        float fRadius = width / 2f;
        float fCenter = width / 2f;
        float fMiddle = height / 2f;
        float fWHRatio = height / (float)width;

        float fDegreeAngle = getAngle(value);
        if (fDegreeAngle == fLastAngle)
            fDegreeAngle += fMarginAngle;

        RectF oval = new RectF(0, 0, width, height);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(fCenter, fMiddle);
        path.arcTo(oval, fBaseAngle - fMarginAngle, fDegreeAngle + fMarginAngle * 2 - fBaseAngle);
        path.close();

        if (timer != null) {
            path.moveTo(fCenter, fMiddle);
            switch (globals.getAnimationMode()) {
                case Ping:
                case Downloading:
                    path.arcTo(oval, fFlowStart - fFlowMax - fFlowNode, fFlowNode + fFlowAngle);
                    break;
                case Uploading:
                    path.arcTo(oval, fFlowStart + fFlowNode, -(fFlowNode + fFlowAngle));
                    break;
            }
            path.close();
        }

        canvas.clipPath(path);
        canvas.drawBitmap(bmpMeterHot, 0, 0, null);
    }

}
