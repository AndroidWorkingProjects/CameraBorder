package customcamera.rahul.com.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private final int BORDER_MARGIN = 40;
    boolean previewing = false;
    private MySurfaceView mySurfaceView = null;
    private SurfaceHolder sh;
    private Camera c;
    private Button click;
    private RelativeLayout boundary;
    private RelativeLayout rl;
    private FrameLayout frameLayout;
    private int[] boundaryLoc;
    private int boundaryWidth;
    private int boundaryHeight;
    private int previewHeight;
    private int previewWidth;
    private int[] cameraResolution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraResolution = new int[2];
        boundaryLoc = new int[2];
        getWindow().setFormat(PixelFormat.UNKNOWN);

        frameLayout = (FrameLayout) findViewById(R.id.rootframe);
        mySurfaceView = (MySurfaceView)findViewById(R.id.surfaceview);
        click = (Button) findViewById(R.id.click);
        //boundary = (RelativeLayout) findViewById(R.id.boundary);
        rl = (RelativeLayout) findViewById(R.id.rel);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Double []aspect = getRatio();
                byte[] croppedImg = mySurfaceView.getPic((int)(boundaryLoc[0]*aspect[1]),(int)(boundaryLoc[1]*aspect[0]),(int)(boundaryWidth*aspect[1])+BORDER_MARGIN,(int)(boundaryHeight*aspect[0])+BORDER_MARGIN);

                Intent intent = new Intent(getApplicationContext(), Preview.class);
                intent.putExtra("PHOTO", croppedImg);
                startActivity(intent);

                overridePendingTransition(0, 0);
//                c.takePicture(null, null, new Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        c.stopPreview();
//
//                        //byte[] croppedImg = getCroppedPic(data);
//
//                    }
//                });
            }
        });

        click.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        DisplayMetrics dm = new DisplayMetrics();
                        MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                        //int topOffset = dm.heightPixels - frameLayout.getMeasuredHeight();

                        click.getLocationInWindow(boundaryLoc);
                        //boundaryLoc[1] -= topOffset;

                        boundaryLoc[0] = BORDER_MARGIN;
                        boundaryLoc[1] = BORDER_MARGIN;
                        previewHeight = frameLayout.getMeasuredHeight();
                        previewWidth = frameLayout.getMeasuredWidth();

                        boundaryWidth = previewWidth-(BORDER_MARGIN*2);
                        boundaryHeight = previewHeight-click.getHeight()-(BORDER_MARGIN*2);

                        mySurfaceView.setBoundaryParams(BORDER_MARGIN, BORDER_MARGIN, boundaryWidth, boundaryHeight);
                        mySurfaceView.invalidate();
                        //boundary.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if(mySurfaceView==null) {
                            //mySurfaceView = new MySurfaceView(getApplicationContext(), 40, 40, boundaryWidth, boundaryHeight);

                            rl.bringToFront();
                        }

                        //sh = mySurfaceView.getHolder();
                        //sh.addCallback(MainActivity.this);
                    }
                });
    }

    public Double[] getRatio(){
        Camera.Size s = mySurfaceView.getCameraParameters().getPreviewSize();
        double heightRatio = (double)s.height/(double)previewHeight;
        double widthRatio = (double)s.width/(double)previewWidth;
        Double[] ratio = {heightRatio,widthRatio};
        return ratio;
    }

    private byte[] getCroppedPic(byte[] data) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.i("asdad", "" + bmp.getHeight() + " " + bmp.getWidth());

        cameraResolution[0] = bmp.getWidth();
        cameraResolution[1] = bmp.getHeight();

        //boundary.getLocationInWindow(boundaryLoc);

        float aspectX = (float) cameraResolution[0]/previewWidth;
        float aspectY = (float) cameraResolution[1]/previewHeight;

        Bitmap croppedImg = Bitmap.createBitmap(bmp, (int) (boundaryLoc[0] * aspectX), (int) (boundaryLoc[1] * aspectY), (int) (boundaryWidth * aspectX), (int) (boundaryHeight * aspectY));

        //Bitmap croppedImg = Bitmap.createBitmap(bmp, 0,0,bmp.getWidth(),bmp.getHeight());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        croppedImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            c = Camera.open();

            Camera.Parameters parameters = c.getParameters();
            Camera.Size myBestSize = getBestPreviewSize(previewWidth, previewHeight, parameters);

            if(myBestSize != null) {
                parameters.setPreviewSize(myBestSize.width, myBestSize.height);
                parameters.setPictureSize(myBestSize.width, myBestSize.height);
                c.setParameters(parameters);
            }
        }
        catch (RuntimeException e){
            if(c!=null){
                c.release();
                c = Camera.open();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
/*        if (display.getRotation() == Surface.ROTATION_90) {
            c.setDisplayOrientation(90);
            parameters.setRotation(90);
            parameters.set("orientation", "landscape");
            parameters.set("rotation", 90);
            //c.setParameters(parameters);
        } else if (display.getRotation() == Surface.ROTATION_270) {
            c.setDisplayOrientation(270);
            parameters.setRotation(270);
            parameters.set("orientation", "landscape");
            parameters.set("rotation", 270);
            //c.setParameters(parameters);
        }*/

        DisplayMetrics dm = new DisplayMetrics();
        MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //int topOffset = dm.heightPixels - frameLayout.getMeasuredHeight();

        //click.getLocationInWindow(boundaryLoc);
        //boundaryLoc[1] -= topOffset;

        previewHeight = frameLayout.getHeight();
        previewWidth = frameLayout.getWidth();

        //boundaryWidth = click.getMeasuredWidth();
        //boundaryHeight = click.getMeasuredHeight();

        if (previewing) {
            c.stopPreview();
            previewing = false;
        }

        if (c != null) {
            try {
                c.setPreviewDisplay(sh);
                c.startPreview();
                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//        }
//    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        c.stopPreview();
        c.release();
        c = null;
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

}
