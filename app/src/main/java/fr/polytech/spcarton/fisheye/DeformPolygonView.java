package fr.polytech.spcarton.fisheye;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


/**
 *
 */
public class DeformPolygonView extends View {

    private ArrayList<MyPolygon> elements;
    private ArrayList<MyPolygon> elementsDeformes;
    private boolean partial;
    public static DeformPolygonView view;
    private float x, y;
    private float precz, precr, preco;

    public DeformPolygonView(Context context, AttributeSet attrs) {

        super(context, attrs);
        partial=true;
        view = this;
    }

    public float getX(){return x;}
    public float getY(){return y;}
    public void setX(float xp){x= xp;}
    public void setY(float yp){y= yp;}


    @Override
    public void onMeasure(int w, int h){

        setMeasuredDimension(w, h);
        x=getMeasuredWidth()/2;
        y=getMeasuredHeight()/2;
        generatePolygons(5, 20);
        deformer(42, 100, 1);

    }

    @Override
    protected void onDraw(Canvas canvas){

        super.onDraw(canvas);

        Paint paint = new Paint();

        for(MyPolygon p :  elementsDeformes)
        {
            paint.setColor(p.color);
            // liste des points sous la forme d'un tableau (x1, y1, x2, y2, ...)
            // les (xi, yi) sont les sommets
            float [] pts = p.getPoints();
            for(int i = 0; i<pts.length-3; i=i+2)
            {
                canvas.drawLine(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], paint);
            }
            // ligne entre le dernier sommet et le premier somment
            canvas.drawLine(pts[pts.length - 2], pts[pts.length - 1], pts[0], pts[1], paint);
        }
    }

    protected void generatePolygons(int marges,int nb) {

        elements = new ArrayList<MyPolygon>();

        // dimension dans laquelle s'inscrit un polygone
        float w = (getMeasuredWidth()-marges*2) / (nb*2);
        float h = (getMeasuredHeight()-marges*2) / (nb*2);

        // pour faire quelques carres différents
        int tiers = nb /3 ;
        int sixieme = nb /6 ;
        int deuxtiers = 2*nb /3 ;
        int troisquarts = 3*nb /4 ;
        float pasW = w/4;
        float pasH = h/4;

        // création de tous les polygones
        for(int i = 0; i < nb; i++) {
            for(int j = 0; j < nb; j++) {
                MyPolygon p = new MyPolygon();
                float dx = w*2*i+marges;
                float dy = h*2*j+marges;

                // ajout des points constituants les polygones
                if ((i == tiers) && (j==sixieme)) {
                    p.addPoint(dx, dy+pasH);
                    p.addPoint(dx+pasW, dy);
                } else {
                    p.addPoint(dx, dy);
                }
                p.addPoint(dx+w/2, dy);
                p.addPoint(dx+w, dy);
                p.addPoint(dx+w, dy+h/2);
                if ((i == troisquarts) && (j==deuxtiers)) {
                    p.addPoint(dx+w, dy-pasH+h);
                    p.addPoint(dx-pasW+w, dy+h);
                } else {
                    p.addPoint(dx + w, dy + h);
                }
                p.addPoint(dx+w/2, dy+h);
                p.addPoint(dx, dy+h);
                p.addPoint(dx, dy+h/2);
                p.color = Color.BLACK;

                // ou une autre couleur qui dépend de i et de j
                elements.add(p);
            }
        }
    }


    public void deformer(float z, float r, float O){

        precz=z;
        precr=r;
        preco=O;
        elementsDeformes = new ArrayList<MyPolygon>();


        for(MyPolygon poly : elements){

            MyPolygon newPoly = new MyPolygon();
            newPoly.color = Color.BLACK;

            for(int i=0; i<poly.getNbPoints(); i++){
                float d = (float)Math.sqrt((poly.xPoint(i) - x) * (poly.xPoint(i) - x) + (poly.yPoint(i) - y) * (poly.yPoint(i) - y));

                if(partial){
                    float dmax = (float)Math.sqrt((-(2*z*O - r*r + (z - O)*(z - O))+Math.sqrt((2*z*O-r*r+(z-O)*(z-O))*(2*z*O-r*r+(z-O)*(z-O))-4*z*z*(O*O-r*r)))/2);
                    if(d > dmax){
                        newPoly.addPoint(poly.xPoint(i), poly.yPoint(i));
                        continue;
                    }
                }

                float ratio = ((float)Math.sqrt((d*d+z*z) * (r*r - (z - O) * (z - O)) + z*z*(z - O) * (z - O)) + z*(z - O))/(d*d+z*z);

                float newX = x + (poly.xPoint(i) - x)*ratio;
                float newY = y + (poly.yPoint(i) - y)*ratio;
                newPoly.addPoint(newX, newY);
            }

            elementsDeformes.add(newPoly);
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
