package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;

public class BuildConstellation_Astro implements Runnable {

    private List<Constellation_Astro> constellation_astros;
    private static BuildConstellation_Astro buildConstellation_astro;
    private AstroActivity astroActivity;
    private boolean init = false;
    private final String fileName = "dataCons.txt";

    private BuildConstellation_Astro(AstroActivity astroActivity) {
        this.constellation_astros = new ArrayList<>();
        this.astroActivity = astroActivity;
    }

    public static BuildConstellation_Astro getInstance(AstroActivity astroActivity) {
        if (buildConstellation_astro == null) {
            buildConstellation_astro = new BuildConstellation_Astro(astroActivity);
        }
        return buildConstellation_astro;
    }

    public static BuildConstellation_Astro getInstance() {
        return buildConstellation_astro;
    }

    private static final String TAG = "BuildConstellation_Astro";

    /**
     * Cette méthode permet de créer les constellations à partir d'images templates afin de connaitre la valeurs des position des étoiles pour chaque constellation
     */
    private void initTemplatesFromDataBase() {

        if (!init) {
            DataInputStream textFileStream = null;

            try {
                textFileStream = new DataInputStream(astroActivity.getAssets().open(String.format("dataCons.txt")));
                Scanner clavier = new Scanner(textFileStream);
                clavier.useDelimiter(";|\\n");


                String[] consName = astroActivity.getResources().getStringArray(R.array.constellations_titles);

                while (clavier.hasNext()) {
                    for (int i = 0; i < consName.length; i++) {
                        String conName = clavier.next();

                        List<Double> x_array = new ArrayList<>();
                        List<Double> y_array = new ArrayList<>();
                        List<Double> label_array = new ArrayList<>();

                        ArraysUtils.initList(x_array,clavier.next());
                        ArraysUtils.initList(y_array,clavier.next());
                        ArraysUtils.initList(label_array,clavier.next());
                        //Création de la constellation, le paramètre ligne sont à null, à faire
                        Constellation_Astro constelation = new Constellation_Astro(conName, i, x_array, y_array, label_array, null);

                        Log.d(TAG, "c: x_array : " + constelation.getEtoile_x_array());
                        Log.d(TAG, "initTemplatesFromDataBase: y_array : " + constelation.getEtoile_y_array());
                        Log.d(TAG, "initTemplatesFromDataBase: label_array : " + constelation.getEtoile_magnitude_array());
                        Log.d(TAG, "initTemplatesFromDataBase: indice : " + ArraysUtils.StringArray(constelation.getIndice_luminosite()));
                        Log.d(TAG, "initTemplatesFromDataBase: name : " + consName[i]);
                        Log.d(TAG, "initTemplatesFromDataBase: size label: " + label_array.size());

                        constellation_astros.add(constelation);
                    }

                }

                init = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "onCreate: fin de l'initialisation des templates");
    }

    private void initTemplatesFromPic() {
        if (!init) {
            String[] consteImages = astroActivity.getResources().getStringArray(R.array.constellations_images);
            String[] consName = astroActivity.getResources().getStringArray(R.array.constellations_titles);

            for (int i = 0; i < consName.length; i++) {

                //Obtention de l'image sous forme de bitmap et conversion sous Mat et sans le text du template
                Bitmap icon = BitmapFactory.decodeResource(astroActivity.getResources(), astroActivity.getResources().getIdentifier(consteImages[i], "drawable", astroActivity.getApplicationContext().getPackageName()));
                Mat temp = ShapeDetector_Astro.deleteName(icon);

                //Obtention seulement des etoiles afin de les localiser
                Mat onlyStar = ShapeDetector_Astro.get_stars(temp);

                Mat detecStar = new Mat();
                List<Double> x_array = new ArrayList<>();
                List<Double> y_array = new ArrayList<>();
                List<Double> label_array = new ArrayList<>();

                //On detecte les étoiles présent dans l'image précédente afin de prendre leur coordonnées ainsi que leur magnétude
                ShapeDetector_Astro.detect_stars(onlyStar, detecStar, x_array, y_array, label_array);

                //Création de la constellation, le paramètre ligne sont à null, à faire
                Constellation_Astro constelation = new Constellation_Astro(consName[i], i, x_array, y_array, label_array, null);

                Log.d(TAG, "initTemplatesFromDataBase: x_array : " + constelation.getEtoile_x_array());
                Log.d(TAG, "initTemplatesFromDataBase: y_array : " + constelation.getEtoile_y_array());
                Log.d(TAG, "initTemplatesFromDataBase: label_array : " + constelation.getEtoile_magnitude_array());
                Log.d(TAG, "initTemplatesFromDataBase: indice : " + ArraysUtils.StringArray(constelation.getIndice_luminosite()));
                Log.d(TAG, "initTemplatesFromDataBase: name : " + consName[i]);
                Log.d(TAG, "initTemplatesFromDataBase: size label: " + label_array.size());

                constellation_astros.add(constelation);
            }

            init = true;
        }
        Log.d(TAG, "initTemplatesFromPic: fin de l'initialisation des templates");
    }

    public List<Constellation_Astro> getConstellation_astros() {
        return constellation_astros;
    }

    @Override
    public void run() {
        if (astroActivity != null) {
            initTemplatesFromDataBase();
        }
    }

}
