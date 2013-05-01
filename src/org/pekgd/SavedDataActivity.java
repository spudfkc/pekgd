package org.pekgd;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.SavedData;
import org.pekgd.model.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * This Activity will show all the saved data for a given user.
 * It will also allow you to view the previous saved data.
 *
 * @author ncc
 *
 */
public class SavedDataActivity extends Activity {

    static private final String TAG = SavedDataActivity.class.getClass().getName();

    private User sessionUser;
    private Dao<User, UUID> userDao;
    private Dao<SavedData, UUID> sessionDao;
    private PekgdDbHelper dbHelper = PekgdDbHelper.getDbHelper(this);

    private ArrayList<SavedData> savedData = new ArrayList<SavedData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_data);
        initialize();
        restoreSessions();

        // When the user clicks the Start Monitor button, it will go to the monitor activity
        Button btnMonitor = (Button) findViewById(R.id.btnStartMonitor);
        if (btnMonitor != null) {
            btnMonitor.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startMonitor();
                }

            });
        }

        // When the user clicks the Delete User button, it will go to the user select activity
        Button btnDelete = (Button) findViewById(R.id.btnDeleteUser);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    deleteUser();
                }

            });
        }

        ListView dataList = (ListView) findViewById(R.id.savedDataList);
        if (savedData.size() == 0) {
            TextView emptyTextView = new TextView(this);
            emptyTextView.setText("No Saved Data Found.");

            LinearLayout layout = (LinearLayout) dataList.getParent();
            layout.removeView(dataList);
            layout.addView(emptyTextView);
        }
        else {
            List<String> savedDataStrs = new ArrayList<String>();
            for (SavedData data : savedData) {
                savedDataStrs.add(data.getStartTime()+"");
            }

            final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedDataStrs);
            dataList.setAdapter(dataAdapter);
            dataList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("name", dataAdapter.getItem(position));
                    try {
                        List<User> qUsers = userDao.queryForFieldValues(params);
                        if (qUsers.size() > 0) {
                            sessionUser = qUsers.get(0);    // FIXME
                        }
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    selectData();
                }
            });
        }
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

    private void deleteUser() {

        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Delete")
        .setMessage("Are you sure you wish to delete this user?")
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDao.delete(sessionUser);
                }
                catch (SQLException e) {
                    Log.e(TAG, "Unable to delete current user", e);
                    throw new RuntimeException(e);
                }

                // Since we deleted the current user, go back to the User select screen
                Intent intent = new Intent(SavedDataActivity.this, UserActivity.class);
                startActivity(intent);
            }

        })
        .setNegativeButton(R.string.no, null)
        .show();

    }

    private void selectData() {
        // TODO
        throw new RuntimeException("Not yet implemented");
    }

    private void initialize() {
        try {
            userDao = dbHelper.getUserDao();
            sessionDao = dbHelper.getSavedDataDao();
        }
        catch (SQLException e) {
            Log.e(TAG, "Could not instantiate DAOs", e);
            throw new RuntimeException(e);
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String userIdStr = bundle.getString(UserActivity.SESSION_USER_ID);
        UUID userId = UUID.fromString(userIdStr);
        try {
            sessionUser = userDao.queryForId(userId);
        }
        catch (SQLException e) {
            Log.e(TAG, "Could not restore user: " + userIdStr, e);
            throw new RuntimeException(e);
        }
    }

    private void restoreSessions() {
        Map<String, Object> queryArgs = new HashMap<String, Object>();
        queryArgs.put("user_id", sessionUser.getId());
        try {
            List<SavedData> tmpResults = sessionDao.queryForFieldValues(queryArgs);
            if (tmpResults != null && tmpResults.size() > 0) {
                savedData.addAll(tmpResults);
            }
        }
        catch (SQLException e) {
            Log.e(TAG, "Could not restore SavedData for user: " + sessionUser.getName());
            // TODO
        }
    }

    private void startMonitor() {
        Intent intent = new Intent(this, MonitorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(UserActivity.SESSION_USER_ID, sessionUser.getId().toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
