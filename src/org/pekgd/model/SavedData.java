package org.pekgd.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class SavedData {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField(canBeNull = false, foreign = true)
    private User user;

    @ForeignCollectionField
    private Collection<DataPoint> data = new ArrayList<DataPoint>();

    public SavedData() { /* needed for ORMLite */ }

    public SavedData(User user) {
        id = UUID.randomUUID();
        this.setUser(user);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<DataPoint> getData() {
        List<DataPoint> result = new ArrayList<DataPoint>();
        result.addAll(data);
        Collections.sort(result);
        return result;
    }

    /**
     * Adds this point to the end of the list of data
     *
     * @param data
     */
    public void addDataPoint(DataPoint point) {
        data.add(point);
    }

    /**
     * Adds this point to the end of the list of data
     *
     * @param x
     * @param y
     */
    public void addDataPoint(double x, double y) {
        data.add(new DataPoint(x, y, this));
    }

}
