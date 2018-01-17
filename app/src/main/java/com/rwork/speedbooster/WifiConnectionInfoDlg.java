package com.rwork.speedbooster;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Created by super2lao on 2/4/2016.
 */
public class WifiConnectionInfoDlg extends DialogFragment {
    int mNum;
    Globals.AvailableItem availableItem;

    EditText edtPassword;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static WifiConnectionInfoDlg newInstance(int num, Globals.AvailableItem item) {
        WifiConnectionInfoDlg f = new WifiConnectionInfoDlg();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        f.setItem(item);

        return f;
    }

    static WifiConnectionInfoDlg newInstance(int num, String ssid, Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            if (result.SSID.equals(ssid)) {
                float strength = WifiManager.calculateSignalLevel(result.level, 10) / 10.f;
                Globals.AvailableItem available = new Globals.AvailableItem(strength, ssid, Globals.getSecurity(result), Globals.getSecurityString(result), true);
                return WifiConnectionInfoDlg.newInstance(0, available);
            }
        }
        return null;
    }

    public void setItem(Globals.AvailableItem item) {
        this.availableItem = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");

        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        switch ((mNum - 1) % 9) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum - 1) % 9) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(availableItem.name);
        View rootView = inflater.inflate(R.layout.dlg_wifi_conn_info, container, false);

        // Password
        edtPassword = (EditText)rootView.findViewById(R.id.edtPassword);
        CheckBox chkShowPassword = (CheckBox)rootView.findViewById(R.id.chkShowPassword);
        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        // Advanced Options
        final View layoutAdvancedOptions = rootView.findViewById(R.id.layoutAdvancedOptions);
        CheckBox chkShowAdvancedOptions = (CheckBox)rootView.findViewById(R.id.chkShowAdvancedOptions);
        chkShowAdvancedOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutAdvancedOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        //layoutAdvancedOptions.setVisibility(View.GONE);
        // Signal Strength
        TextView txtSignalStrength = (TextView)rootView.findViewById(R.id.txtSignalStrength);
        if (availableItem.strength >= 0.7)
            txtSignalStrength.setText(R.string.strong);
        else if (availableItem.strength >= 0.4)
            txtSignalStrength.setText(R.string.good);
        else if (availableItem.strength > 0.2)
            txtSignalStrength.setText(R.string.fair);
        else
            txtSignalStrength.setText(R.string.weak);
        // Security
        TextView txtSecurity = (TextView)rootView.findViewById(R.id.txtSecurity);
        txtSecurity.setText(availableItem.security);

        // OnClickListener
        View btnOK = rootView.findViewById(R.id.btnDlgOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();

                Globals globals = Globals.getInstance(getActivity());

                String networkSSID = availableItem.name;
                String networkPass = edtPassword.getText().toString();

                globals.connectWifi(networkSSID, networkPass);
            }
        });
        View btnCancel = rootView.findViewById(R.id.btnDlgCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Globals.getInstance(getActivity()).connectionCompleted();
            }
        });
        return rootView;
    }

    private static boolean isShowed = false;

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        if (isShowed)
            return 0;
        isShowed = true;
        return super.show(transaction, tag);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        isShowed = false;
        super.onDismiss(dialog);
    }
}

