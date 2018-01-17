package com.rwork.speedbooster;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by super2lao on 2/2/2016.
 */
public class SettingsFragment extends Fragment {

    static String strOutIP = "-";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.frag_settings, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        /**
         * Accuracy
         */
        View layoutAccuracyBig = rootView.findViewById(R.id.layoutAccuracyBig);
        View imgAccuracyBigContents = rootView.findViewById(R.id.imgAccuracyBigContents);
        View.OnClickListener setAccuracyBig = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals globals = Globals.getInstance(getActivity());
                globals.setAccuracy(true);
                updateAccuracy(rootView);
            }
        };
        layoutAccuracyBig.setOnClickListener(setAccuracyBig);
        imgAccuracyBigContents.setOnClickListener(setAccuracyBig);

        View layoutAccuracySmall = rootView.findViewById(R.id.layoutAccuracySmall);
        View imgAccuracySmallContents = rootView.findViewById(R.id.imgAccuracySmallContents);
        View.OnClickListener setAccuracySmall = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals globals = Globals.getInstance(getActivity());
                globals.setAccuracy(false);
                updateAccuracy(rootView);
            }
        };
        layoutAccuracySmall.setOnClickListener(setAccuracySmall);
        imgAccuracySmallContents.setOnClickListener(setAccuracySmall);

        updateAccuracy(rootView);

        /**
         * Speed Units
         */
        View layoutSpeedMbits = rootView.findViewById(R.id.layoutSpeedMbits);
        layoutSpeedMbits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals globals = Globals.getInstance(getActivity());
                globals.setSpeedUnit(Constants.SpeedUnit.SPEED_MEGABPS);
                updateSpeedUnit(rootView);
            }
        });
        View layoutSpeedKbits = rootView.findViewById(R.id.layoutSpeedKbits);
        layoutSpeedKbits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals globals = Globals.getInstance(getActivity());
                globals.setSpeedUnit(Constants.SpeedUnit.SPEED_KILOBPS);
                updateSpeedUnit(rootView);
            }
        });

        updateSpeedUnit(rootView);

        /**
         * IP addresses
         */
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        TextView txtInnerIP = (TextView)rootView.findViewById(R.id.txtInnerIP);
        txtInnerIP.setText(ip);
        TextView txtOutIP = (TextView)rootView.findViewById(R.id.txtOutIP);
        txtOutIP.setText(strOutIP);

        new AsyncTask<String, Float, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    URL whatismyip = null;
                    whatismyip = new URL("http://checkip.amazonaws.com");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            whatismyip.openStream()));

                    strOutIP = in.readLine(); //you get the IP as a String
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                return "executed";
            }

            @Override
            protected void onPreExecute() {}

            @Override
            protected void onProgressUpdate(Float... values) {}

            @Override
            protected void onPostExecute(String result) {
                TextView txtOutIP = (TextView)rootView.findViewById(R.id.txtOutIP);
                txtOutIP.setText(strOutIP);
            }
        }.execute("");

        return rootView;
    }

    public void updateAccuracy(View rootView) {
        Globals globals = Globals.getInstance(getActivity());
        View imgAccuracyBigFrame = rootView.findViewById(R.id.imgAccuracyBigFrame);
        View imgAccuracyBigContents = rootView.findViewById(R.id.imgAccuracyBigContents);
        View imgAccuracySmallFrame = rootView.findViewById(R.id.imgAccuracySmallFrame);
        View imgAccuracySmallContents = rootView.findViewById(R.id.imgAccuracySmallContents);
        if (globals.getAccuracy()) {
            imgAccuracyBigFrame.setBackgroundResource(R.drawable.round_frame);
            imgAccuracyBigContents.setBackgroundResource(R.drawable.accuracy_big_hot);
            imgAccuracySmallFrame.setBackgroundResource(0);
            imgAccuracySmallContents.setBackgroundResource(R.drawable.accuracy_small_cold);
        } else {
            imgAccuracyBigFrame.setBackgroundResource(0);
            imgAccuracyBigContents.setBackgroundResource(R.drawable.accuracy_big_cold);
            imgAccuracySmallFrame.setBackgroundResource(R.drawable.round_frame);
            imgAccuracySmallContents.setBackgroundResource(R.drawable.accuracy_small_hot);
        }
    }

    private void updateSpeedUnit(View rootView) {
        Globals globals = Globals.getInstance(getActivity());
        View imgSpeedMbitsFrame = rootView.findViewById(R.id.imgSpeedMbitsFrame);
        TextView txtSpeedMbits = (TextView)rootView.findViewById(R.id.txtSpeedMbits);
        View imgSpeedKbitsFrame = rootView.findViewById(R.id.imgSpeedKbitsFrame);
        TextView txtSpeedKbits = (TextView)rootView.findViewById(R.id.txtSpeedKbits);
        if (globals.getSpeedUnit() == Constants.SpeedUnit.SPEED_MEGABPS) {
            imgSpeedMbitsFrame.setBackgroundResource(R.drawable.round_frame);
            txtSpeedMbits.setTextColor(ContextCompat.getColor(getActivity(), R.color.settings_hot_color));
            imgSpeedKbitsFrame.setBackgroundResource(0);
            txtSpeedKbits.setTextColor(ContextCompat.getColor(getActivity(), R.color.settings_cold_color));
        } else {
            imgSpeedMbitsFrame.setBackgroundResource(0);
            txtSpeedMbits.setTextColor(ContextCompat.getColor(getActivity(), R.color.settings_cold_color));
            imgSpeedKbitsFrame.setBackgroundResource(R.drawable.round_frame);
            txtSpeedKbits.setTextColor(ContextCompat.getColor(getActivity(), R.color.settings_hot_color));
        }
    }
}
