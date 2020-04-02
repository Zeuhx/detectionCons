package descartes.info.l3ag2.eyetrek.classes;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class Constellation_Astro {
    private static final String TAG = "Constellation_Astro";

    private String name;
    private String connexion;
    private int id;
    private List<Double> etoile_x_array ;
    private List<Double> etoile_y_array ;
    private List<Double> etoile_magnitude_array ;
    private List<Mat> lignes_array ;
    private int nbOfStars ;
    private int [] indice_luminosite;

    /**
     * Exemple
     * x 4 3 5 2 4
     * y 2 3 4 5 2
     * m 1 1 2 4 1.5
     * -
     * 3 2 4 0 1
     */
    public Constellation_Astro(String name, int id, List<Double> etoile_x_array, List<Double> etoile_y_array, List<Double> etoile_magnitude_array, List<Mat> lignes_array) {
        this.name = name;
        this.id = id;
        this.etoile_x_array = etoile_x_array;
        this.etoile_y_array = etoile_y_array;
        this.etoile_magnitude_array = etoile_magnitude_array;
        this.lignes_array = lignes_array;
        this.nbOfStars = etoile_magnitude_array.size() ;
        this.indice_luminosite = ArraysUtils.argsort(ArraysUtils.convertDoublePrimitiveArray(etoile_magnitude_array),false);
        this.connexion = null;
    }

    public Constellation_Astro(String name, int id, List<Double> etoile_x_array, List<Double> etoile_y_array, List<Double> etoile_magnitude_array, List<Mat> lignes_array,String connexion){
        this.name = name;
        this.id = id;
        this.etoile_x_array = etoile_x_array;
        this.etoile_y_array = etoile_y_array;
        this.etoile_magnitude_array = etoile_magnitude_array;
        this.lignes_array = lignes_array;
        this.nbOfStars = etoile_magnitude_array.size() ;
        this.indice_luminosite = ArraysUtils.argsort(ArraysUtils.convertDoublePrimitiveArray(etoile_magnitude_array),false);
        this.connexion = connexion;
    }

    public void aligner_constellation(){
        Log.d(TAG, "aligner_constellation: xArrayA : " + etoile_x_array.toString() + " yArrays : " + etoile_y_array.toString());
        if(indice_luminosite.length >= 2){
            straighten();
            double xCoorMaxLumin = etoile_x_array.get(indice_luminosite[0]);
            double yCoorMaxLumin = etoile_y_array.get(indice_luminosite[0]);

            //UtilAnalyseImage_Astro.alignerToCoordonnes(etoile_x_array,etoile_y_array,xCoorMaxLumin,yCoorMaxLumin);
        }

        Log.d(TAG, "aligner_constellation: xArrayAprès : " + etoile_x_array.toString() + " yArrays : " + etoile_y_array.toString());

    }

    /**
     *  "Redresse" les etoiles pour aligner
     * les deux etoiles les plus brillantes sur l'axe des x
     *
     */
    private double alignementMemeAxe(List<Double> x_array, List<Double> y_array, List<Double> magnitude){

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

    public List<Double> getEtoile_x_array() {
        return etoile_x_array;
    }

    public List<Double> getEtoile_y_array() {
        return etoile_y_array;
    }

    public List<Double> getEtoile_magnitude_array() {
        return etoile_magnitude_array;
    }

    public int getNbOfStars() {
        return nbOfStars;
    }

    public int[] getIndice_luminosite() {
        return indice_luminosite;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /**
     * Met a l'echelle les valeur de x et y en divant par l'echelle entree en parametre
     * @param x_array un tableau d'entree de x qui sera modifier a la sortie
     * @param y_array un tableau d'entree de y qui sera modifier en sortie
     * @param echelle l'echelle
     */
    private void toMemeEchelle(List<Double> x_array, List<Double> y_array, double echelle){
        for (int i = 0; i <x_array.size() ; i++) {
            x_array.set(i, x_array.get(i)/echelle);
            y_array.set(i, y_array.get(i)/echelle);
        }
    }

    private void straighten(){


        int indice1 = indice_luminosite[0];
        int indice2 = indice_luminosite[1];

        double dx = etoile_x_array.get(indice2) - etoile_x_array.get(indice1);
        double dy = etoile_y_array.get(indice2) - etoile_y_array.get(indice1);

        double diff = dy/dx ;
        double angle = 90 * (1 - (Math.signum(dx))) + Math.atan(diff);

        double xprime ;
        double yprime ;

        for (int i = 0; i < nbOfStars; i++) {
            // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
            xprime = ((getEtoile_x_array().get(i) * Math.cos(angle)) - (getEtoile_y_array().get(i) * Math.sin(angle)));
            yprime = ((getEtoile_x_array().get(i) * (Math.sin(angle))) + (getEtoile_y_array().get(i) * Math.cos(angle)));
            etoile_x_array.set(i,xprime);
            etoile_y_array.set(i,yprime);
        }

        Log.d(TAG, "straighten: xArray " + etoile_x_array.toString());

    }

    public String getConnexion(){
        return connexion;
    }


}
