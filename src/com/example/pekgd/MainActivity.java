package com.example.pekgd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;

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

public class MainActivity extends Activity {


    class IOIOGraphThread extends Thread {

        public static final int ANALOG_INPUT_PIN = 35;

        private IOIO ioio;
        private boolean abort = false;
        private String TAG = "IOIOThread";
        private GraphView view;
        private GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(Color.RED, 3);

        /**
         * Constructor
         *
         * @param view The graph view to display the data
         */
        public IOIOGraphThread(GraphView view) {
            this.view = view;
        }

        /**
         * Starts the thread
         */
        @Override
        public void run() {
            super.run();
            while (true) {
                synchronized (this) {
                    if (abort) {
                        break;
                    }
                    ioio = IOIOFactory.create();
                }

                try {
                    // setStatusBarText(WaitingIOIOConnection)
                    ioio.waitForConnect();
                    // setStatusBarText(IOIOConnected)
                    AnalogInput voltage = ioio.openAnalogInput(ANALOG_INPUT_PIN);

                    // Create default series with no data. we will append data as we get it
                    GraphViewSeries series = new GraphViewSeries("TODO description 0", seriesStyle, new GraphViewData[0]);
                    view.addSeries(series);
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        long description;
                        List<GraphViewData> data = new ArrayList<GraphViewData>();
                        GraphViewData receivedData = null;
                        for (int i = 0; i < 10; i++) {
                            description = (System.currentTimeMillis() - startTime);
                            float value = voltage.read();
                            receivedData = new GraphViewData(description, value);
                            data.add(receivedData);
                            // TODO maybe add a sleep here?
                        }
                        series.appendData(receivedData, true);
                    }
                }
                catch (ConnectionLostException e) {
                    // TODO
                    Log.e(TAG, "Lost connectiont to IOIO!", e);
                    if (ioio != null) {
                        ioio.disconnect();
                    }
                    break;
                }
                catch (Exception e) {
                    Log.e(TAG, "Unexpected Exception Caught", e);
                    if (ioio != null) {
                        ioio.disconnect();
                    }
                    break;
                }
                finally {
                    if (ioio != null) {
                        try {
                            ioio.waitForDisconnect();
                        }
                        catch (InterruptedException e) { // no-op }
                    }
                        synchronized (this) {
                            ioio = null;
                        }
                    }
                }
            }
        }
    };

    private IOIOGraphThread ioioThread;
    private GraphView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        view.setScalable(true);
        view.setScrollable(true);

        // This will set the default view of the graph
//        view.setViewPort(arg0, arg1);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.addView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (view == null) {
            throw new RuntimeException("No GraphView to attach to thread!");
        }
        ioioThread = new IOIOGraphThread(view);
        ioioThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ioioThread.abort = true;
        try {
            ioioThread.join();
        }
        catch (InterruptedException e) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
