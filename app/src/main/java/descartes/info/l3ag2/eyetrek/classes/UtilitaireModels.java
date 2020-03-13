package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.MainActivity;

import static java.lang.Thread.sleep;

/**
 * Created by Enzo on 15/04/2019.
 */


public class UtilitaireModels {
    private final static String URL_SERVEUR = "http://www.ens.math-info.univ-paris5.fr/~ij00084/";
    private final static String FICHIER_VERSIONS = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/models_version.txt";
    public static boolean running_download = false;

    private static Context context;
    private static Activity activity;
    private static ProgressDialog progressdialog;

    public static int VERSION_MODELE_FEUILLES = 0;
    public final static String VERSION_MODELE_FEUILLES_URL = URL_SERVEUR + "modele.php?type=feuilles&demande=last_version";
    public final static String FICHIER_MODELE_FEUILLES_URL = URL_SERVEUR + "modele.php?type=feuilles&demande=modele";
    public final static String FICHIER_LABELS_MODELE_FEUILLES_URL = URL_SERVEUR + "modele.php?type=feuilles&demande=label";
    public final static String MODELE_FEUILLES_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/modele_feuilles.tflite";
    public final static String LABELS_MODELE_FEUILLES_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/labels_modele_feuilles.txt";

    public static int VERSION_MODELE_CHAMPIGNIONS = 0;
    public final static String VERSION_MODELE_CHAMPIGNIONS_URL = URL_SERVEUR + "modele.php?type=champignons&demande=last_version";
    public final static String FICHIER_MODELE_CHAMPIGNIONS_URL = URL_SERVEUR + "modele.php?type=champignons&demande=modele";
    public final static String FICHIER_LABELS_MODELE_CHAMPIGNIONS_URL = URL_SERVEUR + "modele.php?type=champignons&demande=label";
    public final static String MODELE_CHAMPIGNIONS_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/modele_champignons.tflite";
    public final static String LABELS_MODELE_CHAMPIGNIONS_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/labels_modele_champignons.txt";

    public static int VERSION_MODELE_OISEAUX = 0;
    public final static String VERSION_MODELE_OISEAUX_URL = URL_SERVEUR + "modele.php?type=oiseaux&demande=last_version";
    public final static String FICHIER_MODELE_OISEAUX_URL = URL_SERVEUR + "modele.php?type=oiseaux&demande=modele";
    public final static String FICHIER_LABELS_MODELE_OISEAUX_URL = URL_SERVEUR + "modele.php?type=oiseaux&demande=label";
    public final static String MODELE_OISEAUX_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/modele_oiseaux.tflite";
    public final static String LABELS_MODELE_OISEAUX_PATH = Environment.getExternalStorageDirectory() + "/EyeTrek/Models/labels_modele_oiseaux.txt";

    /**
     *  Pour mettre à jour les constantes de version des modeles
     */
    public static void load_model_versions(Context c, Activity a, ProgressDialog p){
        context = c;
        activity = a;
        progressdialog = p;


        /*
        try {
            Log.e("load_model_versions", "poids de l'image : " + NetworkUtils.getFileSize(new URL("https://desfichesdescartes.fr/logos/logo17.png")));
            Log.e("load_model_versions", "mémoire restante : " + FileUtils.get_remaining_space());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        */



        File fichier_versions = new File(FICHIER_VERSIONS);
        if(!fichier_versions.exists()){
            creer_fichier_version_modeles();
        } else {
            Log.e("load_model_versions", "Le fichier contenant les versions des modeles existe ( " + fichier_versions.exists() + " )");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fichier_versions.getPath()));

            // On lis les 3 premieres lignes du fichier qui contiennent les versions des 3 modeles de l'appli
            VERSION_MODELE_FEUILLES = Integer.parseInt(reader.readLine());
            VERSION_MODELE_CHAMPIGNIONS = Integer.parseInt(reader.readLine());
            VERSION_MODELE_OISEAUX = Integer.parseInt(reader.readLine());


