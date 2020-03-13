package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import descartes.info.l3ag2.eyetrek.activity.MainActivity;
import descartes.info.l3ag2.eyetrek.R;

public class NetworkUtils {
    /**
     * True si du réseau est disponible (ne vérifie pas si la connexion marche)
     *
     * Source: https://stackoverflow.com/questions/30343011/how-to-check-if-an-android-device-is-online
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    /**
     * True si il est possible de se connecter à internet.
     *
     * Source: https://stackoverflow.com/questions/30343011/how-to-check-if-an-android-device-is-online
     *
     * @param context
     * @return
     */
    public static boolean checkActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e("checkActiveInternetCo()", "Error: ", e);
            }
        } else {
            Log.d("checkActiveInternetCo()", "No network seems present");
        }
        return false;
    }



    public static String sendPost(String r_url , String postDataParams) throws Exception {
        URL url = new URL(r_url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
        writer.write(postDataParams);
        writer.flush();
        writer.close();
        os.close();

        int responseCode=conn.getResponseCode();
        Log.e("sendPost()", "" + responseCode);
        if (responseCode == HttpsURLConnection.HTTP_OK) {

            //BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            String reponse = convertStreamToString(conn.getInputStream());

            /*
            String line="";
            StringBuffer sb = new StringBuffer("");

            while((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();
            return sb.toString();
            */

            Log.e("sendPost()", "reponse : " + reponse);

            return reponse;
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public class DownloadWithProgressDialog extends AsyncTask< String, String, String > {
        ProgressDialog progressdialog;
        public int Progress_Dialog_Progress = 0;
        URL url;
        URLConnection urlconnection;
        int FileSize;
        InputStream inputstream;
        OutputStream outputstream;
        byte dataArray[] = new byte[1024];
        long totalSize = 0;
        String getPath;
        Activity activity;

        boolean running = false;

        public DownloadWithProgressDialog(int Progress_Dialog_Progress, Activity activity, ProgressDialog progressdialog){
            super();
            this.Progress_Dialog_Progress = Progress_Dialog_Progress;
            this.activity = activity;
            this.progressdialog = progressdialog;
            this.running = true;


        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    activity.showDialog(Progress_Dialog_Progress);
                    progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressdialog.setIndeterminate(false);
                    progressdialog.setMax(100);
                }
            });
        }

        @Override
        protected String doInBackground(String...aurl) {

            int count;
            String file_path;

            progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressdialog.setIndeterminate(false);
            progressdialog.setMax(100);

            try {
                Log.e("DownloadWithProgDialog", "Téléchargement de : " + aurl[0]);

                url = new URL(aurl[0]);
                urlconnection = url.openConnection();
                urlconnection.connect();

                file_path = aurl[1];

                FileSize = urlconnection.getContentLength();

                inputstream = new BufferedInputStream(url.openStream());
                outputstream = new FileOutputStream(file_path);


                while ((count = inputstream.read(dataArray)) != -1) {

                    totalSize += count;
                    publishProgress("" + (int)((totalSize * 100) / FileSize));
                    outputstream.write(dataArray, 0, count);
                }

                //publishProgress("" + 50);
                //Thread.sleep(500);

                outputstream.flush();
                outputstream.close();
                inputstream.close();
                running = false;

            } catch (Exception e) {}
            return null;

        }
        protected void onProgressUpdate(String...progress) {
            super.onProgressUpdate();

            String msg = "";
            if(Integer.parseInt(progress[0])>99){

                msg="Finalisation... ";

            }else {

                msg="Téléchargement... "+progress[0]+"%";
            }

            final ProgressDialog final_pb = progressdialog;
            final int final_downloadedSize = Integer.parseInt(progress[0]);
            final String final_msg = msg;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final_pb.setProgress(final_downloadedSize);
                    progressdialog.setMessage(final_msg);
                }
            });




        }

        @Override
        protected void onPostExecute(String unused) {

            activity.dismissDialog(Progress_Dialog_Progress);
        }
    }


    /** méthode pour récupérer la taille d'un fichier
     *
     * @param url (url du fichier)
     * @return (la taille du fichier en octets)
     */
    public static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }
    }


}
