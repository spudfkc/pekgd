package org.pekgd.db;

import org.pekgd.db.contract.DataPointContract;
import org.pekgd.db.contract.MonitorContract;
import org.pekgd.db.contract.UserContract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is a helper class to handle creating, deleting, updating the database.
 *
 * To access the db, instantiate this class.
 *
 * @author ncc
 *
 */
public class PekgdDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pekgd.db";

    public PekgdDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all the tables
        db.execSQL(MonitorContract.SQL_CREATE_ENTRIES);
        db.execSQL(DataPointContract.SQL_CREATE_ENTRIES);
        db.execSQL(UserContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO how to handle upgrades?

        // This will currently just drop all tables and call create again
        db.execSQL(UserContract.SQL_DELETE_ENTRIES);
        db.execSQL(DataPointContract.SQL_DELETE_ENTRIES);
        db.execSQL(MonitorContract.SQL_DELETE_ENTRIES);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO does this even work?
        onUpgrade(db, oldVersion, newVersion);
    }
}
