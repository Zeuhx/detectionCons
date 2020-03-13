package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.List;

/**
 * Cette classe est une classe permettant l'utilisation des outils d'analyse d'une image
 * Les informations sont detaillees directement dans leurs methodes, mais voici un petit appercu
 * Elle propose :
 *  - binariser : la binarisation d'une etoile
 *  - findConstellationMatch : permettant la recherche d'une image correspondante a travers des donnees fournies
 *  - rotate : rotation d'une image en fonction deux deux etoiles rentree en parametre et appelle la methode " rotateTemplate"
 *  - rotateTemplate : lorsqu'on a fini de calculer l'angle, on fait la rotation
 */
public class UtilAnalyseImage_Astro {

    private static final String TAG = "UtilAnalyseImage_Astro";
    private static double[][] etoileArrayDetectee;
    private static double[][][] template;
    public static final int ETOILE_HEMISPHERE_BOREAL = 36;

    private Bitmap bitmap;

    private static UtilAnalyseImage_Astro utilAnalyseImage_astro = null;


    private UtilAnalyseImage_Astro(){ }

    public static UtilAnalyseImage_Astro getInstance(){
        if(utilAnalyseImage_astro == null){
            utilAnalyseImage_astro = new UtilAnalyseImage_Astro();
        }
        return utilAnalyseImage_astro;
    }

    /**
     *
     *  --- Pour OpenCV
     *
     */

    /**
     * Cette methode est appellee pour chaque photo prise ou image importée depuis le provider.
     * Cette methode permet de detecter la position
     * des étoiles ainsi que sa taille
     * on execute un algorithme de detection de blob sur la frame et on stock les données dans un tableau trier.
     */
    public void starAnalyse() {
        Mat mGray;
        mGray = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC1);

        Mat image = new Mat();
        Utils.bitmapToMat(bitmap,image);


        // Mise en gris
        // image seuillé
        Mat tresh = new Mat();

        Imgproc.cvtColor(image, mGray, Imgproc.COLOR_BGR2GRAY);
        /**
         * Dessiner les points a partir des endroits ou il a detecter les blobs de type SIMPLE
         */

        Imgproc.threshold(mGray,tresh,100,255,Imgproc.THRESH_BINARY_INV);


        // Dessiner les points
        MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();
        KeyPoint[] keyPoints = matOfKeyPoints.toArray();
        etoileArrayDetectee = new double[keyPoints.length][3];

        Log.d(TAG,"" + keyPoints.length);
        for (int i=0; i < keyPoints.length; i++) {
            etoileArrayDetectee[i][0] = keyPoints[i].pt.x;
            etoileArrayDetectee[i][1] = keyPoints[i].pt.y;
            etoileArrayDetectee[i][2] = keyPoints[i].size;
        }

