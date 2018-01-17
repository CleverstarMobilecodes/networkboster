package com.rwork.speedbooster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by super2lao on 2/2/2016.
 */
public class FavoriteFragment extends Fragment {

    ListView mViewItems = null;
    FavoriteItemListAdapter adapter;

    private BroadcastReceiver rssiChangeReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive( Context context, Intent intent ) {
            Globals globals = Globals.getInstance(context);
            String action = intent.getAction();
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (supl_error == WifiManager.ERROR_AUTHENTICATING && globals.isConnectionInProgress()) {
                /**
                 * Prompt new security info
                 */
                Activity activity = (Activity)context;
                if (activity.getFragmentManager() != null) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment frag = getFragmentManager().findFragmentById(R.id.dialogContainer);
                    if (frag != null)
                        ft.remove(frag);
                    ft.addToBackStack(null);
                    WifiConnectionInfoDlg dlg = WifiConnectionInfoDlg.newInstance(0, globals.getConnectionSSID(), context);
                    if (dlg != null)
                        dlg.show(ft, "dialog");
                }
            }

            switch ( action ) {
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    if (((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)) == SupplicantState.COMPLETED) {
                        globals.connectionCompleted();
                    }
                    break;
                case WifiManager.RSSI_CHANGED_ACTION:
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    WifiInfo connectInfo = wifiManager.getConnectionInfo();
                    String curSSID = "";
                    if (connectInfo != null)
                        curSSID = connectInfo.getSSID();
                    if (!curSSID.isEmpty() && curSSID.charAt(0) == '"') {
                        curSSID = curSSID.substring(1);
                        curSSID = curSSID.substring(0, curSSID.length() - 1);
                    }

                    ArrayList<Globals.FavoriteItem> favorites = Globals.getInstance(context).getFavorites();
                    for (Globals.FavoriteItem item : favorites) {
                        item.available = false;
                        item.active = false;
                    }
                    List<ScanResult> results = wifiManager.getScanResults();
                    for (int i = 0; i < results.size(); i++) {
                        ScanResult result = results.get(i);
                        String ssid = result.SSID;
                        boolean isActive = ssid.equals(curSSID);
                        for (Globals.FavoriteItem item : favorites) {
                            if (item.name.equals(ssid)) {
                                item.available = true;
                                item.active = isActive;
                            }
                        }
                    }

                    if (adapter != null)
                        adapter.notifyDataSetChanged();

                    break;
            }
            /*wifiManager.startScan();*/
        }};

    public class FavoriteItemListAdapter extends ArrayAdapter<Globals.FavoriteItem> {

        Context mContext;
        int layoutResourceId;

        public FavoriteItemListAdapter(Context mContext, int layoutResourceId, ArrayList<Globals.FavoriteItem> items) {
            super(mContext, layoutResourceId, items);
            this.mContext = mContext;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Globals globals = Globals.getInstance(mContext);
            final Globals.FavoriteItem item = getItem(position);

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            View container = inflater.inflate(layoutResourceId, parent, false);

            View layout = container.findViewById(R.id.layoutFavoriteItem);
            View imgFavorite = container.findViewById(R.id.imgFavorite);
            TextView txtNameOfPoint = (TextView)container.findViewById(R.id.txtNameOfPoint);
            View imgDelete = container.findViewById(R.id.imgDelete);
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(item.name)
                            .setMessage(R.string.confirm_delete_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FavoriteItemListAdapter.this.remove(globals.getFavorite(position));
                                    //globals.removeFavorite(position);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            if (position % 2 == 0) {
                layout.setBackgroundResource(R.drawable.settings_content_item);
            } else {
                layout.setBackgroundResource(0);
            }
            imgFavorite.setBackgroundResource(item.available ? R.drawable.favorite_star : R.drawable.favorite_star_white);
            txtNameOfPoint.setText(item.name);
            txtNameOfPoint.setTextColor(ContextCompat.getColor(mContext, item.active ? R.color.text_selected : R.color.text_normal));

            return container;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_favorite, container, false);

        mViewItems = (ListView)rootView.findViewById(R.id.listView);

        /**
         * Initialize Wifi
         */
        Activity context = getActivity();

        IntentFilter scanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        scanFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(rssiChangeReceiver, scanFilter);
        IntentFilter rssiFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        context.registerReceiver(rssiChangeReceiver, rssiFilter);

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
		 
        final Globals globals = Globals.getInstance(context);
        ArrayList<Globals.FavoriteItem> favorites = globals.getFavorites();
        for (Globals.FavoriteItem item : favorites) {
            item.available = false;
            item.active = false;
        }

        /**
         * Get WIFI list
         */
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            String ssid = result.SSID;
            boolean isActive = ssid.equals(curSSID);
            for (Globals.FavoriteItem item : favorites) {
                if (item.name.equals(ssid)) {
                    item.available = true;
                    item.active = isActive;
                }
            }
        }
        adapter = new FavoriteItemListAdapter(context, R.layout.item_favorite, favorites);
        mViewItems.setAdapter(adapter);
        mViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewDetailsAndConnect(adapter.getItem(position));
            }
        });

        rootView.findViewById(R.id.layoutConfirmDelete).setVisibility(View.INVISIBLE);

        return rootView;
    }

    private void viewDetailsAndConnect(final Globals.FavoriteItem item) {
        final Activity activity = getActivity();
        final Globals globals = Globals.getInstance(activity);
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dlg_wifi_details);
        dialog.setTitle(item.name);

        View btnDlgOK = dialog.findViewById(R.id.btnDlgOK);
        btnDlgOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> results = wifiManager.getScanResults();
                for (int i = 0; i < results.size(); i++) {
                    ScanResult result = results.get(i);
                    if (result.SSID.equals(item.name)) {
                        float strength = WifiManager.calculateSignalLevel(result.level, 10) / 10.f;
                        Globals.AvailableItem availableItem = new Globals.AvailableItem(strength, item.name, Globals.getSecurity(result), Globals.getSecurityString(result), true);
                        if (globals.isConnectedWifi(availableItem.name))
                            globals.connectWifi(availableItem.name);
                        else {
                            Globals.SecurityItem security = globals.findSecurity(item.name);
                            if (security != null && globals.connectWifi(item.name, security.password))
                                return;
                            else {
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                Fragment frag = getFragmentManager().findFragmentById(R.id.dialogContainer);
                                if (frag != null)
                                    ft.remove(frag);
                                WifiConnectionInfoDlg dlg = WifiConnectionInfoDlg.newInstance(0, availableItem);
                                dlg.show(ft, "dialog");
                            }
                        }
                    }
                }
            }
        });
        View btnDlgCancel = dialog.findViewById(R.id.btnDlgCancel);
        btnDlgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView txtDate = (TextView)dialog.findViewById(R.id.txtDate);
        TextView txtPing = (TextView)dialog.findViewById(R.id.txtPing);
        TextView txtDownloadSpeed = (TextView)dialog.findViewById(R.id.txtDownloadSpeed);
        TextView txtUploadSpeed = (TextView)dialog.findViewById(R.id.txtUploadSpeed);
        txtDate.setText(Constants.dateFormat.format(item.date));
        txtPing.setText(String.format("%,.1f ms", item.ping));
        txtDownloadSpeed.setText(globals.getSystemSpeedString(item.download) + " " + globals.getSpeedUnitString());
        txtUploadSpeed.setText(globals.getSystemSpeedString(item.upload) + " " + globals.getSpeedUnitString());

        dialog.show();
    }
}
