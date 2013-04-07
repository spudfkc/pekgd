package org.pekgd.db.contract;

import android.provider.BaseColumns;

public class DataPointContract implements BaseColumns {
    public static final String TABLE_NAME           = "datapoint";
    public static final String COLUMN_NAME_X_VALUE  = "time";
    public static final String COLUMN_NAME_Y_VALUE  = "voltage";
    public static final String COLUMN_NAME_MONITOR_ID = "monitorid";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP= ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_X_VALUE + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_Y_VALUE+ INTEGER_TYPE + COMMA_SEP +
            COLUMN_NAME_MONITOR_ID+ INTEGER_TYPE + COMMA_SEP +
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DataPointContract() { /* no need to instantiate */ }
}
