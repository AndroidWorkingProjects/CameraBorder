package customcamera.rahul.com.customcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by hadoop on 9/4/16.
 */
class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private byte[] mBuffer;

    // Border variables
    private int leftTopX = 0;
    private int leftTopY = 0;
    private int width = 0;
    private int height = 0;

    private final int BORDER_LENGTH = 20;

    private Paint paint;
    private PictureCallbackInterface pictureCallbackInterface;

    public interface PictureCallbackInterface{
        void getPicture(byte[] data);
    }

    // this constructor used when requested as an XML resource
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public void setBoundaryParams(int leftX, int leftY, int borderWidth, int borderHeight){
        leftTopX = leftX;
        leftTopY = leftY;
        width = borderWidth;
        height = borderHeight;;
    }

    public void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        setWillNotDraw(false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if( width > 0 ) {
            Log.i("info", "OnDraw Called");
            canvas.drawLine(leftTopX, leftTopY, leftTopX + BORDER_LENGTH, leftTopY, paint);
            canvas.drawLine(leftTopX, leftTopY, leftTopX, leftTopY + BORDER_LENGTH, paint);

            canvas.drawLine(leftTopX + width, leftTopY, leftTopX + width - BORDER_LENGTH, leftTopY, paint);
            canvas.drawLine(leftTopX + width, leftTopY, leftTopX + width, leftTopY + BORDER_LENGTH, paint);

            canvas.drawLine(leftTopX, leftTopY + height, leftTopX + BORDER_LENGTH, leftTopY + height, paint);
            canvas.drawLine(leftTopX, leftTopY + height, leftTopX, leftTopY + height - BORDER_LENGTH, paint);

            canvas.drawLine(leftTopX + width, leftTopY + height, leftTopX + width - BORDER_LENGTH, leftTopY + height, paint);
            canvas.drawLine(leftTopX + width, leftTopY + height, leftTopX + width, leftTopY + height - BORDER_LENGTH, paint);
        }
    }

    private void updateBufferSize() {
        mBuffer = null;
        System.gc();
        // prepare a buffer for copying preview data to
        int h = mCamera.getParameters().getPreviewSize().height;
        int w = mCamera.getParameters().getPreviewSize().width;
        int bitsPerPixel = ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat());
        mBuffer = new byte[w * h * bitsPerPixel / 8];
        //Log.i("surfaceCreated", "buffer length is " + mBuffer.length + " bytes");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where to draw.
        try {
            mCamera = Camera.open(); // WARNING: without permission in Manifest.xml, crashes
            Log.i("Camera Stage: ", "Opened");
        }
        catch (RuntimeException exception) {
            //Log.i(TAG, "Exception on Camera.open(): " + exception.toString());
            Toast.makeText(getContext(), "Camera broken, quitting :(", Toast.LENGTH_LONG).show();
            // TODO: exit program
        }

        try {
            mCamera.setPreviewDisplay(holder);
            updateBufferSize();
            mCamera.addCallbackBuffer(mBuffer); // where we'll store the image data
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                public synchronized void onPreviewFrame(byte[] data, Camera c) {

                    if (mCamera != null) { // there was a race condition when onStop() was called..
                        mCamera.addCallbackBuffer(mBuffer); // it was consumed by the call, add it back
                    }
                }
            });
        } catch (Exception exception) {
            //Log.e(TAG, "Exception trying to set preview");
            if (mCamera != null){
                mCamera.release();
                mCamera = null;
            }
            // TODO: add more exception handling logic here
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        Log.i("Camera Stage: ", "Destroyed");
    }

    // FYI: not called for each frame of the camera preview
    // gets called on my phone when keyboard is slid out
    // requesting landscape orientation prevents this from being called as camera tilts
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //Log.i(TAG, "Preview: surfaceChanged() - size now " + w + "x" + h);
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        try {
            mParameters = mCamera.getParameters();
            mParameters.set("orientation","landscape");
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            for (Integer i : mParameters.getSupportedPreviewFormats()) {
                //Log.i(TAG, "supported preview format: " + i);
            }

            List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizes) {
                //Log.i(TAG, "supported preview size: " + size.width + "x" + size.height);
            }
            mParameters.setPictureSize(mParameters.getPreviewSize().width,mParameters.getPreviewSize().height);
            mCamera.setParameters(mParameters); // apply the changes
        } catch (Exception e) {
            // older phone - doesn't support these calls
        }

        updateBufferSize(); // then use them to calculate

        Camera.Size p = mCamera.getParameters().getPreviewSize();
        //Log.i(TAG, "Preview: checking it was set: " + p.width + "x" + p.height); // DEBUG
        mCamera.startPreview();
    }

    public Camera.Parameters getCameraParameters(){
        return mCamera.getParameters();
    }

    public void setCameraFocus(Camera.AutoFocusCallback autoFocus){
        if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) ||
                mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)){
            mCamera.autoFocus(autoFocus);
        }
    }

    public void takePicture(){
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                pictureCallbackInterface = (MainActivity)getContext();
                pictureCallbackInterface.getPicture(data);
            }
        });
    }

    public void setFlash(boolean flash){
        if (flash){
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        }
        else{
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
        }
    }
}