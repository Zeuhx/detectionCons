package descartes.info.l3ag2.eyetrek.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.MainActivity;

/**
 * Created by Jérémy on 21/04/2018.
 */

public class UpdateActivity extends Fragment{

    ProgressDialog popup;
    private int AppVersion = 1;

    private static final String VERSIONAPK = "http://www.ens.math-info.univ-paris5.fr/~if04812/";
    private static final String NAMEAPK = "eyetrek.apk";



    protected void onCreate(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_update, container, false);

        TextView versionTitle = (TextView)view.findViewById(R.id.versionTitle);
        versionTitle.setText("Eyetrek v."+AppVersion);

        Button maj = view.findViewById(R.id.btn_maj);
        Button report = view.findViewById(R.id.btn_annuler);
        Button info = view.findViewById(R.id.btn_info);


        maj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadNewVersion().execute();
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }
    class DownloadNewVersion extends AsyncTask<String,Integer,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            popup = new ProgressDialog(getActivity());
            popup.setCancelable(false);

            popup.setMessage("Téléchargement...");

            popup.setIndeterminate(true);
            popup.setCanceledOnTouchOutside(false);
            popup.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            popup.setIndeterminate(false);
            popup.setMax(100);
            popup.setProgress(progress[0]);
            String msg = "";
            if(progress[0]>99){

                msg="Terminer... ";

            }else {

                msg="Téléchargement... "+progress[0]+"%";
            }
            popup.setMessage(msg);

        }
        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            popup.dismiss();

            if(result){

                Toast.makeText(getContext(),"Mise à jour terminée",
                        Toast.LENGTH_SHORT).show();

            }else{

                Toast.makeText(getContext(),"Erreur de téléchargement...",
                        Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            Boolean flag = false;

            try {


                URL url = new URL(VERSIONAPK);

                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setDoOutput(true);
                httpConnection.connect();


                String PATH = Environment.getExternalStorageDirectory()+"/Download/";
                File file = new File(PATH);
                file.mkdirs();

                File outputFile = new File(file,NAMEAPK);

                if(outputFile.exists()){
                    outputFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = httpConnection.getInputStream();

                int total_size = 1431692;//size of apk

                byte[] buffer = new byte[1024];
                int len1 = 0;
                int per = 0;
                int downloaded=0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    downloaded +=len1;
                    per = (int) (downloaded * 100 / total_size);
                    publishProgress(per);
                }
                fos.close();
                is.close();

                OpenNewVersion(PATH);

                flag = true;
            } catch (Exception e) {
                flag = false;
                e.printStackTrace();
            }
            return flag;

        }

    }




    void OpenNewVersion(String location) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(location + "eyetrek.apk")),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }



}
