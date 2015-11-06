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

    private Bitmap source;

    // AJOUT
    // pour dessin non corrigé
    Bitmap calculatedBitmap;
    int originalWidth;
    int originalHeight;
    Paint paintImage = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paintClear = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        paintClear.setColor(0);
    }

    // pour correction
    boolean[][] listePoints; // tableau de vrai, pour recopie, gain mémoire...
    boolean[][] zeros;  // tableau pour les pixels non modifier
    // fin AJOUT


    public DeformImageView(Context context) {
        super(context);
    }

    public DeformImageView(Context context, AttributeSet attrs) {

        super(context, attrs);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        source = BitmapFactory.decodeResource(getResources(), R.drawable.charlie3, options);

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

        // AJOUT
        originalWidth = source.getWidth();
        originalHeight = source.getHeight();
        // fin AJOUT

        // AJOUT
        listePoints = new boolean[originalWidth][originalHeight];
        for (i = 0; i < originalWidth; i++) {
            for (int j = 0; j < originalHeight; j++)
                listePoints[i][j] = false;
        }
        zeros = new boolean[originalWidth][originalHeight];
        // fin AJOUT
    }

    @Override
    protected void onDraw(Canvas canvas){

        // AJOUT
        // super.onDraw(canvas); // sinon, on redessine l'image source, non déformée, en fond
        Rect r = new Rect(0,0,source.getWidth(), source.getHeight());
        /*
        Bitmap b = Bitmap.createBitmap(modified, source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(b, null, r, new Paint(Paint.ANTI_ALIAS_FLAG));
        */
        canvas.drawRect(r,paintClear);
        if (calculatedBitmap != null) canvas.drawBitmap(calculatedBitmap, r, r, paintImage);
        else canvas.drawBitmap(source, r, r, paintImage);
        // fin AJOUT
    }


    public void addXFisheye(float x){

        x = x + this.x;
        if (x<0) x =0;
        if (x>1000) x = 1000;
        this.x = x;
    }

    public void addYFisheye(float y){

        y = y + this.y;
        if (y<0) y =0;
        if (y>1000) y =1000;
        this.y = y;
    }

    public void validerDeplacement(){
        deformer(precz, precr, preco);
    }

    public void deformer(float z, float r, float O){

        precz=z;
        precr=r;
        preco=O;

        // AJOUT
        modified = new int[pixels.length];

        int color;

        // AJOUT
        // déplacement
        float dmax = (float)Math.sqrt((-(2*z*O - r*r + (z - O)*(z - O))+Math.sqrt((2*z*O-r*r+(z-O)*(z-O))*(2*z*O-r*r+(z-O)*(z-O))-4*z*z*(O*O-r*r)))/2);
        int pymin = 0, pymax = 0, pxmin = 0, pxmax = 0;
        pymin = (int) Math.max(y - dmax, 0);
        pymax = (int) Math.min(y + dmax, originalHeight);
        pxmin = (int) Math.max(x - dmax, 0);
        pxmax = (int) Math.min(x + dmax, originalWidth);

        //  ré-initialisation de zeros : recopie
        for(int i=0; i < listePoints.length; i++) System.arraycopy(listePoints[i], 0, zeros[i], 0, listePoints[i].length);
        // fin AJOUT
        for (int yt=0; yt<source.getHeight(); yt++){
            for (int xt=0; xt<source.getWidth(); xt++){

                float d = (float)Math.sqrt((xt - x) * (xt - x) + (yt - y) * (yt - y));

                color = pixels[source.getWidth()*yt+xt];

                if(d > dmax){

                    // AJOUT
                   // modified[source.getWidth()*yt+xt] = color;
                    setNewPixel(xt, yt, color);
                    //fin AJOUT

                } else {


                    float ratio = ((float)Math.sqrt((d*d+z*z) * (r*r - (z - O) * (z - O)) + z*z*(z - O) * (z - O)) + z*(z - O))/(d*d+z*z);

                    int newX = (int)(x + (xt - x)*ratio);
                    int newY = (int)(y + (yt - y)*ratio);

                    // AJOUT
                    // modified[source.getWidth()*newY+newX] = color;
                    setNewPixel(newX, newY, color);

                    // fin AJOUT
                }

            }
        }

        // AJOUT
        correct(pxmin, pxmax, pymin, pymax);
        // fin AJOUT

        // AJOUT
        calculatedBitmap = Bitmap.createBitmap(modified, originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
        // fin AJOUT

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


    // AJOUT
    private void setNewPixel(int i, int j, int p) {
        if ((i < 0) || (i >= originalWidth) || (j < 0) || (j >= originalHeight))
            return;
        else {
            modified[i + originalWidth * j] = p;
            // AJOUT
            zeros[i][j] = true;
            //fin AJOUT
        }
    }
    // fin AJOUT



    // AJOUT
    private void correct(int xmin, int xmax, int ymin, int ymax) {
        int cpt = 0;
        for (int i = xmin; i < xmax; i++)
            for (int j = ymin; j < ymax; j++) {

                if (! zeros[i][j]) {
                    try {
                        cpt++;
                        int p = 0;

                        int sommeR = 0;
                        int sommeG = 0;
                        int sommeB = 0;
                        int sommeA = 0;
                        int nb = 0;

                        int indice = i + originalWidth * j;
                        p = modified[indice];


                        if (i >= 1) {
                            if (zeros[i-1][j]) {
                                p = modified[indice-1];
                                sommeR += getCanal(p, 16);
                                sommeG += getCanal(p, 8);
                                sommeB += getCanal(p, 0);
                                // sommeA += getCanal(p,24);
                                nb += 1;
                            }
                        }

                        if (i < originalWidth - 1) {

                            if (zeros[i+1][j]) {
                                p = modified[indice+1];
                                sommeR += getCanal(p, 16);
                                sommeG += getCanal(p, 8);
                                sommeB += getCanal(p, 0);
                                // sommeA += getCanal(p,24);
                                nb += 1;
                            }
                        }

                        if (j >= 1) {

                            if (zeros[i][j-1]) {
                                p = modified[indice - originalWidth ];
                                sommeR += getCanal(p, 16);
                                sommeG += getCanal(p, 8);
                                sommeB += getCanal(p, 0);
                                // sommeA += getCanal(p,24);
                                nb += 1;
                            }

                            if (i >= 1) {

                                if (zeros[i-1][j-1]) {
                                    p = modified[indice -1 - originalWidth];
                                    sommeR += getCanal(p, 16);
                                    sommeG += getCanal(p, 8);
                                    sommeB += getCanal(p, 0);
                                    // sommeA += getCanal(p,24);
                                    nb += 1;
                                }
                            }

                            if (i < originalWidth - 1) {

                                if (zeros[i+1][j-1]) {
                                    p = modified[indice +1 - originalWidth];
                                    sommeR += getCanal(p, 16);
                                    sommeG += getCanal(p, 8);
                                    sommeB += getCanal(p, 0);
                                    // sommeA += getCanal(p,24);
                                    nb += 1;
                                }
                            }
                        }

                        if (j < originalHeight - 1) {

                            if (zeros[i][j+1]) {
                                p = modified[indice+ originalWidth ];
                                sommeR += getCanal(p, 16);
                                sommeG += getCanal(p, 8);
                                sommeB += getCanal(p, 0);
                                // sommeA += getCanal(p,24);
                                nb += 1;
                            }

                            if (i >= 1) {

                                if (zeros[i-1][j+1]) {
                                    p = modified[indice -1 + originalWidth];
                                    sommeR += getCanal(p, 16);
                                    sommeG += getCanal(p, 8);
                                    sommeB += getCanal(p, 0);
                                    // sommeA += getCanal(p,24);
                                    nb += 1;
                                }
                            }

                            if (i < originalWidth - 1) {

                                if (zeros[i+1][j+1]) {
                                    p = modified[indice +1 + originalWidth];
                                    sommeR += getCanal(p, 16);
                                    sommeG += getCanal(p, 8);
                                    sommeB += getCanal(p, 0);
                                    // sommeA += getCanal(p,24);
                                    nb += 1;
                                }
                            }
                        }

                        if (nb > 0) {
                            int r = (int) (sommeR / nb);
                            int b = (int) (sommeB / nb);
                            int green = (int) (sommeG / nb);

                            int somme = (r << 16) | (green << 8) | (b)
                                    | 0xFF000000;

                            setNewPixel(i, j, somme);

                        }
                        else setNewPixel(i, j, 0xFF000000);

                        zeros[i][j] = true;
                    }

                    catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
      //  System.out.println("cpt nb corr = "+cpt);

    }


    private static int getCanal(int p, int dec) {
        int masque = 0;
        if (dec == 0)
            masque = 0xFF;
        else if (dec == 8)
            masque = 0xFF00;
        else if (dec == 16)
            masque = 0xFF0000;
        else if (dec == 24)
            masque = 0xFF000000;

        int result = p & masque;
        result = result >> dec;

        return result;
    }
    //fin AJOUT

}
