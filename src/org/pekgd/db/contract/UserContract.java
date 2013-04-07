package org.pekgd.db.contract;

import android.provider.BaseColumns;

public class UserContract implements BaseColumns {
    public static final String TABLE_NAME            = "user";
    public static final String COLUMN_NAME_NAME      = "name";
    public static final String COLUMN_NAME_PASSWORD  = "password";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP= ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_PASSWORD+ INTEGER_TYPE + COMMA_SEP +
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public UserContract() { /* no need to instantiate */ }

}
