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

public class MainActivity extends Activity implements MySurfaceView.PictureCallbackInterface {

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
                mySurfaceView.takePicture();
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
                            rl.bringToFront();
                        }

                    }
                });
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
    public void getPicture(byte[] data) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.i("asdad", "" + bmp.getHeight() + " " + bmp.getWidth());

        cameraResolution[0] = bmp.getWidth();
        cameraResolution[1] = bmp.getHeight();

        //boundary.getLocationInWindow(boundaryLoc);

        float aspectX = (float) cameraResolution[0]/previewWidth;
        float aspectY = (float) cameraResolution[1]/previewHeight;
        //Double []aspect = getRatio();
        byte[] croppedImg = mySurfaceView.getPic(data, (int) (boundaryLoc[0] * aspectX), (int) (boundaryLoc[1] * aspectY), (int) (boundaryWidth * aspectX) , (int) (boundaryHeight * aspectY) );

        Intent intent = new Intent(getApplicationContext(), Preview.class);
        intent.putExtra("PHOTO", croppedImg);
        startActivity(intent);

        overridePendingTransition(0, 0);


    }
}
