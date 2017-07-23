package com.example.zhongqing.androiddownloadersample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.zhongqing.androiddownloadersample.R;

/**
 * Created by zhongqing on 23/7/17.
 */

public class DownLoadProgressBar extends View {

    private int progressColor;
    private int progress;
    private float stroke_width;
    private Paint paint;
    public DownLoadProgressBar(Context context) {
        this(context, null);
    }

    public DownLoadProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.DownLoadProgressBar);
        progressColor = mTypedArray.getColor(R.styleable.DownLoadProgressBar_progress_color, Color.WHITE);
        progress = mTypedArray.getInt(R.styleable.DownLoadProgressBar_progress, 0);
        stroke_width = mTypedArray.getDimension(R.styleable.DownLoadProgressBar_stroke_width, 2);

        if(progress > 100 ){
            progress = 100;
        }
        paint = new Paint();
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /** draw the outter circle **/
        int centre = getWidth()/2;
        int radius = (int) (centre - stroke_width/2);
        paint.setColor(progressColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke_width);
        paint.setAntiAlias(true);
        canvas.drawCircle(centre, centre, radius, paint);

        /** draw the inner progress */
        paint.setStrokeWidth(stroke_width);
        paint.setColor(progressColor);
        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawArc(oval, -90, 360 * progress / 100, true, paint);



    }
    public void setProgress(int progress){
        this.progress = progress;
        postInvalidate();
    }

}