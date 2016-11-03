package com.trombipeti.simplecallblocker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.trombipeti.simplecallblocker.adapter.ContactAdapter;
import com.trombipeti.simplecallblocker.model.BlockProfilesSingleton;
import com.trombipeti.simplecallblocker.service.CallListenerService;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    private boolean serviceBound;


    public static final String DATACHANGE_BROADCAST = "com.trombipeti.simplecallblocker.datachangebroadcast";

    public static final String KEY_SELECTED_PROFILE = "SelectedProfile";

    private CallListenerService callService;
    private ServiceConnection callServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            callService = ((CallListenerService.MyBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            callService = null;
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("service_running")) {
                boolean serviceRunning = sharedPreferences.getBoolean("service_running", true);
                if (serviceRunning) {
                    Intent i = new Intent(MainActivity.this, CallListenerService.class);
                    startService(i);
                    if (!serviceBound) {
                        bindService(i, callServiceConnection, BIND_AUTO_CREATE);
                        serviceBound = true;
                    }
                    Toast.makeText(getApplicationContext(), "Call listener service started", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(MainActivity.this, CallListenerService.class);
                    if (serviceBound) {
                        unbindService(callServiceConnection);
                        serviceBound = false;
                    }
                    stopService(i);
                    Toast.makeText(getApplicationContext(), "Call listener service stopped", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();


        BlockProfilesSingleton.Instance().getDefaultProfile()
                .setName(getResources().getString(R.string.title_default_profile));

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean serviceRunning = myPrefs.getBoolean("service_running", true);
        if (serviceRunning) {
            Intent i = new Intent(MainActivity.this, CallListenerService.class);
            startService(i);
            if (!serviceBound) {
                bindService(i, callServiceConnection, BIND_AUTO_CREATE);
                serviceBound = true;
            }
        } else {
            serviceBound = false;
        }
        ListView lvContacts = (ListView) findViewById(R.id.contacts_listview);
        ((ContactAdapter) (lvContacts.getAdapter())).notifyDataSetChanged();

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean serviceRunning = myPrefs.getBoolean("service_running", true);
        if (serviceBound && !serviceRunning) {
            unbindService(callServiceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefsListener);
        boolean serviceRunning = myPrefs.getBoolean("service_running", true);
        if (serviceBound && serviceRunning) {
            unbindService(callServiceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == BlockProfilesSingleton.Instance().size()) {
            Intent i = new Intent(MainActivity.this, BlockerSettingsActivity.class);
            startActivity(i);

            SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            myPrefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener);
            return;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_SELECTED_PROFILE, position);
        arguments.putInt(ProfileDetailFragment.ARG_SECTION_NUMBER, position);
        ProfileDetailFragment newFragment = new ProfileDetailFragment();
        newFragment.setArguments(arguments);
        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        if (number < BlockProfilesSingleton.Instance().size()) {
            mTitle = BlockProfilesSingleton.Instance().get(number).getName();
            invalidateOptionsMenu();
        } else {
            mTitle = getString(R.string.title_settings);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */

}
