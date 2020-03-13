package descartes.info.l3ag2.eyetrek.classes;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jérémy on 21/04/2018.
 */

public class UpdateAPK {


    private final static String VERSIONFILE = "http://www.ens.math-info.univ-paris5.fr/~if04812/versionCode.txt";



    // Valeur contenu dans le fichier du serveur
    private int versionCode;

    // Valeur contenu dans le code de la version de l'application
    private int versionApk;

    private InputStream inputStream;

    public UpdateAPK(Context context){
        this.versionCode = getLastVersion();
        this.versionApk = getVersionAPK(context);
        this.inputStream = null;
    }


    /**
     * getVersionCode - Permet de récupérer la version de l'application installée
     * @param context
     * @return la valeur inscrite sur le fichier
     */
    private int getVersionAPK(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfoi = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfoi.versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    /**
     * getLastVersion - Permet de récupérer la version de l'application installée
     * @return la valeur inscrite sur l'application
     */
    private int getLastVersion() {
        int BUFFER_SIZE = 2000;

        try {
            inputStream = openHttpConnection();
        } catch (IOException exception) {
            exception.printStackTrace();
            return 0;
        }

        String finalCode = "";
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            int reader;
            char[] inputBuffer = new char[BUFFER_SIZE];
            try {
                while ((reader = inputStreamReader.read(inputBuffer)) > 0) {
                    String readString = String.copyValueOf(inputBuffer, 0, reader);
                    finalCode+=readString;
                    inputBuffer = new char[BUFFER_SIZE];
                }
                inputStream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                return 0;
            }
        }

        return Integer.parseInt(finalCode);
    }


    /**
     * openHttpConnection - Permet la connection au service de téléchargement
     * @return
     * @throws IOException
     */
    private InputStream openHttpConnection() throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(VERSIONFILE);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return in;
    }


    public int getVersionCode() {
        return versionCode;
    }

    public int getVersionApk() {
        return versionApk;
    }

}
