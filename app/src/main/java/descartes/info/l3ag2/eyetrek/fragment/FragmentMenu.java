package descartes.info.l3ag2.eyetrek.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;

import static android.content.Context.SENSOR_SERVICE;
import static java.lang.Thread.sleep;

/**
 * Created by Enzo DUTRA
 * A simple {@link Fragment} subclass.
 */
public class FragmentMenu extends Fragment implements SensorEventListener {

    //SharedPreferences = Cach
    private static final String TAG = "FRAGMENT_MENU";
    private static final String PREFS_ANALYSE = "ANALYSE";
    SharedPreferences sharedPreferences;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    // Image de la boussole
    ImageView image_boussole;

    // Images des autres boutons
    ImageView image_bouton_champignons;
    ImageView image_bouton_feuilles;
    ImageView image_bouton_premiers_secours_button;
    ImageView image_bouton_securite_champignons;
    ImageView image_bouton_animal;
    ImageView image_bouton_chants;
    ImageView image_bouton_especes_proches;
    ImageView image_bouton_constellation;


    // Etat du flash
    boolean flashLightStatus = true;

    public FragmentMenu() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.gc();
        //Récupération du cache
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu2, container, false);
        CardView leaf_button = view.findViewById(R.id.leaf_button);
        CardView musrooms_button = view.findViewById(R.id.musrooms_button);
        CardView animal_button = view.findViewById(R.id.animal_button);
        CardView bird_button = view.findViewById(R.id.bird_button);
        CardView premiers_secours_button = view.findViewById(R.id.premiers_secours_button);
        CardView securite_champignons_button = view.findViewById(R.id.securite_champignons);
        CardView especes_proches_button = view.findViewById(R.id.especes_proches_button);
        CardView boutonConstellation = view.findViewById(R.id.constellation_button);

        image_bouton_champignons = view.findViewById(R.id.image_bouton_champignons);
        image_bouton_champignons.setImageDrawable(resizeImage(R.drawable.bouton_champignons));

        image_bouton_feuilles = view.findViewById(R.id.image_bouton_feuilles);
        image_bouton_feuilles.setImageDrawable(resizeImage(R.drawable.bouton_feuilles));
        /*
        image_bouton_premiers_secours_button = view.findViewById(R.id.image_bouton_premiers_secours_button);
        image_bouton_premiers_secours_button.setImageDrawable(resizeImage(R.drawable.bouton_autres));

        image_bouton_securite_champignons = view.findViewById(R.id.image_bouton_securite_champignons);
        image_bouton_securite_champignons.setImageDrawable(resizeImage(R.drawable.bouton_autres));
        */
        image_bouton_animal = view.findViewById(R.id.image_bouton_animal);
        image_bouton_animal.setImageDrawable(resizeImage(R.drawable.bouton_animal));

        image_bouton_chants = view.findViewById(R.id.image_bouton_chants);
        image_bouton_chants.setImageDrawable(resizeImage(R.drawable.bouton_chants));

        image_bouton_especes_proches = view.findViewById(R.id.image_bouton_especes_proches);
        image_bouton_especes_proches.setImageDrawable(resizeImage(R.drawable.bouton_especes_proches));

        image_bouton_constellation = view.findViewById(R.id.image_bouton_constellation);
        image_bouton_constellation.setImageDrawable(resizeImage(R.drawable.bouton_constellation));


        image_boussole = view.findViewById(R.id.image_boussole);
        image_boussole.setImageDrawable(resizeImage(R.drawable.image_boussole));
        currentDegree = 0f;
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);


        //Handler de la partie Feuille
        leaf_button.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentScanFeuille());
            fragmentTransaction.addToBackStack("fragment_scan_feuille");
            //Ajout du choix dans le cache
            //sharedPreferences.edit().putString(PREFS_ANALYSE,"Feuille").apply();
            //on lance la creation du fragment concernant la page d'analyse
            fragmentTransaction.commit();
        });

        //Handler de la partie Champignons
        musrooms_button.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentScanChampignon());
            fragmentTransaction.addToBackStack("fragment_scan_champignon");
            //Ajout du choix dans le cache
            //sharedPreferences.edit().putString(PREFS_ANALYSE,"Feuille").apply();
            //on lance la creation du fragment concernant la page d'analyse
            fragmentTransaction.commit();
        });


        //Handler de la partie Empreinte animal
        animal_button.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnimal());
            fragmentTransaction.addToBackStack("fragment_animal");
            //Ajout du choix dans le cache
            sharedPreferences.edit().putString(PREFS_ANALYSE, "Animal").apply();
            fragmentTransaction.commit();
        });

        //Handler de la partie Chant d'oiseau
        bird_button.setOnClickListener((v) -> {
            //Ajout du choix dans le cache
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAudio());
            fragmentTransaction.addToBackStack("fragment_audio");
            sharedPreferences.edit().putString(PREFS_ANALYSE, "Oiseau").apply();
            fragmentTransaction.commit();
        });

        //Handler de la partie especes proches
        especes_proches_button.setOnClickListener((v) -> {
            //Ajout du choix dans le cache
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentEspecesProches());
            fragmentTransaction.addToBackStack("fragment_espece_proche");
            //sharedPreferences.edit().putString(PREFS_ANALYSE,"Especes proches").apply();
            fragmentTransaction.commit();
        });


        //Handler de la partie Premiers secours
        premiers_secours_button.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentPremiersSecours());
            fragmentTransaction.addToBackStack("fragment_premiers_secours");
            fragmentTransaction.commit();
        });


        //Handler de la partie sur les précotions à prendre avec les champignons
        securite_champignons_button.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentSecuriteChampignons());
            fragmentTransaction.addToBackStack("fragment_securite_champignons");
            fragmentTransaction.commit();
        });

        boutonConstellation.setOnClickListener((v) -> {
            Intent astroMode = new Intent(getContext(), AstroActivity.class);
            startActivity(astroMode);
        });


        return view;
    }



    @Override
    public void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);


        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image_boussole.startAnimation(ra);
        currentDegree = -degree;

    }


    public Drawable resizeImage(int imageResource) {// R.drawable.large_image
        // Get device dimensions
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        double deviceWidth = display.getWidth();

        BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(
                imageResource);
        double imageHeight = bd.getBitmap().getHeight();
        double imageWidth = bd.getBitmap().getWidth();

        double ratio = deviceWidth / imageWidth;
        int newImageHeight = (int) (imageHeight * ratio);

        try{
            Bitmap bMap = BitmapFactory.decodeResource(getResources(), imageResource);
            Drawable drawable = new BitmapDrawable(this.getResources(),
                    getResizedBitmap(bMap, newImageHeight, (int) deviceWidth));

            return drawable;
        } catch(OutOfMemoryError ar){
            ar.printStackTrace();
            Log.e("fragment menu", "Erreur avec le drawable : " + imageResource);
            try {
                sleep(100);
                System.gc();
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inDither=false;                     //Disable Dithering mode
                bmOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
                bmOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
                bmOptions.inTempStorage=new byte[32 * 1024];
                Bitmap bMap = BitmapFactory.decodeResource(getResources(), imageResource, bmOptions);
                Drawable drawable = new BitmapDrawable(this.getResources(),
                        getResizedBitmap(bMap, newImageHeight, (int) deviceWidth));

                return drawable;
            } catch (InterruptedException | OutOfMemoryError err) {
                err.printStackTrace();
            }

            return null;
        }

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        try {

            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, false);

            bm = null;
            System.gc();

            return resizedBitmap;
        } catch(OutOfMemoryError ar){
            ar.printStackTrace();
            return bm;
        }


    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }






}

