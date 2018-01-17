package com.rwork.speedbooster;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by super2lao on 2/1/2016.
 */
public class SidebarFragment extends Fragment {

    public static final String PREF_FILE_NAME = "testpref";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    private View containerView;

    public static SidebarFragment newInstance(String param1, String param2) {
        SidebarFragment fragment = new SidebarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SidebarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "true"));
        if(savedInstanceState != null ){
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sidebar, container, false);
    }

    /////////////////////////////////
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setUp(int fragmentId, DrawerLayout drawerLayput, final Toolbar toolBar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawer = drawerLayput;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayput, toolBar, R.string.drawer_open, R.string.drawer_close){


            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(!mUserLearnedDrawer){
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer+"");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        if(!mUserLearnedDrawer && !mFromSavedInstanceState){
            mDrawer.openDrawer(containerView);
        }
        mDrawer.setDrawerListener(mDrawerToggle);

        mDrawer.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        int[] ids = new int[] {
                R.id.sidebarMain,
                R.id.sidebarAvailable,
                R.id.sidebarHistory,
                R.id.sidebarFavorite,
                R.id.sidebarSettings,
        };
        for (int id : ids) {
            final View item = getActivity().findViewById(id);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        //getActivity().getSupportFragmentManager().popBackStack();//mDrawer.closeDrawer();
                        ((MainActivity) activity).processAction(item.getId());
                        mDrawer.closeDrawer(containerView);
                    }
                }
            });
        }
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }
}
