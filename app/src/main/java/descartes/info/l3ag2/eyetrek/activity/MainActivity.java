package descartes.info.l3ag2.eyetrek.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.classes.DatabaseHandler;
import descartes.info.l3ag2.eyetrek.classes.NetworkUtils;
import descartes.info.l3ag2.eyetrek.classes.UpdateAPK;
//import descartes.info.l3ag2.eyetrek.fragment.FragmentAnalyse;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireModels;
import descartes.info.l3ag2.eyetrek.fragment.FragmentLog;
import descartes.info.l3ag2.eyetrek.fragment.FragmentMenu;
import descartes.info.l3ag2.eyetrek.fragment.FragmentMessage;
import descartes.info.l3ag2.eyetrek.fragment.FragmentOptions;
import descartes.info.l3ag2.eyetrek.fragment.FragmentProfil;
import descartes.info.l3ag2.eyetrek.fragment.FragmentRecherche;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.fragment.FragmentScanFeuille;
import descartes.info.l3ag2.eyetrek.fragment.Profil;
import descartes.info.l3ag2.eyetrek.fragment.UpdateActivity;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;

/**
 * Updated by: Ayaz ABDUL CADER, Dorian Quaboul and Enzo DUTRA a year later
 * <p>
 * Sources :
 * <p>
 * <p>
 * <p>
 * Bottom navigation view with fragments Android Studio (without plugin) - https://youtu.be/afVktMf_LkE
 * <p>
 * Chaîne : Appeteria Technologies
 * <p>
 * <p>
 * <p>
 * Tutoriel Android : tout comprendre sur les Fragments
 * <p>
 * http://mathias-seguy.developpez.com/tutoriels/android/comprendre-fragments/#LII
 */

public class MainActivity extends AppCompatActivity {

