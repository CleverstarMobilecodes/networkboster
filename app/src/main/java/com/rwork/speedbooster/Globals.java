package com.rwork.speedbooster;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by super2lao on 2/5/2016.
 */
public class Globals {

    private static Globals globals;

    private Context context;

    private Globals(Context context) {
        this.context = context;
    }

    public static Globals getInstance(Context context) {
        if (globals == null) {
            globals = new Globals(context);
            //globals.resetSettings();
            globals.loadSettings();
            globals.loadSecurities();
            globals.loadHistories();
            globals.loadFavorites();
        }
        return globals;
    }

    /**
     * Settings
     */
    private boolean accuracy = true;
    private Constants.SpeedUnit speedUnit = Constants.SpeedUnit.SPEED_MEGABPS;

    public boolean getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(boolean accuracy) {
        this.accuracy = accuracy;

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_SETTINGS_ACCURACY, accuracy);
        editor.commit();
    }

    public Constants.SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    public void setSpeedUnit(Constants.SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.PREF_SETTINGS_SPEEDUNIT, speedUnit.ordinal());
        editor.commit();
    }

    public String getSpeedUnitString() {
        return getSpeedUnitString(speedUnit);
    }

    public String getSpeedUnitString(Constants.SpeedUnit speedUnit) {
        switch (speedUnit) {
            case SPEED_KILOBPS:
                return context.getString(R.string.Kbitpersec);
            case SPEED_MEGABPS:
                return context.getString(R.string.Mbitpersec);
        }
        return "";
    }

    public float getSystemSpeedValue(float speed) {
        return getSystemSpeedValue(speedUnit, speed);
    }

    public float getSystemSpeedValue(Constants.SpeedUnit speedUnit, float speed) {
        switch (speedUnit) {
            case SPEED_MEGABPS:
                return speed / 1024f;
        }
        return speed;
    }

    public String getSystemSpeedString(float speed) {
        return getSystemSpeedString(speedUnit, speed);
    }

    public String getSystemSpeedString(Constants.SpeedUnit speedUnit, float speed) {
        return String.format("%,.2f", getSystemSpeedValue(speedUnit, speed));
    }

    /**
     * Test Running
     */
    private Constants.AnimationMode animationMode = Constants.AnimationMode.None;

    public Constants.AnimationMode getAnimationMode() {
        return animationMode;
    }

    public void setAnimationMode(Constants.AnimationMode animationMode) {
        this.animationMode = animationMode;
    }

    public boolean isTestRunning() {
        return animationMode != Constants.AnimationMode.None;
    }

    /**
     * Security List
     */

    /**
     * Created by super2lao on 2/5/2016.
     */
    public static class SecurityItem {
        public String name;
        public String password;

        public SecurityItem(String name, String password) {
            this.name = name;
            this.password = password;
        }
    }

    private ArrayList<SecurityItem> securities = null;
    private int max_securities = 200;

    public int getSecurityCount() {
        return securities.size();
    }

    public SecurityItem getSecurity(int index) {
        return securities.get(index);
    }

    public void addSecurity(SecurityItem item) {
        for (int i = 0; i < securities.size(); i++) {
            SecurityItem security = securities.get(i);
            if (security.name.equals(item.name)) {
                securities.remove(i);
                break;
            }
        }
        securities.add(0, item);
        storeSecurities();
    }

    public void removeSecurity(int index) {
        if (index < securities.size()) {
            securities.remove(index);
            storeSecurities();
        }
    }

    public ArrayList<SecurityItem> getSecurities() {
        return securities;
    }

    public SecurityItem findSecurity(String ssid) {
        for (Globals.SecurityItem securityItem : securities) {
            if (securityItem.name.equals(ssid)) {
                return securityItem;
            }
        }
        return null;
    }

    /**
     * Availables
     */

    public static class AvailableItem {
        public float strength;
        public String name;
        public boolean locked;
        public String security;
        public boolean isActive;

        public AvailableItem(float strength, String name, boolean locked, String security, boolean isActive) {
            this.strength = strength;
            this.name = name;
            this.locked = locked;
            this.security = security;
            this.isActive = isActive;
        }
    }

    /**
     * History
     */

    /**
     * Created by super2lao on 2/5/2016.
     */
    public static class HistoryItem {
        public String name;
        public Date date;
        public float speed;

        public HistoryItem(String name, Date date, float speed) {
            this.name = name;
            this.date = date;
            this.speed = speed;
        }
    }

    private ArrayList<HistoryItem> histories = null;
    private int max_histories = 50;

    public int getHistoryCount() {
        return histories.size();
    }

    public HistoryItem getHistory(int index) {
        return histories.get(index);
    }

    public void addHistory(HistoryItem item) {
//        for (int i = 0; i < histories.size(); i++) {
//            HistoryItem history = histories.get(i);
//            if (history.name.equals(item.name)) {
//                histories.remove(i);
//                break;
//            }
//        }
        histories.add(0, item);
        storeHistories();
    }

    public void removeHistory(int index) {
        if (index < histories.size()) {
            histories.remove(index);
            storeHistories();
        }
    }

    public ArrayList<HistoryItem> getHistories() {
        return histories;
    }

    /**
     * Favorites
     */

    public static class FavoriteItem {
        public String name;
        public Date date;
        public float ping;
        public float download;
        public float upload;
        public boolean available;
        public boolean active;

        public FavoriteItem(String name, Date date, float ping, float download, float upload) {
            this.name = name;
            this.date = date;
            this.ping = ping;
            this.download = download;
            this.upload = upload;
            this.available = false;
            this.active = false;
        }
    }

    private ArrayList<FavoriteItem> favorites = null;
    private int max_favorites = 50;

    public int getFavoriteCount() {
        return favorites.size();
    }

    public FavoriteItem getFavorite(int index) {
        return favorites.get(index);
    }

    public void addFavorite(FavoriteItem item) {
        for (int i = 0; i < favorites.size(); i++) {
            FavoriteItem favorite = favorites.get(i);
            if (favorite.name.equals(item.name)) {
                favorites.remove(i);
                break;
            }
        }

        int index;
        for (index = 0; index < favorites.size(); index++) {
            if (favorites.get(index).download < item.download)
                break;
        }
        favorites.add(index, item);
        storeFavorites();
    }

    public void removeFavorite(int index) {
        if (index < favorites.size()) {
            favorites.remove(index);
            storeFavorites();
        }
    }

    public ArrayList<FavoriteItem> getFavorites() {
        return favorites;
    }

    public boolean isFavoriteInclude(String ssid) {
        for (FavoriteItem item : favorites) {
            if (item.name.equals(ssid))
                return true;
        }
        return false;
    }

    /**
     * Load Settings
     */
    private void loadSettings() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        accuracy = prefs.getBoolean(Constants.PREF_SETTINGS_ACCURACY, true);
        speedUnit = Constants.SpeedUnit.values()[prefs.getInt(Constants.PREF_SETTINGS_SPEEDUNIT, 1)];
    }

    /**
     * Load stored security
     */
    private void loadSecurities() {
        if (securities == null)
            securities = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        int nSecurities = prefs.getInt(Constants.PREF_SECURITY_COUNT, 0);
        for (int i = 0; i < nSecurities; i++) {
            String ssid;
            String password;
            ssid = prefs.getString(String.format(Constants.PREF_SECURITY_SSID, i), null);
            if (ssid == null)
                continue;
            password = prefs.getString(String.format(Constants.PREF_SECURITY_PASS, i), null);
            securities.add(new SecurityItem(ssid, password));
            if (securities.size() > max_securities)
                break;
        }

        /**
         * Rewrite security for refinement
         */
        storeSecurities();
    }

    /**
     * Store securities
     */
    private void storeSecurities() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < securities.size(); i++) {
            SecurityItem item = securities.get(i);
            editor.putString(String.format(Constants.PREF_SECURITY_SSID, i), item.name);
            editor.putString(String.format(Constants.PREF_SECURITY_PASS, i), item.password);
        }
        editor.putInt(Constants.PREF_SECURITY_COUNT, securities.size());
        editor.commit();
    }

    /**
     * Load stored history
     */
    private void loadHistories() {
        if (histories == null)
            histories = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        int nHistories = prefs.getInt(Constants.PREF_HISTORY_COUNT, 0);
        for (int i = 0; i < nHistories; i++) {
            String ssid;
            Date date;
            float speed;
            ssid = prefs.getString(String.format(Constants.PREF_HIST_SSID, i), null);
            if (ssid == null)
                continue;
            try {
                date = Constants.dateFormat.parse(prefs.getString(String.format(Constants.PREF_HIST_DATE, i), null));
            } catch (Exception e) {
                continue;
            }
            speed = prefs.getFloat(String.format(Constants.PREF_HIST_SPEED, i), 0);
            speed = Math.max(speed, 0);
            histories.add(new HistoryItem(ssid, date, speed));
            if (histories.size() > max_histories)
                break;
        }

        /**
         * Rewrite history for refinement
         */
        storeHistories();
    }

    /**
     * Store histories
     */
    private void storeHistories() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < histories.size(); i++) {
            HistoryItem item = histories.get(i);
            editor.putString(String.format(Constants.PREF_HIST_SSID, i), item.name);
            editor.putString(String.format(Constants.PREF_HIST_DATE, i), Constants.dateFormat.format(item.date));
            editor.putFloat(String.format(Constants.PREF_HIST_SPEED, i), item.speed);
        }
        editor.putInt(Constants.PREF_HISTORY_COUNT, histories.size());
        editor.commit();
    }

    /**
     * Load stored favorites
     */
    private void loadFavorites() {
        if (favorites == null)
            favorites = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        int nFavorites = prefs.getInt(Constants.PREF_FAVORITE_COUNT, 0);
        for (int i = 0; i < nFavorites; i++) {
            String ssid;
            Date date;
            float ping, download, upload;
            ssid = prefs.getString(String.format(Constants.PREF_FAVORITE_SSID, i), null);
            if (ssid == null)
                continue;
            try {
                date = Constants.dateFormat.parse(prefs.getString(String.format(Constants.PREF_FAVORITE_DATE, i), null));
            } catch (Exception e) {
                continue;
            }
            ping = prefs.getFloat(String.format(Constants.PREF_FAVORITE_PING, i), 0);
            download = prefs.getFloat(String.format(Constants.PREF_FAVORITE_DOWNLOAD, i), 0);
            upload = prefs.getFloat(String.format(Constants.PREF_FAVORITE_UPLOAD, i), 0);
            ping = Math.max(ping, 0);
            download = Math.max(download, 0);
            download = Math.max(download, 0);
            favorites.add(new FavoriteItem(ssid, date, ping, download, upload));
//            if (favorites.size() > max_favorites)
//                break;
        }

        /**
         * Rewrite favorite for refinement
         */
        storeFavorites();
    }

    /**
     * Reset settings to default
     */
    private void resetSettings() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Store favorites
     */
    private void storeFavorites() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < favorites.size(); i++) {
            FavoriteItem item = favorites.get(i);
            editor.putString(String.format(Constants.PREF_FAVORITE_SSID, i), item.name);
            editor.putString(String.format(Constants.PREF_FAVORITE_DATE, i), Constants.dateFormat.format(item.date));
            editor.putFloat(String.format(Constants.PREF_FAVORITE_PING, i), item.ping);
            editor.putFloat(String.format(Constants.PREF_FAVORITE_DOWNLOAD, i), item.download);
            editor.putFloat(String.format(Constants.PREF_FAVORITE_UPLOAD, i), item.upload);
        }
        editor.putInt(Constants.PREF_FAVORITE_COUNT, favorites.size());
        editor.commit();
    }

    /**
     * Get Security Flag
     * @param config
     * @return
     */
    static boolean getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return true;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return false;
        }
        return (config.wepKeys[0] != null);
    }

    /**
     * Get Security Flag
     * @param result
     * @return
     */
    static boolean getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return true;
        } else if (result.capabilities.contains("PSK")) {
            return true;
        } else if (result.capabilities.contains("EAP")) {
            return true;
        }
        return false;
    }

    /**
     * Get Security String
     * @param config
     * @return
     */
    static String getSecurityString(WifiConfiguration config) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < WifiConfiguration.KeyMgmt.strings.length; i++) {  // LastWPA_PSK, WPA_EAP, IEEE8021X, WPA2_PSK
            if (config.allowedKeyManagement.get(i)) {
                if (sb.length() > 0)
                    sb.append('/');
                sb.append(WifiConfiguration.KeyMgmt.strings[i]);
            }
        }
        return sb.toString().replace('_', '-');
    }

    static String getSecurityString(ScanResult result) {
        String security = result.capabilities;
        security = security.replace('-', '_');
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < WifiConfiguration.KeyMgmt.strings.length; i++) {  // LastWPA_PSK, WPA_EAP, IEEE8021X, WPA2_PSK
            if (security.contains(WifiConfiguration.KeyMgmt.strings[i])) {
                if (sb.length() > 0)
                    sb.append('/');
                sb.append(WifiConfiguration.KeyMgmt.strings[i]);
            }
        }
        return sb.toString().replace('_', '-');
