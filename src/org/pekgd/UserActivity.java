package org.pekgd;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.User;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.j256.ormlite.dao.Dao;

/**
 * This Activity will be used for creating new users and switching
 * between users for monitoring.
 *
 * @author ncc
 *
 */
public class UserActivity extends Activity {

    /*
     * Show current user at top
     * Option to add new user at top
     * Display list of users in listView
     *  onclick of user, switch to that user? or start new MonitorActivity?
     */

    static final String TAG = UserActivity.class.getClass().getName();
    static final String SESSION_USER_ID = "sessionUserId";

    private User sessionUser = null;
    private PekgdDbHelper dbHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        populateUserList();
    }

    private void populateUserList() {
        ArrayList<String> usersList = new ArrayList<String>();
        Dao<User, UUID> userDao = null;
        try {
            userDao = getDbHelper().getUserDao();
            List<User> users = userDao.queryForAll();
            for (User user : users) {
                usersList.add(user.getName());
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "Could not get UserDAO", e);
            throw new RuntimeException("Could not get User DAO", e);
        }

        ArrayAdapter<String> usersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usersList);
        ListView userList = (ListView) findViewById(R.id.userList);
        userList.setAdapter(usersAdapter);
//        userList.
    }

    private PekgdDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = PekgdDbHelper.getDbHelper(this);
        }
        return dbHelper;
    }
}
