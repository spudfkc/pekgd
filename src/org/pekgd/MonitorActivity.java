package org.pekgd;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.DataPoint;
import org.pekgd.model.SavedData;
import org.pekgd.model.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * This Activity saves and graphs the waveform of a user's heart.
 *
 * @author ncc
 *
 */
public class MonitorActivity extends IOIOActivity {


    // Maximum size of the buffer for the IOIO sampling
    public static final int MAX_BUFFER = 10000;

    // The pin on which to read the voltage from the heart monitor circuit.
    public static final int ANALOG_INPUT_PIN = 42;

    // the tag that the logger uses for this class
    // Sticking to the format of the fully-qualified class name
    private static final String TAG = MonitorActivity.class.getClass().getName();

    // This is how the labels along the graph axises will be formatted.
    private static final String FORMAT_PATTERN = "####.##";

    // Helper object for accessing DAOs and communicating with the database
    private PekgdDbHelper dbHelper = null;

    // This is the actual graph that is displayed on the screen. It is the
    // main component of graphing.
    private GraphView view = null;

    // This is the series of data points that we will be displaying on the graph
    // The single GraphView can have multiple series, but we only need one.
    private GraphViewSeries currentSeries;
    private GraphViewSeries nextSeries;

    // The data collected and to be saved in the database - similar to GraphViewSeries
    private SavedData sessionData = null;

    // The user that this monitoring session is for
    private User sessionUser = null;

    // This does the actual formatting of the axis labels
    // using the given pattern.
    private Format formatter = new DecimalFormat(FORMAT_PATTERN);

    // beats per minute of the current user
    private int bpm;

    private boolean paused = false;

    // Data Access Objects used to access different objects from the database
    private Dao<SavedData, UUID> dataDao;
    private Dao<DataPoint, UUID> pointDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        Dao<User, UUID> userDao = null;
        // If we do not yet have a view, create one
        if (view == null) {
            String title = "-No User-";

            // get the current user from the intent and set it for this session
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String userIdStr = extras.getString(UserActivity.SESSION_USER_ID);
                if (userIdStr != null && !userIdStr.equals("")) {
                    UUID userId = UUID.fromString(userIdStr);
                    try {
                        userDao = getDbHelper().getUserDao();
                        sessionUser = userDao.queryForId(userId);
                    } catch (SQLException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
            if (sessionUser == null) {
                throw new RuntimeException("No user specified!");
            }

            title = sessionUser.getName();
            // Set the user for the dataset
            sessionData = new SavedData(sessionUser);

            // We override some methods here so we can adjust how the graph looks.
            // If we don't, the graph will keep auto-adjusting the scale and it
            // becomes unreadable.
            view = new LineGraphView(this, title) {

                /**
                 * This returns the largest possible Y value we can have.
                 * Since we're currently getting a value between 0 and 1 from
                 * the IOIO AnalogInput, we can set the max to 1.
                 *
                 * This may have to change later depending on how we decide to
                 * format labels.
                 */
                @Override
                protected double getMaxY() {
                    double largest = 1;
                    return largest;
                }

                /**
                 * This returns the smallest possible Y value of the graph. We set this to
                 * 0 because we should never get a negative voltage from the IOIO AnalogInput.
                 *
                 */
                @Override
                protected double getMinY() {
                    double min = 0;
                    return min;
                }

                /**
                 * This method will format the X and Y axis labels to be more readable to
                 * humans. It will cut off some of the extra voltage digits that aren't needed
                 * and add some units.
                 */
                @Override
                protected String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // TODO format x-axis
                        return Math.round(value)+"ms";
                    }
                    else {
                        // TODO format y-axis
                        return value+"";
//                        return formatter.format(value)+"mV";
                    }
                }
            };

            // Set the background color of the graph to black
            view.setBackgroundColor(Color.BLACK);

            // This sets the starting position and size of the viewport
            // This may need more tweaking to look right. Also, I'm not sure
            // of performance impacts of such a large viewport.
            // TODO - parameterize
            view.setViewPort(700, 5000);

            // This allows the user to scroll the graph from left to right
            view.setScrollable(true);

            // This will allow the user to scale the graph, so they can zoom in
            // or out on different areas of the graph to get more detail or a
            // higher level of view.
            view.setScalable(true);

            // Let's change how the series looks(that contains the analog input from the IOIO)
            GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(Color.YELLOW, 3);
            currentSeries = new GraphViewSeries(
                    "placeholder", seriesStyle, new GraphViewData[0]);

            // Finally, add the series to the graph
            view.addSeries(currentSeries);
        }

