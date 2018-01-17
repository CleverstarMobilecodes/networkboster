package com.rwork.speedbooster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by super2lao on 2/2/2016.
 */
public class HistoryFragment extends Fragment {

    ListView mViewItems = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_history, container, false);

        mViewItems = (ListView)rootView.findViewById(R.id.listView);

        /**
         * Create ListView
         */
        final Globals globals = Globals.getInstance(getActivity());
        final HistoryItemListAdapter adapter = new HistoryItemListAdapter(getActivity(), R.layout.item_history, globals.getHistories());
        mViewItems.setAdapter(adapter);
        mViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(adapter.getItem(position).name)
                        .setMessage(R.string.confirm_delete_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.remove(globals.getHistory(position));
                                //globals.removeHistory(position);
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

        return rootView;
    }

    public class HistoryItemListAdapter extends ArrayAdapter<Globals.HistoryItem> {

        Context mContext;
        int layoutResourceId;

        public HistoryItemListAdapter(Context mContext, int layoutResourceId, ArrayList<Globals.HistoryItem> items) {
            super(mContext, layoutResourceId, items);
            this.mContext = mContext;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            View container = inflater.inflate(layoutResourceId, parent, false);

            View layout = container.findViewById(R.id.layoutHistoryItem);
            TextView txtNameOfPoint = (TextView)container.findViewById(R.id.txtNameOfPoint);
            TextView txtDate = (TextView)container.findViewById(R.id.txtDate);
            TextView txtSpeed = (TextView)container.findViewById(R.id.txtSpeed);
            TextView txtUnit = (TextView)container.findViewById(R.id.txtUnit);

            Globals globals = Globals.getInstance(getActivity());
            Globals.HistoryItem item = getItem(position);

            if (position % 2 == 0) {
                layout.setBackgroundResource(R.drawable.settings_content_item);
            } else {
                layout.setBackgroundResource(0);
            }
            txtNameOfPoint.setText(item.name);
            txtDate.setText(Constants.dateFormat.format(item.date));
            txtSpeed.setText(globals.getSystemSpeedString(item.speed));
            txtUnit.setText(globals.getSpeedUnitString());
            txtSpeed.setTextColor(ContextCompat.getColor(getActivity(), (position % 2 == 0) ? R.color.dark_green : R.color.dark_red));
            txtUnit.setTextColor(ContextCompat.getColor(getActivity(), (position % 2 == 0) ? R.color.dark_green : R.color.dark_red));

            return container;
        }
    }
}
