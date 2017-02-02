package com.example.lachl.moredata;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This application collects all the sensor data applicable for human activity recognition in
 * regards to walking, going upstairs, and going downstairs. Results are collated into a csv file
 * and the volume keys cycle between labelling modes. This application does use a wakelock, thus
 * will eat your battery life significantly!
 *
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */


public class CollectSensorData extends Activity implements SensorEventListener {

    // Manager Classes

    // CSV files
    private BufferedWriter bw = null;

    // Arrays to hold sensor data
    private ArrayList<Float> verticalRot = new ArrayList<>();
    private ArrayList<Float> horizRot = new ArrayList<>();
    private ArrayList<Float> verticalAccel = new ArrayList<>();
    private ArrayList<Float> totalAccel = new ArrayList<>();
    private ArrayList<Float> totalRot = new ArrayList<>();
    private ArrayList<Float> horizAccel = new ArrayList<>();
    private ArrayList<Float> horizRotJerk = new ArrayList<>();
    private ArrayList<Float> verticalRotJerk = new ArrayList<>();
    private ArrayList<Float> addUpHorizRot = new ArrayList<>();
    private ArrayList<Float> addUpVerticalRot = new ArrayList<>();
    private ArrayList<Float> totalAccelJerk = new ArrayList<>();
    private ArrayList<Float> accelX = new ArrayList<>();
    private ArrayList<Float> accelY = new ArrayList<>();
    private ArrayList<Float> accelZ = new ArrayList<>();
    private ArrayList<Float> gravX = new ArrayList<>();
    private ArrayList<Float> gravY = new ArrayList<>();
    private ArrayList<Float> gravZ = new ArrayList<>();


    // Sensor variables
    private boolean start = false;
    private String activityType = "Unspecified";
    private long initialTime;
    private long stepTime = 0;
    private long prevStepTime = 0;
    private int stepNumber;

    // Pressure
    private float pressureValue;
    private float pressureCount;
    private LimitedQueue prevPressureValues = new LimitedQueue(8);
    private float changeInPressure;
    private float initialPressure = 0.0f;

    // Gravity
    private float gravityX;
    private float gravityY;
    private float gravityZ;
    //private float gravityHorizX;
    //private float gravityHorizY;
    //private float gravityHorizZ;

    // Acceleration
    private float totalAccelValue;
    private float prevTotalAccel = 0.0f;
    private long prevLinearTimestamp;

    // Rotation
    private float prevHorizRot = 0.0f;
    private float prevVertRot = 0.0f;
    private float addUpRot = 0.0f;
    private float addUpVerticalRotVal = 0.0f;
    private long prevTimeGyro = 0;

    // Textview
    TextView currentLabel;
    TextView activityMode;

    //Veryify storage permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Called when the activity is first created
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SensorManager mSensorManager;
        PowerManager mPowerManager;
        PowerManager.WakeLock mWakeLock;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verify storage permissions and set up CSV file
        verifyStoragePermissions(this);
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File myFile = new File(folder, "stepData.csv");
        try {
            FileWriter fw = new FileWriter(myFile, true);
            bw = new BufferedWriter(fw);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());

