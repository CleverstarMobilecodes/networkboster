package com.rwork.speedbooster;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by PanKaj on 2/15/2016.
 */
public class MultiViewFragment extends Fragment {

    private TabLayout tabLayout;
    private FragmentTransaction fragTransaction;
    private int tab_index;
    private ImageView tabone_imageView,tabtwo_imageView,tabthree_imageView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_multiview, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().hide();
        final DrawerLayout dlayout = (DrawerLayout) getActivity().findViewById(R.id.main_layout);
        ImageView drawer_imageView = (ImageView)rootView.findViewById(R.id.drawer_imageView);
        
        tabone_imageView  = (ImageView)rootView.findViewById(R.id.tabone_imageView);
        tabtwo_imageView  = (ImageView)rootView.findViewById(R.id.tabtwo_imageView);
        tabthree_imageView  = (ImageView)rootView.findViewById(R.id.tabthree_imageView);
        
        drawer_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
if(dlayout.isDrawerOpen(GravityCompat.START))
{
    dlayout.closeDrawer(GravityCompat.END);

}else
{
    dlayout.openDrawer(GravityCompat.START);
}
            }
        });
        tabLayout = (TabLayout)rootView.findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ffffff"));
        tabLayout.setSelectedTabIndicatorHeight(0);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                changeFragment(tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
       
        tabLayout.addTab(tabLayout.newTab().setTag(R.string.available).setText(R.string.available));
        tabLayout.addTab(tabLayout.newTab().setTag(R.string.history).setText(R.string.history));
        tabLayout.addTab(tabLayout.newTab().setTag(R.string.favorite).setText(R.string.favorite));
        Bundle args = getArguments();
         tab_index = args.getInt("tab_index", 0);
        selectTab(tab_index);
        if(tab_index == 0)
        {

            changeFragment(R.string.available);
        }else if(tab_index == 1)
        {
            changeFragment(R.string.history);
        }else if(tab_index == 2)
        {
            changeFragment(R.string.favorite);
        }

        return rootView;
    }

    private void selectTab(final int tab_index) {

        new Handler().postDelayed(
                new Runnable(){
                    @Override
                    public void run() {
                        tabLayout.getTabAt(tab_index).select();
                    }
                }, 100);
    }

    private void ChangeVisiblityIcon(int tab_index) {
        if(tab_index == 0)
        {
            tabone_imageView.setVisibility(View.VISIBLE);
            tabtwo_imageView.setVisibility(View.INVISIBLE);
            tabthree_imageView.setVisibility(View.INVISIBLE);
        }else if (tab_index==1)
        {
            tabone_imageView.setVisibility(View.INVISIBLE);
            tabtwo_imageView.setVisibility(View.VISIBLE);
            tabthree_imageView.setVisibility(View.INVISIBLE);
        }else if(tab_index == 2)
        {
            tabone_imageView.setVisibility(View.INVISIBLE);
            tabtwo_imageView.setVisibility(View.INVISIBLE);
            tabthree_imageView.setVisibility(View.VISIBLE);
        }else
        {
            tabone_imageView.setVisibility(View.INVISIBLE);
            tabtwo_imageView.setVisibility(View.INVISIBLE);
            tabthree_imageView.setVisibility(View.INVISIBLE);
        }
    }

    private void changeFragment(Object tag) {
        Fragment fragment = null;
        if(tag.equals(R.string.available))
        {
            ChangeVisiblityIcon(0);
            fragment = new AvailableFragment();
        }
        else if(tag.equals(R.string.history))
        {
            ChangeVisiblityIcon(1);
            fragment = new HistoryFragment();
        }
        else if(tag.equals(R.string.favorite))
        {
            ChangeVisiblityIcon(2);
            fragment = new FavoriteFragment();
        }
        else
        {
            ChangeVisiblityIcon(0);
            fragment = new AvailableFragment();
        }

        fragTransaction = getChildFragmentManager().beginTransaction().replace(R.id.fragment_con, fragment);
        fragTransaction.commit();
    }


}
