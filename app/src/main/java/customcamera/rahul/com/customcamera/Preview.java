package customcamera.rahul.com.customcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by hadoop on 21/3/16.
 */
public class Preview extends Activity {

    ImageView image = null;
    Button close = null;
    private Bitmap bitmap = null;
    int x;
    int y;
    int width;
    int height;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.preview);

        close = (Button)findViewById(R.id.close);
        image = (ImageView)findViewById(R.id.imageView);

        x = getIntent().getExtras().getInt("LEFTX");
        y = getIntent().getExtras().getInt("LEFTY");
        width = getIntent().getExtras().getInt("WIDTH");
        height = getIntent().getExtras().getInt("HEIGHT");

        bitmap = MainActivity.snap;

        // Get cropped image
        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
        asyncTaskRunner.execute();
        //Bitmap croppedImage = getPic(bitmap, x, y, width, height);

        //Bitmap b = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        //image.setImageBitmap(croppedImage);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.slideright);
            }
        });
    }

    public Bitmap getPic(Bitmap data, int x, int y, int width, int height) {
        System.gc();

        return Bitmap.createBitmap(data,x,y,width, height);

//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//        data.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//        return outStream.toByteArray();
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            return getPic(bitmap,x,y,width,height);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            image.setImageBitmap(result);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... text) {
            super.onProgressUpdate(text);
        }
    }
}