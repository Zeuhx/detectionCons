package descartes.info.l3ag2.eyetrek.classes;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class Constellation_Astro {
    private static final String TAG = "Constellation_Astro";

    private List<Double> etoile_x_array ;
    private List<Double> etoile_y_array ;
    private List<Double> etoile_magnitude_array ;
    private List<Mat> lignes_array ;
    private int nbOfStars ;
    private List<Double> luminosite_etoile_array ;

    public Constellation_Astro(List<Double> etoile_x_array, List<Double> etoile_y_array, List<Double> etoile_magnitude_array, List<Mat> lignes_array) {
        this.etoile_x_array = etoile_x_array;
        this.etoile_y_array = etoile_y_array;
        this.etoile_magnitude_array = etoile_magnitude_array;
        this.lignes_array = lignes_array;

        nbOfStars = etoile_magnitude_array.size() ;
    }

    private void aligner_constellation(){

        int index = Integer.parseInt(luminosite_etoile_array.get(0).toString());
        Log.d(TAG, "aligner_constellation: Verif que l'index est un int : " + index);
        Double lumino_x = etoile_x_array.get(index);
        Double lumino_y = etoile_y_array.get(index);

    }

    /**
     *  "Redresse" les etoiles pour aligner
     * les deux etoiles les plus brillantes sur l'axe des x
     *
     */
    private double alignementMemeAxe(List<Double> x_array, List<Double> y_array, List<Double> magnitude){
        magnitude.sort((x,y) -> {
            if(x>y){
                return 1 ;
            } else if (x == y) {
                return 0 ;
            } else {
                return -1 ;
            }
        });

        List<Double> magnitude_etoile_triee = new ArrayList<>(magnitude);
        
        double temp =  magnitude_etoile_triee.get(0);
        int l1 = (int) temp;
            temp =  magnitude_etoile_triee.get(1);
        int l2 = (int) temp;

        Double dx = x_array.get(l2) - x_array.get(l1);
        Double dy = x_array.get(l2) - x_array.get(l1);

        Double angle = 90*(1-Math.signum(dx)) + Math.atan(dy/dx);

        rotation(x_array, y_array, angle);

        return angle;
    }

    /**
     * Cette methode fait une rotation pour une liste de x et y rentrer en parametre ainsi que l'angle choisit
     * @param ret_xlistRot la liste des x qu'on va rotationner
     * @param ret_ylistRot la liste des y qu'on va retotionner
     * @param angle l'angle de rotation
     */
    private void rotation(List<Double> ret_xlistRot,List<Double> ret_ylistRot, Double angle){
        double xprime ;
        double yprime ;
        for (int i = 0; i < etoile_y_array.size(); i++) {
            // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
            xprime = ((etoile_x_array.get(i) * Math.cos(angle)) - (etoile_y_array.get(i) * Math.sin(angle)));
            yprime = ((etoile_x_array.get(i) * (Math.sin(angle))) + (etoile_y_array.get(i) * Math.cos(angle)));
            ret_xlistRot.set(i, xprime);
            ret_ylistRot.set(i, yprime);
        }
    }

}
