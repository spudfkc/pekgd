package org.pekgd;

import java.text.DecimalFormat;
import java.text.Format;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.pekgd.R;
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

    public static final int ANALOG_INPUT_PIN = 33;
    private static final String FORMAT_PATTERN = "";

    private GraphView view;
    private GraphViewSeries currentSeries;
    private String TAG = "PEKGD";


    Format formatter = new DecimalFormat(FORMAT_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (view == null) {
            view = new LineGraphView(this, "TODO: TITLE") {

                /**
                 * This returns the largest possible Y value we can have.
                 * Since we're currently getting a value between 0 and 1 from
                 * the IOIO, we can set the max to 1.
                 * This may have to change later depending on how we decide to
                 * format the y-axis
                 */
                @Override
                protected double getMaxY() {
                    double largest = 1;
                    return largest;
                }

                @Override
                protected double getMinY() {
                    double min = 0;
                    return min;
                }

                /**
                 * This method formats the X and Y values/labels on the different axis
                 */
                @Override
                protected String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // TODO format x-axis
                        return formatter.format(value);
                    }
                    else {
                        // TODO format y-axis
                        return formatter.format(value);
                    }
                }
            };

            view.setBackgroundColor(Color.BLACK);
            view.setViewPort(10, 100000);
            view.setScrollable(true);
            view.setScalable(true);

            GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(Color.RED, 3);
            currentSeries = new GraphViewSeries("TODO description 0", seriesStyle, new GraphViewData[0]);
            view.addSeries(currentSeries);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.addView(view);

        enableUi(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Enables or disables the UI
     * Disabled for when IOIO is not connected
     * @param enable
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
     * @param series
     * @param data
     */
    private void addData(final GraphViewSeries series, final GraphViewData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                series.appendData(data, false);
            }
        });
    }

    /**
     * Creates and returns Looper object
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
     */
    class Looper extends BaseIOIOLooper {
        private AnalogInput input_;
        private DigitalOutput led_;
        private long startTime;

        @Override
        public void setup() throws ConnectionLostException {
            led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
            input_ = ioio_.openAnalogInput(ANALOG_INPUT_PIN);

            startTime = System.currentTimeMillis();

            enableUi(true);
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            led_.write(true);
            final float reading = input_.read();
            double description;
            description = (System.currentTimeMillis() - startTime);
            GraphViewData dataPoint = new GraphViewData(description, reading);
            addData(currentSeries, dataPoint);
            Thread.sleep(500);
        }

        @Override
        public void disconnected() {
            enableUi(false);
        }
    }
}
