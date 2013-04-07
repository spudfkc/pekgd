package org.pekgd.db.contract;

import android.provider.BaseColumns;

public class MonitorContract implements BaseColumns {
    public static final String TABLE_NAME             = "monitor";
    public static final String COLUMN_NAME_USER_ID    = "userid";
    public static final String COLUMN_NAME_START_TIME = "starttime";
    public static final String COLUMN_NAME_END_TIME   = "endtime";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP= ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_START_TIME+ INTEGER_TYPE + COMMA_SEP +
            COLUMN_NAME_END_TIME+ INTEGER_TYPE + COMMA_SEP +
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public MonitorContract() { /* no need to instantiate */ }
}
