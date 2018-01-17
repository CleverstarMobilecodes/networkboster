package com.rwork.speedbooster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Created by super2lao on 2/2/2016.
 */
public class AvailableFragment extends Fragment {

    ListView mViewItems = null;
    AvailableItemListAdapter adapter;
    private Handler handler = new Handler();
    private ArrayList<Globals.AvailableItem> availables;

    public class AvailableItemListAdapter extends ArrayAdapter<Globals.AvailableItem> {

        Context mContext;
        int layoutResourceId;

        public AvailableItemListAdapter(Context mContext, int layoutResourceId, ArrayList<Globals.AvailableItem> items) {
            super(mContext, layoutResourceId, items);
            this.mContext = mContext;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            View container = inflater.inflate(layoutResourceId, parent, false);

            View layout = container.findViewById(R.id.layoutAvailableItem);
            StrengthView imgStrength = (StrengthView)container.findViewById(R.id.imgStrength);
            TextView txtNameOfPoint = (TextView)container.findViewById(R.id.txtNameOfPoint);
            ImageView imgStatus = (ImageView)container.findViewById(R.id.imgStatus);

            Globals.AvailableItem item = getItem(position);

            if (position % 2 == 0) {
                layout.setBackgroundResource(R.drawable.settings_content_item);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    layout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.settings_content_item));
//                } else {
//                    layout.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.settings_content_item));
//                }
            } else {
                layout.setBackgroundResource(0);
            }
            imgStrength.setValue(item.strength);
            txtNameOfPoint.setText(item.name);
            txtNameOfPoint.setTextColor(ContextCompat.getColor(mContext, item.isActive ? R.color.text_selected : R.color.text_normal));
            if(item.locked)
            {
                imgStatus.setImageResource(R.drawable.lock);
            }else
            {
                imgStatus.setImageResource(R.drawable.lock_white);
            }
            if (item.isActive)
            {
                imgStatus.setImageResource(R.drawable.unlock);
            }
            else
            {

                //  imgStatus.setImageResource(R.drawable.lock_white);
            }

            return container;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_available, container, false);

        mViewItems = (ListView)rootView.findViewById(R.id.listView);
         availables = new ArrayList<Globals.AvailableItem>();

        adapter = new AvailableItemListAdapter(getActivity(), R.layout.item_available, availables);
        mViewItems.setAdapter(adapter);
        mViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                establishConnection(position);
            }
        });
       scanWifi();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    private void scanWifi() {
        Log.i("Wifi","Scan");
        /**
         * Initialize Wifi
         */
        Activity context = getActivity();
        Globals globals = Globals.getInstance(context);

         WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectInfo = wifiManager.getConnectionInfo();
        String curSSID = "";
        if (connectInfo != null)
            curSSID = connectInfo.getSSID();
        if (!curSSID.isEmpty() && curSSID.charAt(0) == '"') {
            curSSID = curSSID.substring(1);
            curSSID = curSSID.substring(0, curSSID.length() - 1);
        }

        /**
         * Create ListView
         */
        availables.clear();
        ArrayList<Globals.AvailableItem> items = new ArrayList<>();

        ArrayList<Globals.FavoriteItem> favorites = globals.getFavorites();

        /**
         * Get WIFI list
         */
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            String ssid = result.SSID;
            boolean isActive = ssid.equals(curSSID);
            float strength = WifiManager.calculateSignalLevel(result.level, 10) / 10.f;
            items.add(new Globals.AvailableItem(strength, ssid, Globals.getSecurity(result), Globals.getSecurityString(result), isActive));
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration item : list) {
                boolean included = false;
                for (Globals.AvailableItem availableItem : items) {
                    if (item.SSID.equals(availableItem.name) || item.SSID.equals("\"" + availableItem.name + "\"")) {
                        included = true;
                        break;
                    }
                }
                if (!included)
                    items.add(new Globals.AvailableItem(0, item.SSID, Globals.getSecurity(item), Globals.getSecurityString(item), false));
            }
        }
        for (int i = 0; i < favorites.size(); i++) {
            Globals.FavoriteItem favorite = favorites.get(i);
            for (Globals.AvailableItem item : items) {
                if (item.name.equals(favorite.name)) {
                    availables.add(item);
                    break;
                }
            }
        }
        for (Globals.AvailableItem item : items) {
            if (!availables.contains(item)) {
                availables.add(item);
            }
        }

        adapter.notifyDataSetChanged();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                scanWifi();
            }
        },1000);
    }

    /**
     * Event listener of available item
     * @param position
     */
    private void establishConnection(int position) {
        final Activity activity = getActivity();
        final Globals.AvailableItem availableItem = adapter.getItem(position);
        if (availableItem.isActive)
            return;
        new AlertDialog.Builder(activity)
                .setTitle(availableItem.name)
                .setMessage(R.string.confirm_connect_message)
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Globals globals = Globals.getInstance(activity);
                        if (globals.isConnectedWifi(availableItem.name))
                            globals.connectWifi(availableItem.name);
                        else {
                            /**
                             * Check stored security list
                             */
                            Globals.SecurityItem security = globals.findSecurity(availableItem.name);
                            // Connect with stored info
                            if (security != null && globals.connectWifi(availableItem.name, security.password))
                                return;
                            else {
                                /**
                                 * Prompt new security info
                                 */
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                Fragment frag = getFragmentManager().findFragmentById(R.id.dialogContainer);
                                if (frag != null)
                                    ft.remove(frag);
                                ft.addToBackStack(null);
                                WifiConnectionInfoDlg dlg = WifiConnectionInfoDlg.newInstance(0, availableItem);
                                dlg.show(ft, "dialog");
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
}
