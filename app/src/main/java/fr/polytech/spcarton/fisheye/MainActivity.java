package fr.polytech.spcarton.fisheye;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;



public class MainActivity extends Activity {

    private float z, r, o;
    private SeekBar zbar, rbar, obar;
    //private DeformPolygonView deform;
    private DeformImageView deform;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d("FisheyeTag", "start");

        setContentView(R.layout.activity_main);

        z = 42;
        r= 100;
        o =1;

        deform = (DeformImageView)findViewById(R.id.imageView);
        deform.setRotation(90f);

       // deform.setImageBitmap(myBitmap);

        zbar = (SeekBar) findViewById(R.id.seekBarZ);
        zbar.setMax(100);
        zbar.setProgress(22);

        zbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                z = progress + 20;
                deform.deformer(z, r, o);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        rbar = (SeekBar) findViewById(R.id.seekBarR);
        rbar.setMax(200);
        rbar.setProgress(50);

        rbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                r = progress + 50;
                deform.deformer(z, r, o);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_LOW);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener, mAccelerometer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mSensorEventListener, mAccelerometer);
    }


    /* ACCELEROMETRE EVENTS */
    final SensorEventListener mSensorEventListener = new SensorEventListener() {

        private final int sensibilite = 4;
        private final int coefMoove = 2;
        private final long milliBeforeCalc = 400;
        private long lastTime = System.currentTimeMillis();

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Que faire en cas de changement de précision ?
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            // Que faire en cas d'évènements sur le capteur ?
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

                if (Math.abs(sensorEvent.values[0])>sensibilite || Math.abs(sensorEvent.values[1])>sensibilite){

                    long diff = System.currentTimeMillis() - lastTime;
                    if (diff > milliBeforeCalc){
                        lastTime = System.currentTimeMillis();
                        deform.addXFisheye(coefMoove * sensorEvent.values[1]);
                        deform.addYFisheye((coefMoove) * sensorEvent.values[0]);
                        deform.validerDeplacement();
                        System.out.println("Event accelerometer x " + sensorEvent.values[0] + " y " + sensorEvent.values[1]);
                    }

                }

            }
        }
    };

}