        // grab the layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

        // and dd the graph to the layout
        layout.addView(view);

        try {
            dataDao = getDbHelper().getSavedDataDao();
        }
        catch (SQLException e) {
            Log.e(TAG, "Could not get SavedData Dao", e);
        }

        // Disable the UI until the IOIO is connected
        initSwitch();
        enableUi(false);
        Toast.makeText(this, "Waiting on IOIO connection...", Toast.LENGTH_SHORT).show();
    }

    /**
     * @explain
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the dbHelper
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }

//        dataDao.createOrUpdate(currentSeries);
    }

    /**
     * @TODO
     */
    @Override
    protected void onPause() {
        super.onPause();
        // TODO
    }

    /**
     * @explain
     * This will populate the ActionBar with the items given in activity_main xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * @explain
     * This determines what to when a menu item is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() - " + item + " : " + item.getItemId());
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_user:
                intent = new Intent(this, UserActivity.class);
                break;
            case R.id.menu_saved_data:
                break;
            case R.id.menu_settings:
                break;
            case R.id.menu_monitor:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        if (intent != null) {
            intent.putExtra(UserActivity.SESSION_USER_ID, sessionUser.getId());
            startActivity(intent);
        }
        return true;
    }

    private void initSwitch() {
        Switch swtPause = (Switch) findViewById(R.id.swtPause);
        swtPause.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                pause(isChecked);
            }
        });
    }

    private void pause(boolean value) {
        if (value) {
            if (paused) Log.e(TAG, "Cannot pause - already paused");
            else paused = !paused;
        }
        else {
            if (!paused) Log.e(TAG, "Cannot unpause - already unpaused");
            else paused = !paused;
        }
    }

    /**
     * @explain
     *
     * @return new or cached database helper. It is used to read and
     * write to the database.
     */
    private PekgdDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = PekgdDbHelper.getDbHelper(this);
        }
        return dbHelper;
    }

    /**
     * Enables or disables the UI
     * Disabled for when IOIO is not connected
     * @param enable enables the UI if true, disables the UI if false
     */
    private void enableUi(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(enable);
            }
        });
    }

    /**
     * Appends the given data to the given series and scrolls the graph to the end
     * @param series which series on the graph to append the data to
     * @param data the data to be appended
     */
    private void addData(final GraphViewSeries series, final GraphViewData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                series.appendData(data, true);
                sessionData.addDataPoint(data.valueX, data.valueY);
//                try {
//                    dataDao.createOrUpdate(sessionData);
//                }
//                catch (SQLException e) {
//                    Log.e(TAG, "Unable to save or update data", e);
//                    throw new RuntimeException("Unable to save or update data", e);
//                }
            }
        });
    }

    private void writeDataToSD(GraphViewSeries series) {
        Log.d(TAG, "Writing data...");
        GraphViewData[] data = series.getValues();
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/.pekgd/data");
        dir.mkdirs();
        File output = new File(dir, "data.out");
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            for (GraphViewData d : data) {
                if (d.valueX != 0 && d.valueY != 0) {
                    fos.write(String.valueOf(d.valueX).getBytes());
                    fos.write(',');
                    fos.write(String.valueOf(d.valueY).getBytes());
                    fos.write('\n');
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "File not found!", e);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {}
            }
        }
        Log.d(TAG, "Finished writing data to: \n" + output.getAbsolutePath());

    }

    /**
     * Creates and returns a new Looper object used to interface with the IOIO
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    /**
     *
     * @author ncc
     *
     * Looper object that handles what to do with the IOIO
     *
     * This object has a method to handle what to do during setup, setup(), which is when
     * the IOIO is first connected. It then repeatedly loops until the IOIO is disconnected.
     * At that point, the disconnected() method is then ran to handle any shutdown or
     * disconnect actions that should take place.
     *
     */
    class Looper extends BaseIOIOLooper {

        // This will be used to read the analog input on the IOIO.
        // The analog input will contain the voltage on the pin
        // defined by ANALOG_INPUT_PIN.
        private AnalogInput input_;

        // This will control the LED (DigitalOutput) on the IOIO.
        private DigitalOutput led_;

        // This holds the time that the graph was started, it does not
        // get set until the IOIO is connected.
        private long startTime;

        private long currentSample;
        private final int MAX_SERIES_COUNT = 7000;
        private final double THRESHOLD = 0.55;
        private int currentSeriesCount = 0;
        private int nextSeriesCount = 0;

        /**
         * This method is ran once the IOIO has been successfully connected. It contains
         * any startup actions that need to take place. It should be setting the LED
         * output and the analog input for the voltage. It should also set the start time
         * so we have a reference of when the monitoring started.
         *
         * @throws ConnectionLostException This is thrown when the connection between the
         * application and the IOIO is lost.
         */
        @Override
        public void setup() throws ConnectionLostException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MonitorActivity.this, "IOIO Connected.", Toast.LENGTH_SHORT).show();
                }

            });
            input_ = ioio_.openAnalogInput(ANALOG_INPUT_PIN);
            input_.setBuffer(MAX_BUFFER);

            startTime = System.currentTimeMillis();
            currentSample = 0;
            enableUi(true);
            startTime = System.currentTimeMillis();
        }

        /**
         * This gets ran continuously after the setup() method is ran. It loops until the
         * connection to the IOIO is disconnected.
         * The main purpose of this function is to read the analog input (voltage from the
         * amplifying heart circuit). That data should then be saved for future reference
         * and also passed along to the UI thread so we can update the graph.
         *
         * Since this Looper object is ran in its own thread, we cannot access the UI thread
         * from this thread. But, we can send the data via the addData() method since that will
         * run some actions on the UI thread for us.
         *
         * @throws ConnectionLostException This is thrown when the connection between the
         * application and the IOIO is lost.
         *
         * @throws InterruptedException If the thread has been interrupted while reading from
         * the analog input.
         *
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            if (!paused) {
                int maxRead = 50;
                int samplesAvailable = input_.available();
                Log.d(TAG, "***AvailableSamples*** -> " + samplesAvailable);
                if (samplesAvailable < maxRead) { maxRead = samplesAvailable; }
                for (int i = 0; i < maxRead; i++) {
                    input_.readBuffered();
                    final float reading = input_.readBuffered();
                    GraphViewData dataPoint = new GraphViewData(currentSample++, reading);

                    addData(currentSeries, dataPoint);
                    calculateBpm(reading, currentSample);
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) { /* we can't do much about this */ }

                if (currentSample > MAX_SERIES_COUNT) {
                    Log.d(TAG, "REFRESHING SERIES");
                    currentSample = 0;
                    writeDataToSD(currentSeries);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentSeries.resetData(new GraphViewData[0]);
                        }
                    });
                }

            }
        }

        /**
         * This is ran once the IOIO has been disconnected. This should handle anything that
         * needs to be shutdown or any other actions that should take place if the IOIO gets
         * disconnected.
         *
         * All we need to do for now is disable the UI, which basically disabled any actions to
         * or from the IOIO.
         */
        @Override
        public void disconnected() {
            enableUi(false);
        }
    }
}