        // Create the listeners for each sensor type
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor stepdetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor pressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, stepdetector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);

        // Set labels
        currentLabel = (TextView) findViewById(R.id.currentActivity);
        currentLabel.setText(activityType);
        activityMode = (TextView) findViewById(R.id.recordMode);
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Called each time the sensor changes. Fills buffers to be written to the CSV.
     *
     * @param event The event corresponding to a sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (start) {
            // Only log sensor values on every step
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                registerStep(event);
            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                getLinearAcceleration(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                getGravity(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getGyroscope(event);
            } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                getPressure(event);
            }
        }
    }

    /**
     * A three dimensional vector indicating acceleration along each device axis, not including
     * gravity. All values have units of m/s^2. The coordinate system is the same as is used by
     * the acceleration sensor.
     *
     * @param event The sensor event corresponding to the accelerometer
     */
    private void getLinearAcceleration(SensorEvent event) {
        float horizAccelValue;
        // Get the linear accelerometer values from the event
        float linearX = event.values[0];
        float linearY = event.values[1];
        float linearZ = event.values[2];

        if (stepNumber == 0) {
            initialTime = event.timestamp;
        }
        long timeAcc = (event.timestamp - initialTime) / 1000000;
        float vertAccelValue = (gravityX * linearX) + (gravityY * linearY) + (gravityZ * linearZ);
        totalAccelValue = (float) Math.sqrt((linearX * linearX) + (linearY * linearY) +
                (linearZ * linearZ));
        //    if(totalacceleration > positiveVerticalAcceleration)
        // {horizontaAcceleration = Math.sqrt((totalacceleration*totalacceleration)-(positiveVerticalAcceleration*positiveVerticalAcceleration));}
        //   else { horizontaAcceleration = 0;}

        // Check if horizontal acceleration can be measured
        if ((vertAccelValue * vertAccelValue) < (totalAccelValue * totalAccelValue)) {
            horizAccelValue = (float) Math.sqrt((totalAccelValue * totalAccelValue) -
                    (vertAccelValue * vertAccelValue));
        } else {
            horizAccelValue = 0;
        }

        // Record the values
        if (start) {
            verticalAccel.add(vertAccelValue);
            totalAccel.add(totalAccelValue);
            totalAccelJerk.add((totalAccelValue - prevTotalAccel) / (timeAcc - prevLinearTimestamp));
            horizAccel.add(horizAccelValue);
            accelX.add(linearX);
            accelY.add(linearY);
            accelZ.add(linearZ);
        }
        prevLinearTimestamp = timeAcc;
        prevTotalAccel = totalAccelValue;
    }

    /**
     * A three dimensional vector indicating the direction and magnitude of gravity. Units are
     * m/s^2. The coordinate system is the same as is used by the acceleration sensor.
     *
     * This measure provides us with phone orientation and this orientation is used to
     * determine vertical acceleration.
     *
     * @param event The sensor event corresponding to the gravity sensor
     */
    private void getGravity(SensorEvent event) {
        // If the phone is moving fast the gravity measurement returns a false result
        // Thus only use the gravity at lower speeds
        if (totalAccelValue < 0.75) {
            gravityX = event.values[0];
            gravityY = event.values[1];
            gravityZ = event.values[2];
        }

        if (start) {
            gravX.add(gravityX);
            gravY.add(gravityY);
            gravZ.add(gravityZ);
        }
    }

    /**
     * Atmospheric pressure in hPa (millibar)
     * <p>
     * Change in pressure is the most reliable feature
     *
     * @param event The sensor event corresponding to the pressure sensor
     */
    private void getPressure(SensorEvent event) {
        // Get the pressure value
        pressureValue = event.values[0];
        // Increase pressure count
        pressureCount++;
        // Set the pressure depending if on bottom or top of stairs
        if (pressureCount == 1) {
            initialPressure = pressureValue;
        }
        changeInPressure = pressureValue - initialPressure;
    }

    /**
     * All values are in radians/second and measure the rate of rotation around the device's local
     * X, Y and Z axis. The coordinate system is the same as is used for the acceleration sensor.
     * Rotation is positive in the counter-clockwise direction. That is, an observer looking from
     * some positive location on the x, y or z axis at a device positioned on the origin would
     * report positive rotation if the device appeared to be rotating counter clockwise. Note that
     * this is the standard mathematical definition of positive rotation and does not agree with
     * the definition of roll given earlier.
     * <p>
     * values[0]: Angular speed around the x-axis
     * values[1]: Angular speed around the y-axis
     * values[2]: Angular speed around the z-axis
     * <p>
     * Typically the output of the gyroscope is integrated over time to calculate a rotation
     * describing the change of angles over the time step.
     *
     * @param event The sensor event corresponding to the gyroscope data
     */
    private void getGyroscope(SensorEvent event) {
        // Compute the horizontal rotation by multiplying by gravity and adding
        float horizRotVal = (gravityX * event.values[0]) + (gravityY * event.values[1])
                + (gravityZ * event.values[2]);

        // Compute the total rotation
        float totalRotVal = (float) Math.sqrt((event.values[0] * event.values[0]) +
                (event.values[1] * event.values[1]) + (event.values[2] * event.values[2]));

        // Record the time and convert to ms
        long timeGyro = event.timestamp;
        long timeChange = (timeGyro - prevTimeGyro) / 1000000;

        // Record the rotation over the time change
        float degreesRot = horizRotVal * timeChange;
        addUpRot = addUpRot + degreesRot;

        // Compute the horizontal rotation
        float horizRotX = (1 - gravityX) * event.values[0];
        if (horizRotX < 0) {
            horizRotX = horizRotX * -1;
        }
        float horizRotY = (1 - gravityY) * event.values[1];
        if (horizRotY < 0) {
            horizRotY = horizRotY * -1;
        }
        float horizRotZ = (1 - gravityZ) * event.values[2];
        if (horizRotZ < 0) {
            horizRotZ = horizRotZ * -1;
        }

        // Use horizontal rotation to compute the vertical rotation
        float vertRot = horizRotX + horizRotY + horizRotZ;
        float degreesVertRot = vertRot * timeChange;
        addUpVerticalRotVal += degreesVertRot;

        // Add values to arrays if recording
        if (start) {
            addUpVerticalRot.add(addUpVerticalRotVal);
            addUpHorizRot.add(addUpRot);
            verticalRot.add(vertRot);
            horizRot.add(horizRotVal);
            horizRotJerk.add((horizRotVal - prevHorizRot) / (timeGyro - prevTimeGyro));
            verticalRotJerk.add((vertRot - prevVertRot) / (timeGyro - prevTimeGyro));
            totalRot.add(totalRotVal);
        }
        prevTimeGyro = timeGyro;
        prevHorizRot = horizRotVal;
        prevVertRot = vertRot;
    }

    /**
     * For every step detected save the values to the CSV file. Takes the aggregate
     *
     * @param event The event that registers a step has taken place
     */
    private void registerStep(SensorEvent event) {
        // Keep track of the number of steps and the time of steps
        stepNumber = stepNumber + 1;
        prevStepTime = stepTime;
        stepTime = (new Date()).getTime();
        long stepChangeTime = (stepTime - prevStepTime);

        // If the step if the step is slow the data is unreliable
        GetValues makeValues = new GetValues();
        if (start && stepChangeTime < 1500 && stepChangeTime > 0) {
            float[] totacc = makeValues.findValues(totalAccel);
            float[] vertacc = makeValues.findValues(verticalAccel);
            float[] horizacc = makeValues.findValues(horizAccel);
            float[] horizrot = makeValues.findValues(horizRot);
            float[] vertrot = makeValues.findValues(verticalRot);
            float[] addhorrot = makeValues.findValues(addUpHorizRot);
            float[] addvertrot = makeValues.findValues(addUpVerticalRot);
            float[] horrotjerk = makeValues.findValues(horizRotJerk);
            float[] vertrotjerk = makeValues.findValues(verticalRotJerk);
            float[] totacceljerk = makeValues.findValues(totalAccelJerk);
            float[] accelx = makeValues.findValues(accelX);
            float[] accely = makeValues.findValues(accelY);
            float[] accelz = makeValues.findValues(accelZ);
            float[] gravityx = makeValues.findValues(gravX);
            float[] gravityy = makeValues.findValues(gravY);
            float[] gravityz = makeValues.findValues(gravZ);

            // This is ugly but effective
            String stepText = Long.toString(stepTime) + ", " +
                activityType + ", " +
                Integer.toString(stepNumber) + ", " +
                Long.toString(stepChangeTime) + ", " +
                Float.toString(accelx[0]) + ", " +
                Float.toString(accelx[1]) + ", " +
                Float.toString(accelx[2]) + ", " +
                Float.toString(accelx[3]) + ", " +
                Float.toString(accelx[4]) + ", " +
                Float.toString(accely[0]) + ", " +
                Float.toString(accely[1]) + ", " +
                Float.toString(accely[2]) + ", " +
                Float.toString(accely[3]) + ", " +
                Float.toString(accely[4]) + ", " +
                Float.toString(accelz[0]) + ", " +
                Float.toString(accelz[1]) + ", " +
                Float.toString(accelz[2]) + ", " +
                Float.toString(accelz[3]) + ", " +
                Float.toString(accelz[4]) + ", " +
                Float.toString(gravityx[0]) + ", " +
                Float.toString(gravityx[1]) + ", " +
                Float.toString(gravityx[2]) + ", " +
                Float.toString(gravityx[3]) + ", " +
                Float.toString(gravityx[4]) + ", " +
                Float.toString(gravityy[0]) + ", " +
                Float.toString(gravityy[1]) + ", " +
                Float.toString(gravityy[2]) + ", " +
                Float.toString(gravityy[3]) + ", " +
                Float.toString(gravityy[4]) + ", " +
                Float.toString(gravityz[0]) + ", " +
                Float.toString(gravityz[1]) + ", " +
                Float.toString(gravityz[2]) + ", " +
                Float.toString(gravityz[3]) + ", " +
                Float.toString(gravityz[4]) + ", " +
                Float.toString(totacc[0]) + ", " +
                Float.toString(totacc[1]) + ", " +
                Float.toString(totacc[2]) + ", " +
                Float.toString(totacc[3]) + ", " +
                Float.toString(totacc[4]) + ", " +
                Float.toString(vertacc[0]) + ", " +
                Float.toString(vertacc[1]) + ", " +
                Float.toString(vertacc[2]) + ", " +
                Float.toString(vertacc[3]) + ", " +
                Float.toString(vertacc[4]) + ", " +
                Float.toString(changeInPressure) + ", " +
                Float.toString(pressureValue - (float) prevPressureValues.getIndex(4)) + ", " +
                Float.toString(horizacc[0]) + ", " +
                Float.toString(horizacc[1]) + ", " +
                Float.toString(horizacc[2]) + ", " +
                Float.toString(horizacc[3]) + ", " +
                Float.toString(horizacc[4]) + ", " +
                Float.toString(horizrot[0]) + ", " +
                Float.toString(horizrot[1]) + ", " +
                Float.toString(horizrot[2]) + ", " +
                Float.toString(horizrot[3]) + ", " +
                Float.toString(horizrot[4]) + ", " +
                Float.toString(vertrot[0]) + ", " +
                Float.toString(vertrot[1]) + ", " +
                Float.toString(vertrot[2]) + ", " +
                Float.toString(vertrot[3]) + ", " +
                Float.toString(vertrot[4]) + ", " +
                Float.toString(addhorrot[0]) + ", " +
                Float.toString(addhorrot[1]) + ", " +
                Float.toString(addhorrot[2]) + ", " +
                Float.toString(addhorrot[3]) + ", " +
                Float.toString(addhorrot[4]) + ", " +
                Float.toString(addvertrot[0]) + ", " +
                Float.toString(addvertrot[1]) + ", " +
                Float.toString(addvertrot[2]) + ", " +
                Float.toString(addvertrot[3]) + ", " +
                Float.toString(addvertrot[4]) + ", " +
                Float.toString(horrotjerk[0]) + ", " +
                Float.toString(horrotjerk[1]) + ", " +
                Float.toString(horrotjerk[2]) + ", " +
                Float.toString(horrotjerk[3]) + ", " +
                Float.toString(horrotjerk[4]) + ", " +
                Float.toString(vertrotjerk[0]) + ", " +
                Float.toString(vertrotjerk[1]) + ", " +
                Float.toString(vertrotjerk[2]) + ", " +
                Float.toString(vertrotjerk[3]) + ", " +
                Float.toString(vertrotjerk[4]) + ", " +
                Float.toString(totacceljerk[0]) + ", " +
                Float.toString(totacceljerk[1]) + ", " +
                Float.toString(totacceljerk[2]) + ", " +
                Float.toString(totacceljerk[3]) + ", " +
                Float.toString(totacceljerk[4]) + ", " +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * .25)))) + ", " +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * .3)))) + ", " +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * .37)))) + ", " +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * .56)))) + ", " +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * .13)))) + ", " +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * .34)))) + ", " +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * .38)))) + ", " +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * .65)))) + ", " +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * .85)))) + ", " +
                Float.toString(horizAccel.get((int) (Math.round(horizAccel.size() * .9)))) + ", " +
                Float.toString(addUpHorizRot.get((int) (Math.round(addUpHorizRot.size() * .24)))) + ", " +
                Float.toString(addUpHorizRot.get((int) (Math.round(addUpHorizRot.size() * .29)))) + ", " +
                Float.toString(addUpHorizRot.get((int) (Math.round(addUpHorizRot.size() * .96)))) + ", " +
                Float.toString((totalAccel.get((int) (Math.round(totalAccel.size() * .10))) / (totalAccel.get((int) (Math.round(totalAccel.size() * .23)))))) + ", " +
                Float.toString((totalAccel.get((int) (Math.round(totalAccel.size() * .33))) / (totalAccel.get((int) (Math.round(totalAccel.size() * .23)))))) + ", " +
                Float.toString((totalAccel.get((int) (Math.round(totalAccel.size() * .87))) / (totalAccel.get((int) (Math.round(totalAccel.size() * .82)))))) + ", " +
                Float.toString((totalAccel.get((int) (Math.round(totalAccel.size() * .07))) / (totalAccel.get((int) (Math.round(totalAccel.size() * .15)))))) + ", " +
                Float.toString((totalAccel.get((int) (Math.round(totalAccel.size() * .7))) / (totalAccel.get((int) (Math.round(totalAccel.size() * .9)))))) + ", " +
                Float.toString((verticalAccel.get((int) (Math.round(verticalAccel.size() * .13))) / (verticalAccel.get((int) (Math.round(verticalAccel.size() * .25)))))) + ", " +
                Float.toString((verticalAccel.get((int) (Math.round(verticalAccel.size() * .25))) / (verticalAccel.get((int) (Math.round(verticalAccel.size() * .4)))))) + ", " +
                Float.toString((verticalAccel.get((int) (Math.round(verticalAccel.size() * .38))) / (verticalAccel.get((int) (Math.round(verticalAccel.size() * .26)))))) + ", " +
                Float.toString((verticalAccel.get((int) (Math.round(verticalAccel.size() * .22))) / (verticalAccel.get((int) (Math.round(verticalAccel.size() * .30)))))) + ", " +
                Float.toString((verticalAccel.get((int) (Math.round(verticalAccel.size() * .30))) / (verticalAccel.get((int) (Math.round(verticalAccel.size() * .37)))))) + ", " +
                Float.toString(totalAccel.get(0)) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.025)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.05)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.075)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.1)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.125)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.15)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.175)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.2)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.225)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.25)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.275)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.3)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.325)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.35)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.0375)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.4)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.425)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.45)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.475)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.5)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.525)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.55)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.575)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.6)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.625)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.65)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.0675)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.7)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.725)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.75)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.775)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.8)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.825)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.85)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.875)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.9)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.925)))) + "," +
                Float.toString(totalAccel.get((int) (Math.round(totalAccel.size() * 0.95)))) + "," +
                Float.toString(verticalAccel.get(0)) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.025)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.05)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.075)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.1)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.125)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.15)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.175)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.2)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.225)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.25)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.275)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.3)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.325)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.35)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.375)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.4)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.425)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.45)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.475)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.5)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.525)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.55)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.575)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.6)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.625)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.65)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.675)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.7)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.725)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.75)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.775)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.8)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.825)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.85)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.875)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.9)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.925)))) + "," +
                Float.toString(verticalAccel.get((int) (Math.round(verticalAccel.size() * 0.95)))) + "," +
                Float.toString(totalRot.get(0)) + ", " +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.025)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.05)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.075)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.1)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.125)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.15)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.175)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.2)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.225)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.25)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.0275)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.3)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.325)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.35)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.375)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.4)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.425)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.45)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.475)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.5)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.525)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.55)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.575)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.6)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.625)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.65)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.675)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.7)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.725)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.75)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.775)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.8)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.825)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.85)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.875)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.9)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.925)))) + "," +
                Float.toString(totalRot.get((int) (Math.round(totalRot.size() * 0.95)))) + "," +
                Float.toString(horizRot.get(0)) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.025)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.05)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.075)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.1)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.125)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.15)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.175)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.2)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.225)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.25)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.275)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.3)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.325)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.35)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.375)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.4)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.425)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.45)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.475)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.5)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.525)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.55)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.575)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.6)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.625)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.65)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.675)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.7)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.725)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.75)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.775)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.8)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.825)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.85)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.875)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.9)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.925)))) + "," +
                Float.toString(horizRot.get((int) (Math.round(horizRot.size() * 0.95)))) + "\n";
            write(stepText);
        }


        // Prepare for next cycle
        prevStepTime = stepTime;
        addUpRot = 0;
        addUpVerticalRotVal = 0;
        pressureCount = 0;

        // Clear data
        verticalAccel.clear();
        horizAccel.clear();
        totalAccel.clear();
        totalAccelJerk.clear();
        verticalRot.clear();
        horizRot.clear();
        verticalRotJerk.clear();
        horizRotJerk.clear();
        addUpHorizRot.clear();
        addUpVerticalRot.clear();
        totalRot.clear();
        accelX.clear();
        accelY.clear();
        accelZ.clear();
        gravX.clear();
        gravY.clear();
        gravZ.clear();

        // Set the pressure values
        prevPressureValues.add(pressureValue);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void start(View view) {
        start = true;
        stepNumber = 0;
        activityMode.setText("Recording");
        activityType = "Walking";
        currentLabel.setText(activityType);
    }

    public void Pause(View view) {
        addUpRot = 0;
        start = false;
        activityMode.setText("Paused");
    }

    public void Stop(View view) {
        Context context = getApplicationContext();
        Toast toast;
        if (stepNumber > 0) {
            start = false;
            stepNumber = 0;
            try {
                bw.flush();
                bw.close();
                toast = Toast.makeText(context, "CSV Saved", Toast.LENGTH_SHORT);
                toast.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            toast = Toast.makeText(context, "Nothing to save", Toast.LENGTH_SHORT);
            toast.show();
        }
        activityMode.setText("Stopped");
    }

    private void write(String stepData) {
        try {
            bw.append(stepData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        activityType = "Walking";
                    } else {
                        activityType = "Upstairs";
                    }
                }
                currentLabel.setText(activityType);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        activityType = "Walking";
                    } else {
                        activityType = "Downstairs";
                    }
                }
                currentLabel.setText(activityType);
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}

