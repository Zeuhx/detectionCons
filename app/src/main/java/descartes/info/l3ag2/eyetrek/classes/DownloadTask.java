package descartes.info.l3ag2.eyetrek.classes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Thread.sleep;

public class DownloadTask {
    public static final Boolean SYSTEM_DOWNLOADER = true;
    private Activity activity;
    Context context;
    ProgressDialog mProgressDialog;
    String fileUrl;
    String filepath;
    String fileName;

    public boolean running = false;

    Boolean visibility;

    long reqId;

    //This is important
    public DownloadTask(Context context, Activity activity, String message) {
        this.context = context;
        this.activity = activity;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                try{
                    mProgressDialog = new ProgressDialog(activity);

                    mProgressDialog.setMessage(message);
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setMax(100);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                } catch (Throwable t){
                    t.printStackTrace();
                    Log.e("DownloadTask", "Erreur lors de la creation de la fenetre de dialogue avce la barre de progression");
                }

            }
        });
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void download(String fileUrl, String filepath,
                         Boolean visibility, Boolean choice) {
        // Make directories if required
        this.fileUrl=fileUrl;
        this.filepath=filepath;
        this.fileName="";
        this.visibility=visibility;
        running = true;
        File f = new File(
                Environment.getExternalStoragePublicDirectory("/MDroid")
                        + filepath);
        if (!f.exists())
            f.mkdirs();


        if (choice == SYSTEM_DOWNLOADER) {

            String url=fileUrl;
            new MyAsyncTask().execute(url);

        } else {
            mdroidDownload(fileUrl, fileName);
            reqId =0;
        }

    }

    private void mdroidDownload(String fileUrl, String fileName) {
        Log.e("DownloadTask", "utilisation d'une méthode non prévue");
    }

    class MyAsyncTask extends AsyncTask<String, String, Void> {

        boolean running;


        @Override
        protected Void doInBackground(String...fUrl) {
            int count;


            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {

                URL url = new URL(fUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int lenghtOfFile = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(filepath);

                //#################################

                mydownload(fileUrl, filepath,
                        visibility);


                //##########################################

                byte data[] = new byte[4096];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.w("DownloadTask", "Appel de onPreExecute()");
            running = true;

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    mProgressDialog.show();
                }
            });

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.w("DownloadTask", "Appel de onPostExecute()");

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    mProgressDialog.dismiss();
                }
            });





            running = false;
            UtilitaireModels.running_download = false;
        }

        protected void onProgressUpdate(String... progress) {

            //Log.w("DownloadTask", "Appel de onProgressUpdate()");

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    // Update the progress dialog
                    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
                }
            });



        }


    }




    public long mydownload(String fileUrl, String filepath, Boolean visibility)
    {
        DownloadManager manager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);

        /* TODO- Offer better alternative. Only a temporary, quick,
         * workaround for 2.3.x devices. May not work on all sites.
         */
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
            fileUrl = fileUrl.replace("https://", "http://");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        /*
        try {
            request.setDestinationInExternalPublicDir("/MDroid", filepath);
        } catch (Exception e) {
            Toast.makeText(context, "External storage not found!",
                    Toast.LENGTH_SHORT).show();
            return 0;
        }
        */
        request.setTitle(new File(filepath).getName());
        request.setDescription("File download ...");

        // Visibility setting not available in versions below Honeycomb
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
            if (!visibility)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            else
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // -TODO- save this id somewhere for progress retrieval
        reqId = manager.enqueue(request);
        return reqId;
    }
}
