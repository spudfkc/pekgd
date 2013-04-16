package org.pekgd;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.SavedData;
import org.pekgd.model.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * This Activity saves and graphs the heart activity of a user.
 *
 * @author ncc
 *
 */
public class MonitorActivity extends IOIOActivity {

    // The pin on which to read the voltage from the heart monitor circuit.
    public static final int ANALOG_INPUT_PIN = 33;

    // This is how the labels along the graph axises will be formatted.
    private static final String FORMAT_PATTERN = "####.##";

    private PekgdDbHelper dbHelper = null;

    // This is the actual graph that is displayed on the screen. It is the
    // main component of graphing.
    private GraphView view = null;

    // This is the series of data points that we will be displaying on the graph
    // The single GraphView can have multiple series, but we only need one.
    private GraphViewSeries currentSeries;

    // TODO explain
    private SavedData sessionData = null;

    // TODO explain
    private User sessionUser = null;

    // the tag that the logger uses for this class
    // Sticking to the format of the fully-qualified class name
    private String TAG = MonitorActivity.class.getClass().getName();

    // This does the actual formatting of the axis labels
    // using the given pattern.
    private Format formatter = new DecimalFormat(FORMAT_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        // FIXME get user? how?
        Dao<User, UUID> userDao = null;
        try {

            userDao = getDbHelper().getUserDao();
            sessionUser = new User("admin", "admin");
            userDao.create(sessionUser);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if (sessionUser == null) {
            throw new RuntimeException("No User for the current session!");
        }

        // TODO explain
        sessionData = new SavedData(sessionUser);

        // If we do not yet have a view, create one
        if (view == null) {
            // We override some methods here so we can adjust how the graph looks.
            // If we don't, the graph will keep auto-adjusting the scale and it
            // becomes unreadable.
            String title = "-No User-";
            if (intent != null) {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    String userIdStr = extras.getString(UserActivity.SESSION_USER_ID);
                    if (userIdStr != null && !userIdStr.equals("")) {
                        UUID userId = UUID.fromString(userIdStr);
                        try {
                            sessionUser = userDao.queryForId(userId);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
            }

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
            view.setViewPort(700, 50000);

            // This allows the user to scroll the graph from left to right
            view.setScrollable(true);

            // This will allow the user to scale the graph, so they can zoom in
            // or out on different areas of the graph to get more detail or a
            // higher level of view.
            view.setScalable(true);

            // Let's change how the series looks(that contains the analog input from the IOIO)
            GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(Color.YELLOW, 3);
            currentSeries = new GraphViewSeries(
                    "TODO description 0", seriesStyle, new GraphViewData[0]);

            // Finally, add the series to the graph
            view.addSeries(currentSeries);
        }

        // grab the layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

        // and dd the graph to the layout
        layout.addView(view);



        // Disable the UI until the IOIO is connected
        enableUi(false);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
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
//                view.invalidate(); // TESTING
                sessionData.addDataPoint(data.valueX, data.valueY);
//                try {
//                    Dao<SavedData, UUID> dataDao = getDbHelper().getSavedDataDao();
//                    dataDao.createOrUpdate(sessionData);
//                }
//                catch (SQLException e) {
//                    Log.e(TAG, "Unable to save or update data", e);
//                    throw new RuntimeException("Unable to save or update data", e);
//                }
            }
        });
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
            led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
            input_ = ioio_.openAnalogInput(ANALOG_INPUT_PIN);

            startTime = System.currentTimeMillis();

            enableUi(true);
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
            final float reading = input_.read();
            double description;
            description = (System.currentTimeMillis() - startTime);
            GraphViewData dataPoint = new GraphViewData(description, reading);
            addData(currentSeries, dataPoint);
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) { /* we can't do much about this */ }
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