            Log.e("load_model_versions", "Versions " + VERSION_MODELE_FEUILLES +
                    " " + VERSION_MODELE_CHAMPIGNIONS +
                    " " + VERSION_MODELE_OISEAUX);

        } catch (Throwable e) {
            Log.e("load_model_versions", "Erreur lors de la lecture du fichier ( existance fichier : " + fichier_versions.exists() + " )");
            e.printStackTrace();
            try {
                Log.e("load_model_versions", "Erreur lors de la lecturrre du fichier, rerecréation du fichieer et relecture");
                // Il est possible que l'utilisateur ait modifié le fichier, on le recrée donc
                // et on reessaye de le lire
                reader.close();
                creer_fichier_version_modeles();
                reader = new BufferedReader(new FileReader(fichier_versions.getPath()));

                // On lis les 3 premieres lignes du fichier qui contiennent les versions des 3 modeles de l'appli
                VERSION_MODELE_FEUILLES = Integer.parseInt(reader.readLine());
                VERSION_MODELE_CHAMPIGNIONS = Integer.parseInt(reader.readLine());
                VERSION_MODELE_OISEAUX = Integer.parseInt(reader.readLine());

                reader.close();
            } catch(Throwable t){
                t.printStackTrace();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void creer_fichier_version_modeles() {
        FileWriter fw = null;
        File fichier_versions = new File(FICHIER_VERSIONS);
        try {
            fichier_versions.getParentFile().mkdirs();
            fichier_versions.createNewFile();
            Log.e("load_model_versions", "Fichier créé. " +
                    "\nLisibilité : " + fichier_versions.canRead() +
                    "\nEcriturabilité : " + fichier_versions.canWrite() +
                    "\nChemin : " + fichier_versions.getPath());


            fw = new FileWriter(FICHIER_VERSIONS);
            BufferedWriter write = new BufferedWriter(fw);

            // Pour chaque ligne, si c'est sa version de modele qu'on doit changer, on le fait
            write.write("" + VERSION_MODELE_FEUILLES);
            write.newLine();
            write.write("" + VERSION_MODELE_CHAMPIGNIONS);
            write.newLine();
            write.write("" + VERSION_MODELE_OISEAUX);
            write.newLine();
            write.flush();

            Log.e("load_model_versions", "Le fichier contenant les versions des modeles a été ecrit");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Méthode permettant de mettre à jour les modeles qui ne le sont pas (affiche une boite de dialogue avce l'avancement du téléchargement des modeles)
     */
    public static void update_modeles(){
        if(NetworkUtils.isNetworkAvailable(context)){
            new Thread(() -> {
                try{
                    if(VERSION_MODELE_FEUILLES < get_last_version_available(VERSION_MODELE_FEUILLES_URL)){
                        telecharger(FICHIER_MODELE_FEUILLES_URL, MODELE_FEUILLES_PATH, FICHIER_LABELS_MODELE_FEUILLES_URL, LABELS_MODELE_FEUILLES_PATH, VERSION_MODELE_FEUILLES_URL, "feuilles");
                    } else {
                        Log.w("update_modeles()", "le modele des feuilles est à jour. " +
                                "\nVersion appareil : " + VERSION_MODELE_FEUILLES + " " +
                                "\nVersion serveurs : " + get_last_version_available(VERSION_MODELE_FEUILLES_URL));
                    }

                    if(VERSION_MODELE_CHAMPIGNIONS < get_last_version_available(VERSION_MODELE_CHAMPIGNIONS_URL)){
                        telecharger(FICHIER_MODELE_CHAMPIGNIONS_URL, MODELE_CHAMPIGNIONS_PATH, FICHIER_LABELS_MODELE_CHAMPIGNIONS_URL, LABELS_MODELE_CHAMPIGNIONS_PATH, VERSION_MODELE_CHAMPIGNIONS_URL, "champignons");
                    } else {
                        Log.w("update_modeles()", "le modele des feuilles est à jour. " +
                                "\nVersion appareil : " + VERSION_MODELE_CHAMPIGNIONS + " " +
                                "\nVersion serveurs : " + get_last_version_available(VERSION_MODELE_CHAMPIGNIONS_URL));
                    }

                    if(VERSION_MODELE_OISEAUX < get_last_version_available(VERSION_MODELE_OISEAUX_URL)){
                        telecharger(FICHIER_MODELE_OISEAUX_URL, MODELE_OISEAUX_PATH, FICHIER_LABELS_MODELE_OISEAUX_URL, LABELS_MODELE_OISEAUX_PATH, VERSION_MODELE_OISEAUX_URL, "oiseaux");
                    } else {
                        Log.w("update_modeles()", "le modele des feuilles est à jour. " +
                                "\nVersion appareil : " + VERSION_MODELE_OISEAUX + " " +
                                "\nVersion serveurs : " + get_last_version_available(VERSION_MODELE_OISEAUX_URL));
                    }
                } catch(Throwable t){
                    t.printStackTrace();
                    erreur_telechargement_modeles();
                }
            }).start();


        }

    }

    /** Méthode pour télécharger le fichiier et les labels d'un modele
     *
     * @param url_modele
     * @param path_modele
     * @param url_labels
     * @param path_labels
     * @param version_modele_url
     * @param type (ex: feuilles, oiseaux, etc)
     */
    private static void telecharger(String url_modele, String path_modele, String url_labels, String path_labels, String version_modele_url, String type){
        try {
            if((NetworkUtils.getFileSize(new URL(url_modele)) + NetworkUtils.getFileSize(new URL(url_labels))) < FileUtils.get_remaining_space()){
                attendre(new DownloadTask(context, activity, "Téléchargement du réseau de neurones pour l'analyse de : " + type), url_modele, path_modele);
                attendre(new DownloadTask(context, activity, "Téléchargement de la liste des espèces pour l'analyse de : " + type), url_labels, path_labels);

                Log.w("telecharger()", "le dernier modele de " + type + " a été mis à jour" +
                        "\nVersion serveurs : " + get_last_version_available(version_modele_url));
                //attendre(new descartes.info.l3ag2.eyetrek.activity.MainActivity.DownloadWithProgressDialog(0, activity, progressdialog), url_labels, path_labels);
                set_version_modele(get_last_version_available(version_modele_url), type);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Mise à jour impossible");
                alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
                alertDialog.setMessage(activity.getString(R.string.dialog_no_space_for_models) + type);
                // Alert dialog button
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Alert dialog action goes here
                                // onClick button code here
                                dialog.dismiss();// use dismiss to cancel alert dialog
                            }
                        });
                alertDialog.show();
                Log.e("telecharger()", "Mise à jour des modeles impossible");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Methode pour lancer un telechargement et attendre qu'il termine (ne pas utiliser sur le thead principal)
     *
     * @param telechargement
     * @param url
     * @param path
     */
    private static void attendre(DownloadTask telechargement, String url, String path) {
        telechargement.download(url, path, false, DownloadTask.SYSTEM_DOWNLOADER);
        running_download = true;
        while(telechargement.running && running_download){
            try {
                Log.w("attendre()", "Téléchargement de : " + url);
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.w("attendre()", "Téléchargement du fichier terminé : " + url);
    }





    private static void erreur_telechargement_modeles(){
        try {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        // On affiche un message d'avrtissement à chaque ouverture de cette fonction
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("ERREUR");
                        alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
                        alertDialog.setMessage(context.getResources().getString(R.string.telechargement_modeles_impossible));
                        // Alert dialog button
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Alert dialog action goes here
                                        // onClick button code here
                                        dialog.dismiss();// use dismiss to cancel alert dialog
                                    }
                                });
                        alertDialog.show();
                    } catch(Throwable t){
                        Log.e("erreur_tele_modeles", "Impossible d'afficher le message d'erreur pour le téléchargement des modeles");
                        t.printStackTrace();
                    }

                }
            });
        } catch (Throwable t){
            t.printStackTrace();
        }



    }

    /**
     * Méthode permettant de télécharger le dernier modele du réseau de neuronnes pour l'analyse des feuilles disponibles sans vérification préalabkle de la version.
     * A utiliser dans le cas où le modele présent sur l'appereil est corrompu ou est inexistant.
     */
    public static void download_model_feuilles(){
        try {
            telecharger(FICHIER_MODELE_FEUILLES_URL, MODELE_FEUILLES_PATH, FICHIER_LABELS_MODELE_FEUILLES_URL, LABELS_MODELE_FEUILLES_PATH, VERSION_MODELE_FEUILLES_URL, "feuilles");
        } catch(Throwable t){
            t.printStackTrace();
            erreur_telechargement_modeles();
        }

    }

    /**
     * Méthode permettant de télécharger le dernier modele du réseau de neuronnes pour l'analyse des champignons disponibles sans vérification préalabkle de la version.
     * A utiliser dans le cas où le modele présent sur l'appereil est corrompu ou est inexistant.
     */
    public static void download_model_champignons(){
        try {
            telecharger(FICHIER_MODELE_CHAMPIGNIONS_URL, MODELE_CHAMPIGNIONS_PATH, FICHIER_LABELS_MODELE_CHAMPIGNIONS_URL, LABELS_MODELE_CHAMPIGNIONS_PATH, VERSION_MODELE_CHAMPIGNIONS_URL, "champignons");
        } catch(Throwable t){
            t.printStackTrace();
            erreur_telechargement_modeles();
        }

    }

    /**
     * Méthode permettant de télécharger le dernier modele du réseau de neuronnes pour l'analyse des chants d'oiseaux disponibles sans vérification préalabkle de la version.
     * A utiliser dans le cas où le modele présent sur l'appereil est corrompu ou est inexistant.
     */
    public static void download_model_oiseaux(){
        try {
            telecharger(FICHIER_MODELE_OISEAUX_URL, MODELE_OISEAUX_PATH, FICHIER_LABELS_MODELE_OISEAUX_URL, LABELS_MODELE_OISEAUX_PATH, VERSION_MODELE_OISEAUX_URL, "oiseaux");
        } catch(Throwable t){
            t.printStackTrace();
            erreur_telechargement_modeles();
        }

    }

    /**
     * get_last_version_available() - Permet de se connecter au serveur pour connaitre la derniere version des modeles disponibles
     * @return (la version du dernier modele disponible)
     *
     * @throws IOException
     */
    private static int get_last_version_available(String url_fichier_version) {
        int BUFFER_SIZE = 2000;
        InputStream inputStream = null;
        int response = -1;

        URL url = null;
        URLConnection conn = null;

        try {
            url = new URL(url_fichier_version);
            conn = url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (!(conn instanceof HttpURLConnection)){
            Log.e("get_last_version()", "La connexion n'est pas du type HTTP");
            return Integer.MAX_VALUE;
        }

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestProperty("User-Agent", "Test");
            httpConn.setRequestProperty("Connection", "close");
            httpConn.setConnectTimeout(1500);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            } else {
                Log.e("get_last_version()", "Erreur réseau : " + response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        } else {
            Log.e("get_last_version()", "Aucune information n'a pu etre lue");
        }

        return Integer.parseInt(finalCode);
    }

    public static void set_version_modele(int version, String type) throws IOException {
        File fichier = new File(FICHIER_VERSIONS);
        Log.e("set_version_modele()", "file path : " + FICHIER_VERSIONS);
        Log.e("set_version_modele()", "exists    ? " + fichier.exists());
        Log.e("set_version_modele()", "can write ? " + fichier.canWrite());

        FileWriter fw = new FileWriter(FICHIER_VERSIONS);
        BufferedWriter write = new BufferedWriter(fw);

        // Pour chaque ligne, si c'est sa version de modele qu'on doit changer, on le fait
        if(type == "feuilles"){
            write.write("" + version);
            VERSION_MODELE_FEUILLES = version;
        } else write.write("" + VERSION_MODELE_FEUILLES);
        write.newLine();

        if(type == "champignons"){
            write.write("" + version);
            VERSION_MODELE_CHAMPIGNIONS = version;
        } else write.write("" + VERSION_MODELE_CHAMPIGNIONS);
        write.newLine();

        if(type == "oiseaux"){
            write.write("" + version);
            VERSION_MODELE_OISEAUX = version;
        } else write.write("" + VERSION_MODELE_OISEAUX);
        write.newLine();


        write.flush();

        BufferedReader reader = null;
        try {
            File fichier_versions = new File(FICHIER_VERSIONS);
            reader = new BufferedReader(new FileReader(fichier_versions.getPath()));

            // On lis les 3 premieres lignes du fichier qui contiennent les versions des 3 modeles de l'appli
            Log.w("set_version_modele()", "version feuilles : " + Integer.parseInt(reader.readLine()));
            Log.w("set_version_modele()", "version champignons : " + Integer.parseInt(reader.readLine()));
            Log.w("set_version_modele()", "version oiseaux : " + Integer.parseInt(reader.readLine()));


            Log.e("load_model_versions", "Versions " + VERSION_MODELE_FEUILLES +
                    " " + VERSION_MODELE_CHAMPIGNIONS +
                    " " + VERSION_MODELE_OISEAUX);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }




}
