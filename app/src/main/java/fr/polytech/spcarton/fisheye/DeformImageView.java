package fr.polytech.spcarton.fisheye;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.graphics.Bitmap;


public class DeformImageView extends ImageView {

    private float x, y;
    private float precz, precr, preco;
    private int[] modified, pixels;

    private Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.charlie2);


    public DeformImageView(Context context) {
        super(context);
    }

    public DeformImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pixels = new int[source.getHeight()*source.getWidth()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());

        precz = 42;
        precr= 100;
        preco =1;
        modified = new int[source.getHeight()*source.getWidth()];
        int i=0;
        for (int a:pixels){
            modified[i++] = a;
        }
    }

    @Override
    protected void onDraw(Canvas canvas){

        super.onDraw(canvas);
        Rect r = new Rect(0,0,source.getWidth(), source.getHeight());
        Bitmap b = Bitmap.createBitmap(modified, source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(b, null, r, new Paint(Paint.ANTI_ALIAS_FLAG));
    }


    public void setXFisheye(float x){

        if (x<0) x =0;
        if (x>1000) x =1000;
        this.x = x;
        deformer(precz, precr, preco);
    }

    public void setYFisheye(float y){

        if (y<0) y =0;
        if (y>1000) y =1000;
        this.y = y;
        deformer(precz, precr, preco);
    }

    public void deformer(float z, float r, float O){

        precz=z;
        precr=r;
        preco=O;

        int i=0;
        for (int a:pixels){
            modified[i++] = a;
        }

        int color;

        for (int yt=0; yt<source.getHeight(); yt++){
            for (int xt=0; xt<source.getWidth(); xt++){

                float d = (float)Math.sqrt((xt - x) * (xt - x) + (yt - y) * (yt - y));

                float dmax = (float)Math.sqrt((-(2*z*O - r*r + (z - O)*(z - O))+Math.sqrt((2*z*O-r*r+(z-O)*(z-O))*(2*z*O-r*r+(z-O)*(z-O))-4*z*z*(O*O-r*r)))/2);

                if(d > dmax){

                } else {

                    color = pixels[source.getWidth()*yt+xt];

                    float ratio = ((float)Math.sqrt((d*d+z*z) * (r*r - (z - O) * (z - O)) + z*z*(z - O) * (z - O)) + z*(z - O))/(d*d+z*z);

                    int newX = (int)(x + (xt - x)*ratio);
                    int newY = (int)(y + (yt - y)*ratio);

                    modified[source.getWidth()*newY+newX] = color;
                }

            }
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float x = e.getX();
        float y = e.getY();

        if (e.getPointerCount() > 1) {

            // The coordinates of the current screen contact, relative to
            // the responding View or Activity.
            int action = MotionEventCompat.getActionMasked(e);
            // Get the index of the pointer associated with the action.
            int index = MotionEventCompat.getActionIndex(e);


            this.x = (int)MotionEventCompat.getX(e, index);
            this.y = (int)MotionEventCompat.getY(e, index);


        } else {

            // Single touch event
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - getX();
                    float dy = y - getY();

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) {
                        dx = dx * -1 ;
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < getWidth() / 2) {
                        dy = dy * -1 ;
                    }
            }

            this.x = x;
            this.y = y;

            deformer(precz, precr, preco);

        }

        return true;
    }


}
