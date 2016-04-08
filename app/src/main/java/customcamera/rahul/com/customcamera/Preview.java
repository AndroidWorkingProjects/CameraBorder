package customcamera.rahul.com.customcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by hadoop on 21/3/16.
 */
public class Preview extends Activity {

    ImageView image = null;
    Button close = null;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.preview);

        close = (Button)findViewById(R.id.close);
        image = (ImageView)findViewById(R.id.imageView);

        byte[] bytes = getIntent().getExtras().getByteArray("PHOTO");

        Bitmap b = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        image.setImageBitmap(b);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.slideright);
            }
        });


    }
}
