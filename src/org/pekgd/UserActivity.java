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
    static final int SWIPE_THRESHOLD = 50;

    private float hX;
    private float hY;


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
                    newUserBtnClick();
                }

            });
        }

        populateUserList();
    }

    private void newUserBtnClick() {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }

    private void populateUserList() {
        ArrayList<String> usersList = new ArrayList<String>();
        final Dao<User, UUID> userDao;
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

    private PekgdDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = PekgdDbHelper.getDbHelper(this);
        }
        return dbHelper;
    }
}
