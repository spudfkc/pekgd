package org.pekgd;

import java.sql.SQLException;
import java.util.UUID;

import org.pekgd.db.PekgdDbHelper;
import org.pekgd.model.User;

import com.j256.ormlite.dao.Dao;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class NewUserActivity extends Activity {

    private static final String TAG = NewUserActivity.class.getClass().getName();

    private PekgdDbHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        // Show the Up button in the action bar.
        setupActionBar();

        Button btnSubmit = (Button) findViewById(R.id.btnNewUserSubmit);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

    }

    /**
     * Handled grabbing of data from the form. It then creates a new user and saves it in the
     * database. It will then
     */
    private void submit() {
        String name;
        EditText nameBox = (EditText) findViewById(R.id.nameTextInput);
        name = nameBox.getText().toString();
        if (name == null) {
            // FIXME better validation
            Log.e(TAG, "No name specified for user creation");
            return;
        }
        User user = new User(name, "FIXME"); // FIXME
        // TODO other user info
        try {
            Dao<User, UUID> userDao = getDbHelper().getUserDao();

            userDao.create(user);

            Intent intent = new Intent(this, SavedDataActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(UserActivity.SESSION_USER_ID, user.getId().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
        catch (SQLException e) {
            throw new RuntimeException("Could not create user - " + name);
        }
    };

    /**
     *
     * @return either new or cached db helper for this Context
     */
    private PekgdDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = PekgdDbHelper.getDbHelper(this);
        }
        return dbHelper;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.new_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
