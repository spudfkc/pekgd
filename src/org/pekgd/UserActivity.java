package org.pekgd;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * This Activity will be used for creating new users and switching
 * between users for monitoring.
 *
 * @author ncc
 *
 */
public class UserActivity extends Activity {

    static final String TAG = UserActivity.class.getClass().getName();
    static final String SESSION_USER_ID = "sessionUserId";
//    static final int SWIPE_THRESHOLD = 50;
//
//    private float hX;
//    private float hY;

    // DAO for accessing User objects
    private Dao<User, UUID> userDao = null;

    // The currently selected user for the session
    private User sessionUser = null;

    private PekgdDbHelper dbHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Set new user button up
        Button btnNew = (Button) findViewById(R.id.btnNewUser);
        if (btnNew != null) {
            btnNew.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    newUser();
                }

            });
        }
    }

    @Override
    protected void onStart() {
        // TODO
        super.onStart();
        populateUserList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the dbHelper
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
    }

    /**
     * Starts the NewUserActivity to create a new user
     */
    private void newUser() {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }

    /**
     * Populates the list of users in the view
     * Also sets up onClick actions for the items, when an item is clicked the SavedDatactivity
     * will start.
     */
    private void populateUserList() {
        /* There is a bug in this code where we can run into potential problems when having users
         * with the same name. We may not get the specific user we clicked.
         */
        ArrayList<String> usersList = new ArrayList<String>();
        try {
            if (userDao == null) userDao = getDbHelper().getUserDao();
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

        final ArrayAdapter<String> usersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usersList);
        ListView userList = (ListView) findViewById(R.id.userList);
        userList.setAdapter(usersAdapter);
        userList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("name", usersAdapter.getItem(position));
                try {
                    List<User> qUsers = userDao.queryForFieldValues(params);
                    if (qUsers.size() > 0) {
                        sessionUser = qUsers.get(0);    // FIXME
                    }
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                selectUser();
            }
        });
    }

    /**
     * Takes the session user and starts the SavedDataActivity with that userId stored
     * in the Intent.
     */
    private void selectUser() {
        if (sessionUser == null) {
            throw new RuntimeException("No user selected!");
        }
        Intent intent = new Intent(this, SavedDataActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(SESSION_USER_ID, sessionUser.getId().toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     *
     * @return cached or new PekgdDbHelper
     */
    private PekgdDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = PekgdDbHelper.getDbHelper(this);
        }
        return dbHelper;
    }
}
