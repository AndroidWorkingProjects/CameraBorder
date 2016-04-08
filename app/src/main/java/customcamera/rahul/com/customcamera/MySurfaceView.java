package customcamera.rahul.com.customcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView {
    private Bitmap bmp;
    private SurfaceHolder holder;
    private int leftTopX;
    private int leftTopY;
    private int width;
    private int height;
    private Paint paint;

    public MySurfaceView(Context context, int leftTopX, int leftTopY, int width, int height) {
        super(context);
        holder = getHolder();
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.width = width;
        this.height = height;

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        setWillNotDraw(false);

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(leftTopX,leftTopY,leftTopX+20,leftTopY,paint);
        canvas.drawLine(leftTopX,leftTopY,leftTopX,leftTopY+20,paint);

        canvas.drawLine(leftTopX+width,leftTopY,leftTopX+width-20,leftTopY,paint);
        canvas.drawLine(leftTopX+width,leftTopY,leftTopX+width,leftTopY+20,paint);

        canvas.drawLine(leftTopX,leftTopY+height,leftTopX+20,leftTopY+height,paint);
        canvas.drawLine(leftTopX,leftTopY+height,leftTopX,leftTopY+height-20,paint);

        canvas.drawLine(leftTopX+width,leftTopY+height,leftTopX+width-20,leftTopY+height,paint);
        canvas.drawLine(leftTopX+width,leftTopY+height,leftTopX+width,leftTopY+height-20,paint);
    }
}