    private final static String VERSIONAPK = "http://www.ens.math-info.univ-paris5.fr/~if04812/eyetrek.apk";
    private static final String PREFS = "app_prefs";
    private static final String DIDACTICIEL = "DIDACTICIEL";
    private static final int SIZEAPK = 143992;
    ProgressDialog progressdialog;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        UpdateAPK testUpdate = new UpdateAPK(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = this.getSharedPreferences(PREFS, MODE_PRIVATE);
        //No NotificationBar - Source : https://stackoverflow.com/questions/4222713/hide-notification-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        BottomNavigationView navigation = this.findViewById(R.id.barre_navigation);
        navigation.setSelectedItemId(R.id.analyse_item);
        String prefs_lastname = sharedPreferences.getString("email", null);



        //Si les permissions ne sont pas accordées on affiche le layout d'erreur
        if (!requestsPermissions()) {
            getSupportFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentMessage()).commit();
        }
        //Permiessions acceptées
        else {
            //Affichage du fragment par défault
            this.getSharedPreferences(PREFS, this.MODE_PRIVATE).edit().clear().commit();
            getSupportFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentMenu()).commit();
            navigation.setOnNavigationItemSelectedListener((item) -> {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.profil_item:
                        //Si l'utilisateur est connecté
                        if (sharedPreferences.contains("email")) {
                            fragmentManager.beginTransaction().add(R.id.contenu_fragment, new Profil()).commit();
                        } else {
                            fragmentManager.beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();
                        }
                        fragmentTransaction.addToBackStack("fragment_profil");
                        fragmentTransaction.commit();
                        return true;
                    case R.id.analyse_item:
                        //On récupère le choix de l'utilisateur et on affiche le fragment correspondant
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
                        String mode = getSharedPreferences(PREFS, MODE_PRIVATE).getString("mode", "modedefault");
                        switch (mode) {
                            case "feuille":
                                //fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnalyse());
                                fragmentTransaction.replace(R.id.contenu_fragment, new FragmentScanFeuille());
                                editor.putString("mode", "feuille").apply();
                                break;
                            case "animal":
                                fragmentTransaction.replace(R.id.contenu_fragment, new FragmentMenu());
                                editor.putString("mode", "animal").apply();
                                break;
                            case "oiseau":
                                fragmentTransaction.replace(R.id.contenu_fragment, new FragmentMenu());
                                editor.putString("mode", "oiseau").apply();
                                break;
                            default:
                                fragmentTransaction.replace(R.id.contenu_fragment, new FragmentMenu());

                        }
                        fragmentTransaction.addToBackStack("fragment_scan_feuille");
                        fragmentTransaction.commit();
                        return true;
                    case R.id.recherche_item:
                        fragmentTransaction.replace(R.id.contenu_fragment, new FragmentRecherche());
                        fragmentTransaction.addToBackStack("fragment_recherche");
                        fragmentTransaction.commit();
                        return true;
                }
                return false;
            });



            if(testUpdate.getVersionApk() < testUpdate.getVersionCode()) {
                getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new UpdateActivity()).commit();
            }

            progressdialog = new ProgressDialog(MainActivity.this);
            progressdialog.setIndeterminate(false);

            // On met à jour tous les réseaux de neurones
            new Thread(){
                @Override
                public void run(){
                    UtilitaireModels.load_model_versions(getApplicationContext(), MainActivity.this, progressdialog);
                    UtilitaireModels.update_modeles();
                }
            }.start();



            //Téléchargements des données pour les feuilles depuis le CSV et calcul des couleurs
            if (databaseHandler.emptyTable("leafs") && databaseHandler.emptyTable("animal") && databaseHandler.emptyTable("bird")) {
                Log.e("Database", "Adding data...");
                ProgressDialog boiteChargement;
                boiteChargement = new ProgressDialog(MainActivity.this);
                boiteChargement.setMessage("Téléchargemment des données en cours... \n Veuillez patienter.");
                boiteChargement.setTitle("Téléchargement");
                boiteChargement.setCancelable(false);
                boiteChargement.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            //Ajout des animaux
                            InputStream csvAnimal = getBaseContext().getResources().openRawResource(R.raw.animal);
                            databaseHandler.addAnimalFromCsv(csvAnimal, getBaseContext());
                            //On récupère toutes les feuilles depuis le csv
                            InputStream csvLeaf = getBaseContext().getResources().openRawResource(R.raw.liste_leaf1);
                            databaseHandler.addLeafFromCsv(csvLeaf, getBaseContext());
                            //On récupère toutes les oiseaux depuis le csv
                            InputStream csvBird = getBaseContext().getResources().openRawResource(R.raw.birds);
                            databaseHandler.addBirdFromCsv(csvBird, getBaseContext());
                            //On récupère les couleurs de chaque feuille
                           /* if (databaseHandler.emptyTable("colors")) {
                                List<Leaf> leaves = databaseHandler.getAllLeafs();
                                //Parcours de chaque feuille
                                for (Leaf leaf : leaves) {
                                    int id = leaf.getId();
                                    //Liste de toutes les couleurs d'une image
                                    List<float[]> color = getDominantColor(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(leaf.getPicture(), "drawable", getPackageName())));
                                    HslColor hslColor = new HslColor(id, color);
                                    //Ajout d'une HslColor (id, List<Integer>)
                                    databaseHandler.addColor(hslColor);
                                }
                            }*/
                            Log.e("Database", "Data added !");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        boiteChargement.dismiss();
                    }
                }).start();
            }
            /**
             *
             *
             * ----------------------------------------------Partie Didacticiel -----------------------------------------------------------------------------------------
             *
             *
             */
            //On récupère le booléen dans le cache et on vérifie si la table est vide
            Boolean show = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("menu", false);
            //Si le cache est vide et que la table didacticiel est vide
            if ((!show) && (databaseHandler.emptyTable("didacticiel"))) {
                new TapTargetSequence(this)
                        .targets(
                                TapTarget.forView(findViewById(R.id.analyse_item), "Analyse", "Ce bouton permet d'accéder au mode Analyse !").dimColor(android.R.color.black)
                                        .outerCircleColor(R.color.colorTheme)
                                        .targetCircleColor(R.color.transparent_gray)
                                        .textColor(android.R.color.black),
                                TapTarget.forView(findViewById(R.id.profil_item), "Profil", "Ce bouton permet de vous connecter où de vous inscrire !")
                                        .dimColor(android.R.color.black)
                                        .outerCircleColor(R.color.colorTheme)
                                        .targetCircleColor(R.color.transparent_gray)
                                        .textColor(android.R.color.black),
                                TapTarget.forView(findViewById(R.id.recherche_item), "Encyclopédie", "Ce bouton permet de consulter l'encyclopédie des espèces !")
                                        .dimColor(android.R.color.black)
                                        .outerCircleColor(R.color.colorTheme)
                                        .targetCircleColor(R.color.transparent_gray)
                                        .textColor(android.R.color.black))
                        .listener(new TapTargetSequence.Listener() {

                            //Si l'utilisateur parcours tout le didacticiel ou l'annule au milieu on ajoute un booléen
                            @Override
                            public void onSequenceFinish() {
                                SharedPreferences.Editor editor = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                                editor.putBoolean("menu", true).apply();
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                                SharedPreferences.Editor editor = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                                editor.putBoolean("menu", true).apply();
                            }

                        }).start();

            } else if (!(databaseHandler.emptyTable("didacticiel"))) {
                //On récupère les valeurs depuis la table et on les entre dans le cache
                SharedPreferences.Editor editor = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                editor.putBoolean("menu", databaseHandler.getDidacticiel("menu")).apply();
                editor.putBoolean("analyse", databaseHandler.getDidacticiel("analyse")).apply();
                editor.putBoolean("profil", databaseHandler.getDidacticiel("profil")).apply();
                editor.putBoolean("settings", databaseHandler.getDidacticiel("settings")).apply();
                editor.putBoolean("search", databaseHandler.getDidacticiel("search")).apply();

            }
        }

    }


    /**
     * Méthode appelé lorsqu'il y a une fenêtre qui pop, par exemple un appel ou un sms
     */

    @Override
    public void onPause() {
        super.onPause();
        requestsPermissions();
    }

    /**
     * Méthode appelé lorsque l'utilisateur a quitté l'application mais qu'elle est toujours présente dans la pile
     */

    @Override
    public void onResume() {
        super.onResume();
        requestsPermissions();

    }

    /***

     * Méthode appelé quand l'application a disparu du cycle

     */

    @Override
    protected void onStop() {
        super.onStop();
        //On ajoute les informations du cache uniquement si le cache a été modifié
        sharedPreferences.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        //On enregistre les données du cache dans la base de donnée local
                        DatabaseHandler databaseHandler = new DatabaseHandler(getBaseContext());
                        //On récupère le booléen permettant de savoir si l'utilisateur à déjà vu le didacticiel
                        Boolean menu = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("menu", false);
                        Boolean analyse = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("analyse", false);
                        Boolean settings = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("settings", false);
                        Boolean profil = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("profil", false);
                        Boolean search = getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("search", false);
                        if (menu) {
                            databaseHandler.updateDidacticiel("menu", menu);
                        }
                        if (analyse) {
                            databaseHandler.updateDidacticiel("analyse", analyse);
                        }
                        if (settings) {
                            databaseHandler.updateDidacticiel("settings", settings);
                        }
                        if (profil) {
                            databaseHandler.updateDidacticiel("profil", profil);
                        }
                        if (search) {
                            databaseHandler.updateDidacticiel("search", search);
                        }
                    }
                });

    }

    /**
     * Fonction permettant de vérifier si les permissions ont été validées ou non
     *
     * @return boolean
     */

    public boolean requestsPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        };
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;

            }
        }
        return true;

    }

    /**
     * Fonction retournant tous les hsv d'une image
     * (Sert à connaitre les couleurs d'une feuille)
     *
     * @param bitmap (tableau avec toutes les couleurs de l'image
     * @return
     */
    public static List<float[]> getDominantColor(Bitmap bitmap) {
        int couleurBlanche = Color.rgb(255, 255, 255);
        float[] LOWER_GREEN = new float[]{65, 60, 60};
        float[] HIGHER_GREEN = new float[]{80, 255, 255};
        boolean higher = true;
        boolean lower = true;
        List<float[]> allHsv = new ArrayList<>();
        //Parcours de la bitmap
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                //Extraction de chaque pixel
                int color = bitmap.getPixel(i, j);
                //Extraction de chaque couleur
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float[] hsv = new float[3];
                //Récupération du HSV
                Color.RGBToHSV(r, g, b, hsv);
                int couleurTest = Color.rgb(r, g, b);
                //Si la couleur n'est pas blanche
                if (couleurTest != couleurBlanche) {
                    //On compare avec le seuil de HSV
                    for (int k = 0; k < LOWER_GREEN.length; k++) {
                        if (lower) {
                            if (hsv[k] >= LOWER_GREEN[k]) {
                                lower = true;
                            } else {
                                lower = false;
                            }
                        }
                    }
                    //On compare avec le plafond de HSV
                    for (int l = 0; l < HIGHER_GREEN.length; l++) {
                        if (higher) {
                            if (hsv[l] <= HIGHER_GREEN[l]) {
                                higher = true;
                            } else {
                                higher = false;
                            }
                        }
                    }
                    //Si tout est bon on ajoute les hsv dans la List des hsv
                    if (higher && lower) {
                        allHsv.add(hsv);
                    }
                }
            }
        }
        return allHsv;

    }


    @Override public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contenu_fragment);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                progressdialog = new ProgressDialog(MainActivity.this);
                progressdialog.setMessage("Downloading files from server...");
                progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressdialog.setCancelable(false);
                progressdialog.show();
                return progressdialog;

            default:

                return null;
        }
    }


}

