package com.rwork.speedbooster;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by super2lao on 2/3/2016.
 */
public class StrengthView extends View {

    private float value;

    public StrengthView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        readAttrs(context, attrs, defStyle);
        init();
    }

    public StrengthView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrengthView(final Context context) {
        this(context, null, 0);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value > 1f)
            value = 1f;
        this.value = value;
        invalidate();
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrengthView, defStyle, 0);
        value = a.getFloat(R.styleable.StrengthView_value, 0f);
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

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);
    }

    private void drawBackground(final Canvas canvas) {
        RectF br = new RectF(0, 0, getWidth() * value, getHeight());

        // Draw background (cold)
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.strength_cold);
        bmp = Bitmap.createScaledBitmap(bmp, getWidth(), getHeight(), false);
        canvas.drawBitmap(bmp, 0, 0, null);

        // Draw strength (hot)
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.strength_hot);
        bmp = Bitmap.createScaledBitmap(bmp, getWidth(), getHeight(), false);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.addRect(br, Path.Direction.CW);

        canvas.clipPath(path);
        canvas.drawBitmap(bmp, 0, 0, null);
    }

}
