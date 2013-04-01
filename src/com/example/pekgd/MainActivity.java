package com.example.pekgd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOConnectionRegistry;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends IOIOActivity {

    public static final int ANALOG_INPUT_PIN = 33;

    private GraphView view;
    private GraphViewSeries currentSeries;
    private String TAG = "PEKGD";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * FIXME
         */
//        IOIOConnectionRegistry.addBootstraps(new String[] {
//            "ioio.lib.impl.SocketIOIOConnectionBootstrap",
//            //"ioio.lib.android.accessory.AccessoryConnectionBootstrap",
//            "ioio.lib.android.bluetooth.BluetoothIOIOConnectionBootstrap"
//        });


        view = new LineGraphView(this, "TODO: TITLE") {
            /**
             * This method formats the X and Y values/labels on the different axis
             */
            @Override
            protected String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // TODO format x-axis
                    return value + "";
                }
                else {
                    // TODO format y-axis
                    return value + "";
                }
            }
        };

        view.setBackgroundColor(Color.BLACK);
        view.setScalable(false);
        view.setScrollable(true);

        // This will set the default view of the graph
//        view.setViewPort(arg0, arg1);


        GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(Color.RED, 3);
        currentSeries = new GraphViewSeries("TODO description 0", seriesStyle, new GraphViewData[0]);
        view.addSeries(currentSeries);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        Log.d(TAG, "Is view null? " + view + " END");
        layout.addView(view);

        enableUi(false);
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
