package org.pekgd.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.jjoe64.graphview.GraphView.GraphViewData;

public class DataPoint implements Comparable<DataPoint> {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField
    private double xvalue;

    @DatabaseField
    private double yvalue;

    @DatabaseField(foreign = true)
    private SavedData series;

    private GraphViewData point = null;

    public DataPoint() { /* ORMLite Constructor */ }

    public DataPoint(double xvalue, double yvalue, SavedData series) {
        id = UUID.randomUUID();
        this.series = series;
        this.xvalue = xvalue;
        this.yvalue = yvalue;
    }

    public UUID getId() {
        return id;
    }

    public double getYvalue() {
        return yvalue;
    }

    public void setYvalue(double yvalue) {
        this.yvalue = yvalue;
        point = null;
    }

    public double getXvalue() {
        return xvalue;
    }

    public void setXvalue(double xvalue) {
        this.xvalue = xvalue;
        point = null;
    }

    public GraphViewData getGraphViewData() {
        if (point == null) {
            point = new GraphViewData(xvalue, yvalue);
        }
        return point;
    }

    public SavedData getSeries() {
        return series;
    }

    @Override
    public int compareTo(DataPoint another) {
        if (this.xvalue < another.xvalue) return -1;
        else if (this.xvalue > another.xvalue) return 1;
        return 0;
    }

}
