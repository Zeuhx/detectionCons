package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private List<Double> x_arrayDetect;
    private List<Double> y_arrayDetect;
    private List<Double> label_arrayDetect;
    private int [] indiceEtoilelumin;
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

    public boolean trouver_constellation(Constellation_Astro constellation_astro, List<Double> x_array, List<Double> y_array,
                                       int position1, int position2){
        boolean trouve = false;
        double x0 = x_array.get(position1);
        double x1 = x_array.get(position2);

        double y0 = y_array.get(position1);
        double y1 = y_array.get(position2);

        double dx = x1 - x0;
        double dy = y1 - y0;

        Log.d(TAG, "trouver_constellation: dx : " + dx);
        Log.d(TAG, "trouver_constellation: y1 : " + y1);
        Log.d(TAG, "trouver_constellation: y0 : " + y0);

        double diff = 0;
        double angle;

        diff = dy/dx ;
        //angle = 90 * (1 - (Math.signum(dx))) + Math.atan(diff);

        //Calcul du vecteur entre les deux points les plus lumineux du templates
        double dxt = constellation_astro.getEtoile_x_array().get(constellation_astro.getIndice_luminosite()[1]) - constellation_astro.getEtoile_x_array().get(constellation_astro.getIndice_luminosite()[0]);
        double dyt = constellation_astro.getEtoile_y_array().get(constellation_astro.getIndice_luminosite()[1]) - constellation_astro.getEtoile_y_array().get(constellation_astro.getIndice_luminosite()[0]);

        //Calcule la différence d'angle entre deux vecteurs, la paire d'étoile choisi et le vecteur des deux étoiles les pluse lumineuses du Template
        angle = calculeAngle(dx,dy,dxt,dyt);
        Log.i(TAG, "Rotation angle: " + angle);


        List<Double> x_arrayTemp = new ArrayList<>();
        List<Double> y_arrayTemp = new ArrayList<>();

        rotateTemplates(constellation_astro,angle,x_arrayTemp,y_arrayTemp);


        double cdx = x_arrayTemp.get(constellation_astro.getIndice_luminosite()[1]) - x_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]);
        double test_scale = cdx/dx;

        toMemeEchelle(x_arrayTemp,y_arrayTemp,test_scale);
        alignerToCoordonnes(x_arrayTemp,y_arrayTemp,x0,y0,x_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]),y_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]));

        int nombreEtoileReconnu = checkEtoileMatch(x_arrayTemp,y_arrayTemp,x_array,y_array);


        if( (nombreEtoileReconnu >= constellation_astro.getNbOfStars()/2) && nombreEtoileReconnu <= constellation_astro.getNbOfStars()){
            Log.d(TAG, "starAnalyze:: nombreEtoileReconnu : " + nombreEtoileReconnu + " Name : " + constellation_astro.getName() + " nbstar :  "+ constellation_astro.getNbOfStars());
            trouve = true;
        }

        return trouve;
    }

    /**
     * Cette methode est appellee pour chaque photo prise ou image importée depuis le provider.
     * Cette methode permet de detecter la position
     * des étoiles ainsi que sa taille
     * on execute un algorithme de detection de blob sur la frame et on stock les données dans un tableau trier.
     */
    public Bitmap starAnalyse() {
            //Mat mat = Imgcodecs.imread(file.getAbsolutePath());

            Mat imagefinal = new Mat();
            Utils.bitmapToMat(bitmap,imagefinal);

            Mat image = binariser(imagefinal,100);

            Mat temp_image = ShapeDetector_Astro.get_all(image);

            x_arrayDetect = new ArrayList<>();
            y_arrayDetect = new ArrayList<>();
            label_arrayDetect = new ArrayList<>();
            indiceEtoilelumin = new int[label_arrayDetect.size()];

            Mat starDetected = new Mat();
            ShapeDetector_Astro.detect_stars(temp_image,starDetected,x_arrayDetect,y_arrayDetect,label_arrayDetect);

            indiceEtoilelumin = ArraysUtils.argsort(ArraysUtils.convertDoublePrimitiveArray(label_arrayDetect),false);

            List<Constellation_Astro> list_template = BuildConstellation_Astro.getInstance().getConstellation_astros();

            Log.d(TAG, "starAnalyze: x_array : " + x_arrayDetect);
            Log.d(TAG, "starAnalyze: y_array : " + y_arrayDetect);
            Log.d(TAG, "starAnalyze: label_array : " + label_arrayDetect);
            Log.d(TAG, "starAnalyze: indice : " + ArraysUtils.StringArray(indiceEtoilelumin));
            Log.d(TAG, "starAnalyze: Size : " + x_arrayDetect.size());


           /* boolean trouve = false;

            for (int j = 0; j < list_template.size() ;j++) {

                for (int i = 0; i < indiceEtoilelumin.length; i++) {

                    for (int k = 0; k < indiceEtoilelumin.length; k++) {

                        if (list_template.get(j).getIndice_luminosite().length >= 2) {
                            trouve = trouver_constellation(list_template.get(j), x_arrayDetect, y_arrayDetect, indiceEtoilelumin[i], indiceEtoilelumin[k]);
                        }

                        if (trouve) {
                            Log.d(TAG, "starAnalyze: ON A TROUVE UNE CONSTELLATION : " + BuildConstellation_Astro.getInstance().getConstellation_astros().get(j).getName());
                            //Log.d(TAG, "starAnalyze: i : " + i + " k : " + k);
                        }
                    }

                }
            }*/

        Drawing_Astro.draw_stars(x_arrayDetect,y_arrayDetect,10,image);

        Bitmap bmp = Bitmap.createBitmap(image.cols(),image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bmp);

        return bmp;

    }

    /**
     * Cette fonction est appelée une fois que l'angle de rotation a été calculé, on fait pivoter les données des templates.
     * @param angle angle de rotation déterminé par la fonction de rotation
     * @revenir
     */
    public static void rotateTemplates(Constellation_Astro constellation_astro, double angle, List<Double> x_arrayTemp,List<Double> y_arrayTemp) {
        double xprime ;
        double yprime ;

        for (int i = 0; i < constellation_astro.getNbOfStars(); i++) {
            // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
            xprime = ((constellation_astro.getEtoile_x_array().get(i) * Math.cos(angle)) + (constellation_astro.getEtoile_y_array().get(i)  * Math.sin(angle)));
            yprime = (-(constellation_astro.getEtoile_x_array().get(i) * (Math.sin(angle))) + (constellation_astro.getEtoile_y_array().get(i)   * Math.cos(angle)));
            x_arrayTemp.add(i,xprime);
            y_arrayTemp.add(i,yprime);
        }

    }

    /**
     * L'image source doit etre
     * @param image_src
     */
    public Mat binariser(Bitmap image_src,int tresh){
        Mat image = new Mat();
        Utils.bitmapToMat(image_src,image);

        Mat image_dest = new Mat();
        Imgproc.threshold(image, image_dest, tresh, 255, Imgproc.THRESH_BINARY);
        return image_dest;
    }

    public Mat binariser(Mat mat,int tresh){
        Mat image_dest = new Mat();
        Imgproc.threshold(mat, image_dest, tresh, 255, Imgproc.THRESH_BINARY);
        return image_dest;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void toMemeEchelle(List<Double> x_array, List<Double> y_array, double echelle){
        Log.d(TAG, "toMemeEchelle: xArrayA" + x_array.toString());

        for (int i = 0; i <x_array.size() ; i++) {
            x_array.set(i, x_array.get(i)/echelle);
            y_array.set(i, y_array.get(i)/echelle);
        }

        Log.d(TAG, "toMemeEchelle: xArrayApres : " + x_array.toString());
    }

    public static void alignerToCoordonnes(List<Double> x_array, List<Double> y_array, double xUser, double yUser,double xTempRot, double yTempRot){
        double xVec = xUser - xTempRot;
        double yVec = yUser - yTempRot;

        Log.d(TAG, "alignerToCoordonnes: xVec : " + xVec + ", yVec : " + yVec);

        for(int i = 0; i < x_array.size(); i++){
            x_array.set(i, (x_array.get(i) + xVec));
            y_array.set(i, (y_array.get(i)) + yVec);
        }

        Log.d(TAG, "rotateTemplates: xArrayTempRot : " + x_array.toString() + "Size : " + x_array.size());
        Log.d(TAG, "rotateTemplates: yArrayTempRot : " + y_array.toString() + "Size : " + y_array.size());
    }

    public int checkEtoileMatch(List<Double> x_arrayTemp,List<Double> y_arrayTemp,List<Double> x_array,List<Double> y_array){
        int match = 0;
        double distance = 0;
        double check = 0;

        List<Double> x_arrayTempltem = new ArrayList<>(x_arrayTemp);
        List<Double> y_arrayTempltem = new ArrayList<>(y_arrayTemp);

        List<Double> x_arraytem = new ArrayList<>(x_array);
        List<Double> y_arraytem = new ArrayList<>(y_array);

        int size = x_arrayTempltem.size();
        int size1 = x_arraytem.size();

        Log.d(TAG, "checkEtoileMatch: size xArrayTempltemSize : " + size + " : xArraytemSize : " + size1);

        for(int i = 0; i < size; i++){
            outerloop:
            for(int j = 0; j < size1; j++){
                distance = Math.sqrt(Math.pow(x_arraytem.get(j) - x_arrayTempltem.get(i),2) + Math.pow(y_arraytem.get(j) - y_arrayTempltem.get(i),2));
                check = Math.sqrt(Math.pow(x_arraytem.get(j) - (x_arraytem.get(j)*1.1),2) + Math.pow(y_arraytem.get(j) - (y_arraytem.get(j)*1.1),2));

                if(distance <= check){
                    //Imgproc.line(image,new Point(x_arraytem.get(j)*100,-y_arraytem.get(j)*100),new Point(x_arrayTempltem.get(i)*100,-y_arrayTempltem.get(i)*100),new Scalar(255,0,0),2);
                    match++;
                    x_arraytem.remove(j);
                    y_arraytem.remove(j);
                    size1 = x_arraytem.size();
                    break outerloop;
                }
            }
        }
        return match;

    }

    public double calculeAngle(double udx, double udy,double vdx, double vdy){

        Log.d(TAG, "calculeAngle: udx : " + udx);
        Log.d(TAG, "calculeAngle: vdx : " + vdx);
        Log.d(TAG, "calculeAngle: udy : " + udy);
        Log.d(TAG, "calculeAngle: vdy : " + vdy);

        double uv = udx * vdx + udy * vdy;

        double normeU = Math.sqrt(Math.pow(udx,2) + Math.pow(udy,2));
        double normeV = Math.sqrt(Math.pow(vdx,2) + Math.pow(vdy,2));

        double cosT = uv / (normeU * normeV);

        double angle = Math.acos(cosT);

        Log.d(TAG, "calculeAngle: angle : " + Math.toDegrees(angle));
        return angle;
    }

}