//        String security = result.capabilities;
//        security = security.replace("][", "/");
//        security = security.replace("[", "");
//        security = security.replace("]", "");
//        return security;
    }

    public boolean wpsEnabled(String ssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            if (result.SSID.equals(ssid)) {
                return result.capabilities.contains("WPS");
            }
        }
        return false;
    }

    /**
     * Current System Status
     */
    private boolean connectionInProgress = false;
    private String connectionSSID = "";
    private String connectionPasswd = "";

    public boolean isConnectionInProgress() {
        return connectionInProgress;
    }

    public void connectionCompleted() {
        if (connectionInProgress) {
            connectionInProgress = false;
            //addHistory(new Globals.HistoryItem(connectionSSID, new Date(), 0));
        }
    }

    public String getConnectionSSID() {
        return connectionSSID;
    }

    public boolean connectWifi(String ssid, String passwd) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        WifiConfiguration wfc = new WifiConfiguration();
        if (Build.VERSION.SDK_INT >= 21)
            wfc.SSID = "" + ssid + "";
        else
            wfc.SSID = "\"" + ssid + "\"";
        wfc.preSharedKey = "\"" + passwd + "\"";
        wfc.status = WifiConfiguration.Status.ENABLED;

        if (wpsEnabled(ssid)) {
            // 100
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        } else {
            // 101
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        // connect to and enable the connection
        wifiManager.disconnect();
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration item : list) {
                wifiManager.disableNetwork(item.networkId);
            }
        }
        connectionInProgress = true;
        connectionSSID = ssid;
        connectionPasswd = passwd;
        addSecurity(new Globals.SecurityItem(ssid, passwd));

        int netId = wifiManager.addNetwork(wfc);
        if (netId >= 0) {
            wifiManager.enableNetwork(netId, true);
            if (wifiManager.reconnect())
                return true;
        } else {
            list = wifiManager.getConfiguredNetworks();
            if (list != null) {
                for (WifiConfiguration item : list) {
                    if (item.SSID != null && item.SSID.equals("\"" + ssid + "\"")) {
                        wifiManager.enableNetwork(item.networkId, true);
                        if (wifiManager.reconnect())
                            return true;
                        break;
                    }
                }
            }
        }

        connectionInProgress = false;
        return false;
    }

    public boolean isConnectedWifi(String ssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration item : list) {
                if (item.SSID != null && item.SSID.equals("\"" + ssid + "\"")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean connectWifi(String ssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        // connect to and enable the connection
        wifiManager.disconnect();
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        connectionInProgress = true;
        connectionSSID = ssid;
        connectionPasswd = "";

        if (list != null) {
            for (WifiConfiguration item : list) {
                if (item.SSID != null && item.SSID.equals("\"" + ssid + "\"")) {
                    wifiManager.enableNetwork(item.networkId, true);
                    if (wifiManager.reconnect())
                        return true;
                    break;
                }
            }
        }

        connectionInProgress = false;
        return false;
    }

    public String getCurrentWifiSsid() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectInfo = wifiManager.getConnectionInfo();
        if (connectInfo != null) {
            String curSSID = connectInfo.getSSID();
            if (!curSSID.isEmpty() && curSSID.charAt(0) == '"') {
                curSSID = curSSID.substring(1);
                curSSID = curSSID.substring(0, curSSID.length() - 1);
            }
            return curSSID;
        }
        return null;
    }
}
