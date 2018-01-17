package com.rwork.speedbooster;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by super2lao on 2/2/2016.
 */
public class MainFragment extends Fragment {

    final long lNotifyElapse = 100;
    float ping;
    float download;
    float upload;
    Handler download_handler = new Handler();
    Handler upload_handler = new Handler();
    private long mUploadStartTX,mUploadStartTime;
    private long mDownloadStartRX,mDownloadStartTime;

    private AsyncTask<String, Float, String> ping_task;
    private AsyncTask<String, Float, String> download_task;
    private AsyncTask<String, Float, String> upload_task;
    private float avg_ping;
    private float max_download;
    private float max_upload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_main, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().show();
        ImageButton btnStart = (ImageButton)rootView.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });
        View layoutAddToFavoriteCancel = rootView.findViewById(R.id.layoutAddToFavoriteCancel);
        layoutAddToFavoriteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Globals.getInstance(getActivity()).isTestRunning())
                    return;
                getActivity().findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.GONE);
                getActivity().findViewById(R.id.layoutStatus).setVisibility(View.INVISIBLE);
                getActivity().findViewById(R.id.layoutStartButton).setVisibility(View.VISIBLE);
            }
        });
        View layoutAddToFavorite = rootView.findViewById(R.id.layoutAddToFavorite);
        layoutAddToFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorite();
            }
        });
        Button button;
        button = (Button)rootView.findViewById(R.id.btnConnections);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Globals.getInstance(getActivity()).isTestRunning())
                    return;
                navigateToConnections();
            }
        });

        layoutAddToFavoriteCancel.setVisibility(View.GONE);
        View layoutStatus = rootView.findViewById(R.id.layoutStatus);
        layoutStatus.setVisibility(View.INVISIBLE);

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        //float density = 1;//getActivity().getResources().getDisplayMetrics().density;
//        int dpWidth = (int)(width / density * 0.8);
//        int dpHeight = (int)(height / density * 0.8);
        int dpWidth = (int)(width * 0.8);
        int dpHeight = (int)(height * 0.8);
        int sz = Math.min(dpWidth, dpHeight);
        GaugeView imgMeter = (GaugeView)rootView.findViewById(R.id.imgMeter);
        imgMeter.getLayoutParams().width = sz;
        imgMeter.getLayoutParams().height = sz;

        View layoutStartButton = rootView.findViewById(R.id.layoutStartButton);
        ((RelativeLayout.LayoutParams)layoutStartButton.getLayoutParams()).bottomMargin = (int)(sz * 0.21);
        btnStart.getLayoutParams().width = sz / 3;
        btnStart.getLayoutParams().height = sz / 3;

        GraphView imgDownloadGraph = (GraphView)rootView.findViewById(R.id.imgDownloadGraph);
        imgDownloadGraph.setColor(Color.parseColor("#00f700"));
        imgDownloadGraph.setStrokeWidth(6f);
        GraphView imgUploadGraph = (GraphView)rootView.findViewById(R.id.imgUploadGraph);
        imgUploadGraph.setColor(Color.parseColor("#f24841"));
        imgUploadGraph.setStrokeWidth(6f);

        updateSpeed(rootView, 0);

        return rootView;
    }

    @Override
    public void onDestroyView() {

        if(ping_task != null && ping_task.getStatus() != AsyncTask.Status.FINISHED) {
            ping_task.cancel(true);
        } if(download_task != null && download_task.getStatus() != AsyncTask.Status.FINISHED) {
            download_task.cancel(true);
        }
        if(upload_task != null && upload_task.getStatus() != AsyncTask.Status.FINISHED) {
            upload_task.cancel(true);
        }
        if(download_handler != null)
        {
            download_handler.removeCallbacksAndMessages(null);
        }
        if (upload_handler != null)
        {
            upload_handler.removeCallbacksAndMessages(null);
        }
        Activity activity = getActivity();
        Globals globals = Globals.getInstance(activity);
        String curSSID = globals.getCurrentWifiSsid();
        if (!globals.isFavoriteInclude(curSSID))
            activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.VISIBLE);
        else {
            activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.GONE);
            activity.findViewById(R.id.layoutStatus).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.layoutStartButton).setVisibility(View.VISIBLE);
        }
        GaugeView imgMeter = (GaugeView) activity.findViewById(R.id.imgMeter);
        imgMeter.stopFlowAnimation();
        Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.None);
        Globals.getInstance(activity).addHistory(new Globals.HistoryItem(curSSID, new Date(), download));
        super.onDestroyView();
    }

    /**
     * Start test
     */
    private void startTest() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // previously invisible view
            final View myView = getActivity().findViewById(R.id.imgMeter);

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
            anim.start();
        }

        Activity activity = getActivity();
        Globals globals = Globals.getInstance(activity);
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectInfo = wifiManager.getConnectionInfo();
        if (connectInfo != null) {
            String curSSID = connectInfo.getSSID();
            if (!curSSID.isEmpty() && curSSID.charAt(0) == '"') {
                curSSID = curSSID.substring(1);
                curSSID = curSSID.substring(0, curSSID.length() - 1);
            }

            TextView txtNameOfPoint = (TextView) activity.findViewById(R.id.txtNameOfPoint);
            txtNameOfPoint.setText(curSSID);
            updateSpeed(0);
            activity.findViewById(R.id.layoutStartButton).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.layoutStatus).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.GONE);

          ping_task =  new PingTestTask().execute("");

            globals.setAnimationMode(Constants.AnimationMode.Downloading);

            return;
        }
        Toast.makeText(getActivity(), "Check your connection please", Toast.LENGTH_SHORT).show();
    }

    void updateSpeed(float speed) {
        updateSpeed(getActivity().findViewById(android.R.id.content), speed);
    }

    void updateSpeed(View rootView, float speed) {
        Globals globals = Globals.getInstance(getActivity());

//        Constants.SpeedUnit speedUnit = Constants.SpeedUnit.SPEED_KILOBPS;
//        if (speed > 800f)
//            speedUnit = Constants.SpeedUnit.SPEED_MEGABPS;
        Constants.SpeedUnit speedUnit = globals.getSpeedUnit();

        TextView txtSpeed = (TextView)rootView.findViewById(R.id.txtSpeed);
        if (txtSpeed != null) {
            String strSpeed;
            if (speedUnit == Constants.SpeedUnit.SPEED_KILOBPS) {
                if (speed >= 1000f)
                    strSpeed = String.format("%,d", (int)speed);
                else if (speed >= 100f)
                    strSpeed = String.format("%.1f", speed);
                else
                    strSpeed = globals.getSystemSpeedString(speedUnit, speed);
            } else
                strSpeed = globals.getSystemSpeedString(speedUnit, speed);
            txtSpeed.setText(strSpeed);
            TextView txtSpeedUnit = (TextView) rootView.findViewById(R.id.txtSpeedUnit);
            txtSpeedUnit.setText(globals.getSpeedUnitString(speedUnit));
        }

        GaugeView gauge = (GaugeView)rootView.findViewById(R.id.imgMeter);
        gauge.setValue(rootView, speed);
    }

    class PingTestTask extends AsyncTask<String, Float, String> {

        @Override
        protected String doInBackground(String... params) {
            String strServerUrl = "8.8.8.8";
            int count = 30;
            int testTime = 20;
            startPingTest(strServerUrl, count, testTime);
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
        Activity activity = getActivity();
            View imgPingStatus = activity.findViewById(R.id.imgPingStatus);
            imgPingStatus.setVisibility(View.VISIBLE);
            Animation rotator = AnimationUtils.loadAnimation(activity, R.anim.rotator);
            imgPingStatus.startAnimation(rotator);

        }

        @Override
        protected void onProgressUpdate(Float... values) {

                ((TextView) getActivity().findViewById(R.id.txtPing)).setText(String.format("%,.1f", values[0]));

                  ping = values[0];
              avg_ping = (ping+avg_ping)/2;

        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) getActivity().findViewById(R.id.txtPing)).setText(String.format("%,.1f",avg_ping));
                Activity activity = getActivity();
                View imgPingStatus = activity.findViewById(R.id.imgPingStatus);
                imgPingStatus.clearAnimation();
                imgPingStatus.setVisibility(View.INVISIBLE);
                Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.None);
                download_task =   new DownloadTestTask().execute("");

        }

        private void startPingTest(String strServerAddress, int count, int testTime) {
            // Change Seconds to Milliseconds
            testTime *= 500;

            float fTotalTime = 0;
            int nCountdown = 3;
            float fMinimumTime = -1;

            for (int index = 0;
                 index < count && (fTotalTime < testTime || (nCountdown < 3 && nCountdown > 0));
                 index++)
            {
                try {
             float time = ping(strServerAddress, false);
                    if (time < 0) {
                        nCountdown--;
                        continue;
                    }

                    nCountdown = 3;

                    if (fMinimumTime < 0 || fMinimumTime < time)
                        fMinimumTime = time;
                    fTotalTime += time;
                    float fAverage = fTotalTime / (index + 1);
                    publishProgress(fAverage);
                } catch (final Exception ex) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), ex.getClass().getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
            }
            if (nCountdown <= 0) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                       ((TextView)getActivity().findViewById(R.id.txtPing)).setText(R.string.dashes);
                    }
                });
            }
        }

        public float ping(String server, boolean sudo){
            try {
                String cmd = "ping -c 1 " + server;
                Process p;
                if (!sudo)
                    p = Runtime.getRuntime().exec(cmd);
                else {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                }
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String pattern = "^(\\d+) bytes from (\\d+\\.\\d+\\.\\d+\\.\\d+): icmp_seq=(\\d+) ttl=(\\d+) time=(\\d+\\.?\\d*) ms$";
                // Create a Pattern object
                Pattern r = Pattern.compile(pattern);

                String line;
                while ((line = stdInput.readLine()) != null) {
                    // Now create matcher object.
                    Matcher m = r.matcher(line);
                    if (m.find( )) {
                        return Float.parseFloat(m.group(5));
                    } else {
                    }
                }
                p.destroy();
            } catch (final Exception ex) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), ex.getClass().getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return -1;
        }
    }

    class DownloadTestTask extends AsyncTask<String, Float, String> {

        @Override
        protected String doInBackground(String... params) {
            //String strServerUrl = "http://ipv4.intuxication.testdebit.info/fichiers/100Mo.dat";
            String strServerUrl;
            Globals globals = Globals.getInstance(getActivity());
            if (globals.getAccuracy())
                strServerUrl = "http://apps.sam-platform.com/56baecfe8957df762a8b456d/speedtest.download?size=128M";
            else
                strServerUrl = "http://apps.sam-platform.com/56baecfe8957df762a8b456d/speedtest.download?size=4M";

            int count = 20;
            int testTime = 30;
            startDownloadTest();
            startDownloadTest(strServerUrl, count, testTime);


            return "Executed";
        }

        @Override
        protected void onPreExecute() {
            Activity activity = getActivity();
            GaugeView imgMeter = (GaugeView)activity.findViewById(R.id.imgMeter);
            imgMeter.startFlowAnimation();
            Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.Downloading);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            updateSpeed(values[0]);
            ((TextView) getActivity().findViewById(R.id.txtDownloadSpeed)).setText(Globals.getInstance(getActivity()).getSystemSpeedString(values[0]));
            GraphView imgDownloadGraph = (GraphView)getActivity().findViewById(R.id.imgDownloadGraph);
            imgDownloadGraph.addSample(values[0]);
            download = values[0];
            if(max_download<download) {
                max_download = download;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) getActivity().findViewById(R.id.txtDownloadSpeed)).setText(Globals.getInstance(getActivity()).getSystemSpeedString(max_download));
            GraphView imgDownloadGraph = (GraphView)getActivity().findViewById(R.id.imgDownloadGraph);
            imgDownloadGraph.addSample(max_download);

            download_handler.removeCallbacksAndMessages(null);
                Activity activity = getActivity();
                GaugeView imgMeter = (GaugeView) activity.findViewById(R.id.imgMeter);
                imgMeter.stopFlowAnimation();

                upload_task =   new UploadTestTask().execute("");

                Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.Uploading);

        }

        private void startDownloadTest() {
            mDownloadStartRX = TrafficStats.getTotalRxBytes();
            mDownloadStartTime = System.currentTimeMillis();
            download_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long txBytes = TrafficStats.getTotalRxBytes() - mDownloadStartRX;
                    long interval = System.currentTimeMillis() - mDownloadStartTime;
                    Log.i("Handler", "DOWNLOAD Running");
                    int bytes_msec = (int) (txBytes / interval);
                    publishProgress(fmtBytes(bytes_msec * 1000));
                    startDownloadTest();
                }
            }, 1000);
        }

        private void startDownloadTest(String strServerUrl, int count, int testTime) {
            Log.i("startDownloadTest","start");
            long max_seconds = System.currentTimeMillis()+(testTime*1000);
            //best server
//strServerUrl = "http://get.videolan.org/vlc/2.2.2/win32/vlc-2.2.2-win32.exe";
            /**
             * Download test data
             */
            int bytesRead;
            int bufferSize = 100 * 1024;
            byte[] buffer = new byte[bufferSize];
            try {
                URL url = new URL(strServerUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(connection.getInputStream());
                File target_file = new File(Environment.getExternalStorageDirectory(),"test.data");
if(target_file.exists())
{
    target_file.delete();
}
                OutputStream output = new FileOutputStream(target_file);

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                   Log.i("startDownloadTest","publish");

                    output.write(data, 0, count);
                    if(System.currentTimeMillis()> max_seconds) {
                       if (connection != null) connection.disconnect();
                        output.flush();
                        output.close();
                        input.close();
                        return;
                    }
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.i("startDownloadTest","end");
            return;
        }
    }


    class UploadTestTask extends AsyncTask<String, Float, String> {

        private Random rand = new Random(System.currentTimeMillis());

        @Override
        protected String doInBackground(String... params) {
            String strServerUrl = "http://apps.sam-platform.com/56baecfe8957df762a8b456d/speedtest.upload";
            int testFileSize = 1 * 50 * 1024;
            Globals globals = Globals.getInstance(getActivity());
            if (globals.getAccuracy())
                testFileSize = 1 * 50 * 1024;

            int count = 20000;
            int testTime = 20;
            startUploadTest();
            startUploadTest(strServerUrl, testFileSize, count, testTime);
            ;
            return "Executed";
        }

        private void startUploadTest() {
            mUploadStartTX = TrafficStats.getTotalTxBytes();
            mUploadStartTime = System.currentTimeMillis();
            upload_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long txBytes = TrafficStats.getTotalTxBytes() - mUploadStartTX;
                    long interval = System.currentTimeMillis()-mUploadStartTime;
                    Log.i("Handler","Running");
                    int bytes_msec = (int) (txBytes / interval);
                    publishProgress(fmtBytes(bytes_msec * 1000));
                    startUploadTest();
                }
            }, 1000);
        }

        private void startUploadTest(String strServerUrl, int testFileSize, int count, int testTime) {
long max_seconds = System.currentTimeMillis()+(testTime*1000);
//create test file
            int bufferSize = 1024 * 1024;
            byte[] data = new byte[bufferSize];

            File output_file = new File(Environment.getExternalStorageDirectory(),"test.data");
          try
          {
              FileOutputStream fos = new FileOutputStream(output_file);

            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
          }catch (IOException e)
          {
              e.printStackTrace();
          }


            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            String fileName = output_file.getName();
            try {
                connection = (HttpURLConnection) new URL(strServerUrl).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = output_file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();
               int progress = 0;
                int bytesRead = 0;
                int counter =0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(output_file));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead;
                    // update progress bar

                   if(System.currentTimeMillis()> max_seconds)
                   {
                       if (connection != null) connection.disconnect();
                       return;
                   }
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                // Get server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }
            return;

        }

        @Override
        protected void onPreExecute() {
            Log.e("UploadTestTask", "onPreExecute");
            Activity activity = getActivity();
            GaugeView imgMeter = (GaugeView) activity.findViewById(R.id.imgMeter);
            imgMeter.startFlowAnimation();
            Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.Uploading);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            Log.e("UploadTestTask", "onProgressUpdate");
            updateSpeed(values[0]);
            ((TextView) getActivity().findViewById(R.id.txtUploadSpeed)).setText(Globals.getInstance(getActivity()).getSystemSpeedString(values[0]));
            GraphView imgUploadGraph = (GraphView) getActivity().findViewById(R.id.imgUploadGraph);
            imgUploadGraph.addSample(values[0]);
            upload = values[0];
            if(max_upload < upload) {
                max_upload = upload;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            ((TextView) getActivity().findViewById(R.id.txtUploadSpeed)).setText(Globals.getInstance(getActivity()).getSystemSpeedString(max_upload));
            GraphView imgUploadGraph = (GraphView) getActivity().findViewById(R.id.imgUploadGraph);
            imgUploadGraph.addSample(max_upload);

            upload_handler.removeCallbacksAndMessages(null);
            Log.e("UploadTestTask", "onPostExecute");
            Activity activity = getActivity();
            Globals globals = Globals.getInstance(activity);
            String curSSID = globals.getCurrentWifiSsid();
            if (!globals.isFavoriteInclude(curSSID))
                activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.VISIBLE);
            else {
                activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.GONE);
                activity.findViewById(R.id.layoutStatus).setVisibility(View.INVISIBLE);
                activity.findViewById(R.id.layoutStartButton).setVisibility(View.VISIBLE);
            }
            GaugeView imgMeter = (GaugeView) activity.findViewById(R.id.imgMeter);
            imgMeter.stopFlowAnimation();
            Globals.getInstance(activity).setAnimationMode(Constants.AnimationMode.None);
            Globals.getInstance(activity).addHistory(new Globals.HistoryItem(curSSID, new Date(), max_download));
        }



    }

    private Float fmtBytes(long bytes) {
        float kb = (float) (bytes/1024.0);
        return kb;
    }


    private String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private void addToFavorite() {
            Activity activity = getActivity();
            activity.findViewById(R.id.layoutAddToFavoriteCancel).setVisibility(View.GONE);
            activity.findViewById(R.id.layoutStatus).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.layoutStartButton).setVisibility(View.VISIBLE);

            Globals globals = Globals.getInstance(activity);
            String curSSID = globals.getCurrentWifiSsid();
            if (curSSID != null)
                globals.addFavorite(new Globals.FavoriteItem(curSSID, new Date(), avg_ping, max_download, max_upload));
        }

        private void navigateToConnections() {
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).processAction(R.id.action_available);
            }
        }


        class BallBounces extends SurfaceView implements SurfaceHolder.Callback {
            GameThread thread;
            int screenW; //Device's screen width.
            int screenH; //Devices's screen height.
            int ballX; //Ball x position.
            int ballY; //Ball y position.
            int initialY;
            float dY; //Ball vertical speed.
            int ballW;
            int ballH;
            int bgrW;
            int bgrH;
            int angle;
            int bgrScroll;
            int dBgrY; //Background scroll speed.
            float acc;
            Bitmap ball, bgr, bgrReverse;
            boolean reverseBackroundFirst;
            boolean ballFingerMove;

            //Measure frames per second.
            long now;
            int framesCount = 0;
            int framesCountAvg = 0;
            long framesTimer = 0;
            Paint fpsPaint = new Paint();

            //Frame speed
            long timeNow;
            long timePrev = 0;
            long timePrevFrame = 0;
            long timeDelta;


            public BallBounces(Context context) {
                super(context);
                ball = BitmapFactory.decodeResource(getResources(), R.drawable.cursor); //Load a ball image.
                bgr = BitmapFactory.decodeResource(getResources(), R.drawable.bkgnd); //Load a background.
                ballW = ball.getWidth();
                ballH = ball.getHeight();

                //Create a flag for the onDraw method to alternate background with its mirror image.
                reverseBackroundFirst = false;

                //Initialise animation variables.
                acc = 0.2f; //Acceleration
                dY = 0; //vertical speed
                initialY = 100; //Initial vertical position
                angle = 0; //Start value for the rotation angle
                bgrScroll = 0;  //Background scroll position
                dBgrY = 1; //Scrolling background speed

                fpsPaint.setTextSize(30);
                fpsPaint.setAntiAlias(true);
                //Set thread
                getHolder().addCallback(this);

                setFocusable(true);
            }

            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                //This event-method provides the real dimensions of this custom view.
                screenW = w;
                screenH = h;

                bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //Scale background to fit the screen.
                bgrW = bgr.getWidth();
                bgrH = bgr.getHeight();

                //Create a mirror image of the background (horizontal flip) - for a more circular background.
                Matrix matrix = new Matrix();  //Like a frame or mould for an image.
                matrix.setScale(-1, 1); //Horizontal mirror effect.
                bgrReverse = Bitmap.createBitmap(bgr, 0, 0, bgrW, bgrH, matrix, true); //Create a new mirrored bitmap by applying the matrix.

                ballX = (int) (screenW / 2) - (ballW / 2); //Centre ball X into the centre of the screen.
                ballY = -50; //Centre ball height above the screen.
            }

            //***************************************
            //*************  TOUCH  *****************
            //***************************************
            @Override
            public synchronized boolean onTouchEvent(MotionEvent ev) {

                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ballX = (int) ev.getX() - ballW / 2;
                        ballY = (int) ev.getY() - ballH / 2;

                        ballFingerMove = true;
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        ballX = (int) ev.getX() - ballW / 2;
                        ballY = (int) ev.getY() - ballH / 2;

                        break;
                    }

                    case MotionEvent.ACTION_UP:
                        ballFingerMove = false;
                        dY = 0;
                        break;
                }
                return true;
            }

            @Override
            public void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                //Draw scrolling background.
                Rect fromRect1 = new Rect(0, 0, bgrW - bgrScroll, bgrH);
                Rect toRect1 = new Rect(bgrScroll, 0, bgrW, bgrH);

                Rect fromRect2 = new Rect(bgrW - bgrScroll, 0, bgrW, bgrH);
                Rect toRect2 = new Rect(0, 0, bgrScroll, bgrH);

                if (!reverseBackroundFirst) {
                    canvas.drawBitmap(bgr, fromRect1, toRect1, null);
                    canvas.drawBitmap(bgrReverse, fromRect2, toRect2, null);
                } else {
                    canvas.drawBitmap(bgr, fromRect2, toRect2, null);
                    canvas.drawBitmap(bgrReverse, fromRect1, toRect1, null);
                }

                //Next value for the background's position.
                if ((bgrScroll += dBgrY) >= bgrW) {
                    bgrScroll = 0;
                    reverseBackroundFirst = !reverseBackroundFirst;
                }

                //Compute roughly the ball's speed and location.
                if (!ballFingerMove) {
                    ballY += (int) dY; //Increase or decrease vertical position.
                    if (ballY > (screenH - ballH)) {
                        dY = (-1) * dY; //Reverse speed when bottom hit.
                    }
                    dY += acc; //Increase or decrease speed.
                }

                //Increase rotating angle
                if (angle++ > 360)
                    angle = 0;

                //DRAW BALL
                //Rotate method one
        /*
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, (ballW / 2), (ballH / 2)); //Rotate it.
        matrix.postTranslate(ballX, ballY); //Move it into x, y position.
        canvas.drawBitmap(ball, matrix, null); //Draw the ball with applied matrix.

        */// Rotate method two

                canvas.save(); //Save the position of the canvas matrix.
                canvas.rotate(angle, ballX + (ballW / 2), ballY + (ballH / 2)); //Rotate the canvas matrix.
                canvas.drawBitmap(ball, ballX, ballY, null); //Draw the ball by applying the canvas rotated matrix.
                canvas.restore(); //Rotate the canvas matrix back to its saved position - only the ball bitmap was rotated not all canvas.

                //*/

                //Measure frame rate (unit: frames per second).
                now = System.currentTimeMillis();
                canvas.drawText(framesCountAvg + " fps", 40, 70, fpsPaint);
                framesCount++;
                if (now - framesTimer > 1000) {
                    framesTimer = now;
                    framesCountAvg = framesCount;
                    framesCount = 0;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                thread = new GameThread(getHolder(), this);
                thread.setRunning(true);
                thread.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                thread.setRunning(false);
                while (retry) {
                    try {
                        thread.join();
                        retry = false;
                    } catch (InterruptedException e) {

                    }
                }
            }


            class GameThread extends Thread {
                private SurfaceHolder surfaceHolder;
                private BallBounces gameView;
                private boolean run = false;

                public GameThread(SurfaceHolder surfaceHolder, BallBounces gameView) {
                    this.surfaceHolder = surfaceHolder;
                    this.gameView = gameView;
                }

                public void setRunning(boolean run) {
                    this.run = run;
                }

                public SurfaceHolder getSurfaceHolder() {
                    return surfaceHolder;
                }

                @Override
                public void run() {
                    Canvas c;
                    while (run) {
                        c = null;

                        //limit frame rate to max 60fps
                        timeNow = System.currentTimeMillis();
                        timeDelta = timeNow - timePrevFrame;
                        if (timeDelta < 16) {
                            try {
                                Thread.sleep(16 - timeDelta);
                            } catch (InterruptedException e) {

                            }
                        }
                        timePrevFrame = System.currentTimeMillis();

                        try {
                            c = surfaceHolder.lockCanvas(null);
                            synchronized (surfaceHolder) {
                                //call methods to draw and process next fame
                                gameView.draw(c);
                            }
                        } finally {
                            if (c != null) {
                                surfaceHolder.unlockCanvasAndPost(c);
                            }
                        }
                    }
                }
            }
        }
    }