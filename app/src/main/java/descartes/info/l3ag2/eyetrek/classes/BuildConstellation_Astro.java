package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;

public class BuildConstellation_Astro {

    private List<Constellation_Astro> constellation_astros;

    public BuildConstellation_Astro() {
        this.constellation_astros = new ArrayList<>();
    }

    private static final String TAG = "BuildConstellation_Astr";

    /**
     * Cette méthode permet de créer les constellations à partir d'images templates afin de connaitre la valeurs des position des étoiles pour chaque constellation
     * @param astroActivity
     */
    public void initTemplates(AstroActivity astroActivity){

        String[] consteImages = astroActivity.getResources().getStringArray(R.array.constellations_images);

        for (int i = 0; i < consteImages.length; i++){

            //Obtention de l'image sous forme de bitmap et conversion sous Mat et sans le text du template
            Bitmap icon = BitmapFactory.decodeResource(astroActivity.getResources(),astroActivity.getResources().getIdentifier(consteImages[i],"drawable",astroActivity.getApplicationContext().getPackageName()));
            Mat temp = ShapeDetector_Astro.deleteName(icon);

            //Obtention seulement des etoiles afin de les localiser
            Mat onlyStar = ShapeDetector_Astro.get_stars(temp);

            Mat detecStar = new Mat();
            List<Double> x_array = new ArrayList<>();
            List<Double> y_array = new ArrayList<>();
            List<Double> label_array = new ArrayList<>();

            //On detect les étoiles présent dans l'image précédente afin de prendre leur coordonnées ainsi que leur magnétude
            ShapeDetector_Astro.detect_stars(onlyStar,detecStar,x_array,y_array,label_array);

            Log.d(TAG, "initTemplates: x_array : " + x_array);
            Log.d(TAG, "initTemplates: y_array : " + y_array);
            Log.d(TAG, "initTemplates: label_array : " + label_array);
            Log.d(TAG, "initTemplates: name : " + consteImages[i] + " size X : " + x_array.size() + " size Y : " + y_array.size() + " size label : " + label_array.size());

            //Création de la constellation, le paramètre ligne sont à null, à faire
            Constellation_Astro constelation = new Constellation_Astro(x_array,y_array,label_array,null);
            constellation_astros.add(constelation);

        }


    }

}
