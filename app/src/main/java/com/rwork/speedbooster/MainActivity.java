package com.rwork.speedbooster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private int menuId = 0;

    private Fragment fragSelected = null;

    private Fragment fragMain = null;
    private Fragment fragAvailable = null;
    private Fragment fragHistory = null;
    private Fragment fragFavorite = null;
    private Fragment fragSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SidebarFragment drawerFragment = (SidebarFragment)getSupportFragmentManager().findFragmentById(R.id.sidebar);
        drawerFragment.setUp(R.id.sidebar, (DrawerLayout)findViewById(R.id.main_layout), toolbar);
if(savedInstanceState == null) {
    processAction(R.id.sidebarMain);
}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        switch (menuId) {

            case R.id.sidebarSettings:
                getMenuInflater().inflate(R.menu.menu_settings, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean result = processAction(id);
        if (result)
            return result;
        return super.onOptionsItemSelected(item);
    }

    public boolean processAction(int id) {
        if (Globals.getInstance(this).isTestRunning())
            return false;

        FragmentTransaction fragTransaction;

        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.sidebarMain:
                if (fragMain == null)
                    fragMain = new MainFragment();

                if (fragSelected != fragMain) {
                    fragSelected = fragMain;
                    fragTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragSelected);
                    fragTransaction.commit();
                    menuId = R.id.sidebarMain;
                    invalidateOptionsMenu();
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                return true;

            case R.id.sidebarAvailable:
            case R.id.action_available:
                if (fragAvailable == null)
                    fragAvailable = getFragment(0);
                if (fragSelected != fragAvailable) {
                    fragSelected = fragAvailable;
                    fragTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragSelected);
                    fragTransaction.commit();
                    menuId = R.id.sidebarAvailable;
                    invalidateOptionsMenu();
                    getSupportActionBar().setTitle(R.string.available);
                }
                return true;

            case R.id.sidebarHistory:
            case R.id.action_history:
                if (fragHistory == null)
                    fragHistory= getFragment(1);
                if (fragSelected != fragHistory) {
                    fragSelected = fragHistory;
                    fragTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragSelected);
                    fragTransaction.commit();
                    menuId = R.id.sidebarHistory;
                    invalidateOptionsMenu();
                    getSupportActionBar().setTitle(R.string.history);
                }
                return true;

            case R.id.sidebarFavorite:
            case R.id.action_favorite:
                if (fragFavorite == null)
                    fragFavorite = getFragment(2);
                if (fragSelected != fragFavorite) {
                    fragSelected = fragFavorite;
                    fragTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragSelected);
                    fragTransaction.commit();
                    menuId = R.id.sidebarFavorite;
                    invalidateOptionsMenu();
                    getSupportActionBar().setTitle(R.string.favorite);
                }
                return true;

            case R.id.sidebarSettings:
            case R.id.action_settings:
                if (fragSettings == null)
                    fragSettings = new SettingsFragment();
                if (fragSelected != fragSettings) {
                    fragSelected = fragSettings;
                    fragTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragSelected);
                    fragTransaction.commit();
                    menuId = R.id.sidebarSettings;
                    invalidateOptionsMenu();
                    getSupportActionBar().setTitle(R.string.action_settings);
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                break;
        }

        return false;
    }

    private Fragment getFragment(int index) {

        MultiViewFragment fragment = new MultiViewFragment();
            Bundle args = new Bundle();
            args.putInt("tab_index", index);
        fragment.setArguments(args);
        return fragment;
    }


}