        // Trie le tableau d'etoile par la zone de chaque etoile
        Arrays.sort(etoileArrayDetectee, new java.util.Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                return Double.compare(b[2], a[2]);
            }
        });

        /**
         * Log information des keypoints : triant en fonction de la taille
         * Ici 3e case du tableau
         */
        Log.i(TAG, matOfKeyPoints.dump());
        for (double[] star : etoileArrayDetectee) {
            Log.i(TAG, star[0] + ", " + star[1] + ", " + star[2]);
        }
    }

    /**
     * cette fonction recherche dans l'image utilisateur et dataset de modèle pour trouver une correspondance entre les données
     * Elle recherche des correspondances en analysant chaque couple d'étoiles dans l'ensemble de données d'image utilisateur.
     * On regarde d'abord les deux premières étoiles pour
     * determiner l'échelle de leurs coordonnées et on applique donc cette échelle au modèle examiné
     * Puis on itere sur l'ensemble de données utilisateur pour chaque ensemble de couple possible, en vérifiant s'il y a une correspondance de distance
     *
     * 10% de la distance entre les étoiles correspondantes du modele
     * L’algorithme continue à parcourir jusqu'a ce qu'une correspondance du couple soit trouvée
     * Puis on parcourt jusqu'à que les données utilisateur à partir de la troisième
     * etoile de la correspondance de couple pour trouver le reste des étoiles dans la constellation
     * @param templateData le template
     **/
    private String[][] findConstellationMatch(String[][][] templateData) {

        // recuperer le jeu de donnees de l'image de l'utilisateur à partir de l'activité de la camera
        double[][] utilisateurEtoileData = etoileArrayDetectee;

        // ce tableau contiendra les coordonnees de toutes les etoiles de la constellation identifiee
        String[][] match = new String[2][26];
        int templateNbEtoile;

        // 39 Constellations dans le nord
        for(int i=0;i< ETOILE_HEMISPHERE_BOREAL;i++)  {
            templateNbEtoile = Integer.parseInt(templateData[0][i][1]);

            // itère sur le dataset d'image utilisateur pour l'index de la première étoile du triplet
            for(int etoile1 = 0;etoile1 < utilisateurEtoileData.length - 1;etoile1++) {
                double etoile1_x = utilisateurEtoileData[etoile1][0];

                // itère sur le  dataset d'image utilisateur pour l'index de la deuxième étoile du couple
                for(int etoile2 = etoile1+ 1 ; etoile2<utilisateurEtoileData.length ; etoile2++)  {

                    double[][][] rotatedTemplates = rotate(template, etoile1, etoile2);
                    double etoile2_x = utilisateurEtoileData[etoile2][0];

                    // Calcul du ratio : Échelle en utilisant la distance entre les deux étoiles les plus brillantes, calculez les ratios.
                    double templateXDelta = 0;
                    // templateXDelta = (rotatedTemplates[1][i][2]-rotatedTemplates[1][i][1])/(rotatedTemplates[1][i][1]-rotatedTemplates[1][i][0]);
                    double templateYDelta = 0;
                    // (rotatedTemplates[2][i][2]-rotatedTemplates[2][i][1])/(rotatedTemplates[2][i][1]-rotatedTemplates[2][i][0]);

                    double xDelta = 0 ;
                    // xDelta = (utilisateurEtoileData[etoile1][0])/(utilisateurEtoileData[etoile2][0]-utilisateurEtoileData[etoile1][0]);
                    double yDelta = 0;
                    // yDelta = (utilisateurEtoileData[etoile2][1])/(utilisateurEtoileData[etoile2][1]-utilisateurEtoileData[etoile1][1]);


                    // vérifie si une correspondance a été trouvée avec l'ensemble du couple: 90% de taux

                    if(templateNbEtoile<=utilisateurEtoileData.length && xDelta>(templateXDelta*0.9) && xDelta<(templateXDelta*1.1) && yDelta>(templateYDelta*0.9) && yDelta<(templateYDelta*1.1)){

                        Log.i(TAG, "Template trouvé :  " + templateData[0][i][0]);

                        // si une correspondance a été trouvée, enregistrez leurs coordonnées dans le tableau match [ ] [ ] ainsi que le nom de la constellation identifiée → il devient candidat (à reflechir)

                        match[0][0] = templateData[0][i][0];
                        match[1][0] = Double.toString(utilisateurEtoileData[etoile1][0]);
                        match[2][0] = Double.toString(utilisateurEtoileData[etoile1][1]);
                        match[1][1] = Double.toString(utilisateurEtoileData[etoile2][0]);
                        match[2][1] = Double.toString(utilisateurEtoileData[etoile2][1]);
                    }
                }
            }
        }
        // Si on ne trouve pas, on renvoie un vide
        return new String[2][26];
    }

    /**
     * Retourne le templates pivote de la bonne manieres base de deux etoiles.
     * On calcule d'abord l'angle de rotation
     * Ensuite, on appelle la methode de rotation pour transformer les donnees.
     *
     * @param templateData modele de donnees de constellation.
     * @param brightest_index la plus brillante des deux étoiles.
     * @param second_brightest autre etoile utilisee pour la rotation.
     * @return
     */
    private double[][][] rotate(double[][][] templateData, int brightest_index, int second_brightest) {
        double[][] userStarData = etoileArrayDetectee;

        double x0, y0, x1, y1, dx, dy;
        double angle = 0;
        double diff = 0 ;

        x0 = userStarData[brightest_index][0];
        y0 = userStarData[brightest_index][1];
        x1 = userStarData[second_brightest][0];
        y1 = userStarData[second_brightest][1];

        // Distance
        dx = x1 - x0;
        dy = y1 - y0;

        if(dx == 0){
            diff = 0 ;
        }
        else{
            // Angle en radian, formule pour l'angle
            diff = dy/dx ;
            angle = 90 * (1 - (Math.signum(dx))) + Math.atan(diff);
            Log.i(TAG, "Rotation angle: " + angle);
        }

        return rotateTemplates(templateData,angle,brightest_index);
    }

    /**
     * Cette fonction est appelée une fois que l'angle de rotation a été calculé, on fait pivoter les données des templates.
     * @param templateData modèle constellation data
     * @param angle angle de rotation déterminé par la fonction de rotation
     * @revenir
     */
    private double[][][] rotateTemplates(double[][][]templateData, double angle, int brightest_index) {
        double[][][] rotatedTemplates = new double[5][ETOILE_HEMISPHERE_BOREAL][26];
        double[][] userStarData = etoileArrayDetectee;
        double x, y, xprime, yprime;

        // For each constellation:
        for (int i = 0; i < ETOILE_HEMISPHERE_BOREAL; i++) {
            //Prends le nombre d'étoile pour un constellation donnée
            int numStars = (int) templateData[0][i][1];
                rotatedTemplates[0][i][0] = numStars;

                for (int j = 0; j < numStars; j++) {
                    x = templateData[1][i][j];
                    y = templateData[2][i][j];

                    // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
                    xprime = ((x * Math.cos(angle)) - (y * Math.sin(angle)));
                    yprime = ((x * (Math.sin(angle))) + (y * Math.cos(angle)));

                    rotatedTemplates[1][i][j] = xprime + userStarData[brightest_index][0];
                    rotatedTemplates[2][i][j] = yprime + userStarData[brightest_index][1];
                }
        }
        return rotatedTemplates;
    }

    /**
     * L'image source doit etre l'image grisee
     * @param image_src
     */
    public Mat binariser(Mat image_src,int tresh){
        Mat image_dest = new Mat();
        double img = Imgproc.threshold(image_src, image_dest, tresh, 255, Imgproc.THRESH_BINARY);
        return image_dest;
    }

    /**
     *
     */
    public static void trier_magnitude(List<Double> etoile_magnitude_array){
        // attention ici il ne faut pas trier la liste mais renvoyer une liste qui contient des indices qui trie la liste lignes_array



//         List<Double> magnitude_etoile_triee = new ArrayList<>(etoile_magnitude_array);
//
//         Double l1 = new Double(magnitude_etoile_triee.get(0)) ;
//         Double l2 = new Double(magnitude_etoile_triee.get(1)) ;
//
//         luminosite_etoile_array.set(0, l1);
//         luminosite_etoile_array.set(1, l2);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}


