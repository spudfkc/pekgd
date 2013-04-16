package org.pekgd.db;

import java.sql.SQLException;
import java.util.UUID;

import org.pekgd.db.contract.DataPointContract;
import org.pekgd.db.contract.MonitorContract;
import org.pekgd.db.contract.UserContract;
import org.pekgd.model.DataPoint;
import org.pekgd.model.SavedData;
import org.pekgd.model.User;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.R;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This is a helper class to handle creating, deleting, updating the database.
 *
 * To access the db, instantiate this class.
 *
 * @author ncc
 *
 */
public class PekgdDbHelper extends OrmLiteSqliteOpenHelper {

    private static String TAG = PekgdDbHelper.class.getName();

    public static PekgdDbHelper getDbHelper(Context context) {
        return OpenHelperManager.getHelper(context, PekgdDbHelper.class);
    }


    // Version of the database - change this when you make changes
    // related to the database
    public static final int DATABASE_VERSION = 1;

    // The name of the database for the app
    public static final String DATABASE_NAME = "Pekgd.db";

    // Dao object used to access users
    private Dao<User, UUID> userDao = null;
    private RuntimeExceptionDao<User, UUID> userRuntimeDao = null;

    // Dao object used to access saved data
    private Dao<SavedData, UUID> savedDataDao = null;
    private RuntimeExceptionDao<SavedData, UUID> savedDataRuntimeDao = null;

    // Dao object used to access datapoints
    private Dao<DataPoint, UUID> dataPointDao = null;
    private RuntimeExceptionDao<DataPoint, UUID> dataPointRuntimeDao = null;

    /**
     *
     * @param context
     */
    public PekgdDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        // Create all the tables
        Log.d(TAG, "onCreate()");
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, SavedData.class);
        }
        catch (SQLException e) {
            Log.e(TAG, "Did not successfully create database!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO how to handle upgrades?

        Log.d(TAG, "onUpgrade()");
        // This will currently just drop all tables and call create again
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, SavedData.class, true);
            onCreate(db, connectionSource);
        }
        catch (SQLException e) {
            Log.e(TAG, "Did not successfully create database!", e);
            throw new RuntimeException(e);
        }

        onCreate(db);
    }

    /**
     *
     */
    @Override
    public void close() {
        super.close();
        userDao = null;
        savedDataDao = null;
    }

    /*
     * DAOs
     *
     * Use these if NOT extending OrmLiteActivity?
     */

    public Dao<User, UUID> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    public Dao<SavedData, UUID> getSavedDataDao() throws SQLException {
        if (savedDataDao == null) {
            savedDataDao = getDao(SavedData.class);
        }
        return savedDataDao;
    }

    public Dao<DataPoint, UUID> getDataPointDao() throws SQLException {
        if (dataPointDao == null) {
            dataPointDao = getDao(DataPoint.class);
        }
        return dataPointDao;
    }

    /*
     * RuntimeDAOs
     *
     * Use these if you ARE extending OrmLiteActivity?
     */

    public RuntimeExceptionDao<User, UUID> getRuntimeUserDao() {
        if (userRuntimeDao == null) {
            userRuntimeDao = getRuntimeExceptionDao(User.class);
        }
        return userRuntimeDao;
    }

    public RuntimeExceptionDao<SavedData, UUID> getRuntimeSavedDataDao() {
        if (savedDataRuntimeDao == null) {
            savedDataRuntimeDao = getRuntimeExceptionDao(SavedData.class);
        }
        return savedDataRuntimeDao;
    }

    public Dao<DataPoint, UUID> getRuntimeDataPointDao() throws SQLException {
        if (dataPointRuntimeDao == null) {
            dataPointRuntimeDao = getRuntimeExceptionDao(DataPoint.class);
        }
        return dataPointDao;
    }
}
