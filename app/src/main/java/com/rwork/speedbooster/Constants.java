package com.rwork.speedbooster;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by super2lao on 2/2/2016.
 */
public final class Constants {

    private Constants() {

    }

    public static enum SpeedUnit {
        SPEED_KILOBPS, SPEED_MEGABPS;
    };

    public static final String PREFERENCE_NAME = "speedbooster";

    public static final String PREF_SETTINGS_ACCURACY = "settings_accuracy";
    public static final String PREF_SETTINGS_SPEEDUNIT = "settings_speedunit";

    public enum AnimationMode {
        None, Ping, Downloading, Uploading
    }

    /**
     * Security List
     */
    public static final String PREF_SECURITY_COUNT = "sec_count";
    public static final String PREF_SECURITY_SSID = "security_ssid_%d";
    public static final String PREF_SECURITY_PASS = "security_pass_%d";

    /**
     * Favorite
     */
    public static final String PREF_FAVORITE_COUNT = "favorite_count";
    public static final String PREF_FAVORITE_SSID = "favorite_ssid_%d";
    public static final String PREF_FAVORITE_DATE = "favorite_date_%d";
    public static final String PREF_FAVORITE_PING = "favorite_ping_%d";
    public static final String PREF_FAVORITE_DOWNLOAD = "favorite_download_%d";
    public static final String PREF_FAVORITE_UPLOAD = "favorite_upload_%d";

    /**
     * History
     */
    public static final String PREF_HISTORY_COUNT = "history_count";
    public static final String PREF_HIST_SSID = "hist_ssid_%d";
    public static final String PREF_HIST_DATE = "hist_date_%d";
    public static final String PREF_HIST_SPEED = "hist_speed_%d";

    public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    public static boolean isHexNumber (String cadena) {
        try {
            Long.parseLong(cadena, 16);
            return true;
        }
        catch (NumberFormatException ex) {
            // Error handling code...
            return false;
        }
    }

}
