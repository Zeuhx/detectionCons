package descartes.info.l3ag2.eyetrek.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.OpenCVLoader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Scanner;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.BuildConstellation_Astro;
import descartes.info.l3ag2.eyetrek.classes.UtilAnalyseImage_Astro;
import descartes.info.l3ag2.eyetrek.fragment.FragmentCatalogueCons;
import descartes.info.l3ag2.eyetrek.fragment.FragmentCatalogueStar;
import descartes.info.l3ag2.eyetrek.fragment.FragmentMenuAstro;
import descartes.info.l3ag2.eyetrek.fragment.FragmentProfilAstro;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;

public class AstroActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    static final String TAG = "AstroActivity";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    public static String dataCons[][];
    private BuildConstellation_Astro buildConstellationAstro;

    static {
       System.loadLibrary("opencv_java3");
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"Open CV has loaded correctly");
        } else {
            Log.d(TAG,"Open CV has not loaded correctly, not at all");
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        buildConstellationAstro = new BuildConstellation_Astro();
        buildConstellationAstro.initTemplates(this);
        dataCons = readData();

        //Enlève la bar de notification -> FUll screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.toolbar_astro_mode);

        toolbar = (Toolbar) findViewById(R.id.toolBarAstro);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_astromenu);
        navigationView = (NavigationView) findViewById(R.id.navViewAstro);

        //Relier l'action des "menus" de la navView au bouton de la navView
        navigationView.setNavigationItemSelectedListener(this);


        //Enlever le titre de la toolBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        if(savedInstanceState == null){
            addFragment(new FragmentMenuAstro());
        }

    }

    /**
     * Cette méthode gère les évenemets lors d'un clique sur le bouton retour du téléphone
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_contenairAstro);
            if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
                super.onBackPressed();
            } else {
                replaceFragment(new FragmentMenuAstro());
            }
        }
    }

    /**
     * Méthode qui regarde la pile de fragment s'il le fragment en question existe déja dans la pile, le système le remplace alors, sinon il le creer
     * @param fragment
     */
    private void replaceFragment(Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_contenairAstro, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    /**
     * Cette méthode permet d'ajouter un fragment dans la pile des fragments et par ensuite d'y accéder
     * @param fragment
     */
    private void addFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_contenairAstro, fragment);
        transaction.commit();
    }

    /**
     * Cette méthode créé le bouton "menu" sur la toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_astro, menu);
        return true;
    }

    /**
     * Cette méthode sert à rediriger le l'interface graphique vers un fragment depuis la navigationBar situé dans l'activité
     * @param menuItem
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.catalogconsNav:
                replaceFragment(new FragmentCatalogueCons());
                break;

            case R.id.profilNav:
                replaceFragment(new FragmentProfilAstro());
                break;

            case R.id.catalogstarNav:
                replaceFragment(new FragmentCatalogueStar());
                break;

            case R.id.logoutfromAstroNav:
                Log.d(TAG,"On quitte le menu astro pour revenir dans le menu de EyeTrek");
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Cette méthode permet de créer et de remplir le tableau de données qui sera utilisé dans le Fragment CatalogueConsDetails
     * @return
     */
    private String [][] readData() {
        String data[][] = new String [UtilAnalyseImage_Astro.ETOILE_HEMISPHERE_BOREAL][6];

        try {

            DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("DetailsConst.txt")));
            Scanner clavier = new Scanner(textFileStream);
            clavier.useDelimiter(";|\\n");

            while(clavier.hasNext()){
                for(int i = 0; i < UtilAnalyseImage_Astro.ETOILE_HEMISPHERE_BOREAL; i++){
                    for(int j = 0; j < 6;j++) {
                        data[i][j] = clavier.next();
                        Log.d(TAG, "readData: ["+ i +"][" + j +"]" + data[i][j]);
                    }
                }
            }
            clavier.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}